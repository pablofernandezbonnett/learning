# API Design and Integration

Use this folder for practical API design and integration refresh.

Focus:

- pagination, errors, and versioning
- retry-safe write paths
- queues, logs, and delivery semantics
- webhook intake and replay safety

## Recommended Order

1. [00-rest-vs-graphql.md](./00-rest-vs-graphql.md): REST versus GraphQL tradeoffs
2. [01-advanced-api-design.md](./01-advanced-api-design.md): pagination, error shape, versioning, and client-facing contracts
3. [02-message-brokers-and-delivery-semantics.md](./02-message-brokers-and-delivery-semantics.md): queues, logs, partitions, delivery semantics, and outbox
4. [03-webhooks-basics.md](./03-webhooks-basics.md): webhook intake, verification, deduplication, and async processing
5. [04-graphql-in-depth.md](./04-graphql-in-depth.md): schema, resolvers, DataLoader, and query protection

## Refresh

- [01-advanced-api-design.md](./01-advanced-api-design.md)
- [02-message-brokers-and-delivery-semantics.md](./02-message-brokers-and-delivery-semantics.md)
- [03-webhooks-basics.md](./03-webhooks-basics.md)

## Required

- [00-rest-vs-graphql.md](./00-rest-vs-graphql.md)
- [04-graphql-in-depth.md](./04-graphql-in-depth.md)

## Growth

- [05-message-brokers-cheatsheet.md](./05-message-brokers-cheatsheet.md)

## Core Rule

- API style is a tradeoff, not an ideology
- error shape and retry behavior matter as much as endpoint naming
- async delivery always changes failure and replay behavior
