# PostgreSQL In-Depth for Backend Engineers

> Primary fit: `Shared core`


PostgreSQL is still the default answer for a large number of backend systems,
and for good reason.

If your domain includes orders, payments, inventory, user accounts, or any flow
where correctness matters, Postgres is usually the first database to justify.

This guide focuses on the practical concepts a backend engineer should keep
fresh: transactions, MVCC, indexing, query plans, connection pooling, and
common production pitfalls.

---

## 1. When Postgres Is the Right Choice

Use Postgres when you need:

- ACID transactions
- strong consistency for critical writes
- joins and relational integrity
- flexible but understandable querying
- predictable correctness under concurrency

Typical domains:

- orders
- payments
- inventory
- user accounts
- backoffice workflows

Good practical sentence:

> I default to Postgres for domains where correctness and transactional integrity
> matter more than unconstrained horizontal write scaling.

---

## 2. MVCC — Why Readers and Writers Coexist

Postgres uses **MVCC** (Multi-Version Concurrency Control).

The important practical idea:

- a write does not usually overwrite a row in place
- it creates a new row version
- readers can still see the old committed version

This gives you:

- readers do not block writers in the common case
- writers do not block readers in the common case
- transactions see a consistent snapshot

But it also creates dead tuples, which is why **VACUUM / autovacuum** matters.

Good backend understanding:

> MVCC is a major reason Postgres performs well under mixed read/write load, but
> it also means vacuuming is part of database health, not an implementation detail.

---

## 3. Transactions and Isolation Levels

Postgres default isolation level is **READ COMMITTED**.

That means:

- you do not read uncommitted data
- but repeated reads in one transaction may still observe new committed changes

Isolation levels to keep fresh:

- `READ COMMITTED`: default, practical for many APIs
- `REPEATABLE READ`: stable snapshot within the transaction
- `SERIALIZABLE`: strongest safety, highest coordination cost

Use cases:

- checkout/order creation: often `READ COMMITTED` plus correct locking is enough
- financial correctness or difficult race conditions: consider stronger controls

Rule:

> Isolation level is not a magic fix. You still need correct application logic
> and, when necessary, explicit locking or version checks.

---

## 4. Locking — Optimistic vs Pessimistic

Two patterns matter most for backend work.

### Optimistic Locking

Use a version column and fail on conflicting writes.

Good for:

- APIs where collisions are possible but not constant
- high-throughput systems where blocking is expensive

Tradeoff:

- retries move into application logic

### Pessimistic Locking

Lock the row explicitly.

Typical SQL:

```sql
SELECT * FROM inventory WHERE sku = 'ABC-123' FOR UPDATE;
```

Good for:

- critical short transactions
- stock deduction or financial state transitions

Tradeoff:

- blocking
- lower throughput
- possible deadlocks if badly designed

Senior rule:

> Keep transactions short and lock as little as possible for as little time as possible.

---

## 5. Indexes — Useful, Not Free

Indexes speed reads and cost writes.

You usually index columns used in:

- `WHERE`
- `JOIN`
- `ORDER BY`

Common high-value index patterns:

- exact lookup: `email`, `order_id`, `sku`
- composite index for filtered and sorted queries
- partial index for hot filtered subsets

Concrete example:

```sql
CREATE INDEX idx_orders_store_created
ON orders (store_id, created_at DESC);
```

That is better than indexing every column blindly.

Rule:

> Add indexes for real query patterns, not out of anxiety.

---

## 6. EXPLAIN ANALYZE — Measure Before Fixing

When a query is slow, do not guess.

Start with:

```sql
EXPLAIN ANALYZE
SELECT *
FROM orders
WHERE store_id = 42
ORDER BY created_at DESC
LIMIT 50;
```

What you want to notice:

- `Seq Scan` on a large table -> suspicious
- `Index Scan` or `Index Only Scan` -> usually better
- row estimates wildly different from actuals -> planner may be misled
- sort happening after too many rows -> index may help

Good backend habit:

> Slow query analysis starts with the plan, not with adding random indexes.

---

## 7. Connection Pooling

Postgres is powerful, but connections are not free.

If every app instance opens too many DB connections:

- memory rises on the DB server
- context switching increases
- overall throughput can get worse, not better

For backend engineers, the key concepts are:

- use a connection pool
- size it intentionally
- avoid assuming "more connections = more performance"

In Spring Boot, HikariCP is the default pool.

Rule of thumb:

- tune pool size to workload and DB capacity
- watch latency, DB CPU, and wait time

---

## 8. Autovacuum and Table Health

Because of MVCC, Postgres accumulates dead tuples.

Autovacuum cleans them up and updates planner statistics.

If autovacuum falls behind:

- tables and indexes bloat
- queries get slower
- row estimates degrade

You do not need to be a DBA, but you should know:

- vacuum health affects application performance
- large update/delete-heavy tables need attention
- long-running transactions can block cleanup

---

## 9. Replicas, Failover, and Read Consistency

You will often hear designs like:

- one primary Postgres
- one or more replicas with the same data

The normal reason is:

- better availability if the primary fails
- more read capacity for non-critical traffic

The important mental model is:

- **writes still go to one primary**
- replicas help with failover and read scaling
- replicas do **not** magically give you safe multi-writer behavior

Typical problems:

- **replica lag:** the write committed on the primary, but the replica has not caught up yet
- **read-after-write inconsistency:** the user places an order, then immediately reads from a replica and does not see it
- **failover complexity:** when the primary dies, one replica must be promoted safely
- **split brain risk:** two nodes should not both accept writes as if they were primary

The main tradeoff:

- **asynchronous replication:** better latency, but a failover can lose the very latest writes
- **synchronous replication:** stronger durability guarantees, but higher write latency and possible write unavailability if the sync replica is unavailable

Good practical rule:

- write critical flows like orders, payments, and reservations to the primary
- serve non-critical or read-heavy traffic from replicas when stale reads are acceptable
- for read-after-write-sensitive APIs, read from the primary or use a strategy that waits for consistency

Safe answer shape:

> If I run Postgres with a primary and replicas, I still treat the primary as the
> source of truth for writes. Replicas help with failover and read scaling, but I
> assume lag exists. That means critical read-after-write flows like order status,
> payment state, or reservation confirmation should read from the primary unless I
> have a stronger consistency strategy in place.

What not to say:

- "two Postgres instances with the same data means high availability is solved"
- "I would let both nodes accept writes for tolerance"

For most product backends, the strong default is:

- single writable primary
- one or more replicas
- controlled promotion on failover
- clear routing rules for critical vs non-critical reads

---

## 10. JSONB — Useful, But Not an Excuse

Postgres supports `JSONB`, which is excellent for:

- semi-structured attributes
- event payloads
- flexible metadata

It is not a license to stop modeling relational data.

Use relational columns when:

- the field is core to filtering or joins
- constraints matter
- the shape is stable

Use `JSONB` when:

- the shape is variable
- the data is secondary or extensible

Rule:

> JSONB is a sharp tool, not a replacement for relational design.

---

## 11. Partitioning

Partitioning becomes useful when one table grows large enough that operational
maintenance, deletes, or scans become painful.

Typical candidates:

- events
- logs
- audit records
- time-series-like order history

Usual pattern:

- range partition by date

Benefit:

- faster retention deletes
- smaller indexes per partition
- more predictable maintenance

But:

- it adds operational complexity
- you do not need it early

---

## 12. Practical Checklist

Use this when thinking about Postgres in a backend design:

- Does this domain require transactional integrity?
- Do I understand the most important queries?
- Are the right indexes in place for those queries?
- Could concurrency create lost updates or race conditions?
- Is the transaction short and well-bounded?
- Is connection pool size intentional?
- Could MVCC/vacuum health affect this workload?
- Am I using JSONB for the right reasons?

---

## 13. Interview Framing

Good short answer:

> Postgres is usually my default for critical backend domains because it gives me
> ACID transactions, strong consistency, mature query capabilities, and reliable
> concurrency control. The main things I keep in mind are transaction boundaries,
> locking strategy, indexing based on real query patterns, and measuring with
> EXPLAIN ANALYZE before tuning.
