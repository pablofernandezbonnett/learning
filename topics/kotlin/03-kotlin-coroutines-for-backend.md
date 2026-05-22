# Kotlin Coroutines for Backend Engineers

Use this note when you want the backend version of coroutines, not the Android
or UI-first explanation.

The main value is not "lighter threads" as a slogan.
The value is clearer async orchestration, structured cancellation, and cheaper
waiting when the stack is coroutine-friendly.

---

## Why This Matters

Backend concurrency work usually goes wrong in one of two ways:

- shared mutable state is not protected correctly
- waiting work is coordinated badly and turns into tangled futures, leaked work, or poor cancellation

Coroutines help mainly with the second problem.
They do not remove the need for server-side correctness, shared-truth
protection, or capacity limits.

If you keep one line warm, keep this:

> coroutines are mainly a structured async programming model, not a magic
> replacement for every thread, pool, or blocking dependency

---

## Smallest Useful Mental Model

A coroutine is a unit of work that can suspend without blocking the underlying
thread for the whole wait.

The useful split is:

- `thread`: runtime worker that executes code
- `coroutine`: logical unit of work that may suspend and later resume
- `suspend`: this function may pause at suspension points
- `coroutineScope`: child work belongs to one parent scope and should finish or fail with it

Practical translation:

- use coroutines to coordinate independent I/O work more clearly
- do not treat them as proof that the underlying database or HTTP client is non-blocking

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- coroutines are just lighter threads
- if I use `launch` everywhere, the backend becomes scalable
- suspension automatically makes blocking dependencies safe

Better mental model:

- coroutines are a structured way to express async work and cancellation
- `async` is useful only when the child tasks are actually independent
- blocking code is still blocking unless you move it carefully or replace the library stack

Small concrete example:

- weak approach: one request fires three downstream calls in ad-hoc background jobs and hopes everything gets joined later
- better approach: one `coroutineScope` owns the fan-out, failure, and timeout of that request

---

## 1. The First Useful Backend Example

This is the smallest good backend-style coroutine example:

```kotlin
suspend fun buildCheckoutView(userId: String): CheckoutView = coroutineScope {
    val cart = async { cartClient.fetchCart(userId) }
    val pricing = async { pricingClient.fetchPricing(userId) }

    CheckoutView(
        cart = cart.await(),
        pricing = pricing.await(),
    )
}
```

Why this is good:

- the two remote calls are independent
- they run concurrently inside one request scope
- if the parent scope is cancelled, the child work is cancelled too

What this does not prove:

- it does not prove the clients are non-blocking
- it does not remove the need for timeouts or request budgets

Short rule:

> use `async` for bounded fan-out of genuinely independent child work, not as a default wrapper around every call

---

## 2. What Good Looks Like In Practice

Strong default:

- use `suspend` when the function really participates in async orchestration
- use `coroutineScope` for request-scoped child work
- use `async` only for independent work that you really want to run in parallel
- keep cancellation, timeout, and error ownership tied to the request scope
- keep shared mutable state small even if the async code looks elegant

Bad vs better:

- bad: coroutine code launches work into a long-lived global scope and loses request ownership
- better: the request or service scope owns the child work explicitly

- bad: coroutine code hides blocking JDBC or HTTP work and assumes suspension solved the throughput problem
- better: the team knows which calls are actually blocking, which dispatcher runs them, and where the real capacity limit still lives

Small practical rule:

- coroutines help most when they make orchestration simpler and failure handling clearer

---

## 3. Timeouts and Cancellation Matter

Async code is only better when failure and timeout behavior also improve.

Good minimal shape:

```kotlin
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout

suspend fun buildBundle(productId: String): ProductBundle =
    withTimeout(250) {
        coroutineScope {
            val stock = async { stockClient.fetch(productId) }
            val pricing = async { pricingClient.fetch(productId) }
            ProductBundle(stock.await(), pricing.await())
        }
    }
```

Why this is stronger:

- the timeout belongs to the whole request budget
- the child work is tied to one parent scope
- cancellation is not left to chance

Be careful:

- cancellation in Kotlin is cooperative
- code that never checks for suspension or cancellation points may keep running longer than you want

---

## 4. Dispatchers and Blocking Work

This is where a lot of coroutine explanations become too optimistic.

Useful mental model:

- `Dispatchers.Default`: CPU-bound work
- `Dispatchers.IO`: blocking I/O work
- custom dispatcher or framework dispatcher: when the stack needs clearer isolation

Good rule:

> choose the dispatcher by the kind of work, not by habit

Bad vs better:

- bad: everything runs wherever it happened to start
- better: blocking calls, CPU-heavy work, and request orchestration are separated intentionally

Important caution:

- moving blocking calls to `Dispatchers.IO` can help avoid blocking the wrong worker threads
- it does not make the dependency non-blocking
- the database connection pool, remote service, or broker is still the real bottleneck

---

## 5. Coroutines and Shared State

Coroutines help with async flow, not with business-rule correctness by themselves.

This is still wrong:

```kotlin
var counter = 0

suspend fun increment() {
    counter++
}
```

If several coroutines touch the same mutable state concurrently, you still need
coordination.

What to use depends on the problem:

- immutable or request-local state when possible
- atomics for one independently changing value
- `Mutex` when one coroutine-critical section must be protected
- database locking or transactional protection when the real truth is shared across instances

Short rule:

> coroutine syntax does not remove race conditions; it only changes how async work is expressed

---

## 6. Flow and Channels: Use Them For The Right Job

`Flow` is useful when the result is not one value, but a stream of values over
time.

Examples:

- stock updates
- progress events
- streaming notifications

`Channel` is useful when coroutines need explicit handoff between producer and
consumer work.

Examples:

- bounded worker handoff
- pipeline-style processing

Do not reach for either first when:

- the operation returns one value
- a normal `suspend` function is enough
- the complexity would mostly come from the abstraction, not from the real problem

---

## 7. Spring and Kotlin Reality

In backend work, coroutine discussions often become Spring discussions.

The high-value practical cautions are:

- `suspend` controllers and service methods are fine when the stack supports them
- transaction boundaries still need care
- `@Async` and coroutines together are usually a sign that the model is getting muddled
- blocking JPA and JDBC still carry their normal runtime limits

Use [../spring-boot/15-kotlin-spring-idioms.md](../spring-boot/15-kotlin-spring-idioms.md)
for the framework-specific details.

Short rule:

> coroutine-friendly code and coroutine-friendly infrastructure are not the same thing

---

## 8. Coroutines vs Virtual Threads

This comparison is worth keeping clean:

- coroutines are a language-level async model with structured concurrency and explicit suspension
- virtual threads are a JVM runtime model that makes blocking code cheaper to run

Prefer coroutines when:

- the service is Kotlin-heavy
- the libraries and team already support coroutine-style orchestration
- structured cancellation and async composition matter a lot

Prefer virtual threads when:

- the stack is still mostly blocking
- the codebase is Java-heavy or Spring MVC / JDBC heavy
- you want a lower-migration-cost concurrency improvement on Java 21+

Do not explain them as:

- "the same thing in different syntax"

Reusable takeaway:

> coroutines are strongest when I want clear async composition and structured cancellation in a Kotlin stack. Virtual threads are strongest when I want blocking-style JVM code to scale better without a larger async rewrite.

---

## 9. What To Practice

1. take one request that fans out to two independent downstream calls and rewrite it with `coroutineScope + async`
2. add one request-level timeout around that fan-out
3. identify which child calls are actually blocking and which are coroutine-friendly
4. decide whether the real correctness boundary is in memory, in one process, or at the database

If you can explain those four points clearly, your coroutine understanding is
already useful for backend work.

---

## 10. Further Reading

- Kotlin coroutines guide: https://kotlinlang.org/docs/coroutines-guide.html
- Coroutine context and dispatchers: https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html
- [../../labs/kotlin-basics/06-backend-coroutine-boundaries.kt](../../labs/kotlin-basics/06-backend-coroutine-boundaries.kt)
- [../spring-boot/15-kotlin-spring-idioms.md](../spring-boot/15-kotlin-spring-idioms.md)
- [../architecture/08-concurrency-models-comparison.md](../architecture/08-concurrency-models-comparison.md)
