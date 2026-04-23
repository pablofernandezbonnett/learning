# Architecture and Distributed Systems

Use this folder for practical architecture refresh around service boundaries,
distributed flows, failure handling, and network behavior.

Focus:

- monolith versus microservices decisions
- retries, timeouts, circuit breakers, and graceful degradation
- distributed transactions, outbox, and idempotent consumers
- networking, tracing, and event-driven flows
- caching, concurrency, service discovery, and advanced distributed-system mechanics

## Recommended Order

1. [01-monolith-vs-microservices.md](./01-monolith-vs-microservices.md)
2. [02-resiliency-patterns.md](./02-resiliency-patterns.md)
3. [03-distributed-transactions-and-events.md](./03-distributed-transactions-and-events.md)
4. [04-networking-fundamentals.md](./04-networking-fundamentals.md)
5. [05-distributed-tracing.md](./05-distributed-tracing.md)
6. [06-reactive-and-event-driven-basics.md](./06-reactive-and-event-driven-basics.md)

## Refresh

- [01-monolith-vs-microservices.md](./01-monolith-vs-microservices.md)
- [02-resiliency-patterns.md](./02-resiliency-patterns.md)
- [03-distributed-transactions-and-events.md](./03-distributed-transactions-and-events.md)
- [04-networking-fundamentals.md](./04-networking-fundamentals.md)
- [05-distributed-tracing.md](./05-distributed-tracing.md)
- [06-reactive-and-event-driven-basics.md](./06-reactive-and-event-driven-basics.md)

## Required

- [07-caching-strategies.md](./07-caching-strategies.md)
- [09-domain-driven-design.md](./09-domain-driven-design.md)
- [11-service-discovery.md](./11-service-discovery.md)

## Growth

- [08-concurrency-models-comparison.md](./08-concurrency-models-comparison.md)
- [10-consistent-hashing.md](./10-consistent-hashing.md)
- [12-consensus-and-leader-election.md](./12-consensus-and-leader-election.md)
- [13-enterprise-integration-patterns.md](./13-enterprise-integration-patterns.md)
- [14-b2b-vs-b2c-commerce-systems.md](./14-b2b-vs-b2c-commerce-systems.md)
- [15-retail-inventory-and-fulfillment-systems.md](./15-retail-inventory-and-fulfillment-systems.md)

## Core Rule

- service boundaries matter more than service count
- local correctness, replay safety, and recovery paths matter more than diagram density
- distributed systems move complexity into communication and operations
