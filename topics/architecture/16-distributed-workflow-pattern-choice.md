# Distributed Workflow Pattern Choice

Many teams learn `saga`, `outbox`, `queue`, `Kafka`, or `CQRS` as separate
pattern names.
That is not enough.

The real skill is choosing the smallest pattern that solves the real problem
without importing extra complexity for free.

---

## Why This Matters

Distributed workflow design is one of the easiest places to over-engineer.
Teams often jump from one local transaction problem straight into `Kafka`,
`sagas`, and workflow engines before naming the actual failure they are trying
to survive.

That creates two common interview and production failures:

- the design sounds advanced but solves the wrong problem
- the team pays the operational cost of patterns it did not really need

Good judgment here is mostly pattern choice judgment.

---

## Smallest Mental Model

Use this mental model first:

- synchronous call = one service needs an answer now
- queue = one background job needs one processing path
- event stream = one business fact needs several independent consumers or replay
- outbox = one service must save state and publish reliably
- saga = one workflow spans several services and needs forward steps plus recovery
- `CQRS` = write model and read model are different enough that one model no longer serves both well

Short version:

- outbox solves dual-write safety
- saga solves cross-service workflow recovery
- queue solves background work
- event stream solves fan-out and replay

If you remember only that separation, many weak designs disappear quickly.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- microservices mean we should probably use Kafka and sagas

Why weak:

- it starts from technology and architecture fashion, not from the failure mode

Better mental model:

- first name the business workflow
- then name the failure or coordination problem
- then choose the smallest pattern that closes that gap

That order is what keeps distributed systems from becoming ceremony.

---

## Step 1. Name The Real Problem First

Before choosing a pattern, ask which problem you actually have:

- one request should not wait for slow follow-up work
- several systems care about the same business fact
- one service must write to DB and publish an event
- one workflow spans several services and can fail halfway
- the read side needs a very different shape from the write side
- event history itself has product or audit value

Each one points to a different default.

---

## Problem -> Pattern Choice

### 1. Slow follow-up work after a successful write

Concrete example:

- order is saved
- email and analytics can happen later

Strong default:

- keep the critical write synchronous
- move the non-critical follow-up work to a queue or event path

Use queue when:

- one worker path owns the job

Use event stream when:

- several independent consumers care about the fact

Tradeoff:

- you gain lower user-facing latency
- you accept async boundaries and retry behavior

### 2. Save state and publish an event reliably

Concrete example:

- save order state
- publish `OrderConfirmed`

Strong default:

- use the outbox pattern

Why:

- this is the classic dual-write problem
- one local transaction cannot cover both your database and the broker directly

Avoid weak instinct:

- "save row, then publish event, it will probably be fine"

Tradeoff:

- you gain reliable publication
- you add outbox storage, relay logic, and monitoring for stuck rows

### 3. One business workflow spans several services

Concrete example:

- order creation
- payment authorization
- inventory reservation

Strong default:

- use local transactions plus saga-style recovery thinking

Use choreography when:

- the workflow is still small
- the event chain is easy to explain

Use orchestration when:

- the workflow is long-lived or business-critical
- you need one place to understand current state and compensation

Tradeoff:

- you gain recoverable workflow control
- you accept eventual consistency, compensation logic, and more operational state

### 4. Several systems need the same fact, and replay matters

Concrete example:

- order placed
- fraud, warehouse, analytics, and search all care

Strong default:

- event stream or distributed log

Why:

- this is not just a single background job anymore
- the event itself has durable value across consumers

Tradeoff:

- you gain replay and fan-out
- you accept partitioning, consumer-group behavior, and idempotent consumer design

### 5. Reads and writes have very different needs

Concrete example:

- strict write-side order rules
- heavy dashboard and search reads

Strong default:

- `CQRS` only when the separation is genuinely useful

Do not jump there if:

- one relational model still works well enough

Tradeoff:

- you gain write and read models optimized for their real jobs
- you accept sync lag, more infrastructure, and more reasoning about projections

### 6. Full history is part of the value

Concrete example:

- ledger or compliance-heavy money flow

Strong default:

- consider event sourcing only when historical event truth is itself valuable

Tradeoff:

- you gain audit depth and replayability
- you accept high conceptual and operational complexity

This should stay a rarer choice than people often pretend.

---

## Small Concrete Example

Problem:

- checkout should return fast
- stock reservation, fraud, and email can happen later
- order state and event publication must not drift apart

Possible solutions:

1. do everything synchronously in one request
2. save order, then publish event directly
3. save order, save outbox row in the same local transaction, publish later, let consumers handle follow-up work

Best approach:

- option 3

Why:

- it keeps the critical write durable
- it avoids fragile dual writes
- it creates a clean async boundary for later work

What pattern actually solved what:

- outbox solved DB-plus-event coordination
- event-driven consumers solved fan-out
- idempotent consumers solved retry safety

No saga is needed yet unless the cross-service workflow itself needs explicit
compensation.

---

## Strong Defaults

Use these as practical defaults unless the prompt forces something stronger:

- one service writes and many later steps can happen later -> outbox plus async consumers
- one background task path -> queue
- many consumers or replay need -> event stream
- cross-service business workflow -> saga thinking
- long complex workflow -> orchestration over choreography
- duplicates are possible -> at-least-once plus idempotent consumers

These defaults are usually stronger than chasing end-to-end "exactly once."

---

## When Not To Reach For The Bigger Pattern

Do not choose saga when:

- one service still owns the whole critical transaction
- compensation is not actually needed yet

Do not choose Kafka when:

- one worker group simply needs background jobs
- replay and independent consumers do not matter

Do not choose `CQRS` when:

- the read and write pressures are still close enough
- you are solving style preference rather than a real read/write mismatch

Do not choose event sourcing when:

- current state plus normal audit logging is enough

This is where many impressive but weak interview answers reveal themselves.

---

## Interview-Ready Cases

If the interviewer asks:

- "how do you keep DB write and event publish consistent?"
  answer with outbox

- "what if payment succeeds but inventory fails later?"
  answer with local transactions plus compensation, usually saga-style workflow recovery

- "when should this be Kafka instead of SQS?"
  answer with fan-out, replay, and durable event value

- "do we need `CQRS` here?"
  answer by checking whether the read model and write model are truly diverging

The strongest interview move is not naming the fanciest pattern.
It is naming the problem first and then defending the smallest strong pattern.

---

## 20-Second Answer

> Choose distributed workflow patterns by failure mode, not by pattern name.
> Use queue for one background path, event stream for fan-out and replay, outbox for DB-plus-event consistency, saga for cross-service workflow recovery, and `CQRS` only when read and write needs genuinely diverge. The strongest design is usually the smallest pattern that closes the real coordination gap.

---

## What To Internalize

- pattern choice starts from the failure mode
- outbox and saga solve different problems
- queue and event stream solve different async shapes
- `CQRS` and event sourcing should stay justified, not decorative
- the smallest strong pattern is usually the best answer in work and in interviews
