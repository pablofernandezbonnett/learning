# Practical DDD in Spring Boot

This note applies [DDD's business boundaries](../architecture/09-domain-driven-design.md)
and [hexagonal dependency direction](../architecture/21-hexagonal-architecture.md)
in a Spring application. Start with a real domain rule; the package layout and
interfaces should support that rule.

## Place Each Responsibility

| Part | Owns | Avoid |
| --- | --- | --- |
| Controller or listener | Parse input, call a use case, map the result | Business decisions and direct entity mutation |
| Application service | Orchestrate the use case and local transaction | Becoming the only home for domain rules |
| Domain model | State transitions and invariants | HTTP, JPA, and provider types |
| Infrastructure adapter | Persistence, messaging, and provider integration | Choosing business policy |

Package by business context when that makes ownership clearer. For example,
`order/domain`, `order/application`, and `order/infrastructure` keep order
rules together while allowing separate packages for payment or inventory.
One context does not imply one deployed service.

## One Use Case

Suppose an order can be cancelled while pending, but not after shipment. Put
that rule in an operation on the order model; keep the controller thin:

```kotlin
class Order(private var status: OrderStatus) {
    fun cancel() {
        require(status == OrderStatus.PENDING) { "Order cannot be cancelled" }
        status = OrderStatus.CANCELLED
    }
}

@Service
class CancelOrder(private val orders: OrderRepository) {
    @Transactional
    fun execute(id: UUID) {
        val order = orders.get(id)
        order.cancel()
        orders.save(order)
    }
}
```

`OrderRepository` here is an application-facing contract. A Spring Data/JPA
adapter can implement it. The example omits API error mapping, authorization,
and persistence mapping so the ownership of the cancellation rule stays clear.
Those concerns still need explicit handling in a real endpoint.

## Keep Models Separate When Their Jobs Differ

A JPA entity represents stored state; an API DTO represents a public contract;
a domain model protects business behavior. They can have different fields and
lifecycles. Map between them when exposing the persistence model would leak
lazy loading, storage changes, or internal state. A simple CRUD feature does
not need three copies of every field just to satisfy a pattern.

Use a repository for aggregate lifecycle work. Reporting or cross-context
reads may fit a projection or a dedicated query model better; see
[cross-domain reads](../architecture/19-database-per-service-and-read-model-migration.md).

If cancellation must notify another service, saving the order and publishing
to a broker are two writes. Record an outbox entry in the local transaction
and publish it reliably afterward; the event handler must tolerate repeats.
See [distributed workflows and outbox](../architecture/03-distributed-transactions-and-events.md).

## Test at the Right Boundary

- Test `Order.cancel()` with allowed and rejected states without starting Spring.
- Test the application service's transaction and persistence behavior where
  the integration matters.
- Test HTTP mapping and the persistence adapter separately for their contracts.

Use only as much structure as the rules require. For thin CRUD, a clear use
case and explicit API contract may be sufficient.
