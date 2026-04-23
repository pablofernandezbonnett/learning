# Testing Strategies

> Primary fit: `Shared core`


You do not need a testing manifesto.
You do need a practical model that shows:

- what risk each test level covers
- where real confidence comes from
- how to test failure cases, not only the main success case

This note keeps the topic grounded in Spring Boot / Kotlin backend work.

---

## 1. What A Testing Strategy Actually Solves

The real question is not "do you write tests".
It is:

- what failure does this test protect against?
- what boundary does it validate?
- how expensive is it to run?

Two quick terms used in this note:

- `boundary` means a place where your code talks to something external, such as a database, cache, message broker, or another service
- `happy path` means the main success case, where everything works as expected
- `state transition` means an important business state change, for example `PENDING -> PAID`

Short rule:

> good test strategy is based on which failure matters most, not on dogmatic rules

---

## 2. Unit Tests

### What they solve

Unit tests validate business logic in isolation.

### Smallest example

- instantiate one service
- mock repository or client dependency
- assert business decision

```kotlin
whenever(stockRepository.available("sku-1")).thenReturn(0)

assertThrows<InsufficientStockException> {
    orderService.reserve("sku-1", 1)
}
```

### Where they fit

- pure business rules
- validation logic
- `if / else` business rules
- failure-case behaviour

### What they do not prove

- real SQL correctness
- real request and response format compatibility
- real behaviour with databases, caches, or message brokers such as Postgres, Redis, or Kafka

Pros:

- very fast feedback
- easy to debug
- strong coverage of business rules and edge cases

Tradeoffs / Cons:

- mocks can create false confidence
- weak proof of real boundary behaviour

### Practical summary

> I use unit tests for fast feedback on business logic and failure cases, but I do not
> pretend they prove real boundary behaviour.

---

## 3. Integration Tests

### What they solve

Integration tests prove that your code talks correctly to a real dependency.

### Smallest example

- repository writes to real Postgres
- query returns expected result
- migration and constraints behave like the same database engine used in production

Why Testcontainers matters:

- `Testcontainers` runs real dependencies, such as Postgres, inside tests
- `H2` is a lightweight in-memory database often used as a substitute in Java tests
- H2 is not Postgres
- local in-memory substitutes can hide SQL, locking, or migration problems

### Practical shape

```kotlin
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
    @Autowired lateinit var userRepository: UserRepository

    @Test
    fun `findByEmail returns null for unknown address`() {
        assertNull(userRepository.findByEmail("ghost@example.com"))
    }
}
```

### Where they fit

- JPA (Java Persistence API) queries
- migrations
- Redis integration
- Kafka producer/consumer integration
- controller-to-service-to-database boundary

Pros:

- much stronger confidence on real dependency behaviour
- catches migration, SQL, request/response format, and wiring/configuration issues

Tradeoffs / Cons:

- slower than unit tests
- heavier continuous integration (`CI`) and local runtime setup

### Practical summary

> I prefer integration tests with the real engine when engine behaviour matters. That is
> why Testcontainers is much more trustworthy than H2 for Postgres-specific behaviour.

---

## 4. Contract Tests

### What they solve

Contract tests catch changes in request or response shape between services without
having to run the whole distributed system.

Here:

- `contract` means the response or request shape another service depends on
- `schema drift` means one side silently renames, removes, or changes fields and breaks the other side

### Smallest example

- service A expects `firstName`
- service B changes response to `name`
- contract test fails in continuous integration (`CI`) before that change breaks production

### Where they fit

- service-to-service HTTP APIs
- consumer-driven integration boundaries, meaning the consumer defines what response shape it needs
- teams with independent release cadence

### Small pseudocode example

Provider contract says:

```json
{
  "userId": "U-123",
  "firstName": "Ana"
}
```

Consumer test expects:

```kotlin
assertThat(response.jsonPath("$.userId")).exists()
assertThat(response.jsonPath("$.firstName")).exists()
```

If the provider silently renames `firstName` to `name`, the contract test fails before the change reaches production.

### Practical summary

> I use contract tests when a downstream service boundary (a dependency my service calls or consumes) is important enough that request or response shape changes would hurt us, but full end-to-end testing would be too slow or too brittle.

---

## 5. End-To-End And Smoke Tests

### What they solve

They prove a full critical user journey still works across many layers.

Here:

- `end-to-end` means testing a full flow across multiple layers or services
- `smoke test` means a small sanity check that tells you the system is alive after a deploy or startup

### Good use cases

- checkout
- login
- payment confirmation
- one or two critical user journeys

### Small examples

**End-to-end test**

- open the app
- log in
- place an order
- confirm that the order appears in the user history

**Smoke test**

- app starts
- health endpoint returns `200`
- login endpoint responds
- one critical API call still works after deploy

### Why not too many

- slow
- brittle
- expensive to debug

Short rule:

> end-to-end (`E2E`) tests are valuable, but only for a few critical flows

Pros:

- strongest user-journey confidence
- good final smoke check across many layers

Tradeoffs / Cons:

- slowest feedback
- most brittle debugging surface

---

## 6. The Practical Pyramid

You want:

- many unit tests
- some integration tests
- few end-to-end (`E2E`) tests

Because:

- the lower layers are faster and easier to debug
- the upper layers give broader confidence but cost more

The exact ratio is not sacred.
The useful principle is.

---

## 7. Spring Boot Test Slices

Spring test slices help you test one layer without loading the whole app.
Here, a `slice` means a focused part of the Spring application context.

| Annotation | Use for |
|---|---|
| `@WebMvcTest` | controller logic, validation, error responses |
| `@DataJpaTest` | repository and query correctness |
| `@DataRedisTest` | Redis repository/data access behaviour |
| `@SpringBootTest` | full-context smoke or broad integration tests |

Minimal examples:

**`@WebMvcTest`**
```kotlin
@WebMvcTest(ProductController::class)
class ProductControllerTest {
    @Autowired lateinit var mockMvc: MockMvc
    @MockBean lateinit var productService: ProductService
}
```

**`@DataJpaTest`**
```kotlin
@DataJpaTest
class UserRepositoryTest {
    @Autowired lateinit var userRepository: UserRepository
}
```

**`@DataRedisTest`**
```kotlin
@DataRedisTest
class SessionCacheRepositoryTest {
    @Autowired lateinit var sessionCacheRepository: SessionCacheRepository
}
```

**`@SpringBootTest`**
```kotlin
@SpringBootTest
class ApplicationSmokeTest {
    @Test
    fun contextLoads() {
    }
}
```

Good line:

> I use test slices to keep feedback fast. I only reach for full `@SpringBootTest`
> when I really need broad wiring confidence.

---

## 8. Performance Tests

Functional correctness does not tell you how the system behaves under load.

This matters most for:

- checkout
- payment
- flash sale traffic
- read-heavy retail or partner APIs

Common tools:

- `k6`
- `JMeter`
- `Gatling`

You do not need loyalty to one tool.
The important part is knowing what kind of traffic you are generating and what you are measuring.

Minimal `k6` example:

```javascript
import http from 'k6/http';
export const options = { vus: 100, duration: '30s' };

export default function () {
  http.post('http://localhost:8080/api/checkout',
    JSON.stringify({ cartId: 'C-001' }),
    { headers: { 'Content-Type': 'application/json' } }
  );
}
```

Here, `vus: 100` means `100` virtual users generating traffic during the test.

What to watch:

- p95 and p99 latency
- error rate
- throughput ceiling, meaning the highest sustained request rate the system can handle before latency or errors become unacceptable

The easiest mental model is:

- take all requests in the test
- sort them from fastest to slowest
- `p95` is the point where 95% of requests are at or below that latency
- `p99` is the point where 99% of requests are at or below that latency

In plain English:

- `p95` tells you what the experience looks like for almost everyone
- `p99` tells you what the slow unhappy tail looks like

If you had `100` requests:

- `p95 = 180ms` means request number `95` finished in about `180ms` or less
- the slowest `5` requests were worse than that
- `p99 = 900ms` means request number `99` finished in about `900ms` or less
- only the slowest `1` request was worse than that

Example:

- average = `120ms`
- `p95 = 180ms`
- `p99 = 900ms`

That means the system looks fine if you only read the average, but a small number of
users still hit a much slower experience.

That is why tail latency matters more than averages in backend work.

Good sentence:

> I look at p95 and p99, not only average latency, because averages can hide the slow tail.
> In production, users remember the slow outliers too.

---

## 9. Real Backend Use Cases

### Commerce backend

- integration tests for Postgres queries and cache boundaries
- performance tests for checkout or catalog traffic spikes
- a few end-to-end (`E2E`) tests for core retail journeys

### Payments backend

- strong unit tests for important business state changes and duplicate-protection rules, such as making the same request safe to repeat
- integration tests for database constraints and boundaries that stay safe under duplicate requests or retries
- focused tests on failure cases, not only success

### Platform backend

- API contract tests and integration tests for payment-provider boundaries
- failure-case coverage for retries and incoming webhook calls from providers

---

## 10. The Big Traps

1. **Testing only the main success case**
   Example: payment tests never cover timeout or duplicate retry behaviour.

2. **Using only mocks**
   Example: repository tests pass, but the real Postgres query is wrong.

3. **Too many brittle end-to-end (`E2E`) tests**
   Example: every flow is tested at the slowest layer.

4. **Confusing coverage with confidence**
   Example: high line coverage but weak boundary testing.

5. **No testing of the error contract**
   Example: API error shape drifts and breaks clients.

---

## 11. Practical Summary

Good short answer:

> I think about testing by risk. Unit tests cover business logic quickly, integration
> tests prove real boundary behaviour with engines like Postgres or Redis, and I keep
> only a few end-to-end tests for critical user journeys. I care a lot about failure
> cases, not only main-success-case assertions.
