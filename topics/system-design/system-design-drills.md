# System Design Drills

> Primary fit: `Shared core`

Use this after [system-design-guide.md](./system-design-guide.md).

This file exists so system design does not stay as passive reading.
Each drill is small on purpose.
The goal is to practice the answer shape until the core moves become automatic.

Working rule:

1. clarify only what changes the design
2. state the invariant
3. name the source of truth
4. walk the critical write path
5. explain retries, duplicates, and recovery

---

## 1. Drill: Retry-Safe Checkout Commit

Prompt:

> Design a checkout write path that should not charge twice or create two paid
> orders when the client retries.

What to clarify:

- is `PENDING` acceptable
- does the payment provider support idempotency keys
- do we reserve stock before or after payment authorization

Answer spine:

- invariant: one purchase intent must not create two paid orders
- source of truth: relational order and payment tables
- request identity: idempotency key or stable checkout session ID
- write path: create `PENDING` attempt, call provider safely, move through valid states
- async path: webhook confirms uncertain provider result
- recovery: replay-safe webhook handling and reconciliation for stuck pending state

Common traps:

- pretending one synchronous call fully settles money
- treating local transaction as if it covers the provider too
- forgetting duplicate webhook delivery

---

## 2. Drill: Hot SKU Inventory Reservation

Prompt:

> A small set of products is extremely hot during flash sales. Design the
> reservation path so stock does not oversell.

What to clarify:

- exact correctness requirement for final stock
- expected peak write contention per SKU
- how long a reservation can stay held before release

Answer spine:

- invariant: final committed stock must not go below zero
- source of truth: inventory table, not Redis cache
- critical write path: decrement or reserve inside one protected DB rule
- concurrency choice: optimistic for moderate conflicts, pessimistic or atomic update for hot rows
- async path: release expired holds later
- recovery: sweeper for stale reservations and metrics for contention or failed claims

Common traps:

- using only in-memory locks
- caching final stock truth
- discussing replicas before the write rule is safe

---

## 3. Drill: Webhook Intake

Prompt:

> Design a payment webhook endpoint that must accept valid events quickly and
> never apply the same business transition twice.

What to clarify:

- provider signature model
- retry behaviour of the provider
- whether downstream work can happen after acknowledgement

Answer spine:

- invariant: one external event must not create duplicate internal effects
- source of truth: processed-event store plus business state tables
- intake path: verify signature, claim event ID, perform valid transition, acknowledge fast
- async path: publish downstream work after durable local success
- recovery: provider retry, internal replay, and visibility into stuck or rejected events

Common traps:

- trusting unsigned payloads
- doing expensive work before acknowledging
- deduplicating with a race-prone read-then-write check

---

## 4. Drill: Notification Fan-Out

Prompt:

> After an order becomes paid, email, analytics, fraud, and warehouse systems
> should react independently.

What to clarify:

- does the user wait for any of these follow-up actions
- does any consumer require replay
- do all consumers need the same payload shape

Answer spine:

- invariant: paid order state should remain correct even if follow-up systems lag
- source of truth: order state in the transactional store
- write path: move order to `PAID`, persist outbox event
- async path: broker fan-out to independent consumers
- recovery: replay-safe consumers, retry policy, DLT ownership

Common traps:

- coupling all side effects into the request path
- using direct HTTP calls where replayable events would fit better
- assuming one broken consumer should block all others forever

---

## 5. Drill: Search Projection

Prompt:

> Product catalog writes are correctness-sensitive, but product browse and
> search traffic is much heavier. Design the write/read split.

What to clarify:

- how stale search results can be
- whether browse needs full-text search or just filtering
- who owns catalog truth

Answer spine:

- invariant: product truth comes from the write store, not the search index
- source of truth: catalog database
- write path: durable product update first
- async path: event or outbox updates projection store
- recovery: replay or rebuild projection from source events or source DB

Common traps:

- treating the projection as the only truth
- hiding propagation delay from the product team
- skipping rebuild strategy

---

## 6. Self-Review Rubric

Score each category `0-2`:

- invariant is explicit
- source of truth is explicit
- critical write path is explicit
- async boundary is explicit
- retry and duplicate behaviour is explicit
- recovery path is explicit
- tradeoff is explicit

Interpretation:

- `12-14`: strong practical answer
- `8-11`: usable but missing one or two important layers
- `0-7`: still too vague or too box-driven

---

## 7. How To Practice

A good short loop is:

1. set a 12-15 minute timer
2. answer one drill out loud or in markdown
3. grade it with the rubric
4. rewrite only the missing pieces

If the answer feels weak, do not add more boxes first.
Usually the missing part is one of:

- invariant
- source of truth
- duplicate prevention
- recovery path
