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

## Why This Matters

Postgres matters because it is still the strongest default for many correctness-
critical backend systems, but teams often use it well at the feature level and
poorly at the concurrency or operational level.

This topic matters in practice because Postgres decisions directly affect:

- correctness under concurrent writes
- request latency
- lock contention
- pool saturation
- failover and stale-read behavior

If you understand transactions, locking, plans, pools, and replica tradeoffs,
you can explain not just why Postgres is chosen, but how to use it safely.

## Smallest Mental Model

The smallest useful Postgres model is:

- Postgres is a transactional source of truth
- reads and writes interact through MVCC and transaction rules
- performance depends on query shape, indexes, vacuum health, and pool behavior
- replicas help reads and failover, but the primary still owns the write truth

Strong default:

- use Postgres when correctness and relational integrity matter
- keep transactions short
- design indexes for real queries
- measure plans before tuning

## Bad Mental Model vs Better Mental Model

Bad mental model:

- Postgres is just a relational database, so the main job is writing SQL
- ACID means concurrency problems are mostly solved automatically
- replicas mean horizontal scaling and availability are basically handled

Better mental model:

- Postgres is a correctness engine whose behavior depends on transaction and lock design
- ACID does not remove race conditions caused by weak application logic
- replicas add read scale and failover options, but also add lag and routing tradeoffs

Small concrete example:

- weak approach: move reads to replicas everywhere and assume order-status reads stay correct immediately after write
- stronger approach: keep read-after-write-sensitive flows on the primary and use replicas only where stale reads are acceptable

Reusable takeaway:

> I default to Postgres when transactional correctness matters, but I keep the
> real risks in view: lock shape, query plans, pool pressure, vacuum health, and
> replica lag under real traffic.

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

## 6. Table Design Defaults For Transactional Postgres Tables

Good schema design prevents application work later.

The smallest useful model is:

- model current business truth first
- make invalid states harder to store
- add indexes and read shapes after the write model is honest

Good default checklist for a new transactional table:

- a stable primary key
- `created_at` for creation time
- `updated_at` when the row is expected to change over time
- `NOT NULL` on fields that are required by the business rule
- `CHECK` constraints for simple invariants such as non-negative quantity or amount
- `UNIQUE` constraints for real business identities such as external references, emails, or idempotency keys
- foreign keys when the relationship is real inside the same service boundary
- a `version` column when optimistic locking is likely to matter

Small concrete example:

```sql
CREATE TABLE orders (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  external_order_id TEXT NOT NULL,
  customer_id BIGINT NOT NULL REFERENCES customers(id),
  status TEXT NOT NULL,
  total_amount_cents BIGINT NOT NULL CHECK (total_amount_cents >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  UNIQUE (tenant_id, external_order_id)
);
```

Why this is a strong starting point:

- the row has a clear identity
- required business fields cannot silently disappear into `NULL`
- one external order cannot be created twice for the same tenant
- simple money invariants are protected in the database, not only in app code

Practical cautions:

- do not make columns nullable by habit
- do not skip constraints just because the app also validates
- do not copy current truth into several tables unless the copy is an intentional snapshot or read model

Useful nuance:

- some duplication is correct, such as storing the price charged on an order line even if the catalog price changes later
- the rule is not "never copy data"; the rule is "do not duplicate current truth without a reason"

---

## 7. EXPLAIN ANALYZE — Measure Before Fixing

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

## 8. Connection Pooling

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

## 9. Autovacuum and Table Health

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

## 10. Replicas, Failover, and Read Consistency

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

## 11. Views and Materialized Views

Views are one of the simplest ways to keep read logic cleaner without changing
the base tables.

Plain-English version:

- a `view` stores the query definition, not separate data
- a `materialized view` stores the query result and must be refreshed later

Use a normal view when:

- the main win is readability
- you want to hide join complexity from backoffice or reporting queries
- you want a stable read shape without duplicating data

Use a materialized view when:

- the query is expensive
- the result is reused often
- some staleness is acceptable

Small concrete example:

- a support dashboard that repeatedly aggregates yesterday's orders may be a good materialized-view candidate
- a read model that simply joins `orders` and `customers` for admin search may only need a normal view

Strong default:

> use normal views for reusable read logic and materialized views for genuinely
> expensive reports where refresh lag is acceptable

Main caution:

- materialized views are not self-refreshing
- if you do not have a refresh plan, you do not really have a production design yet

---

## 12. Roles and Least Privilege

Database roles are not just DBA ceremony.
They are one of the easiest ways to reduce accidental damage.

Good practical default:

- keep migration or admin privileges separate from the runtime application role
- give read-only jobs read-only access
- avoid using one broad super-role for app traffic, scripts, reports, and support tasks

Small concrete example:

- the app that serves customer checkout probably needs `SELECT`, `INSERT`, `UPDATE`, and maybe limited `DELETE`
- a reporting job may only need `SELECT`
- a schema migration pipeline may need `ALTER`, `CREATE`, and `DROP`, which the runtime app should not have

Why this matters:

- limits blast radius
- reduces accidental schema or data damage
- makes security reviews easier to reason about

Practical rule:

> separate who can change data, who can read data, and who can change schema
> before you need an incident to teach the lesson

---

## 13. JSONB — Useful, But Not an Excuse

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

## 14. Partitioning

Partitioning becomes useful when one table grows large enough that operational
maintenance, deletes, or scans become painful.

Bad mental model:

- partitioning is a smart default because bigger systems are "more scalable"

Better mental model:

- partitioning is an operational tool for very large tables with a natural partition key, often time-based
- it helps most when retention, maintenance, and large-range reads are the real problem

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
- it does not fix bad query shape by itself
- you do not need it early for ordinary order, payment, or user tables

Strong default:

> partition only when one large-table pain is already real: retention, maintenance
> windowing, or large-range access on a natural partition key

---

## 15. Practical Checklist

Use this when thinking about Postgres in a backend design:

- Does this domain require transactional integrity?
- Is the write model clear before I optimize reporting?
- Are `NOT NULL`, `UNIQUE`, `CHECK`, and foreign keys carrying the important local rules?
- Do I understand the most important queries?
- Are the right indexes in place for those queries?
- Could concurrency create lost updates or race conditions?
- Is the transaction short and well-bounded?
- Is connection pool size intentional?
- Could MVCC/vacuum health affect this workload?
- Do I need a normal view, a materialized view, or neither?
- Are runtime, reporting, and migration roles separated enough?
- Am I using JSONB for the right reasons?

---

## 16. Interview Framing

Practical summary:

> Postgres is usually my default for critical backend domains because it gives me
> ACID transactions, strong consistency, mature query capabilities, and reliable
> concurrency control. The main things I keep in mind are transaction boundaries,
> locking strategy, indexing based on real query patterns, and measuring with
> EXPLAIN ANALYZE before tuning.

## What To Internalize

- Postgres is a strong default for correctness-critical domains
- MVCC explains both concurrency behavior and why vacuum matters
- isolation level alone does not fix weak write logic
- a good Postgres table starts with honest constraints before clever optimization
- indexes and plans should follow real query patterns
- views help read clarity; materialized views help repeated heavy reads when stale data is acceptable
- least-privilege roles are part of practical design, not only security theater
- replicas help, but primary truth and replica lag must stay explicit

---

## Further Reading

- PostgreSQL constraints:
  https://www.postgresql.org/docs/current/ddl-constraints.html
- PostgreSQL `CREATE INDEX`:
  https://www.postgresql.org/docs/current/sql-createindex.html
- PostgreSQL materialized views:
  https://www.postgresql.org/docs/16/rules-materializedviews.html
- PostgreSQL table partitioning:
  https://www.postgresql.org/docs/current/ddl-partitioning.html
