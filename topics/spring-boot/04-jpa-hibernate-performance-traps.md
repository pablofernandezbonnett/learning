# 18. JPA (Java Persistence API) and Hibernate Performance Traps

> Primary fit: `Shared core`


JPA can make data access productive, but it is easy to write code that looks
clean and performs badly.

This is one of the most useful refresh topics for senior Java backend work.

---

## 1. The Core Rule

JPA is an abstraction over SQL, not a replacement for understanding SQL.

If you do not know:

- what query is being generated
- how many queries run
- what is loaded eagerly or lazily
- what indexes exist

then you are coding blind.

---

## 2. The N+1 Problem

Classic example:

- query 100 orders
- lazily read `order.customer` or `order.items`
- Hibernate runs one query for the list and many more for associations

This is one of the most common production performance bugs.

Typical fixes:

- fetch join where appropriate
- entity graph
- projections when you do not need full entities
- batch fetching when full join loading is not the right choice

Bad shape:

```kotlin
@GetMapping("/orders")
fun listOrders(): List<OrderSummaryResponse> {
    val orders = orderRepository.findTop100ByStatusOrderByCreatedAtDesc(OrderStatus.PAID)

    return orders.map { order ->
        OrderSummaryResponse(
            orderId = order.id!!,
            customerName = order.customer.name,
            itemCount = order.items.size,
        )
    }
}
```

Why this is bad:

- the first query loads the orders
- then `order.customer` and `order.items` can trigger extra queries per order
- the controller code looks small, but the SQL cost grows with the result size

Better shape:

```kotlin
interface OrderSummaryView {
    fun getOrderId(): Long
    fun getCustomerName(): String
    fun getItemCount(): Long
}

@Query(
    """
    select
        o.id as orderId,
        c.name as customerName,
        count(i.id) as itemCount
    from Order o
    join o.customer c
    left join o.items i
    where o.status = :status
    group by o.id, c.name, o.createdAt
    order by o.createdAt desc
    """
)
fun findOrderSummaries(status: OrderStatus): List<OrderSummaryView>
```

Why this is better:

- one query is shaped for the endpoint
- you fetch only the fields the API actually needs
- the cost is visible in the query instead of being hidden in entity navigation

Practical rule:

> If the endpoint is read-heavy, assume you should inspect the generated SQL.

---

## 3. Lazy Loading Pitfalls

Lazy loading is useful, but dangerous if you stop thinking about boundaries.

Common problems:

- hidden extra queries during JSON serialization
- `LazyInitializationException` outside the transaction
- large object graphs loaded by accident

Good practice:

- do not expose JPA entities directly from controllers
- map to DTOs (data transfer objects)
- decide explicitly what the API needs

Bad shape:

```kotlin
@RestController
class OrderController(
    private val orderRepository: OrderRepository,
) {
    @GetMapping("/orders/{id}")
    fun getOrder(@PathVariable id: Long): Order =
        orderRepository.findById(id).orElseThrow()
}
```

Why this is bad:

- JSON serialization can touch lazy associations after the repository call finishes
- that can trigger hidden queries or fail with `LazyInitializationException`
- your API shape is now coupled to your persistence model

Better shape:

```kotlin
data class OrderDetailsResponse(
    val id: Long,
    val customerName: String,
    val total: BigDecimal,
)

interface OrderRepository : JpaRepository<Order, Long> {
    @Query(
        """
        select o
        from Order o
        join fetch o.customer
        where o.id = :id
        """
    )
    fun findByIdWithCustomer(id: Long): Order?
}

@Service
class OrderQueryService(
    private val orderRepository: OrderRepository,
) {
    @Transactional(readOnly = true)
    fun getOrderDetails(id: Long): OrderDetailsResponse {
        val order = orderRepository.findByIdWithCustomer(id)
            ?: throw NoSuchElementException()

        return OrderDetailsResponse(
            id = order.id!!,
            customerName = order.customer.name,
            total = order.total,
        )
    }
}
```

Why this is better:

- the query method makes the fetch plan explicit instead of hoping lazy loading behaves well
- `@Transactional(readOnly = true)` keeps a clear read boundary, but it is not what fixes the extra query risk
- the controller returns a stable API contract instead of an entity graph

Precision note:

- if this were `findById(id)` with one later access to `order.customer.name`, that would usually be one hidden extra query, not `N+1`
- it becomes `N+1` when the same lazy access pattern happens across a list of `N` orders
- the main lesson here is still the same: do not hide fetch behavior behind entity navigation

---

## 4. Dirty Checking and Write Amplification

Hibernate tracks managed entities and flushes changes automatically.

This is convenient, but it can hide:

- unexpected updates
- too many flushed entities
- heavy persistence contexts in batch work

For bulk updates or imports:

- prefer explicit batching
- consider native SQL or tailored repository methods
- clear the persistence context when processing large volumes

Bad shape:

```kotlin
@Transactional
fun importUsers(rows: List<UserCsvRow>) {
    rows.forEach { row ->
        userRepository.save(User.from(row))
    }
}
```

Why this is bad:

- every entity becomes managed in one growing persistence context
- Hibernate dirty checking work grows with the number of managed entities
- the large flush happens late, usually at commit time

Better shape:

```kotlin
@Transactional
fun importUsers(rows: List<UserCsvRow>) {
    rows.forEachIndexed { index, row ->
        entityManager.persist(User.from(row))

        if ((index + 1) % 500 == 0) {
            entityManager.flush()
            entityManager.clear()
        }
    }

    entityManager.flush()
    entityManager.clear()
}
```

Why this is better:

- batching keeps memory and dirty checking work bounded
- `flush()` writes in chunks instead of one huge end-of-transaction flush
- `clear()` prevents the persistence context from growing without limit

---

## 5. Fetching the Wrong Shape

A lot of backend endpoints do not need full entities.

Better options:

- projections
- DTO queries
- field-restricted reads
- repository methods shaped for the use case

Good rule:

> Read models and write models do not always need the same object shape.

That matters in:

- product listing
- order history
- admin dashboards
- search/filter endpoints

Bad shape:

```kotlin
@GetMapping("/products")
fun listProducts(): List<Product> =
    productRepository.findAll()
```

Why this is bad:

- a product list endpoint rarely needs the full entity
- you can load fields that are useless for the page, such as large descriptions,
  supplier links, audit data, or lazy collections
- returning entities makes it easier for more fields to leak into the API later

Better shape:

```kotlin
interface ProductCardView {
    fun getId(): Long
    fun getName(): String
    fun getPrice(): BigDecimal
    fun getThumbnailUrl(): String
}

@Query(
    """
    select
        p.id as id,
        p.name as name,
        p.price as price,
        p.thumbnailUrl as thumbnailUrl
    from Product p
    where p.active = true
    order by p.updatedAt desc
    """
)
fun findActiveProductCards(): List<ProductCardView>
```

Why this is better:

- the read model matches the page or endpoint
- payload size and query cost stay easier to reason about
- later entity changes do not automatically change the API response

---

## 6. Entity vs DTO vs `@JsonView`

One common overcorrection is:

- do not return entities directly
- then create many near-identical DTOs
- then push API DTO concerns down into repositories to avoid that duplication

The real goal is simpler:

- keep persistence concerns out of the controller contract
- keep API concerns out of the core repository abstraction
- avoid needless DTO duplication when the difference is only a small view change

Use this rule:

- JPA entity or model class: persistence model and write-side business behavior
- Kotlin `data class` DTO: request or response contract at the API boundary
- projection or query model: repository read shape when the query does not need the full entity
- `@JsonView`: small serialization variants of the same response DTO, not a substitute for query design

Kotlin note:

- Kotlin `data class` is a strong default for DTOs
- it is usually a poor default for JPA entities because entity lifecycle, equality, and proxy behavior are different concerns

Bad shape:

```kotlin
object OrderViews {
    interface Summary
}

@Entity
class Order(
    @Id
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonView(OrderViews.Summary::class)
    val customer: Customer,
)

@RestController
class OrderController(
    private val orderRepository: OrderRepository,
) {
    @JsonView(OrderViews.Summary::class)
    @GetMapping("/orders/{id}")
    fun getOrder(@PathVariable id: Long): Order =
        orderRepository.findById(id).orElseThrow()
}
```

Why this is bad:

- `@JsonView` changes JSON serialization, but it does not fix lazy loading or query count
- the API is still coupled to the JPA entity shape
- serialization annotations now leak into the persistence model

Better shape:

```kotlin
object OrderViews {
    interface Summary
    interface Details : Summary
}

data class OrderResponse(
    @field:JsonView(OrderViews.Summary::class)
    val id: Long,

    @field:JsonView(OrderViews.Summary::class)
    val status: String,

    @field:JsonView(OrderViews.Details::class)
    val customerName: String,

    @field:JsonView(OrderViews.Details::class)
    val total: BigDecimal,
)

interface OrderRow {
    fun getId(): Long
    fun getStatus(): String
    fun getCustomerName(): String
    fun getTotal(): BigDecimal
}

interface OrderRepository : JpaRepository<Order, Long> {
    @Query(
        """
        select
            o.id as id,
            o.status as status,
            c.name as customerName,
            o.total as total
        from Order o
        join o.customer c
        where o.id = :id
        """
    )
    fun findOrderRow(id: Long): OrderRow?
}

@Service
class OrderQueryService(
    private val orderRepository: OrderRepository,
) {
    @Transactional(readOnly = true)
    fun getOrder(id: Long): OrderResponse {
        val row = orderRepository.findOrderRow(id) ?: throw NoSuchElementException()

        return OrderResponse(
            id = row.getId(),
            status = row.getStatus(),
            customerName = row.getCustomerName(),
            total = row.getTotal(),
        )
    }
}
```

Why this is better:

- the repository still works in query-shaped persistence data, not controller-specific transport rules
- the service maps that read shape into one API DTO
- `@JsonView` is used only for small response variants on the API DTO

When `@JsonView` is a good fit:

- summary vs details of the same response contract
- public vs internal field visibility on the same endpoint family
- small output differences where the query shape is still basically the same

When separate DTOs are the better answer:

- the summary endpoint and detail endpoint need materially different queries
- one endpoint is read-heavy and needs a very small projection
- the field set difference reflects a different business meaning, not just a serialization preference

Practical rule:

> Do not use `@JsonView` to justify returning entities. Use it only when one
> API DTO has small view variations and the underlying query shape is still a
> good fit.

---

## 7. Transactions and JPA Performance

JPA performance problems often become worse because of transaction scope.

Why this happens:

- a transaction keeps a persistence context alive
- managed entities stay tracked for dirty checking until flush or commit
- lazy associations can still fire queries late while the transaction is open
- writes and locks can stay open longer than the business operation really needs

That means performance is not only about "was the SQL fast?"
It is also about how long you keep Hibernate tracking state and how long the database has to carry the transaction.

Common bad shape:

- load entities early
- do more application work than necessary inside the transaction
- call another service or payment provider while the transaction is still open
- touch lazy associations late in the flow
- let one large flush happen only at commit time

Why this is expensive:

- locks can be held longer
- more managed entities means more dirty checking work
- late lazy loads become harder to notice
- the final commit can become a big surprise because many changes flush together

Smallest mental model:

- short transaction: "protect one local unit of work"
- wide transaction: "keep a lot of ORM state alive while other things happen"

Bad shape:

```kotlin
@Transactional
fun checkout(orderId: Long) {
    val order = orderRepository.findById(orderId).orElseThrow()

    pricingService.recalculate(order)
    paymentClient.charge(order) // slow network call inside the DB transaction

    order.markPaid()
}
```

Why this is bad:

- the transaction stays open while waiting on the network
- JPA keeps tracking the entity during work that is not part of the local database write
- if more associations are touched later, extra queries can still happen inside the same long transaction

Better shape:

```kotlin
@Service
class CheckoutOrchestrator(
    private val orderPersistenceService: OrderPersistenceService,
    private val paymentClient: PaymentClient,
) {
    fun checkout(orderId: Long) {
        val paymentIntent = orderPersistenceService.createPaymentIntent(orderId)
        val paymentResult = paymentClient.charge(paymentIntent)
        orderPersistenceService.confirmPayment(orderId, paymentResult)
    }
}

@Service
class OrderPersistenceService(
    private val orderRepository: OrderRepository,
) {
    fun createPaymentIntent(orderId: Long): PaymentIntent {
        val order = orderRepository.findById(orderId).orElseThrow()
        return PaymentIntent(order.id!!, order.total)
    }

    @Transactional
    fun confirmPayment(orderId: Long, paymentResult: PaymentResult) {
        val order = orderRepository.findById(orderId).orElseThrow()
        order.markPaid(paymentResult.reference)
    }
}
```

Why this is better:

- each transaction protects one short local write boundary
- the remote call is outside the database transaction
- Hibernate tracks less state for less time
- `createPaymentIntent(...)` does not need a write transaction because building a plain object in memory is not a database write

Precision note:

- `PaymentIntent(order.id!!, order.total)` is just object construction in application memory
- the meaningful database write happens later, when `confirmPayment(...)` changes managed state and commits it
- a read-only transaction on the read step can still be reasonable in some designs, but that would be for read consistency or fetch behavior, not because the constructor is a write

Practical rules:

- keep transactions around the smallest local write that must be atomic
- do not put slow network calls inside JPA transactions
- use `readOnly = true` for read flows when appropriate, but remember that it does not fix bad fetch shape
- for large batch writes, flush and clear explicitly instead of relying on one huge commit-time flush

What to listen for in production:

- endpoints that get slower as result size grows
- transactions that look simple in code but hold locks for too long
- sudden latency spikes at commit time
- "it only runs one repository method" code paths that still issue many SQL statements

Short sentence:

> With JPA I keep the transaction boundary as small as the local consistency boundary. If a transaction is wide, Hibernate keeps tracking more state, lazy loads can fire late, and commit cost becomes harder to predict.

---

## 8. When Not To Be Clever

Do not fight JPA to make it do everything.

In this note, when I say `JPA`, I mean the broader Hibernate/JPA style, including:

- `JpaRepository` or Spring Data JPA repository convenience
- loading entities and navigating associations
- Hibernate dirty checking and flush on commit
- derived queries and JPQL on top of the entity model

JPA is very good at:

- aggregate-oriented writes
- normal CRUD flows
- simple entity relationships
- business logic that benefits from an object model

JPA gets uncomfortable when the main need is not "load an aggregate and change it."
That usually happens in:

- reporting queries
- search/filter pages with many joins
- bulk updates or deletes
- database-specific features such as CTEs, window functions, or vendor-specific tuning
- very hot read paths where you need exact control of selected fields and joins

Typical trap:

- the query shape is becoming complex
- performance is already sensitive
- but the code keeps trying to stay "pure JPA" through more entities, more mappings, more annotations, and more repository magic

That often creates the worst combination:

- the code looks abstract
- the generated SQL is hard to reason about
- and nobody is fully in control of either the object model or the query model

Signs you are fighting JPA instead of using it:

- you need a flattened read model, but keep loading a deep entity graph
- you need to update thousands of rows, but still loop through entities one by one
- you care about the exact SQL plan, but the code hides the query behind entity navigation
- the repository method name or specification chain is now harder to understand than the SQL would be

Better options:

- a projection for a small read model
- a JPQL query that is explicit about joins and selected fields
- native SQL when the database feature or query plan matters directly
- `JdbcTemplate` for reporting-style or bulk operations where ORM adds little value
- a simpler aggregate boundary so one entity tree does not try to model every read path

Small comparison:

### Good fit for entity-centric JPA

```kotlin
@Transactional
fun markOrderPaid(orderId: Long, paymentReference: String) {
    val order = orderRepository.findById(orderId).orElseThrow()
    order.markPaid(paymentReference)
}
```

Why JPA fits here:

- one aggregate is loaded
- one business-state change is applied
- dirty checking and flush-on-commit are helping, not getting in the way

### Better fit for a projection

```kotlin
interface ProductCardRow {
    fun getId(): Long
    fun getName(): String
    fun getPrice(): BigDecimal
}

@Query(
    """
    select
        p.id as id,
        p.name as name,
        p.price as price
    from Product p
    where p.active = true
    order by p.updatedAt desc
    """
)
fun findProductCards(): List<ProductCardRow>
```

Why projection fits here:

- the endpoint only needs a small read shape
- loading the full entity graph would add cost without adding value
- you still keep Spring Data ergonomics, but the query shape is explicit

### Better fit for explicit JPQL

```kotlin
@Query(
    """
    select o
    from Order o
    join fetch o.customer
    where o.status = :status
    order by o.createdAt desc
    """
)
fun findRecentPaidOrders(status: OrderStatus): List<Order>
```

Why JPQL fits here:

- you still want to work with entities
- but you need explicit control over joins and fetch behavior
- it is a good middle ground when derived queries are too implicit, but native SQL would be unnecessary

### Better fit for `JdbcTemplate`

```kotlin
data class MonthlyRevenueRow(
    val month: YearMonth,
    val revenue: BigDecimal,
)

class RevenueReportRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun findMonthlyRevenue(): List<MonthlyRevenueRow> =
        jdbcTemplate.query(
            """
            select
                date_trunc('month', paid_at) as month_start,
                sum(amount) as revenue
            from payments
            where status = 'PAID'
            group by date_trunc('month', paid_at)
            order by month_start desc
            """
        ) { rs, _ ->
            MonthlyRevenueRow(
                month = YearMonth.from(rs.getTimestamp("month_start").toLocalDateTime()),
                revenue = rs.getBigDecimal("revenue"),
            )
        }
}
```

Why `JdbcTemplate` fits here:

- this is a reporting query, not an aggregate lifecycle
- the SQL shape is the main thing you care about
- ORM mapping would add abstraction but not much benefit

Practical decision rule:

- if the operation is primarily business-state change on one aggregate, JPA is often a good fit
- if the operation is primarily query shaping, aggregation, or bulk data movement, step closer to SQL

Bad answer pattern:

> We should keep everything in JPA because mixing approaches is messy.

That sounds tidy, but it often means:

- you are optimizing for framework consistency instead of runtime clarity
- you are accepting hidden SQL and harder performance debugging

Better senior answer:

> I use JPA where it makes the write model easier to maintain. If a read path or bulk operation needs tighter control, I switch to projection, JPQL, or SQL for that path instead of forcing the whole problem through entities.

Senior rule:

> Use JPA where it improves maintainability. Step outside it when the read or
> write path clearly needs more control.

---

## 9. Key Lines

- "The biggest JPA trap is assuming clean repository code means efficient SQL."
- "I treat N+1, lazy loading, and oversized entity graphs as things to verify early, not after production gets slow."
- "For read-heavy endpoints I often prefer projections or DTO-shaped queries over loading full entities."
- "I do not return JPA entities from controllers. If two endpoints only differ a little, I may use `@JsonView` on one response DTO, but I still keep query shape and API shape explicit."

---

## 10. Further Reading

- Hibernate ORM user guide:
  https://docs.hibernate.org/orm/current/userguide/html_single/Hibernate_User_Guide.html
- Spring transaction management:
  https://docs.spring.io/spring-framework/reference/data-access/transaction.html
- Spring web annotations refresher:
  [07-web-annotations.md](./07-web-annotations.md)
