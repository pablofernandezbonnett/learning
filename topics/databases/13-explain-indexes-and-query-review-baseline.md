# EXPLAIN, Indexes, and Query Review Baseline

Use this note when you want the fastest practical reopen of SQL performance
judgment for backend work.

This note stays SQL-first on purpose. The main learning value here is plan and
index judgment, not JVM syntax comparison.

Why this matters:

- slow endpoints often become "database problems" without anyone checking where
  the time really went
- many backend engineers know SQL syntax but still guess when query plans or
  indexes need review
- the highest-return SQL skill is not memorizing optimizer theory, but reading
  enough evidence to stop making random changes

## Smallest Useful Mental Model

Query tuning is usually one review loop:

1. find the slow endpoint or flow
2. inspect the real SQL
3. inspect the execution plan
4. decide whether the fix belongs in:
   - query shape
   - index shape
   - pagination shape
   - ORM read pattern
5. measure again

Short rule:

- measure first, then tune

## Bad Mental Model vs Better Mental Model

Bad mental model:

- the query feels slow, so add an index

Better mental model:

- first confirm the real SQL, then inspect the plan, then choose the smallest
  change that matches the evidence

Weak habits:

- tuning from repository code without looking at generated SQL
- adding several single-column indexes when the hot query uses a combined filter
- treating every sequential scan as a bug
- ignoring sort and pagination shape

Stronger habits:

- inspect the query plan before changing anything
- compare rows scanned with rows returned
- ask whether the index matches the real `WHERE`, `JOIN`, and `ORDER BY`
- review whether the read pattern itself is the real problem

## Small Concrete Example

Imagine a backoffice endpoint:

```sql
SELECT id, status, created_at
FROM orders
WHERE tenant_id = 42 AND status = 'PAID'
ORDER BY created_at DESC
LIMIT 50;
```

The first useful question is not "which index should I add?"
It is "what plan is the database using now?"

In PostgreSQL, the normal starting point is:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, status, created_at
FROM orders
WHERE tenant_id = 42 AND status = 'PAID'
ORDER BY created_at DESC
LIMIT 50;
```

If this endpoint is important, a common strong index shape is:

```sql
CREATE INDEX idx_orders_tenant_status_created
ON orders (tenant_id, status, created_at DESC);
```

Why this is usually stronger than separate indexes on each column:

- the filter columns and sort order live in one index path
- the planner does not need to improvise from partial access paths
- the index matches the actual list query instead of existing "just in case"

## What To Read In The Plan First

Start with only a few signals:

- scan type
- estimated rows
- actual rows
- sort work
- join behavior

Fast mental map:

- `Seq Scan`: full table scan
- `Index Scan`: index used, then table row lookup
- `Index Only Scan`: index may satisfy the read without going back to the table
- `Bitmap Index Scan` plus `Bitmap Heap Scan`: index helps, but many table rows still need fetching
- `Sort`: sorting happens after row retrieval
- `Nested Loop`: can be fine for small inner lookups, dangerous for large repeated work
- `Hash Join`: often useful for larger joins

What usually deserves attention:

- large table plus `Seq Scan` where the filter is selective
- row estimates very different from actual rows
- sort over far more rows than the endpoint really needs
- join plans doing repeated work because the read shape is wide or badly filtered

## Index Review Baseline

Good default questions before adding an index:

- which real query becomes cheaper?
- does the query filter by equality, range, or both?
- does it sort after filtering?
- does the hot path need one composite index instead of several single-column indexes?
- what write cost will this index add?

Strong defaults:

- keep the primary key and real unique constraints
- add foreign-key-supporting indexes when the access pattern needs them
- add one composite index for the dominant filtered-and-sorted list query
- add partial indexes only when a hot subset of rows matters repeatedly
- remove overlapping indexes when they stop serving a real query

## Query Review Checklist

When a read path is slow, check in this order:

1. is this really a database problem?
2. what SQL is actually running?
3. what does the plan say?
4. is the access path wrong, or is the query shape wrong?
5. is deep offset pagination, broad row width, or ORM-driven N+1 the bigger problem?
6. after one change, did the new plan and endpoint timing improve?

## Main Tradeoff or Failure Mode

Indexes make many reads faster.
They also:

- slow writes
- increase storage
- add maintenance cost
- make index sprawl easy

The common failure is not having too few tricks.
It is changing too many things without evidence.

## Practical Rule

If you cannot answer:

- what SQL runs
- what plan the database chose
- why the current index shape helps or does not help

then the tuning conversation is still too early.

## Reusable Takeaway

> Senior SQL tuning is mostly disciplined review: inspect the real SQL, inspect
> the plan, match indexes to real filter and sort patterns, and change one
> thing because the evidence points there.

## Further Reading

- PostgreSQL `EXPLAIN`: https://www.postgresql.org/docs/current/using-explain.html
- PostgreSQL `CREATE INDEX`: https://www.postgresql.org/docs/current/sql-createindex.html
- [08-query-optimization.md](./08-query-optimization.md): deeper companion note with broader examples and tradeoffs
