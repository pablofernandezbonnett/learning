# Payment Integration Patterns

Payment is the domain where correctness is non-negotiable. A bug in authentication
is a security incident. A bug in payment is a financial and legal incident.

This document covers the backend patterns behind PSP (Payment Service Provider)
integrations. Stripe is used here as the concrete example because the APIs are
well-known, but the same core patterns apply across other PSPs.

---

## Why This Matters

Payment integrations look like API integrations from far away, but the real
problem is protecting money and order state under retries, partial failure, and
asynchronous confirmation.

This matters because many weak payment designs work on the happy path and then
fail exactly where the business risk is highest: double charge, missing order,
or replayed confirmation.

## Bad Mental Model vs Better Mental Model

Bad mental model:

- payment is mainly "call the PSP and trust the response"
- if the checkout request times out, retrying is mostly harmless
- one synchronous success response is enough to settle the flow

Better mental model:

- payment is a correctness-sensitive state machine
- every mutating provider call needs idempotency
- webhook or later confirmation is often the durable signal that settles the
  business flow safely

Small concrete example:

- weak approach: charge the card and create the order in one synchronous step
- better approach: create a payment attempt safely, use provider idempotency,
  keep local truth for payment and order state, and let durable confirmation
  move the order forward

Strong default:

- treat payment confirmation as asynchronous unless the provider model clearly
  proves otherwise
- keep your own authoritative payment and order state instead of trusting the
  PSP response alone

Interview-ready takeaway:

> I treat payment integration as a correctness problem: safe payment attempt
> creation, idempotent provider calls, asynchronous confirmation, and strict
> local state transitions so retries or duplicate webhooks cannot create
> duplicate charges.

---

## 0. What Payment Integration Actually Is

The core problem is not "how do I call the PSP API?"

The core problem is:

- how do I avoid double charge under retries?
- how do I confirm payment safely when the flow is asynchronous?
- how do I keep order state, payment state, and webhook handling from drifting apart?

Smallest reliable mental model:

1. create or update a payment attempt safely
2. use idempotency on every mutating provider call
3. treat webhooks as the durable confirmation signal
4. process webhook events idempotently
5. keep your own source of truth for order and payment state

20-second answer:

> Payment integration is mainly a correctness problem, not just an HTTP integration
> problem. I think in terms of idempotency, asynchronous confirmation, safe state
> transitions, and making sure retries or duplicate webhooks cannot create duplicate
> charges or inconsistent order state.

---

## 1. The Core Flow — Why Not Simple HTTP

The naive implementation: client sends card details → your backend calls Stripe →
Stripe returns success → you create the order.

**Why this breaks:**
- Network timeout between Stripe and your backend → you don't know if the charge
  succeeded → retry → double charge.
- Your server crashes after Stripe charges but before you save the order → money
  taken, no order created.
- Card details pass through your server → you are now in PCI DSS scope.

**The correct flow is asynchronous, not synchronous:**

```
Client                  Your Backend           Stripe
  |                         |                     |
  |--[1] POST /checkout --→|                     |
  |                        |--[2] Create PaymentIntent →|
  |                        |←-- client_secret ---------|
  |←-- client_secret -----→|                     |
  |                         |                     |
  |--[3] Submit card via Stripe.js (client-side, NOT your server)
  |                         |                     |
  |                         |←-- [4] Webhook: payment_intent.succeeded
  |                         |                     |
  |                        [5] Confirm order, capture funds
```

Key points:
- Card details go directly from the browser to Stripe via Stripe.js or Elements.
  Your server never sees the raw card number. You are NOT in full PCI scope.
- You receive confirmation asynchronously via webhook, not via the HTTP response.
- The `client_secret` returned in step 2 is what allows the client to complete
  the payment on Stripe's side without exposing your secret API key.

---

## 2. Payment Intents (Stripe) — What It Is and Why It Exists

A `PaymentIntent` represents the full lifecycle of a payment attempt.

```kotlin
// Spring Boot / Kotlin — Stripe SDK
@Service
class StripePaymentService(
    @Value("\${stripe.secret-key}") private val secretKey: String,
) {
    init { Stripe.apiKey = secretKey }

    fun createPaymentIntent(
        orderId: Long,
        amountCents: Long,
        currency: String = "jpy",
        idempotencyKey: String,       // CRITICAL — see section 4
    ): PaymentIntentCreateResult {
        val params = PaymentIntentCreateParams.builder()
            .setAmount(amountCents)
            .setCurrency(currency)
            .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL) // Auth & Capture
            .putMetadata("order_id", orderId.toString())
            .build()

        val requestOptions = RequestOptions.builder()
            .setIdempotencyKey(idempotencyKey)
            .build()

        val intent = PaymentIntent.create(params, requestOptions)
        return PaymentIntentCreateResult(
            paymentIntentId = intent.id,
            clientSecret = intent.clientSecret,   // sent to client — NOT the secret key
        )
    }
}

data class PaymentIntentCreateResult(
    val paymentIntentId: String,
    val clientSecret: String,
)
```

**PaymentIntent states:**
```
created → requires_payment_method → requires_confirmation
       → requires_action (3DS)
       → processing
       → requires_capture          ← if using MANUAL capture (Auth & Capture)
       → succeeded / canceled
```

---

## 3. Auth & Capture Pattern

For physical goods e-commerce, you should almost never charge immediately.
The Visa/Mastercard best practice (and in some jurisdictions, the law) is:

1. **Authorization (checkout):** Block/freeze the amount on the customer's card.
   The money is not moved. The customer's available credit is reduced.
2. **Capture (shipment):** When the consignment is loaded onto the carrier truck,
   your OMS calls the payment service to capture the funds. Money moves now.

Why this matters:
- If the product is out of stock after payment auth → release the authorization,
  no refund needed (no money moved).
- If you capture at checkout and the item is lost in the warehouse → you must
  issue a full refund, which takes days to settle.

```kotlin
// Auth — done at checkout (CaptureMethod.MANUAL above)
// Intent moves to: requires_capture

// Capture — done when consignment ships
fun capturePayment(paymentIntentId: String, idempotencyKey: String) {
    val intent = PaymentIntent.retrieve(paymentIntentId)
    val requestOptions = RequestOptions.builder()
        .setIdempotencyKey(idempotencyKey)
        .build()
    intent.capture(requestOptions)
    // Intent moves to: succeeded
}

// Release (if order cancelled before shipment)
fun cancelAuthorization(paymentIntentId: String) {
    val intent = PaymentIntent.retrieve(paymentIntentId)
    intent.cancel()
    // Authorization released — no charge, no refund needed
}
```

---

## 4. Idempotency — Preventing Double Charges

The most important pattern in payment integration.

**Scenario:** User clicks "Buy". Request reaches Stripe. Network timeout before
your backend gets the response. Client retries. Without idempotency → two charges.

**Stripe's mechanism:** Include an `Idempotency-Key` header on every mutating API call.
If Stripe receives the same key within 24 hours, it returns the cached response of
the first call — no second charge.

**Key construction:** Must be unique per payment attempt, stable on retry.

```kotlin
// Good: deterministic from business data
fun buildIdempotencyKey(orderId: Long, attemptNumber: Int): String =
    "order-$orderId-attempt-$attemptNumber"

// Good: from a UUID stored server-side on first attempt
// Store the idempotency key when you first create the PaymentIntent.
// On retry, retrieve the stored key and use it again.

// Bad: UUID generated fresh on every retry call — defeats the purpose
```

**Server-side idempotency (your own DB, not just Stripe's):**

Do not rely only on "read first, then insert later."

Under concurrency, two retries can both read "not found" and both continue.
The important control is:

- unique constraint on your idempotency key
- claim the key before the provider call
- return the cached result if the attempt already exists
- make the "still in progress" case explicit

```kotlin
@Entity
@Table(name = "payment_attempts")
class PaymentAttempt(
    @Id @GeneratedValue val id: Long = 0,
    @Column(unique = true) val idempotencyKey: String,  // unique constraint
    val orderId: Long,
    var stripePaymentIntentId: String? = null,
    var clientSecret: String? = null,
    var status: String = "INITIATING",                   // INITIATING / PENDING / SUCCEEDED / FAILED
    val createdAt: Instant = Instant.now(),
)

// In your service:
@Transactional
fun initiatePayment(orderId: Long, amountCents: Long): PaymentIntentCreateResult {
    val idempotencyKey = "order-$orderId-attempt-1"

    // Return the cached result if this attempt already finished creation
    paymentAttemptRepository.findCompletedByIdempotencyKey(idempotencyKey)
        ?.let { existing ->
            return PaymentIntentCreateResult(
                paymentIntentId = existing.stripePaymentIntentId!!,
                clientSecret = existing.clientSecret!!,
            )
        }

    // Claim the key first. insertIfAbsent must be backed by the unique constraint.
    val claimed = paymentAttemptRepository.insertIfAbsent(
        PaymentAttempt(
            idempotencyKey = idempotencyKey,
            orderId = orderId,
            status = "INITIATING",
        )
    )
    if (!claimed) {
        throw RetryLaterException("Payment attempt is already in progress")
    }

    // Only the caller that claimed the key should create the provider-side intent.
    val result = stripePaymentService.createPaymentIntent(
        orderId = orderId,
        amountCents = amountCents,
        idempotencyKey = idempotencyKey,
    )

    paymentAttemptRepository.markCreated(
        idempotencyKey = idempotencyKey,
        paymentIntentId = result.paymentIntentId,
        clientSecret = result.clientSecret,
        status = "PENDING",
    )

    return result
}
```

If the provider call fails, either clear the reservation or mark it as failed on
purpose. Do not leave retry behavior implicit.

---

## 5. Webhook Handling — The Async Confirmation

Your backend must listen for Stripe webhook events. This is how you know a
payment succeeded or failed — not from the HTTP response of the initial call.

**Critical rules:**
1. **Verify the HMAC signature on every request** — reject anything that fails.
   Without this, anyone can POST a fake "payment_intent.succeeded" to your endpoint.
2. **Respond 200 immediately** — Stripe expects a response within 30 seconds.
   Queue the work asynchronously.
3. **Handle events idempotently** — Stripe guarantees at-least-once delivery.
   You will receive duplicate events. Do not use only a read-then-write check.
   Claim the event ID with a unique insert, or another insert-if-absent pattern,
   before running the business transition.

```kotlin
@RestController
@RequestMapping("/webhooks/stripe")
class StripeWebhookController(
    @Value("\${stripe.webhook-secret}") private val webhookSecret: String,
    private val paymentEventProcessor: PaymentEventProcessor,
) {

    private val logger = LoggerFactory.getLogger(StripeWebhookController::class.java)

    @PostMapping
    fun handle(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") signature: String,
    ): ResponseEntity<Unit> {
        // Step 1: Verify signature — NEVER skip this
        val event = try {
            Webhook.constructEvent(payload, signature, webhookSecret)
        } catch (e: SignatureVerificationException) {
            logger.warn("Stripe webhook signature verification failed")
            return ResponseEntity.status(400).build()
        }

        logger.info("Stripe webhook received: type={} id={}", event.type, event.id)

        // Step 2: Respond immediately — process async
        paymentEventProcessor.processAsync(event.id, event.type, payload)

        return ResponseEntity.ok().build()
    }
}

@Service
class PaymentEventProcessor(
    private val processedEventRepository: ProcessedStripeEventRepository,
    private val orderService: OrderService,
) {
    private val logger = LoggerFactory.getLogger(PaymentEventProcessor::class.java)

    @Async
    @Transactional
    fun processAsync(eventId: String, eventType: String, rawPayload: String) {
        // Step 3: Claim the event ID first.
        // insertIfAbsent must be backed by a unique constraint on eventId.
        val claimed = processedEventRepository.insertIfAbsent(eventId)
        if (!claimed) {
            logger.info("Stripe event already claimed, skipping: id={}", eventId)
            return
        }

        try {
            when (eventType) {
                "payment_intent.succeeded"    -> handlePaymentSucceeded(rawPayload)
                "payment_intent.payment_failed" -> handlePaymentFailed(rawPayload)
                "charge.refunded"             -> handleRefund(rawPayload)
                else -> logger.info("Unhandled Stripe event type: {}", eventType)
            }
        } catch (e: Exception) {
            logger.error("Failed to process Stripe event: id={} type={}", eventId, eventType, e)
            // Because the claim and business update live in the same transaction,
            // a failure rolls back the claim and lets provider retry work correctly.
            throw e
        }
    }

    private fun handlePaymentSucceeded(payload: String) {
        val intent = Event.retrieve(/* parse from payload */)
        val orderId = intent.metadata["order_id"]?.toLong()
            ?: throw IllegalStateException("No order_id in PaymentIntent metadata")

        logger.info("Payment succeeded for orderId={} intentId={}", orderId, intent.id)
        orderService.confirmPayment(orderId, intent.id)
    }

    private fun handlePaymentFailed(payload: String) {
        // Parse intent, extract orderId, release inventory reservation, notify user
        logger.warn("Payment failed — releasing reservation")
    }
}
```

---

## 6. Error Handling — Not All Errors Are Equal

Payment errors fall into three categories with different retry strategies:

| Category | Examples | Retry? |
|---|---|---|
| Card declined | insufficient_funds, do_not_honor | No — user must provide new card |
| Network / transient | timeout, 502 Bad Gateway | Yes — with exponential backoff + idempotency key |
| Fraud / hard decline | stolen_card, fraudulent | No — often should flag account |
| Invalid request | missing required field | No — fix the code |

```kotlin
fun handleStripeError(e: StripeException): PaymentError {
    return when (e) {
        is CardException -> when (e.code) {
            "insufficient_funds"    -> PaymentError.InsufficientFunds
            "card_declined"         -> PaymentError.CardDeclined(e.message ?: "Declined")
            "expired_card"          -> PaymentError.CardExpired
            "stolen_card",
            "fraudulent"            -> PaymentError.FraudSuspected
            else                    -> PaymentError.CardDeclined(e.message ?: "Declined")
        }
        is RateLimitException      -> PaymentError.RateLimit     // retry after backoff
        is ApiConnectionException  -> PaymentError.NetworkError  // retry with same idempotency key
        is AuthenticationException -> PaymentError.Configuration // misconfigured API key — alert
        else                       -> PaymentError.Unknown(e.message ?: "Unknown error")
    }
}

sealed class PaymentError {
    object InsufficientFunds : PaymentError()
    data class CardDeclined(val reason: String) : PaymentError()
    object CardExpired : PaymentError()
    object FraudSuspected : PaymentError()
    object RateLimit : PaymentError()
    object NetworkError : PaymentError()
    object Configuration : PaymentError()
    data class Unknown(val message: String) : PaymentError()
}
```

---

## 7. Refunds

Refunds are separate API calls, not a reversal of the original charge.
Always store the `charge_id` (or `payment_intent_id`) from the succeeded event.

```kotlin
fun refundPayment(
    paymentIntentId: String,
    amountCents: Long?,   // null = full refund
    reason: RefundReason,
    idempotencyKey: String,
): String {
    val intent = PaymentIntent.retrieve(paymentIntentId)
    val chargeId = intent.latestCharge   // get the charge from the intent

    val params = RefundCreateParams.builder()
        .setCharge(chargeId)
        .apply { amountCents?.let { setAmount(it) } }
        .setReason(reason.stripeValue)
        .build()

    val requestOptions = RequestOptions.builder()
        .setIdempotencyKey(idempotencyKey)
        .build()

    val refund = Refund.create(params, requestOptions)
    logger.info("Refund created: id={} status={}", refund.id, refund.status)
    return refund.id
}

enum class RefundReason(val stripeValue: RefundCreateParams.Reason) {
    DUPLICATE(RefundCreateParams.Reason.DUPLICATE),
    FRAUDULENT(RefundCreateParams.Reason.FRAUDULENT),
    CUSTOMER_REQUEST(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER),
}
```

---

## 8. PSP Comparison

| | Stripe | Wallet-style PSP | Adyen |
|---|---|---|---|
| Region focus | Global | Region-specific or local-market focus | Global / Enterprise |
| Integration style | REST + Webhooks | REST + Webhooks | REST + Webhooks |
| Idempotency | `Idempotency-Key` header | Same concept, different field name | `Reference` field |
| Auth & Capture | Yes — `capture_method: manual` | Yes | Yes |
| 3DS / SCA | Built-in via PaymentIntent | Redirect flow | Built-in |
| Webhook delivery | At-least-once (handle duplicates) | At-least-once | At-least-once |

---

## 9. PCI DSS Basics (What a Backend Engineer Needs to Know)

You do not need to be a PCI auditor. You need to know enough to not create
compliance problems for your company.

**Level 1 (avoid this):** Your server touches raw card data (PAN, CVV).
You are now in full PCI scope — quarterly audits, penetration tests, dedicated
cardholder data environment.

**Level 4 (target this):** Card data goes directly to the PSP via their JS library
or hosted fields integration. Your server only sees tokens and payment
intent IDs. You are in the minimal PCI scope (SAQ A).

**Backend rules:**
- Never log the full card number, CVV, or expiry date. Ever.
- Never store CVV — not even temporarily.
- Tokens (`tok_xxx`) from the PSP are single-use and safe to log.
- Use HTTPS everywhere (TLS 1.2+ only). No HTTP in payment flows.
- Your webhook endpoint must verify the HMAC signature before processing.

**Practical summary:**
"By using Stripe Elements or a hosted fields integration, the card data never
touches our servers. We receive a token. Our PCI scope is SAQ A — the lightest
possible. This is a deliberate architectural decision, not just a convenience."

---

## 10. Answer Shapes

### 20-second answer

> In payment integrations I assume retries, timeouts, and duplicate webhooks are normal.
> So I use provider-side idempotency keys, keep my own payment attempt state durably,
> treat the webhook as the real confirmation signal, and process webhook events
> idempotently. The goal is that the user never gets double-charged and the order
> state never drifts from the payment state.

### 1-minute answer

> I frame payments as a correctness-first distributed workflow. The initial HTTP response
> is not enough to trust, because the provider may succeed while my system times out, or
> my server may crash after the provider call but before my local state is updated. So I
> use idempotency keys on every mutating provider call, store payment attempts and order
> state durably on my side, and treat webhook confirmation as the durable signal for the
> final transition. Webhook endpoints must verify signatures, respond quickly, and process
> events idempotently because delivery is at-least-once. If my flow also needs internal
> event publication, I keep the database write and the intent to publish together via an
> outbox-style pattern instead of pretending one local transaction makes the whole system
> atomic.

---

## Further Reading

- Stripe PaymentIntents API: https://docs.stripe.com/payments/payment-intents
- Stripe Webhooks: https://docs.stripe.com/webhooks
- Stripe Signature Verification: https://docs.stripe.com/webhooks/signature
- PCI DSS Overview: https://www.pcisecuritystandards.org/standards/pci-dss
- OWASP Transaction Authorization Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Transaction_Authorization_Cheat_Sheet.html
