# Lifecycles and Flows Cheat Sheet

> Primary fit: `Shared core`

Use this note when the word `lifecycle` is still too vague and you need to
separate framework behavior from business workflow.

Shortest rule:

- first ask yourself which lifecycle they mean
- then answer only that category

Quick terms used here:

- `TTL` = `time to live`: how long a temporary hold or cached value lasts before expiry
- `PSP` = `payment service provider`

The first distinction matters:

- `framework lifecycle`: Spring bean, proxy, startup, shutdown
- `transaction lifecycle`: local unit of work around DB changes
- `business lifecycle / state machine`: order, payment, reservation states
- `end-to-end flow`: checkout as a multi-step user-critical workflow

Useful opening line:

> I first separate technical lifecycle from business workflow. A Spring bean has a framework lifecycle. Checkout and payment are better explained as flows with explicit state transitions, retries, and failure handling.

---

## 1. Spring Bean Lifecycle

This is the framework lifecycle.

```text
Class scanning / @Bean factory method
        ->
Instantiation (constructor)
        ->
Dependency injection
        ->
Aware interfaces
        ->
BeanPostProcessor.beforeInit
        ->
@PostConstruct / afterPropertiesSet
        ->
BeanPostProcessor.afterInit
        ->   proxy may be created here
Bean ready for use
        ->
@PreDestroy / destroy
```

What to say:

- constructor creates the bean instance
- dependency injection wires collaborators
- `@PostConstruct` runs after injection and is good for validation or warm-up
- Spring may wrap the bean in a proxy after initialization
- `@PreDestroy` runs on graceful shutdown

Common traps:

- `@Transactional` depends on a proxy, so self-invocation can bypass it
- `@PreDestroy` is not guaranteed on crash or `kill -9`
- prototype beans do not get the same shutdown tracking as singletons

30-second answer:

> The Spring bean lifecycle is instantiation, dependency injection, init hooks like `@PostConstruct`, proxy wrapping, normal use, and finally cleanup with `@PreDestroy`. A common senior-level detail is that `@Transactional` works through proxies, so internal self-calls can skip the transaction advice.

---

## 2. Transaction Lifecycle

This is not a full business workflow. It is a local consistency boundary.

```text
Caller enters public service method
        ->
Spring proxy opens transaction
        ->
Reads / writes happen under propagation + isolation rules
        ->
Commit or rollback
        ->
After-commit follow-up: outbox relay, event publish, async side effects
```

What to say:

- transactions group DB changes into one local unit of work
- propagation defines how nested calls join or start transactions
- isolation defines what concurrent transactions can observe
- commit and rollback apply to the local database boundary only

What not to imply:

- one `@Transactional` does not make HTTP calls, Kafka publishes, payment-provider calls, and Redis updates globally atomic

Senior line:

> I treat transactions as local consistency boundaries, not as a magic wrapper around every external side effect.

Common mistakes:

- putting slow network calls inside the transaction
- making the transaction too wide
- assuming transaction correctness replaces idempotency
- using `REQUIRES_NEW` casually

---

## 3. Checkout Flow

`Checkout` is best explained as an end-to-end workflow plus state transitions.

Minimal shape:

1. validate request and require an idempotency key
2. load cart and calculate final price
3. soft-reserve inventory with a `TTL` (`time to live`)
4. create or persist order intent in the source of truth
5. initiate payment or authorize funds
6. return pending response or `client_secret` to the client
7. receive async provider confirmation
8. move order to `PAID` only on a valid transition
9. confirm inventory reservation or release it on failure
10. publish order-confirmed event and continue fulfillment

What matters here:

- source of truth
- idempotency
- retries and timeouts
- compensating actions
- async confirmation
- monitoring of drop-off and failure points

Important nuance:

- in a simplified design drill, capture may happen inside checkout
- for physical goods, a more realistic model is often `authorize at checkout` and `capture on shipment`
- for digital goods, immediate capture at checkout can be reasonable

30-second answer:

> I would model checkout as a user-critical flow, not as one big transaction. The core is idempotent request handling, stock reservation, payment initiation, explicit order states, and compensation if any step fails. The source of truth should be durable storage, and external confirmation should be asynchronous.

---

## 4. Payment Lifecycle

Payment has both a provider state machine and an internal business state machine.

Provider-facing example:

```text
created
-> requires_payment_method
-> requires_confirmation
-> requires_action      (optional 3DS / SCA)
-> processing
-> requires_capture     (if auth/capture is split)
-> succeeded / canceled
```

Business-facing example:

```text
INITIATED
-> PENDING_PROVIDER_CONFIRMATION
-> AUTHORIZED
-> CAPTURED
-> SETTLED

failure branches:
FAILED / CANCELED / REFUNDED
```

What to say:

- the initial HTTP response is not always the final truth
- provider webhooks or async callbacks often decide the final result
- every mutating provider call needs idempotency
- every webhook event must be authenticated and deduplicated
- valid state transitions matter as much as signature verification

Critical invariants:

- the same payment event must not move state twice
- a `payment_succeeded` event must not create an order from nothing
- capture must not run twice
- refund rules depend on the current payment and order state

30-second answer:

> I explain payment as a state machine. The payment attempt is created, may require customer action, may end in authorization or capture, and reaches a terminal state like succeeded, canceled, or failed. In production, idempotency and webhook-safe state transitions are more important than the happy path.

---

## 5. Order Lifecycle

A simple retail-friendly order lifecycle:

```text
CART
-> PENDING_PAYMENT
-> PAID
-> FULFILLMENT_PENDING
-> SHIPPED
-> DELIVERED

side branches:
PENDING_PAYMENT -> CANCELED
PENDING_PAYMENT -> PAYMENT_FAILED
PAID -> REFUND_PENDING -> REFUNDED
```

Useful rules:

- only a pending order can become paid
- a paid order can be canceled only under business rules
- a shipped order cannot be treated like an unpaid cart
- each transition should be explicit and auditable

Useful line:

> I prefer explicit order states with guarded transitions instead of a vague boolean like `isPaid`.

---

## 6. Inventory Reservation Lifecycle

Inventory reservation is its own state machine.

```text
AVAILABLE
-> SOFT_HELD
-> COMMITTED
-> SHIPPED
-> DELIVERED

side branches:
SOFT_HELD -> AVAILABLE    (timeout / release)
COMMITTED -> AVAILABLE    (cancel + restock)
DELIVERED -> AVAILABLE    (return + restock)
```

Why this matters:

- payment, order, and inventory do not share the same consistency rules
- reservation prevents overselling before the final commit
- `TTL`-based soft holds need cleanup and clear expiration behavior

---

## 7. What To Say If Someone Just Says "Lifecycle"

Clarify by category:

- if they mean Spring: explain bean lifecycle and proxy behavior
- if they mean DB logic: explain transaction boundary, propagation, isolation, commit/rollback
- if they mean checkout or payment: answer with state machine + flow + failure handling

Good sentence:

> For checkout or payment I would not use the word lifecycle in the same sense as a Spring bean. I would describe the business states, allowed transitions, source of truth, retries, and compensating actions.

---

## 8. Checklist

For any critical flow, mention:

- source of truth
- idempotency key or deduplication key
- sync path versus async confirmation
- timeout and retry policy
- compensating action or reconciliation path
- guarded state transitions
- observability: metrics, logs, trace IDs, business KPIs

If you cover those, the explanation usually sounds clear instead of only descriptive.

If you go blank, use this rescue shape:

1. define the source of truth
2. define the business states
3. define what can be retried
4. define what is async
5. define what happens on failure

That rescue shape works for checkout, payment, order, and reservation discussions.

---

## 9. What To Rehearse Out Loud

- one payment or payment-adjacent flow with idempotency and webhook confirmation
- one partial-failure answer across DB and async boundaries
- one workflow answer with explicit states and milestones
- one answer about when not to split services before workflow ownership is clear

---

## 10. Related Notes

- [worked-diagrams.md](./worked-diagrams.md)
