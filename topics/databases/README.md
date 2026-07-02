# Databases for Backend Engineers

Use this folder to refresh the database topics that most often decide backend
correctness and performance.
The emphasis is not just on SQL syntax.
It is on the places where data modeling, transactions, retries, and concurrency
directly decide whether the business result is correct.

Why this folder matters:

- database mistakes often look like application bugs, but the root cause is
  usually a wrong data, transaction, or concurrency assumption
- backend interviews and production incidents both tend to expose weak judgment
  around retries, locks, query shape, and source of truth
- this folder keeps the focus on the database decisions that most often matter
  under real load and real business risk

Focus:

- idempotency and transaction safety, meaning the same request can be retried without creating a second business result by accident
- database concurrency and locking, meaning what protects you when two writes overlap on the same data
- SQL judgment beyond ORM convenience, because production behavior still depends on query shape and indexes
- store choice, scaling, and database-specific tradeoffs instead of one vague "NoSQL vs SQL" debate

Working style:

- explain the first useful meaning of the jargon before going deeper
- prefer short practical definitions over telegraphic shorthand
- connect DB mechanics back to checkout, payments, orders, and inventory
- use plain SQL first, and add Java or Kotlin only when application-level read shape or ORM behavior is the real teaching point

Smallest mental model:

- start with business truth
- then protect the write path
- then optimize query shape and scale without breaking correctness

Decision boundary for SQL performance material:

- use `03` when your SQL language and read-shape intuition feels rusty
- use `13` when you want the fastest reopen of plan reading and index review
- use `08` when you need the deeper performance diagnosis loop, including `N+1`, connection wait, and application read shape
- use `10` when the question is PostgreSQL-specific rather than generic SQL tuning

## Recommended Order

1. [01-idempotency-and-transaction-safety.md](./01-idempotency-and-transaction-safety.md): request identity, deduplication, and transaction boundaries
2. [02-database-locks-and-concurrency.md](./02-database-locks-and-concurrency.md): lost updates, optimistic locking, pessimistic locking, and isolation
3. [03-sql-refresh-for-backend-engineers.md](./03-sql-refresh-for-backend-engineers.md): joins, aggregation, CTEs, window functions, and pagination shape
4. [13-explain-indexes-and-query-review-baseline.md](./13-explain-indexes-and-query-review-baseline.md): fast reopen of `EXPLAIN`, index review, and practical tuning judgment

## Refresh

- [01-idempotency-and-transaction-safety.md](./01-idempotency-and-transaction-safety.md)
- [02-database-locks-and-concurrency.md](./02-database-locks-and-concurrency.md)
- [03-sql-refresh-for-backend-engineers.md](./03-sql-refresh-for-backend-engineers.md)
- [13-explain-indexes-and-query-review-baseline.md](./13-explain-indexes-and-query-review-baseline.md)
- [04-sql-vs-nosql.md](./04-sql-vs-nosql.md)

## Required

- [05-database-scaling.md](./05-database-scaling.md)
- [06-redis-in-depth.md](./06-redis-in-depth.md)
- [08-query-optimization.md](./08-query-optimization.md)
- [10-postgres-in-depth.md](./10-postgres-in-depth.md)

## Growth

- [07-mongodb-in-depth.md](./07-mongodb-in-depth.md)
- [09-dynamodb.md](./09-dynamodb.md)
- [11-redis-streams-and-delivery-tradeoffs.md](./11-redis-streams-and-delivery-tradeoffs.md)
- [12-dynamodb-cheatsheet.md](./12-dynamodb-cheatsheet.md)

## Core Rule

- local transactions solve the local database boundary, not the whole workflow
- concurrency bugs come from stale reads plus overlapping writes
- ORM convenience never removes the need for SQL judgment

## Companion Lab

Use [../../labs/kotlin-backend-examples/README.md](../../labs/kotlin-backend-examples/README.md)
topics `correctness/idempotency` and `correctness/locking` after the first two
core notes.

Use [../../labs/sql-query-review/README.md](../../labs/sql-query-review/README.md)
after `13`, `08`, or `10` when you want to inspect real PostgreSQL plans,
composite indexes, and pagination tradeoffs instead of only reading about them.
