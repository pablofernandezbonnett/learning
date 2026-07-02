# API Design and Integration

Use this folder for practical API design and integration refresh.
The main concern here is not endpoint fashion.
It is how requests behave under retries, partial failure, asynchronous delivery,
and long-lived client contracts.

Focus:

- pagination, errors, and versioning so clients can evolve without surprises
- HTTP semantics such as caching, headers, conditional requests, and compression
- retry-safe write paths so repeated requests do not create duplicate business actions
- queues, logs, and delivery semantics, meaning what your messaging system really guarantees about retries, ordering, and duplicates
- webhook intake and replay safety when another system calls you back later

Decision boundary for broker material:

- start with `02` if you need the real mental model for queue vs log, retries, and idempotent consumers
- use `05` only as a short reopen after `02`, not as the primary note
- use `06` when the question is specifically about Kafka partitions, offsets, consumer groups, and `DLT` handling

## Recommended Order

1. [01-advanced-api-design.md](./01-advanced-api-design.md): pagination, error shape, versioning, and other contract choices that clients feel directly
2. [09-http-caching-etag-and-compression.md](./09-http-caching-etag-and-compression.md): how reuse, validation, and transfer size affect API behavior under real traffic
3. [02-message-brokers-and-delivery-semantics.md](./02-message-brokers-and-delivery-semantics.md): queues, logs, partitions, retry behavior, and the outbox pattern that keeps a DB write and event publish coordinated
4. [06-kafka-practical-foundations.md](./06-kafka-practical-foundations.md): partition keys, consumer groups, offsets, idempotent consumers, and `DLT` thinking, meaning what to do with messages that keep failing and should leave the main retry path
5. [03-webhooks-basics.md](./03-webhooks-basics.md): webhook intake, signature checks, deduplication, and safe asynchronous follow-up work
6. [07-sync-vs-async-integration-choice.md](./07-sync-vs-async-integration-choice.md): how to choose the boundary between immediate response and later async processing
7. [08-contract-testing-and-api-evolution.md](./08-contract-testing-and-api-evolution.md): how to evolve service and API contracts safely, and where contract tests help before full end-to-end integration
8. [00-rest-vs-graphql.md](./00-rest-vs-graphql.md): when a resource-oriented API is the simpler fit and when a graph-shaped client contract helps more
9. [04-graphql-in-depth.md](./04-graphql-in-depth.md): schema design, resolvers, `DataLoader`, meaning a batching helper that avoids one query per nested item, and how to stop flexible queries from becoming expensive or unsafe

## Refresh

- [01-advanced-api-design.md](./01-advanced-api-design.md)
- [09-http-caching-etag-and-compression.md](./09-http-caching-etag-and-compression.md)
- [02-message-brokers-and-delivery-semantics.md](./02-message-brokers-and-delivery-semantics.md)
- [06-kafka-practical-foundations.md](./06-kafka-practical-foundations.md)
- [03-webhooks-basics.md](./03-webhooks-basics.md)
- [07-sync-vs-async-integration-choice.md](./07-sync-vs-async-integration-choice.md)
- [08-contract-testing-and-api-evolution.md](./08-contract-testing-and-api-evolution.md)

## Required

- [09-http-caching-etag-and-compression.md](./09-http-caching-etag-and-compression.md)

## Growth

- [00-rest-vs-graphql.md](./00-rest-vs-graphql.md)
- [04-graphql-in-depth.md](./04-graphql-in-depth.md)
- [05-message-brokers-cheatsheet.md](./05-message-brokers-cheatsheet.md): reopen-only companion for `02` and `06`, not a standalone study path

## Companion Lab

Use [../../labs/kotlin-backend-examples/README.md](../../labs/kotlin-backend-examples/README.md)
topics `integration/async-boundaries` and `integration/kafka-patterns` after
reading the broker and Kafka notes.

## Core Rule

- API style is a tradeoff, not an ideology
- error shape and retry behavior matter as much as endpoint naming
- async delivery always changes failure and replay behavior
