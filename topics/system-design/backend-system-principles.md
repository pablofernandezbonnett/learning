# BACKEND PRINCIPLES FOR PRODUCT ENGINEERS

> Primary fit: `Shared core`

Keep these warm. They form a compact system-design backbone.

Use this as a recall card, not as a primary study document.

---

## 1. Design for Failure

Systems fail.
Networks fail.
Dependencies fail.

Always design recovery paths.

---

## 2. Idempotency is Mandatory

Distributed systems retry.
Every critical operation must tolerate duplicates.

---

## 3. Separate Consistency Domains

Inventory != Orders != Payments

Different rules → different boundaries.

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

---

## 5. Cache Improves Performance but Risks Correctness

Cache:
- product data
- availability estimates

Never cache:
- final payment state
- order confirmation

---

## 6. Source of Truth Must Be Clear

Redis is not a source of truth.
Databases define consistency.

---

## 7. Event-Driven Reduces Coupling

Async communication:
- improves resilience
- increases complexity

Use when independence matters.

---

## 8. Observability Is Architecture

Logs, metrics, tracing:
If you cannot see it, you cannot operate it.

---

## 9. Architecture Follows Business

Design decisions must reflect:

- revenue impact
- user experience
- operational cost

---

## 10. Simplicity Scales Better Than Cleverness

Prefer clarity.
Prefer predictable systems.
Avoid premature complexity.

---

## Compressed Version

If you need a quick reset, come back to these five:

1. define the source of truth
2. name the failure mode
3. separate read scale from write correctness
4. make retries safe
5. keep the design simpler than your first impulse
