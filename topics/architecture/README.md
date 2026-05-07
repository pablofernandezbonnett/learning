# Architecture and Distributed Systems

Use this folder for practical architecture refresh around service boundaries,
distributed flows, failure handling, and network behavior.
The goal is to understand how backend systems stay correct and operable once the
work is split across processes, databases, queues, and teams.

Focus:

- monolith versus microservices decisions, which are really decisions about team boundaries and operational cost
- retries, timeouts, circuit breakers, and graceful degradation, meaning how the system behaves when one dependency is slow or down but the whole product should not collapse
- distributed transactions, outbox, and idempotent consumers, meaning how you keep a cross-system workflow safe when one local transaction no longer covers the whole flow
- networking, tracing, and event-driven flows so you can reason about where requests go and why failures become harder to debug
- caching, concurrency, service discovery, and other mechanics that affect scale, latency, and coordination

## Recommended Order

1. [01-monolith-vs-microservices.md](./01-monolith-vs-microservices.md): how to choose service boundaries without treating microservices as a default maturity badge
2. [02-resiliency-patterns.md](./02-resiliency-patterns.md): retries, timeouts, circuit breakers, fallback behavior, and the cost of each choice
3. [03-distributed-transactions-and-events.md](./03-distributed-transactions-and-events.md): what replaces one big transaction when a workflow crosses services, including outbox and compensation
4. [04-networking-fundamentals.md](./04-networking-fundamentals.md): HTTP, gRPC, DNS, load balancing, and the network-level realities behind service-to-service calls
5. [05-distributed-tracing.md](./05-distributed-tracing.md): how to follow one request across several hops without guessing from logs alone
6. [06-reactive-and-event-driven-basics.md](./06-reactive-and-event-driven-basics.md): when asynchronous flows help and when they mainly add complexity
7. [16-distributed-workflow-pattern-choice.md](./16-distributed-workflow-pattern-choice.md): how to choose between queue, event stream, outbox, saga, and `CQRS` based on the real coordination problem

## Refresh

- [01-monolith-vs-microservices.md](./01-monolith-vs-microservices.md)
- [02-resiliency-patterns.md](./02-resiliency-patterns.md)
- [03-distributed-transactions-and-events.md](./03-distributed-transactions-and-events.md)
- [04-networking-fundamentals.md](./04-networking-fundamentals.md)
- [05-distributed-tracing.md](./05-distributed-tracing.md)
- [06-reactive-and-event-driven-basics.md](./06-reactive-and-event-driven-basics.md)
- [16-distributed-workflow-pattern-choice.md](./16-distributed-workflow-pattern-choice.md)

## Required

- [07-caching-strategies.md](./07-caching-strategies.md): where cache helps, where stale data hurts, and how to think about invalidation
- [09-domain-driven-design.md](./09-domain-driven-design.md): how to shape boundaries and business language without turning `DDD` (`Domain-Driven Design`) into ceremony
- [11-service-discovery.md](./11-service-discovery.md): how services find each other in dynamic environments such as Kubernetes

## Growth

- [08-concurrency-models-comparison.md](./08-concurrency-models-comparison.md): shared-memory threads, goroutines, coroutines, and virtual threads in practical backend terms
- [10-consistent-hashing.md](./10-consistent-hashing.md): how distributed caches and partitioned systems spread load without reshuffling everything
- [12-consensus-and-leader-election.md](./12-consensus-and-leader-election.md): how one instance becomes the active coordinator safely, and why this is harder than a plain lock
- [13-enterprise-integration-patterns.md](./13-enterprise-integration-patterns.md): practical message and integration patterns without turning the topic into acronym soup
- [14-b2b-vs-b2c-commerce-systems.md](./14-b2b-vs-b2c-commerce-systems.md): how requirements change when the same platform serves `B2C` (end consumers) versus `B2B` (business accounts)
- [15-retail-inventory-and-fulfillment-systems.md](./15-retail-inventory-and-fulfillment-systems.md): real operational tradeoffs around stock, reservation, order routing, and fulfillment

## Core Rule

- service boundaries matter more than service count
- local correctness, replay safety, and recovery paths matter more than diagram density
- distributed systems move complexity into communication and operations
