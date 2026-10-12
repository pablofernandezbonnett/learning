# PostgreSQL: Data Integrity and Operating Choices

PostgreSQL is a relational store for data that needs constraints, transactions,
and flexible queries. Choosing it is only the start: transaction duration,
connection count, row-version cleanup, and replica lag affect how the system
behaves under load and failure.

## Put Local Rules in the Database

Use primary keys, foreign keys, `NOT NULL`, `CHECK`, and `UNIQUE` constraints
for rules the database can enforce. For example, a tenant's external order ID
must be unique even if two requests arrive together:

```sql
CREATE TABLE orders (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id bigint NOT NULL,
    external_order_id text NOT NULL,
    amount_cents bigint NOT NULL CHECK (amount_cents >= 0),
    UNIQUE (tenant_id, external_order_id)
);
```

The constraint protects this one-table rule; it does not make a payment-provider
call and the database commit one transaction. For retries and multi-table
writes, see [Idempotency and transaction safety](./01-idempotency-and-transaction-safety.md).
For competing updates, see [Database locks and concurrency](./02-database-locks-and-concurrency.md).

PostgreSQL defaults to `READ COMMITTED`: each statement sees a snapshot of
committed data, so two reads in one transaction may see different results.
Choose a concurrency control from the business invariant rather than assuming
an isolation level alone will protect it.

## Understand MVCC and Cleanup

With multiversion concurrency control (MVCC), an update creates a new row
version while older versions may remain visible to existing readers. Ordinary
reads and writes generally avoid blocking each other, but updates to the same
row can still contend. Old row versions later need cleanup.

`VACUUM` makes space from dead row versions reusable; autovacuum runs routine
cleanup and statistics collection. Long-running transactions can delay removal
of versions they might still need. For update-heavy tables, watch dead tuples,
table growth, vacuum progress, and old transactions before treating a slower
query as an index problem. Routine `VACUUM` does not normally return all freed
space to the operating system.

For slow SQL, use the measured query and its plan in
[Query optimization](./08-query-optimization.md); this note does not repeat the
plan-reading method.

## Keep Connections Within Capacity

Each application instance contributes connections to PostgreSQL. Size pools
for the total deployment, including background jobs and migration processes.
If requests wait for a pool connection, measure the wait, transaction length,
database CPU, and active sessions. Raising every pool limit can increase
contention and memory use without improving throughput.

Keep transactions short and return connections promptly. A pooler can help
when many clients need short database sessions, but its transaction or session
mode must fit the application's connection behavior.

## Treat Replicas as a Freshness Decision

A standby can serve reads and be promoted after primary failure. With
asynchronous replication, a standby may lag; a newly committed order may not
appear there immediately, and failover can lose recent commits that did not
reach it. Route read-after-write-sensitive decisions to the primary or use an
explicit wait-for-replay strategy.

Synchronous replication can make commit wait for a configured standby. That
changes write latency and availability when the standby is slow or absent.
Document the acceptable data-loss and recovery windows, monitor replay lag,
and test promotion and application reconnection. A second server alone does
not define a safe failover process.

## Use PostgreSQL Read and Storage Features for a Reason

| Feature | Useful when | Decision to make |
|---|---|---|
| View | Several callers need one reusable query | Can the underlying query stay fast? |
| Materialized view | A costly result can be refreshed on a schedule | How stale may it be, and who runs refresh? |
| `jsonb` | Some attributes have variable shape | Which fields need constraints or indexes? |
| Table partitioning | Retention, maintenance, or range queries strain one large table | Will queries include the partition key, and how are partitions managed? |

A normal view stores a query definition; a materialized view stores results and
needs refresh. `jsonb` does not replace explicit columns for stable business
fields, and partitioning does not make an inefficient query fast by itself.
Test the actual query and maintenance workload before adding either feature.

Separate the runtime role from schema-migration and reporting roles. Grant
each only the operations it needs; the application normally does not need
permission to alter tables.

## Review Before Production

1. Name the write invariants and enforce local ones with constraints or an
   appropriate guarded write.
2. Test a duplicate request and two concurrent updates to the same fact.
3. Measure query cost, connection waits, long transactions, and vacuum health.
4. Define which reads may use replicas and how failover changes routing.
5. Rehearse backup restoration and schema changes against realistic data.

## PostgreSQL References

- [MVCC and transaction isolation](https://www.postgresql.org/docs/current/mvcc-intro.html)
- [Routine vacuuming](https://www.postgresql.org/docs/current/routine-vacuuming.html)
- [Warm standby and replication](https://www.postgresql.org/docs/current/warm-standby.html)
- [Materialized view refresh](https://www.postgresql.org/docs/current/sql-refreshmaterializedview.html)
- [Table partitioning](https://www.postgresql.org/docs/current/ddl-partitioning.html)
