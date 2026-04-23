# Reactive, Event-Driven, and Webhooks Basics

> Primary fit: `Shared core / Payments / Fintech`

You do not need to become a reactive-programming evangelist.

These concepts are related, but they are not the same thing.

This note exists so you can explain them cleanly without mixing:

- webhook intake
- internal events
- broker-based event-driven flows
- reactive programming

The goal is not theory.
The goal is to know what problem each idea solves, where it fits, and what a minimum example looks like.

If you only keep one line in your head, make it this:

> webhook = external HTTP callback, event-driven = architecture style, reactive = programming model

---

## 1. The Short Definitions

### Webhook

An external system sends your server an HTTP request when an event happens.

Short version:

> webhook = external event delivered over HTTP

### Event-driven design

A system reacts to events instead of forcing all work through one synchronous request chain.

Short version:

> event-driven = state changes or domain events trigger follow-up work

### Reactive programming

A programming model for composing asynchronous, non-blocking flows, often as streams.

Short version:

> reactive = how code handles async work and streaming, not the business architecture by itself

---

## 2. What Problem Each One Solves

### Webhook

Use it when another system needs to notify you that something happened.

Typical cases:

- payment provider confirms a payment
- Shopify tells you an order changed
- GitHub tells CI that code was pushed

### Event-driven design

Use it when one business action should trigger follow-up work without coupling everything into one request.

Typical cases:

- order placed -> reserve stock
- order placed -> send confirmation email
- payment confirmed -> update order state
- stock updated -> refresh search index

### Reactive programming

Use it when the system handles many concurrent I/O-heavy requests or streaming flows and blocking threads becomes wasteful.

Typical cases:

- API gateway or high-concurrency edge service
- SSE or WebSocket stream
- telemetry ingestion
- chat, notifications, or live dashboards

---

## 3. What Not To Confuse

- webhook is not the same thing as Kafka or RabbitMQ
- event-driven is not the same thing as WebFlux
- reactive programming is not required for event-driven architecture
- Spring application events are local to your process unless you publish to a broker
- WebFlux is an implementation choice, not an architecture

Short rule:

> webhook is an integration pattern, event-driven is an architecture style, reactive is a programming model

---

## 4. The Smallest Examples

### 4.1 Webhook

Another system pushes an event to your endpoint:

```kotlin
@RestController
class PaymentWebhookController {

    @PostMapping("/webhooks/payments")
    fun handle(@RequestBody payload: String): ResponseEntity<Unit> {
        verifier.check(payload)
        processor.enqueue(payload)
        return ResponseEntity.ok().build()
    }
}
```

Important part:

- receive
- verify
- acknowledge quickly
- process safely

### 4.2 Internal event in Spring

One part of your app announces a business event, another part reacts.

```kotlin
data class OrderPlacedEvent(val orderId: String)

@Service
class OrderService(
    private val publisher: ApplicationEventPublisher,
) {
    fun placeOrder(orderId: String) {
        publisher.publishEvent(OrderPlacedEvent(orderId))
    }
}

@Component
class OrderEmailListener {

    @EventListener
    fun on(event: OrderPlacedEvent) {
        println("Send confirmation for order ${event.orderId}")
    }
}
```

This is event-driven inside one application.

### 4.3 Reactive endpoint

The endpoint itself is implemented with non-blocking composition.

```kotlin
@RestController
class PriceController(
    private val pricingClient: PricingClient,
) {
    @GetMapping("/prices/{sku}")
    fun getPrice(@PathVariable sku: String): Mono<PriceDto> {
        return pricingClient.fetchPrice(sku)
            .map { price -> PriceDto(sku, price.amount) }
    }
}
```

The point here is not "events".
The point is non-blocking I/O and async composition.

---

## 5. How They Fit Together In Real Systems

These ideas often appear in the same system, but at different layers.

### Payments

Typical flow:

1. client calls your payment API
2. your backend creates a local `PENDING` payment state
3. provider later sends a webhook
4. your webhook endpoint verifies signature and deduplicates event delivery
5. your backend moves payment state to `SUCCEEDED` or `FAILED`
6. your system publishes internal follow-up events or broker messages for email, ledger, fraud, or analytics

What matters:

- webhook = external confirmation
- event-driven = follow-up work after the state change
- reactive = optional implementation style on the HTTP edge

### Retail

Typical flow:

1. order created
2. inventory service reserves stock
3. warehouse gets fulfillment signal
4. analytics updates sales counters
5. search or catalog read model refreshes

That is event-driven because one business fact triggers multiple follow-up actions in other parts of the system.

You may still use plain blocking Spring MVC and be fully event-driven.
You do not need WebFlux just because the architecture is event-driven.

---

## 6. When To Use Each One

### Use webhooks when

- another platform owns the event
- polling would be wasteful or too delayed
- you need external callbacks such as payment confirmation or app integrations

### Use event-driven design when

- many systems react to the same business fact
- retries and asynchronous work need clean boundaries
- user-facing latency should not include all follow-up work

### Use reactive programming when

- the service is I/O-bound and concurrency is high
- streaming is part of the use case
- your team can operate the model confidently

---

## 7. When Not To Reach For Them

### Do not choose webhooks if

- you only need an occasional read and polling is simpler
- the receiver cannot expose a stable endpoint

### Do not choose event-driven design if

- the workflow is tiny and synchronous coupling is acceptable
- the extra broker, retry, and eventual-consistency cost is not justified

### Do not choose WebFlux just because it sounds advanced

- if the app is mostly blocking JPA and simple CRUD, WebFlux often adds complexity without much gain
- reactive style helps most when the bottleneck is concurrent I/O, not when the system is mainly CPU work or blocking DB access

---

## 8. The Most Important Distinction

If you only remember one sentence, remember this:

> webhook is how an external event enters your system, event-driven is how your system reacts to events, and reactive is how your code may handle async work internally

---

## 8.5 Choice By Use Case

### Payment provider confirms a final result

- webhook: yes
- broker: maybe inside your own system after you process the webhook
- reactive: optional
  Why: the external provider owns the event, so the normal entry point is an HTTP callback.

### One internal business event triggers several follow-up actions

- webhook: no
- internal event or broker: yes
- reactive: optional
  Why: this is a case for async follow-up work inside your own system, not for an external callback.

### Live dashboard or streaming updates

- webhook: usually no
- event-driven: maybe, if backend events feed the stream
- reactive: maybe, if non-blocking streaming really helps the service
  Why: here the main problem is long-lived streaming or high-concurrency I/O, not external event intake.

### Small CRUD app with blocking JPA

- webhook: only if an external platform really needs to call you
- event-driven: maybe, but only if the workflow actually needs async follow-up work
- reactive: usually no
  Why: if the app is simple and mostly blocking, WebFlux often adds more complexity than value.

---

## 9. 20-Second Answer

> A webhook is an external event delivered over HTTP. Event-driven design means one
> business event can trigger follow-up work asynchronously instead of doing everything in
> one synchronous request. Reactive programming is different again: it is a non-blocking
> way to compose async flows, for example with WebFlux. In payments and retail, these
> ideas often appear together, but they solve different problems.

---

## 10. 1-Minute Answer

> I separate these concepts very explicitly. A webhook is just an external callback over
> HTTP, for example a payment provider confirming a final result. Event-driven design is
> broader: once an important business fact happens, like order placed or payment confirmed,
> multiple follow-up actions can happen asynchronously instead of being forced into one user
> request. Reactive programming is different again; it is about handling I/O and streams in
> a non-blocking way, for example with WebFlux. So in a payment system I might receive a
> provider webhook, verify and deduplicate it, update local payment state, and then trigger
> internal events or broker-based processing. Reactive style is optional there; it depends
> on the traffic pattern and whether non-blocking I/O actually helps the service.

---

## 11. Related Reading

- [../api/03-webhooks-basics.md](../api/03-webhooks-basics.md)
- [../api/02-message-brokers-and-delivery-semantics.md](../api/02-message-brokers-and-delivery-semantics.md)
- [03-distributed-transactions-and-events.md](./03-distributed-transactions-and-events.md)
- [05-distributed-tracing.md](./05-distributed-tracing.md)
- [../devops/03-observability-and-monitoring.md](../devops/03-observability-and-monitoring.md)
