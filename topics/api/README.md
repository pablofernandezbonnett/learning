# API Design and Integration

Use this folder for practical API design and integration refresh.
The main concern here is not endpoint fashion.
It is how requests behave under retries, partial failure, asynchronous delivery,
and long-lived client contracts.

Focus:

- pagination, errors, and versioning so clients can evolve without surprises
- retry-safe write paths so repeated requests do not create duplicate business actions
- queues, logs, and delivery semantics, meaning what your messaging system really guarantees about retries, ordering, and duplicates
- webhook intake and replay safety when another system calls you back later

## Recommended Order

1. [00-rest-vs-graphql.md](./00-rest-vs-graphql.md): when a resource-oriented API is the simpler fit and when a graph-shaped client contract helps more
2. [01-advanced-api-design.md](./01-advanced-api-design.md): pagination, error shape, versioning, and other contract choices that clients feel directly
3. [02-message-brokers-and-delivery-semantics.md](./02-message-brokers-and-delivery-semantics.md): queues, logs, partitions, retry behavior, and the outbox pattern that keeps a DB write and event publish coordinated
4. [03-webhooks-basics.md](./03-webhooks-basics.md): webhook intake, signature checks, deduplication, and safe asynchronous follow-up work
5. [04-graphql-in-depth.md](./04-graphql-in-depth.md): schema design, resolvers, DataLoader, and how to stop flexible queries from becoming expensive or unsafe

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
