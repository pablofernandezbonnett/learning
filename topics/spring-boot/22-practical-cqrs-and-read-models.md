# Practical CQRS and Read Models in Spring Boot

`CQRS` (`Command Query Responsibility Segregation`) becomes confusing fast when
people explain it as:

- one buzzword
- one diagram
- two databases everywhere

This note keeps it practical.

---

## Why This Matters

Spring teams usually hit `CQRS` through one of these pains:

- write-side JPA model is clean, but dashboard queries are ugly and slow
- reporting reads keep dragging critical write paths down
- one endpoint really wants a flattened read shape, not a deep entity graph
- people start forcing every query through the aggregate model

This matters because `CQRS` is not mainly about being advanced.
It is about accepting that one model no longer serves both writes and reads well.

---

## Smallest Useful Mental Model

Use this definition:

- **command side**: validate and commit business changes
- **query side**: read data in the shape that screens, dashboards, or search actually need

Short rule:

> CQRS is useful when the write model protects invariants well, but the read side wants a different shape badly enough to justify separation.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- every Spring service should use CQRS
- CQRS means event sourcing
- CQRS means one write database and one read database from day one

Better mental model:

- most systems should start simpler
- projections, DTO queries, or a dedicated read model are often enough
- heavier CQRS becomes worth it when read shape and write model truly diverge

Small concrete example:

- weak approach: admin dashboard loads `Order` aggregates, payment entities, and
  shipment graphs just to render one table
- better approach: command side keeps strong order rules, while query side reads
  a flattened `admin_order_view`

---

## 1. What Problem CQRS Actually Solves

The write side and read side often want different things.

### Write side wants

- local invariants
- transaction clarity
- aggregate boundaries
- safer state transitions

### Read side wants

- flattened shapes
- joins or denormalized documents
- filters and sorting
- high read throughput

Trying to force both through one JPA entity model usually creates pain.

---

## 2. The Smallest Practical Versions

Not every useful read separation is "full CQRS".

### Version A: Projection query in the same database

Good fit:

- one endpoint needs fewer columns
- a full aggregate load is wasteful
- no separate read store is justified yet

Examples:

- Spring Data projection
- JPQL DTO query
- `JdbcTemplate` for a report-like endpoint

This is often the first good step.

### Version B: Dedicated read table or materialized view

Good fit:

- one hot read path needs a stable flattened shape
- the write side still stays in the same service

This is a stronger separation without needing a whole distributed architecture.

### Version C: Separate read model fed by events or outbox

Good fit:

- several services contribute to the read
- live composition became too slow or brittle
- one denormalized read shape is used a lot

This is the form most people mean when they say CQRS in a distributed system.

---

## 3. Strong Default

Use this progression:

1. keep writes simple and correct
2. use projection or DTO queries for read-heavy endpoints
3. add a dedicated read model when reads and writes are clearly diverging
4. use event-fed read models when the view crosses service boundaries or read load becomes serious

That is usually stronger than:

1. hear `CQRS`
2. add Kafka and another database immediately

---

## 4. Concrete Example

Imagine:

- write side owns order placement, payment authorization state, and cancellation rules
- admin dashboard needs this table:
  `orderId`, `customerEmail`, `orderStatus`, `paymentStatus`, `shipmentStatus`, `lastUpdated`

Weak approach:

- load rich JPA entities and traverse relationships for every row

Better approach:

- command side keeps aggregates and local rules
- query side reads a flattened dashboard model

That read model can start as:

- SQL projection

And later evolve into:

- event-fed projection table

If the read becomes hot or cross-service.

---

## 5. How It Fits In A Spring Codebase

### Command side

Owns:

- controllers for write actions
- application services
- aggregates
- repositories for aggregate lifecycle

Example:

```kotlin
@Service
class PlaceOrderService(
    private val orders: OrderRepository,
    private val outbox: OutboxWriter,
) {
    @Transactional
    fun place(cmd: PlaceOrderCommand): UUID {
        val order = Order.place(cmd.customerId, cmd.items)
        orders.save(order)
        outbox.write("OrderPlaced", order.id.toString())
        return order.id
    }
}
```

### Query side

Owns:

- read-specific queries
- projection DTOs
- optional read-store update handlers

Example:

```kotlin
data class AdminOrderRow(
    val orderId: UUID,
    val customerEmail: String,
    val orderStatus: String,
    val paymentStatus: String,
)
```

The point is not the DTO itself.
The point is:

- query side shape is chosen for the screen
- not inherited accidentally from the write aggregate

---

## 6. Same Service CQRS vs Cross-Service CQRS

### Same-service CQRS

Use when:

- one service owns the write truth
- one or more read paths need a different shape

Examples:

- order history projection
- support dashboard inside the same service boundary

This is often the highest-return first step.

### Cross-service CQRS

Use when:

- the read model needs data from several service-owned sources
- live API composition is too slow, too fragile, or too expensive

Examples:

- merchant dashboard
- search index
- fulfillment overview

This is more expensive because:

- now you need event or outbox-fed synchronization

---

## 7. Where Outbox Fits

Outbox and CQRS solve different problems.

### Outbox solves

- save state and publish change reliably

### CQRS solves

- read side and write side need different shapes

They often appear together, but they are not the same thing.

Short rule:

> Outbox protects publication. CQRS protects read and write specialization.

---

## 8. When Not To Use CQRS

Do not use CQRS when:

- one relational model still serves reads and writes well
- the team mainly needs better SQL or projections
- the read traffic is modest
- the domain is still moving fast and read shapes are unstable

Good practical line:

> If projections or normal SQL solve the problem, I stay simpler and do not promote it to full CQRS yet.

---

## 9. Big Traps

1. **Calling every DTO query CQRS**
   Example: normal read optimization gets overhyped into architecture theater.

2. **Forcing read paths through aggregates**
   Example: dashboard reads pay the cost of domain-rich entity loading.

3. **Treating the read model as source of truth**
   Example: support tool writes directly into a projection table.

4. **Adding distributed CQRS before normal projection or composition was tried**
   Example: Kafka arrives before the real read pain is even measured.

5. **Combining CQRS and event sourcing by default**
   Example: team imports two complex patterns when one lighter read split would do.

---

## 10. Practical Summary

Practical summary:

> In Spring Boot, I start by keeping the write side clean with aggregates and application services. If read-heavy endpoints want a very different shape, I move them to projections, DTO queries, or a dedicated read model. I only call it fuller CQRS when that read/write split becomes substantial enough to justify separate read storage or event-fed projections.
