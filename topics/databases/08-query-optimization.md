# Query Optimization for Backend Engineers

> Primary fit: `Shared core`


Slow query diagnosis is one of the highest-value senior backend topics because it tests
whether you can reason from symptoms to evidence instead of guessing.

This note follows the same reusable pattern:

- what query optimization actually means
- the smallest broken example
- how to measure correctly
- the practical summary

---

## 1. What Query Optimization Actually Means

Query optimization is not "add an index when something feels slow".

It is usually this loop:

1. identify the slow endpoint or flow
2. inspect the real SQL or query pattern
3. inspect the execution plan
4. change one of:
   - index
   - actual query
   - how the application reads the data
   - pagination strategy
   - connection behavior
5. measure again

Short rule:

> measure first, then fix

---

## 2. The Smallest Broken Example: N+1

The most common Spring and JPA performance bug is not an advanced SQL trick.
It is N+1.

```kotlin
val orders = orderRepository.findAll()
orders.forEach { order ->
    println(order.items.size)
}
```

Why it is broken:

- one query loads the orders
- then one extra query runs for each order's items

That means:

- 1 query for the list
- N more queries for associations

Typical fixes:

- fetch join
- entity graph
- projection or DTO query
- batch fetching

Practical rule:

> if the endpoint is read-heavy, inspect the generated SQL instead of trusting the
> repository abstraction blindly

---

## 3. The First Diagnosis Step: Execution Plan

In Postgres, the normal tool is:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT o.id, o.status
FROM orders o
WHERE o.store_id = 42
ORDER BY o.created_at DESC;
```

What you are looking for:

- `Seq Scan` on a large table when an indexable filter exists
- huge difference between rows examined and rows returned
- expensive sort or join behavior

Fast mental map:

- `Seq Scan`: whole table scan
- `Index Scan`: index used, then heap lookup
- `Index Only Scan`: best read path, no heap access needed
- `Nested Loop`: okay when inner side is small, dangerous when it is not
- `Hash Join`: often good for larger joins

Clean practical line:

> I do not guess at query tuning. I run `EXPLAIN ANALYZE`, check scan type, row counts,
> and the actual time spent, then change the query or index based on evidence.

---

## 4. Indexes: The Smallest Mental Model

Without an index, the database often scans the table row by row.
With an index, it can jump toward the matching rows instead of reading everything.

What matters in practice:

- indexes speed up reads
- indexes cost write performance
- index choice depends on the real filter and sort pattern

Good default index type:

- B-tree for equality, range, sorting, and prefix lookups

Common useful patterns:

- compound index when the query filters and sorts
- partial index when only part of the table matters often
- covering index when you want index-only scans

Example:

```sql
CREATE INDEX idx_orders_store_status_date
ON orders(store_id, status, created_at DESC);
```

Why this is a good answer:

- equality columns first
- sort column after
- index matches the real query instead of existing "just in case"

---

## 5. Pagination: OFFSET vs Keyset

Offset pagination is one of the most common practical query traps.

Broken version for deep pages:

```sql
SELECT *
FROM products
ORDER BY created_at DESC
LIMIT 20 OFFSET 10000;
```

Why it degrades:

- the database still walks and discards the first 10,000 rows
- deeper pages get slower linearly

Keyset version:

```sql
SELECT *
FROM products
WHERE (created_at, id) < ('2026-03-26 10:00:00', 4821)
ORDER BY created_at DESC, id DESC
LIMIT 20;
```

Why it is better:

- it uses the index boundary directly
- deeper pages stay efficient
- sort order is more stable

Practical rule:

- use offset for bounded admin pagination
- use keyset for feeds, histories, and deep user-facing pagination

---

## 6. Query Shape Matters More Than ORM Comfort

A lot of slow endpoints come from fetching the wrong data:

- full entities when only three fields are needed
- huge joins for a list screen
- late lazy loads during serialization

Better options:

- projections
- DTO queries
- narrower read models
- native SQL when the actual query matters more than ORM convenience

Short rule:

> read models and write models do not always need the same object structure

---

## 7. Connection Pooling Is Part Of Query Performance

Sometimes the query is fast but the request is still slow because it is waiting for a DB
connection.

That means you should also ask:

- is the connection pool exhausted?
- are transactions holding connections too long?

HikariCP example:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      connection-timeout: 2000
      leak-detection-threshold: 5000
```

What this means in practice:

- a slow request is not always a slow SQL statement
- queueing for connections can be the real latency source

---

## 8. MongoDB Note

The same general rule still applies in MongoDB:

- inspect the real plan
- do not assume the query is fine because the syntax is short

The equivalent question is:

- are we seeing `IXSCAN` or `COLLSCAN`?

For aggregation pipelines:

- put `$match` early
- put `$sort` early when it can use an index
- limit early when possible
- treat `$lookup` as expensive

---

## 9. Slow Query Diagnosis Loop

This is the practical sequence:

1. confirm the slow endpoint
2. identify the real SQL or Mongo query
3. inspect execution plan
4. compare rows examined vs rows returned
5. check indexes and sort pattern
6. check N+1 or ORM-generated extra queries
7. check connection pool and transaction scope
8. change one thing and measure again

---

## 10. 20-Second Answer

> My first query optimization step is always measurement, not guessing. In Spring apps I
> first look for N+1 and the actual generated SQL. In Postgres I use `EXPLAIN ANALYZE` to
> inspect scan type, row counts, and timing. Then I decide whether the problem is an index,
> the actual SQL pattern, deep offset pagination, or connection pool contention.

---

## 11. 1-Minute Answer

> I approach query optimization as an evidence problem. The most common application-layer
> issue is N+1, so in JPA-based services I inspect the generated SQL early instead of
> trusting repository code. In Postgres I use `EXPLAIN ANALYZE` and look for sequential
> scans on large tables, high rows-examined versus rows-returned, and expensive sort or
> join behavior. I choose indexes based on the actual filter and sort pattern rather than
> adding them blindly, and I remember that indexes improve reads but cost writes. I also
> look at pagination strategy because deep `OFFSET` queries degrade badly and keyset is often
> a better fit for user-facing feeds. Finally, I check the connection pool and transaction
> scope because some "slow queries" are actually fast queries waiting for a JDBC connection.

---

## 12. What To Internalize

- measure before tuning
- N+1 is one of the most common real production query problems
- execution plans matter more than intuition
- indexes must match the real query, not wishful thinking
- deep offset pagination is often a hidden performance trap
- query latency and connection pool latency are different problems
