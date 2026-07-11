# Java Backend Engineering Cheatsheet

Use this note when the gap is not "what feature exists in Java" but "what does
good Java backend judgment look like today?"

This is the practical layer between:

- language features
- clean code
- runtime behavior
- scalable backend design

It is intentionally not a `JIT` deep dive.
It is the cheat sheet for the decisions that come up in normal engineering work.

Shortest rule:

> write code that is easy to read, hard to misuse, explicit about side effects,
> cheap enough for the real workload, and calm under concurrency and retries

---

## 1. What Good Java Backend Code Usually Looks Like

Good backend Java code is usually:

- explicit
- boring in the good sense
- easy to review
- easy to test
- clear about state changes
- hard to break by accident

That usually means:

- names carry intent
- input validation is near the boundary
- business rules are visible
- persistence and network calls are obvious
- mutability is narrow
- failure paths are deliberate

Bad mental model:

> Good code is code that uses advanced Java features.

Better mental model:

> Good code is code where the next engineer can understand the business flow,
> change it safely, and see where correctness or latency can break.

---

## 2. Strong Defaults Worth Remembering

- prefer small explicit methods over giant "do everything" service methods
- prefer immutable DTO-style shapes for requests, responses, and projections
- prefer records for data carriers
- prefer interfaces in signatures and concrete implementations inside methods
- prefer constructor injection and explicit dependencies
- prefer one obvious side effect at a time in the main flow
- prefer `BigDecimal` for money
- prefer `int` over `Integer` unless nullability or object semantics are required
- prefer `ArrayList`, `HashMap`, `HashSet`, and `ArrayDeque` as normal defaults unless the access pattern says otherwise
- prefer `List.copyOf`, `Set.copyOf`, and `Map.copyOf` at boundaries where mutation should stop
- prefer a modular monolith over premature service splitting

If you remember one line:

> Choose the simplest shape that protects correctness, readability, and the real
> access pattern.

---

## 3. Method Shape and Readability

A strong service method usually reads top to bottom like a small workflow:

1. validate
2. load required state
3. decide business action
4. persist state change
5. trigger follow-up side effects

Weak shape:

```java
public void process(Order order) {
    // validation, DB writes, HTTP calls, mutation, and notifications mixed together
}
```

Stronger shape:

```java
public PaymentResponse authorizeCheckout(CheckoutRequest request) {
    validate(request);

    Cart cart = cartService.load(request.cartId());
    PaymentAuthorization authorization = paymentGateway.authorize(cart);

    Order order = orderFactory.createPendingOrder(cart, authorization);
    orderRepository.save(order);

    return PaymentResponse.from(order);
}
```

Why this is better:

- intent is obvious
- side effects are visible
- the order of operations is defendable
- changes are easier to review

Short rule:

> A good service method should read like a workflow, not like a scavenger hunt.

---

## 4. Boundaries and Side Effects

The most important engineering distinction is usually not "OO vs functional."
It is:

- pure local decision
- state mutation
- persistence
- remote side effect

Make those boundaries visible.

Typical boundary types:

- database write
- HTTP call
- queue publish
- cache write
- filesystem write

Why this matters:

- tests become clearer
- retries become safer
- transactional boundaries become easier to reason about
- reviewers can see the blast radius of a change

Good rule:

> If a method changes business state and also talks to the network, make that
> fact obvious in the code shape.

Related reading:

- [../testing/02-clean-code-and-code-review.md](../testing/02-clean-code-and-code-review.md)
- [../spring-boot/03-transactions-and-isolation.md](../spring-boot/03-transactions-and-isolation.md)

---

## 5. Data Modeling and Return Types

Use the smallest type that preserves meaning.

Strong defaults:

- use `record` for immutable request/response/projection shapes
- use domain methods for meaningful state transitions
- return `List`, `Set`, and `Map`, not concrete mutable implementations
- use immutable copies at boundaries where callers should not mutate state

Examples:

```java
public record ProductView(String id, String name, BigDecimal price) {}
```

```java
public List<OrderLineView> lines() {
    return List.copyOf(lines);
}
```

Good rule:

> Make invalid states harder to represent and accidental mutation harder to
> spread.

For collection and primitive-wrapper judgment, use:

- [07-java-collections-and-modeling-cheatsheet.md](./07-java-collections-and-modeling-cheatsheet.md)

---

## 6. Exceptions, Validation, and Error Handling

Do not collapse all failures into one generic exception flow.

Separate at least these categories:

- invalid input
- business rule rejection
- temporary infrastructure failure
- permanent infrastructure failure
- duplicate or replay-safe request

Why this matters:

- retries depend on failure type
- HTTP response shape depends on failure type
- operations teams need to know which failures are noise and which are real incidents

Weak approach:

```java
throw new RuntimeException("failed");
```

Stronger approach:

- validation error -> explicit client-facing rejection
- payment timeout -> retryable or temporary failure path
- duplicate webhook -> safe no-op acknowledgment

Short rule:

> Different failure modes deserve different handling paths because they create
> different operational and business consequences.

---

## 7. `Optional`, `null`, and API Design

Strong defaults:

- do not use `null` when the model can be explicit
- use `Optional<T>` mainly for return values where absence is normal
- do not use `Optional` for fields, DTO payload noise, or every parameter by reflex

Use `Optional` well:

```java
Optional<Product> findById(String productId);
```

Use `Optional` poorly:

```java
record Request(Optional<String> couponCode, Optional<String> note) {}
```

Better rule:

> Use `Optional` to model optional return information, not to make every type
> look modern.

---

## 8. Collections, Types, and Everyday Data-Structure Judgment

The main rule is not memorizing complexity tables.
It is choosing by access pattern.

Keep these defaults warm:

- `HashMap` for lookup by key
- `HashSet` for uniqueness and membership
- `ArrayList` for ordered iteration and indexed access
- `ArrayDeque` for queue or stack behavior
- `PriorityQueue` when you repeatedly need the highest or lowest next item
- `BigDecimal` for exact decimal business values such as money
- `int` when nullability is not part of the model
- `Integer` when object semantics or nullability are truly required

Use the dedicated companion for the collection-level details:

- [07-java-collections-and-modeling-cheatsheet.md](./07-java-collections-and-modeling-cheatsheet.md)

---

## 9. Concurrency Without Pretending To Be A JVM Researcher

You do not need to become a Java Memory Model specialist first.
You do need a few safe rules.

Keep these warm:

- shared mutable state is the main source of trouble
- immutability and request-local state are the safest defaults
- `ConcurrentHashMap` is for shared concurrent access, not `HashMap`
- `AtomicInteger` is for simple concurrent counters, not for all compound logic
- locks solve some problems but widen contention if used casually
- virtual threads make blocking cheaper, not correctness automatic

Good rules:

- prefer immutable or request-scoped state first
- when sharing mutable state across threads, choose the right concurrent primitive deliberately
- concurrency is not only "how many threads"; it is also visibility, contention, and downstream limits

One strong sentence:

> Virtual threads can make blocking style cheaper, but they do not remove shared
> state bugs, lock contention, database pool limits, or provider rate limits.

Use these when you want the deeper notes:

- [02-java-concurrency-and-jmm.md](./02-java-concurrency-and-jmm.md)
- [05-concurrency-in-production.md](./05-concurrency-in-production.md)
- [../architecture/08-concurrency-models-comparison.md](../architecture/08-concurrency-models-comparison.md)

---

## 10. Performance: The Practical Version

Most backend performance problems are not solved by micro-optimizing syntax.

Common real bottlenecks:

- slow SQL
- too many queries
- network latency
- too many remote calls in one request
- oversized payloads
- unbounded retries
- lock contention
- queue growth with no backpressure

Good performance rules:

- measure before rewriting
- fix query shape before arguing about tiny object allocations
- avoid unnecessary work in hot loops and hot serialization paths
- be careful with stream-heavy code in hot paths if it hides extra allocations or logic
- cache only when the read pattern and staleness tradeoff justify it
- do not keep widening concurrency if the real bottleneck is the DB pool or downstream service

Bad mental model:

> Performance means writing tricky code.

Better mental model:

> Performance means spending time where the latency or throughput budget is
> actually being lost.

Use these when needed:

- [01-jvm-memory-and-gc.md](./01-jvm-memory-and-gc.md)
- [06-jit-profiling-and-memory-leaks.md](./06-jit-profiling-and-memory-leaks.md)
- [../databases/13-explain-indexes-and-query-review-baseline.md](../databases/13-explain-indexes-and-query-review-baseline.md)

---

## 11. What Makes A System Scalable

At the practical level, a scalable system is one that can handle more useful
work without correctness or operability collapsing at the first hot path.

That usually requires:

- stateless or mostly stateless request handling where possible
- clear ownership of source-of-truth data
- bounded concurrency
- caching only where stale reads are acceptable
- asynchronous processing where immediate coupling would hurt too much
- idempotency for retries and at-least-once delivery
- observability for bottlenecks and failure rates
- backpressure or admission control so the system fails in a controlled way

What scalability is not:

- just "more instances"
- just "microservices"
- just "use Kafka"
- just "use Redis"

Short rule:

> A scalable system is one whose bottlenecks, correctness boundaries, and
> overload behavior are understood and controlled.

Good first questions:

1. what is the source of truth
2. what is the hottest path
3. what can be stale
4. what must be idempotent under retry
5. where do we shed load or queue it

Useful companion docs:

- [../system-design/system-design-decision-cheatsheet.md](../system-design/system-design-decision-cheatsheet.md)
- [../architecture/02-resiliency-patterns.md](../architecture/02-resiliency-patterns.md)
- [../architecture/07-caching-strategies.md](../architecture/07-caching-strategies.md)
- [../sre/05-capacity-planning-and-load-shedding.md](../sre/05-capacity-planning-and-load-shedding.md)

---

## 12. Distributed Systems Hints Worth Keeping Warm

Once work crosses process or database boundaries, remember:

- the network fails
- retries create duplicates unless the workflow is idempotent
- one local transaction no longer protects the whole workflow
- async flows improve decoupling but complicate visibility and debugging
- ordering, consistency, and latency are tradeoffs, not default guarantees

Strong defaults:

- keep the write path simple
- use SQL as the usual source of truth for business-critical workflows
- treat outbox plus idempotent consumers as a serious default for DB-write plus event-publish coordination
- prefer modular-monolith clarity before service explosion
- model workflow state transitions explicitly

Short rule:

> Distributed systems are mostly about communication failure, replay safety, and
> boundary clarity, not about diagram style.

Useful related docs:

- [../architecture/01-monolith-vs-microservices.md](../architecture/01-monolith-vs-microservices.md)
- [../architecture/03-distributed-transactions-and-events.md](../architecture/03-distributed-transactions-and-events.md)
- [../architecture/16-distributed-workflow-pattern-choice.md](../architecture/16-distributed-workflow-pattern-choice.md)
- [../api/02-message-brokers-and-delivery-semantics.md](../api/02-message-brokers-and-delivery-semantics.md)

---

## 13. Testing and Review Judgment

Good engineering judgment shows up in tests and reviews too.

Strong defaults:

- test business-critical branching and failure paths first
- test idempotency where retries or duplicate delivery matter
- test transaction boundaries and side effects where they are risky
- review for correctness, side effects, and operational risk before style polish

In reviews, ask:

- is the intent obvious
- are the state transitions explicit
- are side effects visible
- is failure handling believable
- is the code making hidden performance or concurrency assumptions

Use:

- [../testing/02-clean-code-and-code-review.md](../testing/02-clean-code-and-code-review.md)
- [../testing/01-testing-strategies.md](../testing/01-testing-strategies.md)

---

## 14. What To Reopen Regularly

If you want a durable refresh loop, keep these warm:

- records, sealed types, pattern matching, virtual threads
- collection and type judgment
- transactions, isolation, idempotency, and retries
- SQL query shape and indexing basics
- clear service-method shape and explicit side effects
- caching, queues, and async boundaries
- load shedding, timeouts, and request budgets

That is a more valuable long-term refresh than becoming prematurely deep in
`JIT` internals.

---

## 15. Short Takeaways

- write boring, explicit, reviewable backend code
- choose data structures by access pattern, not habit
- keep mutability narrow
- model failures by type, not with one generic exception bucket
- shared mutable state is the main concurrency danger
- most performance work is query, network, or contention work before it is syntax work
- scalability means controlled bottlenecks and overload behavior, not only more services
- distributed systems reward idempotency, explicit workflow state, and simple write paths
