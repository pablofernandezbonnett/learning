# Webhooks for Backend Systems

> Primary fit: `Shared core / Payments / Fintech`


You do not need to be an integration-platform specialist, but payments,
third-party APIs, and retail flows often rely on webhook-style communication.

Webhooks are not a new framework and they are not the same thing as reactive programming.

They are a simple integration pattern:

- one system exposes an event
- another system provides an HTTP endpoint
- when the event happens, the sender pushes an HTTP request to that endpoint

This note exists to separate the concept cleanly from:

- polling APIs
- internal application events
- Kafka or RabbitMQ
- WebFlux and reactive style

---

## 1. What A Webhook Actually Is

The shortest good definition is:

> a webhook is an HTTP callback triggered by an event in another system

Smallest example:

- Stripe marks a payment as succeeded
- Stripe sends `POST /webhooks/payments` to your backend
- your backend verifies the request, records the event, and updates local state

Why people call it a "reverse API":

- with polling, your system keeps asking "did anything happen?"
- with a webhook, the other system pushes the event to you when it happens

That metaphor is useful, but do not over-literalize it.
It is still just an HTTP request arriving at your server.

---

## 2. Webhook vs Polling

### Polling

- your client calls the API repeatedly
- you control when you ask
- you may waste requests when nothing changed

### Webhook

- the sender calls you when the event happens
- lower unnecessary traffic
- closer to real-time event delivery
- your endpoint must be reachable and robust

Practical rule:

- use polling when you need occasional or on-demand reads
- use webhooks when event-driven updates and integration latency matter

---

## 3. Webhooks Are Not New

No, webhooks are not a relatively new technology.

What is true:

- the term was coined by Jeff Lindsay in 2007
- SaaS adoption made them much more common later
- they feel "modern" because Stripe, GitHub, Shopify, Slack, Zapier, and similar tools use them everywhere

The important conclusion is:

> webhooks are a mature integration pattern, not a trendy new stack

---

## 4. Webhook vs Internal Events vs Message Brokers

These concepts get mixed together too easily.

### Webhook

- external HTTP callback
- sender chooses when to push
- receiver exposes an HTTP endpoint

### Spring application event

- internal in-process event
- same application boundary
- no network involved

### Kafka or RabbitMQ message

- broker-mediated message delivery
- not the same as an external HTTP callback
- often used **after** webhook intake, not instead of it

Typical real pattern:

`provider webhook -> your HTTP endpoint -> verification/dedupe -> queue or worker -> business processing`

---

## 5. Webhook vs WebFlux

WebFlux is optional.

A webhook is just an HTTP request.
You can receive it with:

- Spring MVC
- Spring WebFlux
- Rails
- Express
- FastAPI
- basically any web framework

What WebFlux changes:

- how your server handles the request internally
- especially under high concurrency or non-blocking I/O

What WebFlux does **not** change:

- the fact that the sender is still making an HTTP callback
- the need for signature verification
- the need for idempotency

Short rule:

> webhook is the integration pattern, WebFlux is one possible server implementation style

---

## 6. The Smallest Spring Boot Shape

### Spring MVC

```kotlin
@RestController
class PaymentWebhookController {

    @PostMapping("/webhooks/payments")
    fun handle(@RequestBody payload: String): ResponseEntity<Unit> {
        processor.processAsync(payload)
        return ResponseEntity.ok().build()
    }
}
```

### Spring WebFlux

```kotlin
@RestController
class PaymentWebhookController {

    @PostMapping("/webhooks/payments")
    fun handle(@RequestBody payload: Mono<String>): Mono<ResponseEntity<Void>> {
        return payload
            .flatMap { body -> processor.processAsync(body) }
            .thenReturn(ResponseEntity.ok().build())
    }
}
```

Important point:

- the interesting part is not whether the return type is `ResponseEntity` or `Mono<ResponseEntity<...>>`
- the interesting part is what you do around correctness and security

---

## 7. The Real Production Checklist

For a serious webhook endpoint, always think about:

1. **authenticity**
   - verify signature or shared secret
2. **replay safety**
   - event ID deduplication
   - timestamp tolerance if the provider supports it
3. **fast acknowledgement**
   - do not block the sender with heavy work
4. **idempotent processing**
   - duplicate delivery must not duplicate business effects
5. **workflow validation**
   - do not let a valid webhook force an invalid state transition
6. **observability**
   - log delivery ID, trace ID, and failure reasons

Short rule:

> webhook handling is mainly a correctness and security problem, not an endpoint syntax problem

---

## 8. How This Fits Your Telemetry Project

For your Kotlin + WebFlux + Redis telemetry simulator, webhooks make sense if:

- an external system pushes telemetry or alert events into your backend
- or your backend pushes outbound callbacks when a threshold or alert condition is hit

In that architecture:

- webhook intake is the external HTTP edge
- Redis latest-state storage is your current-state layer
- Redis Pub/Sub or Streams is your internal fanout layer
- SSE or WebSocket is your dashboard delivery layer

That is a good fit.
Just do not blur those layers together conceptually.

---

## 9. Choice By Use Case

### Payment provider confirms a final result

- webhook: yes
- polling: usually no
- internal queue or worker after intake: yes
  Why: the provider owns the event, so the normal integration point is an HTTP callback.

### You only need to check status occasionally

- webhook: maybe not
- polling: yes
  Why: if freshness is not critical, polling can be simpler than exposing and operating a webhook endpoint.

### External platform sends events, but processing is heavy

- webhook: yes
- heavy work inside the request: no
- queue or async worker after acknowledgement: yes
  Why: acknowledge quickly, then process safely in the background.

### Internal event inside your own application

- webhook: no
- Spring event or broker: maybe yes
  Why: if the event stays inside your own system, webhook is the wrong abstraction.

---

## 10. The Big Traps

1. **Doing heavy business work before acknowledging the webhook**
   Example: provider waits too long, times out, and retries the same event again.

2. **Skipping signature or authenticity checks**
   Example: any caller can hit the endpoint and fake a provider event.

3. **Assuming webhook delivery happens exactly once**
   Example: duplicate delivery creates duplicate payments, orders, or emails.

4. **Treating webhook as the same thing as Kafka or WebFlux**
   Example: the explanation mixes external HTTP callbacks with internal messaging or reactive code style.

5. **Using webhook where occasional polling would be simpler**
   Example: low-value status checks become harder because the receiver now has to expose and operate a public callback endpoint.

---

## 11. 20-Second Answer

> A webhook is an HTTP callback triggered by an external event. It is not a new technology
> and it is not the same thing as WebFlux or Kafka. The main difference from polling is
> that the sender pushes the event to your endpoint when it happens. In production, the key
> concerns are signature verification, replay protection, fast acknowledgement, and idempotent
> processing.

---

## 12. 1-Minute Answer

> I think of a webhook as an external event arriving via HTTP. It is basically the push
> counterpart to polling: instead of my service repeatedly asking whether something changed,
> the provider calls my endpoint when an event happens. That pattern is mature, not new.
> The important distinction is that webhook is the integration pattern, while WebFlux is only
> one possible implementation style on the receiver side. I can receive a webhook with normal
> Spring MVC or with WebFlux; the real engineering concerns stay the same: verify authenticity,
> deduplicate event delivery, acknowledge quickly, keep heavy work asynchronous, and make sure
> the business state transition itself is idempotent and valid.

---

## 13. Related Reading

- [01-idempotency-and-transaction-safety.md](../databases/01-idempotency-and-transaction-safety.md)
