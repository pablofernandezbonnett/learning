# Concurrency in Production

Use this after [02-java-concurrency-and-jmm.md](./02-java-concurrency-and-jmm.md).

The first concurrency note teaches the primitives.
This one focuses on what usually goes wrong in live backend systems:

- thread pools fill with blocking work
- one request fans out too aggressively
- queues grow silently and latency becomes bursty
- local locks look safe but do not protect shared truth across instances

If you keep one line warm, keep this:

> production concurrency is mainly about protecting the request budget,
> bounding work, and keeping shared truth safe under parallel pressure

---

## 1. What Changes In Production

The beginner question is:

> "How do I run things concurrently?"

The production question is:

> "How do I stop concurrency from turning one slow dependency into a queueing
> problem, a duplicate-work problem, or a broken business rule?"

That changes the emphasis.

You care more about:

- bounded parallelism
- timeouts and cancellation
- admission control
- lock contention
- DB or broker boundaries

You care less about:

- hand-creating threads
- academic taxonomy for its own sake

Virtual-thread reminder:

- virtual threads reduce the cost of blocking per task
- they do not remove connection-pool limits, downstream rate limits, lock contention, or request-budget pressure

---

## 2. Pool Saturation Is A Real Failure Mode

A fixed-size pool is not "bad".
It is only dangerous when you keep submitting blocking work without treating the
pool as a finite resource.

Kotlin sketch:

```kotlin
class PricingFacade(
    private val executor: ExecutorService,
    private val client: PricingClient,
) {
    fun fetchAll(skus: List<String>): List<Price> {
        return skus.map { sku ->
            CompletableFuture.supplyAsync(
                { client.fetchPrice(sku) }, // blocking I/O still occupies one worker
                executor,
            )
        }.map { future ->
            future.join()
        }
    }
}
```

<details>
<summary>Java version</summary>

```java
public final class PricingFacade {
    private final ExecutorService executor;
    private final PricingClient client;

    public PricingFacade(ExecutorService executor, PricingClient client) {
        this.executor = executor;
        this.client = client;
    }

    public List<Price> fetchAll(List<String> skus) {
        return skus.stream()
            .map(sku -> CompletableFuture.supplyAsync(
                () -> client.fetchPrice(sku), // blocking I/O still occupies one worker
                executor
            ))
            .map(CompletableFuture::join)
            .toList();
    }
}
```

</details>

What goes wrong:

- every blocking remote call occupies one pool worker
- too many requests queue behind the pool
- latency spikes before the service looks "down"

Short rule:

> if the dependency is blocking, the thread pool is part of the capacity plan

---

## 3. Bound Fan-Out To The Request Budget

Parallel downstream calls can help.
Unbounded parallelism plus no overall timeout usually hurts more than it helps.

Kotlin sketch:

```kotlin
fun buildCheckoutView(userId: String, executor: ExecutorService): CheckoutView {
    val cart = CompletableFuture.supplyAsync(
        { cartClient.fetchCart(userId) },
        executor,
    )
    val pricing = CompletableFuture.supplyAsync(
        { pricingClient.fetchPricing(userId) },
        executor,
    )

    return cart
        .thenCombine(pricing) { cartView, pricingView ->
            CheckoutView(cart = cartView, pricing = pricingView)
        }
        .orTimeout(250, TimeUnit.MILLISECONDS) // bounds this future's wait, not child I/O
        .join()
}
```

<details>
<summary>Java version</summary>

```java
public CheckoutView buildCheckoutView(String userId, ExecutorService executor) {
    CompletableFuture<CartView> cart = CompletableFuture.supplyAsync(
        () -> cartClient.fetchCart(userId),
        executor
    );

    CompletableFuture<PricingView> pricing = CompletableFuture.supplyAsync(
        () -> pricingClient.fetchPricing(userId),
        executor
    );

    return cart
        .thenCombine(pricing, CheckoutView::new)
        .orTimeout(250, TimeUnit.MILLISECONDS) // bounds this future's wait, not child I/O
        .join();
}
```

</details>

Why this matters:

- per-call timeouts are not enough if the whole request still waits forever
- the budget belongs to the user-facing request, not to each child call independently
- fan-out is only useful if the calls are actually independent
- cheap task creation is not permission to multiply downstream load without a budget

`CompletableFuture.orTimeout` completes the future exceptionally; it does not
interrupt the running suppliers. Configure deadlines on each HTTP client call,
pass the remaining request budget downstream, and bound queued work. Otherwise
the caller may time out while the dependency calls keep consuming capacity.

Do not parallelize when:

- one step depends on the previous result
- the dependency is already the bottleneck
- the downstream system cannot tolerate the extra concurrency

---

## 4. Admission Control Beats Silent Queue Growth

One of the cleanest production patterns is to reject or defer work early instead
of letting queues grow until every request becomes slow.

Kotlin sketch:

```kotlin
class ExportController(
    private val permits: Semaphore = Semaphore(8),
) {
    fun startExport(request: ExportRequest): ResponseEntity<String> {
        if (!permits.tryAcquire()) {
            return ResponseEntity.status(503).body("export capacity is full")
        }

        try {
            exportService.run(request) // bounded expensive work
            return ResponseEntity.ok("export completed")
        } finally {
            permits.release()
        }
    }
}
```

<details>
<summary>Java version</summary>

```java
public final class ExportController {
    private final Semaphore permits = new Semaphore(8);

    public ResponseEntity<String> startExport(ExportRequest request) {
        if (!permits.tryAcquire()) {
            return ResponseEntity.status(503).body("export capacity is full");
        }

        try {
            exportService.run(request); // bounded expensive work
            return ResponseEntity.ok("export completed");
        } finally {
            permits.release();
        }
    }
}
```

</details>

This is not fancy.
It is often exactly the right thing.

Short rule:

> bounded work plus a clear "busy" answer is healthier than pretending every
> request can always start immediately

This example runs the export before responding, so a successful response is
`200 OK`. Use `202 Accepted` only when the work is accepted for later
processing and the API provides a way to learn its outcome. A shared-capacity
limit is shown as `503`; a per-client rate limit could instead use `429`.

---

## 5. Local Locks Are Not Global Correctness

This trap matters a lot in backend systems:

- one service instance protects a critical section with `synchronized`
- another service instance does the same
- both still race on the same database row or business key

Kotlin sketch:

```kotlin
class ReservationService(
    private val repository: StockRepository,
) {
    private val lock = Any()

    fun reserveOne(productId: Long): Boolean = synchronized(lock) {
        val stock = repository.findStock(productId)
        if (stock <= 0) return false

        repository.updateStock(productId, stock - 1) // safe only inside this JVM instance
        true
    }
}
```

<details>
<summary>Java version</summary>

```java
public final class ReservationService {
    private final StockRepository repository;
    private final Object lock = new Object();

    public ReservationService(StockRepository repository) {
        this.repository = repository;
    }

    public boolean reserveOne(long productId) {
        synchronized (lock) {
            int stock = repository.findStock(productId);
            if (stock <= 0) {
                return false;
            }

            repository.updateStock(productId, stock - 1); // safe only inside this JVM instance
            return true;
        }
    }
}
```

</details>

Why this is incomplete:

- the lock only protects one app instance
- another instance can still read the same old stock and overwrite it
- the real correctness boundary is usually the database write rule

That is why this note belongs next to
[../databases/02-database-locks-and-concurrency.md](../databases/02-database-locks-and-concurrency.md).

Short rule:

> in-memory locking protects local state; inventory, money, and order truth
> usually need protection at the shared durable boundary

---

## 6. What To Watch In Production

If concurrency is going wrong, these are the first questions to ask:

- Is the pool full of blocking work?
- Is queue depth growing?
- Is p95 or p99 latency rising long before outright failure?
- Are retries multiplying work?
- Is the same business key causing contention or duplicate handling?
- Is a "safe" local mutex hiding a cross-instance race?

Good signals:

- active threads or active virtual tasks
- queue size
- semaphore permits in use
- timeout count
- retry count
- JDBC connection pool usage
- outbound HTTP connection usage
- lock wait time
- business metrics such as "stuck pending payments" or "inventory conflicts"

---

## 7. Companion

Read first:

- [02-java-concurrency-and-jmm.md](./02-java-concurrency-and-jmm.md)
- [../databases/02-database-locks-and-concurrency.md](../databases/02-database-locks-and-concurrency.md)

Then try:

- [../../labs/kotlin-backend-examples/README.md](../../labs/kotlin-backend-examples/README.md)
  exercise `Production concurrency`

Implement the scenario yourself if you want to observe queueing and bounded admission.

References: [Java `CompletableFuture` API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/CompletableFuture.html) for timeout and cancellation behavior; [HTTP semantics](https://www.rfc-editor.org/rfc/rfc9110.html) for `202` and `503` responses.
