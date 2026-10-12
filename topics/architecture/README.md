# Architecture and Distributed Systems

Use this block to decide where software boundaries belong and what changes
when work crosses a process, database, network, or team boundary. Start from
the business need and failure behavior, then choose the smallest mechanism
that makes the system correct and operable.

## Service and Data Boundaries

- [Choose application and service boundaries](./01-monolith-vs-microservices.md): when a service split earns its cost.
- [Shared database during a split](./18-shared-db-and-independent-deployments.md): deployment versus write ownership.
- [Cross-domain reads](./19-database-per-service-and-read-model-migration.md): API composition, projections, and reporting.
- [Domain-driven design](./09-domain-driven-design.md): business language and model boundaries.
- [Hexagonal architecture](./21-hexagonal-architecture.md): keep business rules independent from adapters and frameworks.

## Workflows and Integration

- [Choose a distributed workflow pattern](./16-distributed-workflow-pattern-choice.md): begin with the coordination gap.
- [Recover a cross-service workflow](./03-distributed-transactions-and-events.md): partial states, compensation, outbox, and retries.
- [Webhooks, events, and reactive code](./06-reactive-and-event-driven-basics.md): three different boundaries.
- [Integrate external systems](./13-enterprise-integration-patterns.md): translation, update cadence, and reconciliation.
- [Edge patterns](./17-gateway-vs-bff-vs-edge-patterns.md): gateway, backend for frontend, reverse proxy, and service mesh.
- [Self-service edge platforms](./20-self-service-edge-platforms.md): provide shared edge capability to teams safely.

## Failure and Scale Mechanics

- [Resiliency patterns](./02-resiliency-patterns.md): timeouts, retries, circuit breakers, and degraded behavior.
- [Networking fundamentals](./04-networking-fundamentals.md): HTTP, DNS, load balancing, and network failure.
- [Distributed tracing](./05-distributed-tracing.md): follow one request across services.
- [Caching strategies](./07-caching-strategies.md): improve reads while managing stale data and invalidation.
- [Concurrency models](./08-concurrency-models-comparison.md): compare ways to run work concurrently.
- [Consistent hashing](./10-consistent-hashing.md): distribute keys while limiting reshuffling.
- [Service discovery](./11-service-discovery.md): locate changing service instances.
- [Leader election and leases](./12-consensus-and-leader-election.md): coordinate workers without assuming exactly-once effects.

## Domain Examples

- [B2B and B2C commerce](./14-b2b-vs-b2c-commerce-systems.md): buyer identity, contract pricing, and approval decisions.
- [Inventory and fulfillment](./15-retail-inventory-and-fulfillment-systems.md): sellable stock, reservation, and physical work.
