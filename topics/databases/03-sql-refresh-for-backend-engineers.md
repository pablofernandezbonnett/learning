# SQL: Shape the Result You Need

SQL remains useful even when an application uses an ORM. Start by naming what
one result row represents, then choose the joins, grouping, and ordering that
produce it. This note is a short practice guide for reading and writing those
queries.

Before writing SQL, ask:

1. What does one output row represent: an order, a customer, or a daily total?
2. Can related data be absent, or can one row have many related rows?
3. Which rows are filtered before aggregation, and which groups afterward?
4. What order makes a limited result deterministic?

## Join Without Changing the Meaning

An `INNER JOIN` keeps only matching rows. A `LEFT JOIN` also keeps rows from
the left side when no match exists. For example, count paid orders while still
showing customers with none:

```sql
SELECT c.id, COUNT(o.id) AS paid_orders
FROM customers c
LEFT JOIN orders o
  ON o.customer_id = c.id AND o.status = 'PAID'
GROUP BY c.id;
```

The status filter belongs in `ON` here. Putting `o.status = 'PAID'` in `WHERE`
would remove customers without a matching order. `COUNT(o.id)` returns zero
for them; `COUNT(*)` would count the unmatched customer row as one.

Check **cardinality** before adding another join. Joining an order to three
lines and two payment attempts can produce six rows. A later `SUM` or `COUNT`
may then overstate the business value. Aggregate each one-to-many side first,
or query it separately when that is clearer.

## Group at the Intended Level

`GROUP BY` collapses rows into groups. Aggregates such as `COUNT` and `SUM`
then describe each group. `WHERE` filters input rows; `HAVING` filters grouped
results:

```sql
SELECT customer_id, COUNT(*) AS paid_orders
FROM orders
WHERE status = 'PAID'
GROUP BY customer_id
HAVING COUNT(*) >= 5;
```

This returns one row per customer who has at least five paid orders. If the
required result is one row per day instead, change the grouping key rather
than adding unrelated columns to `GROUP BY`.

## Use a CTE to Name a Step

A common table expression (`WITH`) names an intermediate result. It can make a
query with several steps easier to read:

```sql
WITH line_totals AS (
  SELECT order_id, SUM(quantity * unit_price_cents) AS total_cents
  FROM order_lines
  GROUP BY order_id
)
SELECT o.id, t.total_cents
FROM orders o
JOIN line_totals t ON t.order_id = o.id
WHERE o.customer_id = 42;
```

This CTE produces one total per order before the join. It is a readability
tool, not an automatic performance improvement. For a real workload, inspect
the plan; filtering orders first might be cheaper than aggregating every line.

## Use a Window When Rows Must Remain Separate

A window function calculates across related rows without collapsing them as
`GROUP BY` does. `ROW_NUMBER` can identify the latest order for each customer:

```sql
WITH ranked AS (
  SELECT o.id, o.customer_id, o.created_at,
         ROW_NUMBER() OVER (
           PARTITION BY o.customer_id
           ORDER BY o.created_at DESC, o.id DESC
         ) AS position
  FROM orders o
)
SELECT id, customer_id, created_at
FROM ranked
WHERE position = 1;
```

`id` breaks ties when two orders have the same timestamp. Other window
functions can rank rows or calculate running totals; choose one when the
output still needs individual rows.

## Check the Boundary With the Application

An ORM can map routine writes and simple reads well. Use explicit SQL when a
join, aggregation, or window makes the result clearer and easier to verify.
Inspect the generated SQL if an ORM hides query count or result shape.

For slow queries, indexes, and pagination, use
[Query optimization](./08-query-optimization.md). For local transaction
correctness, use [Idempotency and transaction safety](./01-idempotency-and-transaction-safety.md).
For deciding whether reporting needs a separate read model, use
[Cross-domain reads](../architecture/19-database-per-service-and-read-model-migration.md).

## Further Reading

- [PostgreSQL joins](https://www.postgresql.org/docs/current/tutorial-join.html)
- [Grouping and aggregates](https://www.postgresql.org/docs/current/tutorial-agg.html)
- [`WITH` queries](https://www.postgresql.org/docs/current/queries-with.html)
- [Window functions](https://www.postgresql.org/docs/current/tutorial-window.html)
