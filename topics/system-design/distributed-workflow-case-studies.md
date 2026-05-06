# Distributed Workflow Case Studies

Use this note when you want practice cases that feel close to interviews and
real backend design reviews.

The goal is not to memorize one "correct architecture."
The goal is to learn how to frame the problem, compare plausible solutions, and
defend the strongest one.

---

## Why This Matters

Many people know the pattern names:

- outbox
- saga
- webhook deduplication
- `CQRS`

But interviews and production design reviews rarely ask for the name alone.
They ask whether you can read the situation and choose a good boundary.

That is what these cases practice.

---

## Smallest Mental Model

For each case, force yourself to answer in this order:

1. what must never go wrong
2. what local write is the source of truth
3. what must finish now
4. what can happen later
5. what fails under retry, timeout, or duplicate delivery
6. what pattern closes that gap with the least extra machinery

If you follow that order, your answer usually becomes much stronger.

---

## Case 1. Checkout With Payment And Inventory

### Problem

Design checkout for a commerce system:

- create order
- authorize payment
- reserve inventory
- confirm order

Do not double charge.
Do not oversell stock.

### Bad mental model

- wrap everything in one transaction and call it done

Why weak:

- payment provider and inventory service usually live outside one local database boundary

### Possible solutions

1. one synchronous chain, fail the whole request on any later problem
2. local order write plus direct cross-service calls with no explicit recovery
3. local transactions per service, explicit states, compensation, and reliable async follow-up

### Best approach

- option 3

Strong shape:

- create durable `PENDING` order
- authorize payment
- reserve inventory
- confirm or cancel through valid state transitions
- use compensation if one later step fails

Patterns that fit:

- idempotent request key on checkout
- saga-style workflow recovery
- outbox if order state and event publication must stay coordinated

### Main tradeoff

- stronger correctness and recovery
- more workflow state, async reasoning, and observability burden

### Interview-ready takeaway

> I would treat checkout as a correctness-critical state machine, not one big transaction. Each service keeps local correctness, and the wider workflow reaches a safe final state through explicit states, idempotency, and compensation.

---

## Case 2. Payment Provider Timeout

### Problem

You call a payment provider.
The request times out.
You do not know whether the provider succeeded.

### Bad mental model

- timeout means failure

Why weak:

- the provider may have processed the request even though your side did not get the answer

### Possible solutions

1. return failure immediately and retry freely
2. return success optimistically
3. store a durable pending state and confirm later through webhook, callback, or reconciliation

### Best approach

- option 3

Strong shape:

- store payment attempt as `PENDING`
- use request idempotency
- use provider-safe idempotency if supported
- confirm final state asynchronously

### Main tradeoff

- honest uncertainty and safer correctness
- more state transitions and a less "clean" synchronous API

### Interview-ready takeaway

> I would rather return `PENDING` than lie about final success or failure while the provider boundary is uncertain.

---

## Case 3. Order Created Triggers Many Consumers

### Problem

When an order is created:

- warehouse cares
- analytics cares
- email cares
- fraud may care

### Bad mental model

- call every downstream system inside the request so everything is immediately updated

Why weak:

- user latency and failure now depend on several later systems

### Possible solutions

1. all synchronous HTTP calls
2. one queue with one worker doing everything
3. durable event publication and several independent consumers

### Best approach

- option 3 when several systems care independently

Strong shape:

- durable local order write
- outbox if needed
- publish `OrderCreated`
- independent consumers react safely

### Main tradeoff

- lower coupling and better fan-out
- replay, idempotency, and consumer monitoring now matter

### Interview-ready takeaway

> Once several systems care about the same business fact, I prefer a durable event flow over a long synchronous request chain. The key is making publication reliable and consumers replay-safe.

---

## Case 4. Webhook Intake From A PSP

### Problem

A payment provider sends webhook callbacks.
Delivery is at-least-once.
Messages may arrive late or duplicated.

### Bad mental model

- webhooks arrive exactly once and in order

Why weak:

- many providers explicitly do not promise that

### Possible solutions

1. process directly and assume one delivery
2. verify signature, acknowledge quickly, and process idempotently
3. same as option 2, plus queue or worker if later processing is slow

### Best approach

- option 2 or 3 depending on follow-up cost

Strong shape:

- verify signature
- deduplicate on event ID or business key
- store durable state transition
- keep later work async if needed

### Main tradeoff

- safer intake and replay handling
- more bookkeeping and support for stuck callbacks

### Interview-ready takeaway

> I assume webhook delivery is at-least-once, verify authenticity first, acknowledge quickly, and make the business effect idempotent.

---

## Case 5. Search Or Dashboard Read Model Is Too Slow

### Problem

The write model is correct, but read queries for dashboards or search are now
too expensive.

### Bad mental model

- add more indexes forever and hope one model still serves everything

Why weak:

- heavy reads can still fight critical writes

### Possible solutions

1. keep one relational model and tune harder
2. add caching only
3. split write and read concerns with projections or `CQRS`

### Best approach

- option 1 if pain is still modest
- option 3 only when the read/write mismatch is real and lasting

Strong shape:

- critical writes stay strict on source-of-truth store
- async projection builds search or dashboard model later

### Main tradeoff

- faster and more specialized reads
- sync lag, more storage, and more projection logic

### Interview-ready takeaway

> I would not jump to `CQRS` just because reads are annoying. I would use it when the write model and read model are truly diverging and heavy reads are starting to threaten critical writes.

---

## Case 6. Inventory Reservation Under High Contention

### Problem

A flash sale creates contention on a few hot `sku` values.

### Bad mental model

- just scale the app horizontally

Why weak:

- hot rows, lock contention, and duplicate retries may remain the real bottleneck

### Possible solutions

1. more replicas only
2. synchronous reservation with strong locking and admission control
3. async reservation queue by `sku` key

### Best approach

- depends on latency requirement

Strong default:

- if user needs immediate confirmation, keep reservation synchronous but explicit about contention controls
- if `PENDING` is acceptable, keyed async reservation can reduce contention pressure

### Main tradeoff

- stronger immediate certainty often costs throughput
- async smoothing often costs immediacy

### Interview-ready takeaway

> I would choose the reservation boundary based on whether the user needs immediate stock certainty or can accept a pending state while the system serializes contention safely.

---

## Strong Defaults Across Cases

These are the defaults worth keeping warm:

- local transaction for local truth
- outbox for DB-plus-event consistency
- at-least-once plus idempotent consumers as the practical async default
- `PENDING` over fake certainty when the external result is unclear
- saga recovery only when the workflow truly spans several services
- `CQRS` only when the read/write mismatch is real

These are strong because they solve common backend failures without pretending
the distributed world is simpler than it is.

---

## How To Practice For Interviews

For each case, answer in this structure:

1. clarify what must be strongly correct
2. name the source of truth
3. name the sync boundary
4. name the async boundary
5. explain retry, timeout, and duplicate handling
6. choose the smallest strong pattern
7. state one tradeoff clearly

That answer shape works well in real design reviews too.

---

## 20-Second Answer

> Good distributed workflow design is mostly problem framing. Start with invariants, source of truth, and uncertainty boundaries. Then choose the smallest strong pattern: outbox for dual-write safety, saga for cross-service recovery, idempotent consumers for at-least-once delivery, and `CQRS` only when read and write needs genuinely diverge.

---

## What To Internalize

- pattern names matter less than problem choice
- `PENDING` is often a strong answer
- outbox, saga, and `CQRS` solve different kinds of pain
- interview strength comes from explaining boundaries and tradeoffs clearly
- the smallest defensible pattern is usually the best real-world answer
