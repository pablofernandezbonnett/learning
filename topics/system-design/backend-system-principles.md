# BACKEND PRINCIPLES FOR PRODUCT ENGINEERS

> Primary fit: `Shared core`

Keep these warm.
They form a compact system-design backbone for the rest of the repo.

Use this as a recall card, not as a primary study document, but it should still
be readable even when the topic is rusty.

---

## 1. Design for Failure

Assume that networks time out, dependencies slow down, and workers crash in the
middle of a flow.
The important design question is not "can this fail?" but "what does the system
do next when it fails?"

Always design recovery paths, not only happy paths.

---

## 2. Idempotency is Mandatory

Distributed systems retry because clients retry, workers retry, and providers
sometimes send the same callback or event more than once.
An operation is idempotent when repeating the same request does not create a
second business result by accident.

For money, orders, reservations, and webhooks, duplicate safety is not a nice
extra. It is part of correctness.

---

## 3. Separate Consistency Domains

Inventory, orders, and payments do not all need the same rules at the same
time.
Each domain has its own write rules, failure modes, and recovery path, so do
not force the whole workflow into one fake boundary just because the user sees
one button click.

---

## 4. Read Scaling and Write Scaling Are Different Problems

Reads:
- replicas
- caching
- CDN

Writes:
- partitioning
- locking strategy
- throughput control

Read traffic is usually about serving data faster and cheaper.
Write traffic is usually about protecting correctness under contention.
If you mix those two problems together, you often pick the wrong tool.

Short explanation:

> read scale is usually a speed problem; write scale is usually a correctness
> problem first and only then a throughput problem

---

## 5. Cache Improves Performance but Risks Correctness

Cache:
- product data
- availability estimates

Never cache:
- final payment state
- order confirmation

Cache is a speed tool, not the final authority on business truth.
Use it where slightly stale data is acceptable, and stay cautious where one
wrong read can produce a wrong charge, wrong reservation, or wrong user promise.

`Slightly stale` means the cached value may be a little behind the latest
database state, but still acceptable for that use case.

---

## 6. Source of Truth Must Be Clear

The source of truth is the durable place whose final state you trust when
systems disagree.
That might be a relational database, a ledger table, or another durable store,
but it should be explicit.

Redis is often useful for speed or coordination.
That does not automatically make it the final business authority.

---

## 7. Event-Driven Reduces Coupling

Async communication:
- improves resilience
- increases complexity

Use asynchronous events when independence between components really matters, not
just because a broker feels more advanced.
The win is looser coupling.
The cost is retries, duplicate handling, replay logic, and more operational
visibility work.

`Replay` here means reading an old event again later so a new or recovering
consumer can rebuild or catch up safely.

---

## 8. Observability Is Architecture

Logs, metrics, tracing:
If you cannot see it, you cannot operate it.

If a design only works when everything is healthy and everyone guesses from raw
logs during failure, the design is incomplete.
You need enough visibility to see slow paths, stuck work, retries, and business
outcomes such as pending orders or failed payments.

`Tracing` means following one request or event across several services so you
can see where the latency or failure actually happened.

---

## 9. Architecture Follows Business

Design decisions must reflect:

- revenue impact
- user experience
- operational cost

Good architecture is not a technology shopping list.
It is a set of tradeoffs shaped by what the business cannot afford to get wrong,
what the user can tolerate, and what the team can realistically operate.

---

## 10. Simplicity Scales Better Than Cleverness

Prefer clarity.
Prefer predictable systems.
Avoid premature complexity.

Simple does not mean naive.
It means every component has a reason to exist, every failure path is legible,
and the team can explain the system without hand-waving.

---

## Compressed Version

If you need a quick reset, come back to these five:

1. define the source of truth
2. name the failure mode
3. separate read scale from write correctness
4. make retries safe
5. keep the design simpler than your first impulse
