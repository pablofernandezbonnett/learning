# API Design and Integration

Start with [API design](./01-api-design.md) for the sequence from caller need
to contract, security, failure behavior, and evolution. Then follow the route
that matches the problem.

## HTTP Contracts

- [API design](./01-api-design.md): the general method and core decisions.
- [Hotel offer search case](./10-hotel-offer-search-case.md): apply the contract, pagination, and reservation decisions to one flow.
- [HTTP caching](./09-http-caching-etag-and-compression.md): freshness, conditional requests, and transfer cost.
- [Contract testing and evolution](./08-contract-testing-and-api-evolution.md): compatibility and verification across releases.
- [REST vs GraphQL](./00-rest-vs-graphql.md): choose a client-facing API style.
- [GraphQL in depth](./04-graphql-in-depth.md): schema, resolvers, query cost, and pagination when GraphQL is the fit.

## Async Integrations

- [Sync vs async](./07-sync-vs-async-integration-choice.md): decide what the caller needs now and what may finish later.
- [Brokers and delivery semantics](./02-message-brokers-and-delivery-semantics.md): queue or stream, duplicates, retries, and outbox.
- [Webhooks](./03-webhooks-basics.md): accept external events safely.
- [Kafka foundations](./06-kafka-practical-foundations.md): keys, partitions, offsets, consumer groups, and replay.

For practice, use the async boundary and Kafka delivery exercises in the
[Kotlin backend lab](../../labs/kotlin-backend-examples/README.md).
