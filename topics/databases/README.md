# Databases for Backend Engineers

Use this folder to refresh the database topics that most often decide backend
correctness and performance.
The emphasis is not just on SQL syntax.
It is on the places where data modeling, transactions, retries, and concurrency
directly decide whether the business result is correct.

Focus:

- idempotency and transaction safety, meaning the same request can be retried without creating a second business result by accident
- database concurrency and locking, meaning what protects you when two writes overlap on the same data
- SQL judgment beyond ORM convenience, because production behavior still depends on query shape and indexes
- store choice, scaling, and database-specific tradeoffs instead of one vague "NoSQL vs SQL" debate

## Recommended Order

1. [01-idempotency-and-transaction-safety.md](./01-idempotency-and-transaction-safety.md): request identity, deduplication, and transaction boundaries
2. [02-database-locks-and-concurrency.md](./02-database-locks-and-concurrency.md): lost updates, optimistic locking, pessimistic locking, and isolation
3. [03-sql-refresh-for-backend-engineers.md](./03-sql-refresh-for-backend-engineers.md): joins, aggregation, CTEs, window functions, and pagination shape

## Refresh

- [01-idempotency-and-transaction-safety.md](./01-idempotency-and-transaction-safety.md)
- [02-database-locks-and-concurrency.md](./02-database-locks-and-concurrency.md)
- [03-sql-refresh-for-backend-engineers.md](./03-sql-refresh-for-backend-engineers.md)
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
