# Databases

Use this block to protect data under retries and concurrent writes, choose a
store that fits the work, and diagnose performance from evidence. Start with
the business fact and its owner before tuning a query or splitting storage.

## Correctness and SQL

1. [Idempotency and transactions](./01-idempotency-and-transaction-safety.md): keep retries and local commits safe.
2. [Locks and concurrency](./02-database-locks-and-concurrency.md): protect competing writes.
3. [SQL refresh](./03-sql-refresh-for-backend-engineers.md): joins, aggregation, CTEs, and window functions.
4. [Query optimization](./08-query-optimization.md): locate slow work, inspect plans, and verify one change at a time.

## Storage and Scale Choices

- [Choose a data store](./04-sql-vs-nosql.md): start from business rules and required reads and writes.
- [Database scaling](./05-database-scaling.md): locate the bottleneck before changing capacity or topology.
- [Redis](./06-redis-in-depth.md): data structures and operational tradeoffs.
- [PostgreSQL](./10-postgres-in-depth.md): constraints, vacuum, connections, and replicas.

## Other Store and Delivery Models

- [MongoDB](./07-mongodb-in-depth.md): document modeling and query behavior.
- [DynamoDB](./09-dynamodb.md): design from access patterns, consistency, and hot-key risk.
- [Redis Streams and Pub/Sub](./11-redis-streams-and-delivery-tradeoffs.md): delivery semantics when Redis carries messages.

Use the [SQL query review lab](../../labs/sql-query-review/README.md) after
query optimization. Its prompts guide your own comparison of plans, indexes,
and pagination on a local PostgreSQL database.
