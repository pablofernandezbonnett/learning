# SQL Refresh for Backend Engineers

> Primary fit: `Shared core`


You do not need to be a DBA, but backend discussions get shallow fast if your
SQL intuition is rusty.

If you spent years behind JPA, Hibernate, or Spring Data, it is easy to stay
productive while letting raw SQL judgment get rusty.

This refresher is not about becoming a DBA.
It is about regaining the SQL tools that matter in backend work.

---

## 1. What Still Matters

For senior backend work, the high-value SQL topics are:

- ACID and transaction basics
- joins
- grouping and aggregation
- CTEs
- window functions
- pagination shape
- query plans and index awareness

You do not need every SQL feature.
You need to recognize when the abstraction is hiding something important.

## 2. ACID In Practical Terms

`ACID` is the short name for the four classic properties of a relational
transaction:

- `Atomicity`
- `Consistency`
- `Isolation`
- `Durability`

What that means in plain backend language:

- `Atomicity`: the local database changes inside the transaction happen together
  or not at all
- `Consistency`: the data should move from one valid state to another valid
  state if your constraints and business rules are correct
- `Isolation`: concurrent transactions should not see each other in a way that
  breaks correctness; the isolation level defines that behavior
- `Durability`: once the transaction commits, the database should keep the
  change even if the application crashes afterward

Smallest useful example:

- deduct balance
- create order row

If both writes are in one local database transaction:

- either both commit
- or both roll back

That is the practical value of `ACID`.

Important nuance:

`ACID` is about the **local database transaction**.
It does **not** mean one Spring method call gives you atomic consistency across:

- Kafka
- Stripe
- another service over HTTP
- email sending

So in real backend systems, `ACID` usually solves one part of the problem:

- keep local database truth correct

And then you still need other tools for the wider flow:

- idempotency
- retries
- outbox
- reconciliation
- compensation

Good line:

> ACID is what makes one local database transaction reliable: all-or-nothing
> writes, controlled concurrency behavior, and durable commit. It is very
> useful, but it only solves the local database boundary, not the whole
> distributed workflow.

---

## 3. Joins

Joins are still the bread and butter of relational queries.

The main practical ones:

- `INNER JOIN`: only matching rows
- `LEFT JOIN`: keep the left side even if the right side is missing

Backend examples:

- orders joined with customers
- orders joined with payments
- products joined with category metadata

Good rule:

> Use `LEFT JOIN` when missing related data is still a valid result.

Common mistake:

- writing a `LEFT JOIN` and then filtering the right table in `WHERE`, which can
  accidentally turn it back into inner-join behavior

---

## 4. Aggregation

Aggregation is how you answer questions like:

- total sales per day
- order count by status
- top products by revenue

The key tools:

- `GROUP BY`
- `COUNT`
- `SUM`
- `AVG`
- `HAVING`

Practical backend use:

- dashboards
- admin reports
- analytics-style API endpoints

Good rule:

> Group only by what the result really needs. Over-grouping usually means the
> query shape is not thought through.

---

## 5. Normalization and Denormalization in Backend Terms

Normalization sounds academic until duplicate business truth starts drifting.

Plain-English version:

- normalization means each important fact has one primary home
- denormalization means you copy or pre-shape data to make a read path easier

Why this matters:

- duplicate "current truth" becomes hard to update safely
- write logic gets fragile when one business fact lives in several places
- read-heavy paths sometimes still need a denormalized shape for speed or clarity

Useful backend translation of the common forms:

- `1NF`: keep one value per field, not repeating groups stuffed into one column
- `2NF`: if the row is identified by multiple columns, other fields should depend on the whole key
- `3NF`: non-key fields should describe the row itself, not some other non-key fact

Weak approach:

- copy current customer profile fields into every order row because it feels convenient

Better approach:

- keep current customer truth in `customers`
- keep order truth in `orders`
- copy only the facts that intentionally become historical snapshots, such as the delivery address used for that order or the price charged at checkout time

Strong default:

> normalize the transactional write path first, then denormalize only for a proven
> read shape or for a historical snapshot that must not change later

Important nuance:

- not every copy of data is a mistake
- copying `unit_price_paid` onto an order line is often correct because the business truth is "what the customer paid then," not "what the catalog says now"

---

## 6. OLTP vs OLAP Changes Schema Shape

This is one of the most useful design distinctions to keep fresh.

- `OLTP` means transactional processing: many small reads and writes, short transactions, current business truth
- `OLAP` means analytical processing: heavier scans, aggregation, historical analysis, reporting

Bad mental model:

- one schema shape should serve checkout writes and executive reporting equally well

Better mental model:

- design the transactional schema for correctness and safe writes first
- shape reporting reads separately with SQL, views, materialized views, or a warehouse-style model when reporting needs become heavy

Small concrete example:

- `orders`, `order_lines`, and `payments` are a good `OLTP` shape for creating and updating orders safely
- a daily revenue dashboard may be better served by a summarized read model than by asking the live checkout tables to answer every report directly

Practical rule:

> OLTP models protect the write path. OLAP models optimize analysis. If one shape
> tries to do both jobs equally, one of the jobs usually becomes awkward.

---

## 7. CTEs

CTE means Common Table Expression, usually written with `WITH`.

Why they help:

- break large queries into readable steps
- make report-like SQL easier to reason about
- help when the same intermediate set is easier to name explicitly

Example shape:

```sql
WITH recent_orders AS (
  SELECT id, customer_id, total_yen
  FROM orders
  WHERE created_at >= NOW() - INTERVAL '7 days'
)
SELECT customer_id, SUM(total_yen) AS weekly_total
FROM recent_orders
GROUP BY customer_id;
```

Important practical note:

- a CTE is not automatically a performance win
- use it first for readability and query structuring
- always verify with `EXPLAIN ANALYZE`

In modern PostgreSQL, non-recursive `WITH` queries can often be inlined, but you
still should not assume a CTE is free.

---

## 8. Window Functions

Window functions are one of the most useful SQL features to keep fresh.

They let you calculate across related rows without collapsing the result set the
way `GROUP BY` does.

Common examples:

- `row_number()`
- `rank()`
- running totals with `sum(...) over (...)`

Backend use cases:

- latest order per customer
- top N products per category
- ranking search or sales results
- rolling metrics

Concrete example:

```sql
SELECT *
FROM (
  SELECT
    o.*,
    row_number() OVER (
      PARTITION BY customer_id
      ORDER BY created_at DESC
    ) AS rn
  FROM orders o
) t
WHERE rn = 1;
```

That is often cleaner than complex self-joins.

---

## 9. When Native SQL Is Better Than ORM Magic

ORM tools are productive, but some queries are simply clearer in SQL.

Good candidates:

- reporting queries
- heavy aggregation
- window functions
- carefully tuned read paths
- database-specific features like partial indexes or JSONB operators

Good rule:

> Use ORM for common CRUD and aggregate boundaries. Use SQL when the query shape
> is the real problem to solve.

That is not a failure of the ORM.
It is just being honest about the abstraction boundary.

---

## 10. Pagination and Ordering

SQL refresher is also about remembering that query shape affects scale.

Important examples:

- deep `OFFSET` pagination gets slower as the offset grows
- keyset pagination is usually better for large, ordered feeds
- `ORDER BY` should match useful indexes when possible

This matters in:

- order history
- product listing
- admin search screens

---

## 11. What To Keep in Your Head

- "I still rely on SQL fundamentals when JPA abstractions stop being transparent."
- "ACID is about one local database transaction, not about making every external system part of the same atomic unit."
- "I normalize write truth first, then denormalize intentionally for reporting, snapshots, or a proven read path."
- "OLTP and OLAP are different jobs, so I do not force one schema shape to be perfect at both."
- "Window functions are especially useful for ranking and latest-per-group queries."
- "I use CTEs mainly for readability, then verify the actual plan instead of assuming they are faster."
- "If a reporting or aggregation query becomes awkward in JPA, I prefer explicit SQL over pretending the ORM is always the best interface."

---

## 12. Further Reading

- PostgreSQL joins tutorial:
  https://www.postgresql.org/docs/16/tutorial-join.html
- PostgreSQL `WITH` queries:
  https://www.postgresql.org/docs/14/queries-with.html
- PostgreSQL window functions tutorial:
  https://www.postgresql.org/docs/current/tutorial-window.html
- PostgreSQL window functions reference:
  https://www.postgresql.org/docs/17/functions-window.html
