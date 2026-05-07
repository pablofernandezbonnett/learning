# 14. AppSec Lab — Webhook Signature Verification and Idempotency

This lab targets a high-value backend security pattern:

- accept an external event
- verify it is authentic
- process it exactly once from your application's point of view

This is directly relevant to:

- Stripe
- GitHub webhooks
- internal event callbacks

---

## Goal

Build a webhook endpoint:

`POST /webhooks/payments`

Rules:

- reject unsigned or invalid requests
- accept valid requests
- process each event ID only once
- return quickly

This lab is about correctness and security together.

---

## Step 1. Start With the Threat Model

If you skip this, the implementation becomes mechanical.

Main threats:

- anyone can send a fake "payment succeeded" request
- a real event can be replayed
- your endpoint does heavy work synchronously and times out
- duplicate delivery creates duplicate order transitions

Controls:

- HMAC signature verification
- timestamp tolerance if the provider supports it
- event ID deduplication
- async processing after fast acknowledgement

---

## Step 2. Verify the Signature

The endpoint must not trust the payload on arrival.

Example shape:

```kotlin
@PostMapping("/webhooks/payments")
fun handle(
    @RequestBody payload: String,
    @RequestHeader("X-Signature") signature: String,
): ResponseEntity<Unit> {
    if (!signatureVerifier.isValid(payload, signature)) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
    }

    webhookProcessor.processAsync(payload)
    return ResponseEntity.ok().build()
}
```

The point is not the exact header name.
The point is:

- no signature -> reject
- bad signature -> reject
- only verified payloads proceed

---

## Step 3. Deduplicate by Event ID

Even valid events may arrive more than once.

Do not rely only on:

```kotlin
if (processedEventRepository.existsByEventId(eventId)) {
    return
}
```

That read-then-write check is race-prone under concurrent duplicate delivery.

Use a unique event ID plus a "claim first" pattern instead.

Example shape:

```kotlin
val claimed = processedEventRepository.insertIfAbsent(eventId)
if (!claimed) return
```

`insertIfAbsent` should be backed by a unique constraint on `eventId`.

If your business transition and your claim live in the same local database
transaction, a failure rolls the claim back and the provider retry can try
again cleanly.

---

## Step 4. Keep the Business Transition Safe

Do not let the webhook jump the state machine incorrectly.

Concrete example:

- `payment_succeeded` can move `PENDING_PAYMENT` -> `PAID`
- it must not re-run on an already `PAID` order
- it must not create an order from nothing

This is where idempotency and workflow validation meet.

---

## Step 5. Test the Failure Paths

Minimum test cases:

- invalid signature -> `400`
- valid signature + first delivery -> `200`
- valid signature + duplicate event -> `200`, no double processing
- valid signature + bad state transition -> rejected or ignored safely

If you only test the happy path, the lab misses the point.

---

## Step 6. Suggested Service Shape

```kotlin
@Service
class PaymentWebhookProcessor(
    private val processedEventRepository: ProcessedEventRepository,
    private val orderRepository: OrderRepository,
) {
    @Transactional
    fun process(event: PaymentEvent) {
        val claimed = processedEventRepository.insertIfAbsent(event.id)
        if (!claimed) return

        val order = orderRepository.findById(event.orderId)
            ?: throw IllegalStateException("Unknown order")

        if (order.status != OrderStatus.PENDING_PAYMENT) return

        order.status = OrderStatus.PAID
        orderRepository.save(order)
    }
}
```

This is intentionally simple.
The important part is that the event is claimed before the business transition,
and that the claim is protected by a unique constraint.

---

## Common Mistakes

- trusting the payload without signature verification
- using only the order ID for deduplication instead of the event ID
- marking an event as processed before the business change succeeds
- doing expensive downstream work before acknowledging the provider
- allowing duplicate state transitions

---

## Completion Checklist

- Can the endpoint reject forged requests?
- Can the processor tolerate duplicate delivery?
- Is the business transition idempotent?
- Does the endpoint return quickly?
- Could you explain why webhook security is not only "an integration detail"?

---

## What You Should Be Able to Say After This Lab

> Webhook handling needs both authenticity and idempotency. A valid event must
> come from the provider, and the same event must not trigger the business flow
> twice.

---

## Related Reading

- [../../topics/security/05-payment-integration-patterns.md](../../topics/security/05-payment-integration-patterns.md)
- [../../topics/security/07-secrets-logging-and-secure-sdlc.md](../../topics/security/07-secrets-logging-and-secure-sdlc.md)
