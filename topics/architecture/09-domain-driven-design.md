# Domain-Driven Design (DDD)

> Primary fit: `Shared core`

DDD is a software design approach that models code around the **business domain** — not
around database tables, REST endpoints, or technical layers. Coined by Eric Evans (2003).

The core idea: the hardest part of software is understanding the business problem. If your
code reflects the business language and rules accurately, it becomes easier to change when
the business changes.

Important clarification:

- `DDD` is **not** the same thing as microservices
- `DDD` is a way to model the domain and its boundaries
- you can apply it inside a modular monolith, not only in a distributed system

Short rule:

> DDD is about domain boundaries, not deployment style.

---

## 1. Ubiquitous Language — The Foundation

Before writing a single class, DDD demands a **shared vocabulary** between developers and
domain experts (product managers, business analysts, operations teams).

**The problem without it:**
- Business says "cancel the order". Dev says "update status to VOID".
- Business says "reserve stock". Dev says "decrement inventory count".
- In a payment flow, business says "cancel the payment" or "refund the payment". Dev says "set status to VOID" or "reverse transaction".
- In a payment flow, business says "authorize" vs "capture". Dev treats both as "mark paid", and the real money-state distinction gets lost.
- Mismatch between business intent and code causes bugs and miscommunication.

**With ubiquitous language:**
The same words appear in requirements, code, database, and conversations.
`cancelOrder()`, `reserveStock()`, `OrderStatus.CANCELLED` — the code reads like the domain.

**Practical signal:** If a product manager can read your class names and method names and
roughly understand what they do, you are using ubiquitous language.

---

## 2. Bounded Context — The Most Important Concept

A **Bounded Context** is an explicit boundary within which a specific domain model applies.
The same word can mean different things in different contexts.

**Retail example:**
The word **"Product"** means something different in each context:

```
┌─────────────────────┐   ┌─────────────────────┐   ┌─────────────────────┐
│  Product Catalogue  │   │  Inventory Context  │   │   Order Context     │
│  Context            │   │                     │   │                     │
│  Product = {        │   │  Product = {        │   │  OrderItem = {      │
│    sku,             │   │    sku,             │   │    sku,             │
│    name,            │   │    warehouseQty,    │   │    priceAtPurchase, │
│    description,     │   │    reservedQty,     │   │    quantity         │
│    images,          │   │    location         │   │  }                  │
│    category         │   │  }                  │   │  (snapshot — never  │
│  }                  │   │                     │   │   changes after     │
└─────────────────────┘   └─────────────────────┘   │   order placed)     │
                                                    └─────────────────────┘
```

Each context owns its model. The Inventory team does not use the Catalogue's `Product`
class — it defines its own. Communication between contexts happens through events or APIs,
not shared database tables or shared classes.

**Payments example:**
The word **"Payment"** also means different things in different contexts:

```
┌─────────────────────┐   ┌─────────────────────┐   ┌─────────────────────┐
│  Checkout Context   │   │  Payment Context    │   │ Settlement / Ledger │
│                     │   │                     │   │ Context             │
│  Payment = {        │   │  PaymentAttempt = { │   │  LedgerEntry = {    │
│    paymentMethod,   │   │    paymentIntentId, │   │    entryType,       │
│    amount,          │   │    providerRef,     │   │    amount,          │
│    customerChoice   │   │    status,          │   │    currency,        │
│  }                  │   │    authorizedAt,    │   │    bookingDate      │
│                     │   │    capturedAt       │   │  }                  │
│                     │   │  }                  │   │                     │
└─────────────────────┘   └─────────────────────┘   └─────────────────────┘
```

Here the same broad business word points to different models:

- checkout cares about user intent and payment method choice
- payment execution cares about authorization, capture, retry, and provider references
- settlement or ledger cares about accounting records, booked amounts, and reconciliation

**Why this matters for microservices:**
A Bounded Context is the natural boundary for a microservice. Each service owns its own
data model and database. This is *exactly why* microservices work — and why merging the
databases of two microservices breaks the boundary and creates coupling.

**Practical tip:** "Our microservices follow Bounded Context boundaries from DDD. The Order
Service has its own model of a product (price at purchase time, quantity ordered). It never
shares a database with the Catalogue Service. If the Catalogue changes a product's name,
the Order history is unaffected — it captured the name at order time."

---

## 3. The Building Blocks (Tactical DDD)

### Entity — Identity Matters

An object with a **unique identity** that persists through state changes.

```kotlin
// An Order is an Entity — it has an ID, and its state changes over time
// (PENDING → PAID → SHIPPED → DELIVERED). It is still the same Order.
@Entity
class Order(
    @Id val id: UUID = UUID.randomUUID(),
    val customerId: UUID,
    var status: OrderStatus = OrderStatus.PENDING,
    val items: MutableList<OrderItem> = mutableListOf()
) {
    fun pay() {
        check(status == OrderStatus.PENDING) { "Only pending orders can be paid" }
        status = OrderStatus.PAID
    }

    fun cancel() {
        check(status != OrderStatus.SHIPPED) { "Cannot cancel a shipped order" }
        status = OrderStatus.CANCELLED
    }
}
```

### Value Object — Defined by Its Attributes

An object with **no identity** — two instances with the same attributes are equal.
Value Objects are **immutable**.

```kotlin
// Money is a Value Object. ¥2,990 is ¥2,990 regardless of which instance it is.
// You don't "change" money — you create a new amount.
data class Money(val amount: BigDecimal, val currency: Currency) {
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Cannot add different currencies" }
        return Money(amount + other.amount, currency)
    }
}

// Address is a Value Object. You don't "update" an address — you replace it.
data class Address(val postalCode: String, val prefecture: String, val street: String)
```

**Why Value Objects matter:** They encode domain rules (can't add JPY to USD) and eliminate
primitive obsession (storing money as a `Double` loses precision and meaning).

### Aggregate — The Consistency Boundary

An **Aggregate** is a cluster of Entities and Value Objects that must be consistent together.
One Entity is the **Aggregate Root** — all access to the aggregate goes through it.

```kotlin
// Order is the Aggregate Root.
// OrderItem is part of the aggregate — it has no life outside an Order.
// You never directly modify an OrderItem — you go through Order.
class Order(val id: UUID, ...) {
    private val _items: MutableList<OrderItem> = mutableListOf()
    val items: List<OrderItem> get() = _items.toList() // immutable view

    fun addItem(sku: String, quantity: Int, price: Money) {
        check(status == OrderStatus.PENDING) { "Cannot add items to a non-pending order" }
        _items.add(OrderItem(sku, quantity, price))
    }

    fun totalAmount(): Money = items.fold(Money(BigDecimal.ZERO, JPY)) { acc, item ->
        acc + item.subtotal()
    }
}
```

**Rules for aggregates:**
- Only persist through the root (one `OrderRepository`, not an `OrderItemRepository`)
- Keep aggregates small — large aggregates cause lock contention under concurrent writes
- One aggregate per transaction — if you need to update two aggregates atomically, use Domain Events

**Fintech example:**

- `Money` is still a **Value Object**
- `PaymentAttempt` is an **Entity** because one attempt has identity and changes state over time
- `PaymentIntent` can be the **Aggregate Root** that decides whether a new attempt can be created or captured

```kotlin
data class Money(
    val amount: BigDecimal,
    val currency: Currency,
)

class PaymentAttempt(
    val id: UUID,
    val amount: Money,
    var status: PaymentStatus = PaymentStatus.INITIATED,
    private var providerReference: String? = null,
) {
    fun markAuthorized(ref: String) {
        check(status == PaymentStatus.INITIATED) { "Only initiated attempts can be authorized" }
        status = PaymentStatus.AUTHORIZED
        providerReference = ref
    }

    fun capture() {
        check(status == PaymentStatus.AUTHORIZED) { "Only authorized attempts can be captured" }
        status = PaymentStatus.CAPTURED
    }
}

class PaymentIntent(
    val id: UUID,
    val amount: Money,
) {
    private val attempts: MutableList<PaymentAttempt> = mutableListOf()

    fun startAttempt(): PaymentAttempt {
        check(attempts.none { it.status == PaymentStatus.CAPTURED }) {
            "Cannot start a new attempt after capture"
        }
        return PaymentAttempt(UUID.randomUUID(), amount).also { attempts.add(it) }
    }
}
```

This is the same idea in a payment domain:

- `Money` carries domain rules as a Value Object
- `PaymentAttempt` is an Entity with identity and state transitions
- `PaymentIntent` is the Aggregate Root protecting payment-level invariants

### Domain Event — Something That Happened

A **Domain Event** captures that something meaningful happened in the domain.
It is immutable (it happened — you cannot un-happen it).

```kotlin
// Past tense — it already happened
data class OrderPaid(
    val orderId: UUID,
    val customerId: UUID,
    val amount: Money,
    val occurredAt: Instant = Instant.now()
)

// In the aggregate root — collect events, publish after transaction commits
class Order(...) {
    private val events: MutableList<Any> = mutableListOf()

    fun pay() {
        check(status == OrderStatus.PENDING)
        status = OrderStatus.PAID
        events.add(OrderPaid(id, customerId, totalAmount())) // record the event
    }

    fun pullEvents(): List<Any> = events.toList().also { events.clear() }
}
```

**Fintech example:**

```kotlin
data class PaymentAuthorized(
    val paymentIntentId: UUID,
    val paymentAttemptId: UUID,
    val amount: Money,
    val providerReference: String,
    val occurredAt: Instant = Instant.now()
)
```

The same rule applies:

- the name is in past tense because it already happened
- the event carries business meaning, not just technical logging
- other contexts can react without sharing the same database

Typical reactions:

- ledger or settlement records the financial side
- notification service tells the user the authorization succeeded
- risk or analytics systems observe the payment lifecycle

After the `@Transactional` method commits, the application service publishes these events
to Kafka. Other contexts (Inventory, Warehouse, Notification) react independently.
This is the basis of the **Outbox Pattern** (see [Idempotency and Transaction Safety](../databases/01-idempotency-and-transaction-safety.md)).

### Repository — Abstraction Over Persistence

A **Repository** provides a collection-like interface for accessing aggregates.
It hides whether data comes from Postgres, MongoDB, or cache.

```kotlin
// Domain-level interface — no Spring, no JPA here
interface OrderRepository {
    fun findById(id: UUID): Order?
    fun save(order: Order)
    fun findPendingByCustomer(customerId: UUID): List<Order>
}

// Infrastructure implementation with Spring Data JPA
@Repository
class JpaOrderRepository(private val jpa: OrderJpaRepository) : OrderRepository {
    override fun findById(id: UUID) = jpa.findById(id).orElse(null)?.toDomain()
    override fun save(order: Order) = jpa.save(order.toEntity())
    override fun findPendingByCustomer(customerId: UUID) =
        jpa.findByCustomerIdAndStatus(customerId, "PENDING").map { it.toDomain() }
}
```

### Application Service — Orchestrating Use Cases

The Application Service is the entry point for a use case. It is thin — it delegates all
business logic to the domain. It coordinates: load aggregate, call domain method,
save aggregate, publish events.

```kotlin
@Service
class OrderApplicationService(
    private val orders: OrderRepository,
    private val eventPublisher: DomainEventPublisher
) {
    @Transactional
    fun payOrder(orderId: UUID) {
        val order = orders.findById(orderId) ?: throw OrderNotFoundException(orderId)
        order.pay()                          // domain logic lives here, not here
        orders.save(order)
        order.pullEvents().forEach { eventPublisher.publish(it) }
    }
}
```

---

## 4. Strategic vs Tactical DDD

| Level | Focus | Key concepts |
|---|---|---|
| **Strategic** | How to divide the system | Bounded Contexts, Context Map, Ubiquitous Language |
| **Tactical** | How to implement within a context | Aggregates, Entities, Value Objects, Domain Events, Repositories |

Most developers encounter tactical patterns first. Strategic DDD is where the real
architectural value is — deciding the boundaries between services.

---

## 5. Context Map — How Bounded Contexts Relate

When two Bounded Contexts need to communicate, the relationship type matters:

First, the two words that confuse almost everyone:

- **Upstream** = the side that publishes, owns, or defines something the other side depends on
- **Downstream** = the side that consumes it and has to live with that dependency

Plain-language version:

- upstream is the side saying "this is the model / contract / data I provide"
- downstream is the side saying "I need that, so I either align with it or translate it"

Small concrete example:

- Catalogue publishes product data
- Order Service consumes product data
- so Catalogue is **upstream**
- Order Service is **downstream**

This is not about network direction or who calls whom first.
It is about **dependency and influence**: whose model sets the terms, and who has to adapt.

| Relationship | Meaning | Example |
|---|---|---|
| **Shared Kernel** | Two teams intentionally share a small common model | Two services sharing a `Money` value object library |
| **Customer/Supplier** | The downstream side depends on the upstream side, but can still influence the contract | Order Service (customer) depends on Catalogue Service (supplier) |
| **Conformist** | The downstream side just accepts the upstream model as-is | Your service consuming a third-party payment API |
| **Anti-Corruption Layer (ACL)** | The downstream side protects itself by translating the upstream model into its own language | Wrapping SAP/Hybris in a facade so internal code stays clean |

Good business-friendly explanation:

- `Customer/Supplier` = "we depend on your data, but we can still ask you to shape it better for our needs"
- `Conformist` = "we depend on your data and we do not really get to negotiate, so we adapt"
- `ACL` = "we still depend on your system, but we keep a translation layer so your language does not pollute ours"

The **ACL** is the most common pattern when integrating with legacy systems or external APIs.
See [Enterprise Integration Patterns](./13-enterprise-integration-patterns.md) for implementation details.

This is why `ACL` fits naturally with `DDD`:

- `DDD` defines the Bounded Context and its own language
- the `ACL` protects that context when another system uses a different model

Short rule:

> DDD defines the boundary; the ACL protects it from model leakage.

---

## 6. When to Use DDD — and When Not To

### Use DDD when:
- The domain is **complex** with rich business rules (e-commerce, fintech, logistics)
- Multiple **teams** work on related domains — Bounded Contexts prevent coupling
- The **business changes frequently** — DDD makes change cheaper because the code reflects
  the business
- You need to decide **microservice boundaries** — Bounded Contexts are the answer

### Do NOT use DDD when:
- The application is **CRUD-heavy with no real business logic** (admin panels, simple content
  management, internal tools) — the abstraction adds cost without benefit
- **Small teams / early startups** where the domain is still being discovered — DDD structure
  requires upfront investment
- **Data pipelines / ETL / analytics services** — the domain model doesn't apply
- You are under extreme **time pressure** for an MVP — apply DDD selectively to the core
  domain, not the entire system

**The pragmatic rule:** Apply DDD to your **core domain** (the part that makes your product
unique and valuable). For generic subdomains (email sending, PDF generation, auth), use
simpler approaches or third-party services.

---

## 7. DDD + Microservices — The Connection

This is why DDD appears so often in backend and platform role descriptions.

The hardest question in microservices is: "Where do I draw the service boundary?" DDD
answers it: **one Bounded Context = one microservice** (or a small cluster of microservices
within the same context).

```
Commerce example:

Bounded Context    →    Microservice          →    Database
─────────────────────────────────────────────────────────────
Product Catalogue  →    catalogue-service     →    MongoDB (flexible schema)
Inventory          →    inventory-service     →    Postgres (ACID stock counts)
Order Management   →    order-service         →    Postgres (ACID transactions)
Customer           →    customer-service      →    Postgres
Notifications      →    notification-service  →    No persistent state
```

Each service owns its data. Communication across boundaries happens via events (Kafka)
or APIs — never shared databases. This is DDD's Bounded Context principle applied to
infrastructure.

```
Payments / fintech-style example:

Bounded Context      →    Microservice              →    Database
────────────────────────────────────────────────────────────────────
Payment Intent       →    payment-service           →    Postgres
Ledger / Balance     →    ledger-service            →    Postgres
Fraud / Risk         →    risk-service              →    own data store
Notification         →    notification-service      →    No critical write state
Reconciliation       →    reconciliation-service    →    Postgres
```

Here the same word should not mean the same thing everywhere:

- `payment` in `Payment Intent` means a user-facing payment attempt and status
- `payment` in `Ledger` means a durable money movement record
- `payment` in `Reconciliation` means matching internal state against a `PSP` (`payment service provider`) or settlement evidence

---

## 8. The Interview Answer

**"Are you familiar with DDD? How have you applied it?"**

*"Yes, I’m familiar with DDD, mainly as a way to think about domain boundaries and
language. I would not claim I have applied textbook DDD everywhere, but I do use the
core ideas in complex domains: for example, separating Inventory, Orders, and Catalogue
because they speak different business languages. Inside a domain, I would use patterns
like Aggregates, Value Objects such as Money, and Domain Events when they help protect
real business rules. I would not force DDD onto simple CRUD or thin integration services
where the overhead is not justified."*
