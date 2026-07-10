# Hexagonal Architecture (Ports and Adapters)

Hexagonal architecture becomes vague fast when people explain it only as:

- one diagram shape
- "independence from frameworks"
- another synonym for clean code

This note keeps it practical.

---

## Why This Matters

Many Spring services become hard to change because the framework and the infrastructure
start deciding the design:

- controllers call repositories directly
- JPA entities become the whole model
- provider SDKs leak into use-case code
- tests need half the application context just to verify one business rule

Hexagonal architecture matters because it gives the core use case and domain model a
clear boundary from HTTP, databases, queues, and external providers.

---

## Smallest Useful Mental Model

Use this definition:

- the **inside** owns business rules and use cases
- **ports** define what the inside needs or exposes
- **adapters** connect those ports to HTTP, JPA, Kafka, Stripe, SAP, or any other detail

Short rule:

> DDD models the business. Hexagonal architecture keeps the framework and infrastructure around that model instead of inside it.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- hexagonal means six layers or a special folder shape
- every interface in the codebase is a port
- putting interfaces everywhere automatically improves architecture

Better mental model:

- a port exists to protect a meaningful boundary
- adapters translate between the outside world and the application's use-case model
- dependency direction matters more than the exact package names

Small concrete example:

- weak approach: `OrderController` loads `OrderEntity`, calls `StripeClient`, and saves through `JpaRepository`
- better approach: controller calls a use case, the use case depends on ports, and adapters implement those ports for JPA and Stripe

---

## 1. What A Port Actually Is

A **port** is an interface at the application boundary.

Two common kinds:

- **inbound port**: what the application lets the outside world ask it to do
- **outbound port**: what the application needs from the outside world

In Java terms:

- `PlaceOrderUseCase` can be an inbound port
- `OrderRepository` and `PaymentGateway` can be outbound ports

The important point is not "interface because clean code."
The important point is that the use case depends on stable business-facing abstractions
instead of HTTP or database details.

---

## 2. Inbound And Outbound Adapters

An **adapter** connects one outside technology or protocol to a port.

Typical inbound adapters:

- REST controller
- Kafka consumer
- webhook controller
- CLI handler

Typical outbound adapters:

- JPA repository adapter
- Stripe or Adyen gateway adapter
- email adapter
- Kafka publisher adapter

Plain-English version:

- inbound adapters translate requests into use-case calls
- outbound adapters translate use-case needs into infrastructure calls

---

## 3. Small Spring Boot Example

Imagine one checkout flow where the application must:

- place an order
- authorize payment
- save local state

### Inbound port

```java
public interface PlaceOrderUseCase {
    UUID place(PlaceOrderCommand command);
}
```

### Outbound ports

```java
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(UUID orderId);
}

public interface PaymentGateway {
    PaymentAuthorization authorize(Order order);
}
```

### Application service

```java
@Service
public class PlaceOrderService implements PlaceOrderUseCase {
    private final OrderRepository orders;
    private final PaymentGateway payments;

    public PlaceOrderService(OrderRepository orders, PaymentGateway payments) {
        this.orders = orders;
        this.payments = payments;
    }

    @Override
    public UUID place(PlaceOrderCommand command) {
        Order order = Order.place(command.customerId(), command.items());
        PaymentAuthorization authorization = payments.authorize(order);
        order.markAuthorized(authorization.providerReference());
        orders.save(order);
        return order.id();
    }
}
```

### Inbound adapter

```java
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final PlaceOrderUseCase placeOrder;

    public OrderController(PlaceOrderUseCase placeOrder) {
        this.placeOrder = placeOrder;
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> place(@RequestBody PlaceOrderRequest request) {
        UUID orderId = placeOrder.place(request.toCommand());
        return ResponseEntity.ok(Map.of("orderId", orderId));
    }
}
```

### Outbound adapters

```java
@Component
public class JpaOrderRepositoryAdapter implements OrderRepository {
    private final SpringDataOrderRepository jpaRepository;

    public JpaOrderRepositoryAdapter(SpringDataOrderRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Order order) {
        jpaRepository.save(OrderEntity.fromDomain(order));
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return jpaRepository.findById(orderId).map(OrderEntity::toDomain);
    }
}

@Component
public class StripePaymentGatewayAdapter implements PaymentGateway {
    private final StripeClient stripeClient;

    public StripePaymentGatewayAdapter(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    @Override
    public PaymentAuthorization authorize(Order order) {
        StripeResponse response = stripeClient.authorize(order.id(), order.totalAmount());
        return new PaymentAuthorization(response.reference());
    }
}
```

What this buys you:

- the use case is not tied to Spring MVC
- the use case is not tied to JPA
- the payment provider can change behind the `PaymentGateway` port

---

## 4. How It Fits With DDD

DDD and hexagonal architecture are related, but they are not the same thing.

`DDD` helps answer:

- what is the domain language
- where is the bounded context
- what invariants belong inside one aggregate

Hexagonal architecture helps answer:

- how does the use case stay decoupled from transport and persistence details
- where do provider SDKs and JPA mappings live
- how do we swap adapters without rewriting the core flow

Good combined shape:

- domain model and use cases inside
- ports at the application boundary
- adapters at the edge

Short reusable explanation:

> DDD gives you the business model. Hexagonal architecture gives you the boundary discipline that stops frameworks and providers from owning that model.

---

## 5. Another Useful Example: Webhook As Inbound Adapter

Hexagonal architecture is not only about REST + database.

A payment webhook is a good example because the entry point is asynchronous.

Good shape:

- inbound adapter: `StripeWebhookController`
- inbound port: `HandlePaymentWebhookUseCase`
- outbound ports: `OrderRepository`, `OutboxWriter`
- outbound adapters: JPA implementation and outbox publisher implementation

Why this is useful:

- webhook signature parsing stays at the edge
- the use case works in application language such as `payment authorized`
- the same core logic can be reused even if the provider changes later

---

## 6. Testing By Boundary

One of the best signs that a hexagonal boundary is real is that the tests get simpler.

Strong default:

- test the use case with fake `OrderRepository` and fake `PaymentGateway`
- test the controller as an HTTP adapter with `@WebMvcTest`
- test the JPA adapter separately with `@DataJpaTest`

Small example:

```java
@Test
void place_order_marks_authorized_and_saves() {
    FakeOrderRepository orders = new FakeOrderRepository();
    FakePaymentGateway payments = new FakePaymentGateway("psp-123");
    PlaceOrderService service = new PlaceOrderService(orders, payments);

    UUID orderId = service.place(new PlaceOrderCommand(customerId, items));

    assertThat(orders.findById(orderId)).isPresent();
}
```

The point is not test style purity.
The point is that the use case can be verified without booting all of Spring.

---

## 7. How To Migrate From A Layered Spring Service

Do not treat hexagonal architecture as a rewrite-only idea.

Good migration path:

1. stop controllers from mutating entities or calling repositories directly
2. introduce a use-case interface such as `PlaceOrderUseCase`
3. move repository and provider dependencies behind outbound ports
4. keep JPA, HTTP, and provider-specific mapping in adapters
5. split domain model from persistence model only where the boundary is under pressure

This is usually better than:

1. rename packages to `domain`, `application`, `infrastructure`
2. keep all the old coupling

---

## 8. When Not To Push Hexagonal Hard

Do not add hexagonal ceremony mechanically when:

- the feature is thin CRUD with weak business rules
- one simple repository-backed admin flow has no meaningful external boundary
- the team will create ports for trivial wrappers that add no real isolation

Good default:

- use hexagonal boundaries where the application core is worth protecting
- stay lighter where the code is mostly transport and persistence plumbing

---

## 9. Big Traps

1. **Calling every interface a port**
   Example: naming noise grows, but the dependency direction does not improve.

2. **Letting adapters leak their models inward**
   Example: JPA entities or Stripe SDK types appear in the use case.

3. **Treating package names as architecture**
   Example: folders say `domain` and `infrastructure`, but controllers still call repositories directly.

4. **Over-abstracting trivial CRUD**
   Example: six classes appear where one straightforward service would have been enough.

5. **Assuming hexagonal replaces domain modeling**
   Example: ports and adapters exist, but the business invariants are still vague.

---

## 10. Practical Summary

Practical summary:

> In a Spring Boot service, I use hexagonal architecture by keeping use cases and domain rules independent from HTTP, JPA, and provider SDKs. Controllers, JPA mappings, and external clients become adapters around ports. I use that structure where the core business flow is worth protecting, not as ceremony for every CRUD endpoint.
