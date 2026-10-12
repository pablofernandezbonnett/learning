# Diagnose and Improve a Slow Query

Use this note when a request or SQL statement is slow. The first job is to
locate the time: a slow endpoint may be waiting for a connection, running many
queries, calling another service, or executing one expensive statement. Tune
the part that evidence identifies.

## 1. Find the Expensive Boundary

For a slow **request**, inspect a trace or timing breakdown. Separate time
spent waiting for a database connection from SQL execution, application work,
cache misses, and outbound calls. Count statements too: a fast query repeated
hundreds of times can dominate the request.

For a slow **statement**, capture the actual SQL, representative parameter
values, table sizes, and the execution plan. Compare the same workload before
and after one change. Do not add indexes from repository code alone; an ORM
may generate a different query from the one you expect.

## 2. Read the Plan Without Guessing

PostgreSQL's `EXPLAIN` shows the chosen plan; `ANALYZE` runs the statement and
adds observed row counts and timing. For a read-only query:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, status, created_at
FROM orders
WHERE tenant_id = 42 AND status = 'PAID'
ORDER BY created_at DESC
LIMIT 50;
```

Read the plan from the scans upward. Start with:

- **Estimated versus actual rows:** a large mismatch can lead the planner to
  choose a poor path; check statistics and parameter values.
- **Rows, loops, and filters:** identify repeated work or many rows discarded
  before the small result is returned.
- **Sorts and joins:** check whether they process far more rows than the
  endpoint needs, or spill to disk.
- **Buffers and elapsed time:** locate expensive nodes; a buffer hit is not
  the same as a physical disk read.

A sequential scan is not automatically bad, and an index scan is not
automatically good. A small table or a query returning much of a table can
make a sequential scan cheaper. An index-only scan may still fetch table
pages when PostgreSQL needs to check row visibility. Judge the whole plan and
the measured time, not one node name.

`EXPLAIN ANALYZE` **executes** the statement. Use care with writes and other
side effects; do not run it on a production mutation as if it were read-only.

## 3. Change the Query or Index That Causes the Cost

For the example above, this is a candidate index:

```sql
CREATE INDEX idx_orders_tenant_status_created
ON orders (tenant_id, status, created_at DESC);
```

The leading columns match equality filters, followed by the requested order.
That may let PostgreSQL find the first matching rows without a large sort.
It is a **hypothesis**, not a universal index recipe: compare the plan and
latency on representative data. A different predicate, sort, or data
distribution may call for another shape.

Before adding an index, ask which real query becomes cheaper and what it
costs in writes and storage. Primary keys and real unique constraints already
provide indexes in PostgreSQL. Add other indexes for observed joins, lookups,
and filtered lists; review overlapping ones rather than indexing every
column. [PostgreSQL's index guide](https://www.postgresql.org/docs/current/indexes.html)
explains the tradeoffs and index types.

An index cannot fix every read shape. Check for:

- **N+1 queries:** loading a list and then one associated record collection
  per item. Inspect generated SQL and consider a bounded fetch, projection,
  or batch strategy.
- **Too many columns or rows:** return the fields the screen needs and limit
  each page.
- **Deep `OFFSET`:** skipped rows still need work. If the user pages far
  through a stable ordered list, use a cursor with a unique tie-breaker such
  as `(created_at, id)` and an index that supports that order.
- **A mismatched read model:** after simpler query and index changes fail,
  consider a projection or search store with an explicit freshness and
  rebuild plan. The original owner still makes the final business decision.

If the SQL is fast but the request waits, inspect connection-pool saturation,
long transactions, and downstream latency before changing indexes.

## 4. Verify the Improvement

Run the same plan and request measurement again. Compare statement time,
rows processed, sort work, buffer use, request latency, and any added write
cost. Test with realistic parameters and data volume; an empty development
table cannot prove a production index will help.

Use the [SQL query review lab](../../labs/sql-query-review/README.md) to
compare a plan before and after an index and to inspect pagination cost.

## Primary References

- [PostgreSQL: using `EXPLAIN`](https://www.postgresql.org/docs/current/using-explain.html) for plan nodes, estimates, actual work, and buffers.
- [PostgreSQL: multicolumn indexes](https://www.postgresql.org/docs/current/indexes-multicolumn.html) for leading-column behavior.
- [PostgreSQL: `LIMIT` and `OFFSET`](https://www.postgresql.org/docs/current/queries-limit.html) for skipped-row cost and stable ordering.
