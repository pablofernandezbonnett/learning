# Java Concurrency and the JMM

> Primary fit: `Shared core`

You do not need to be a JVM concurrency specialist.
You do need to explain shared state, blocking work, and visibility clearly when
backend work goes past normal CRUD.

This note keeps the topic practical:

- what usually goes wrong
- the smallest code examples
- the few Java tools that matter most
- how to explain it clearly

Reading rule:

- use the Kotlin sketch as the fast mental anchor
- open the Java block when you want the direct Java/JMM version

---

## 1. What Concurrency Problem Usually Matters

In backend work, the main problem is usually not "how do I create threads?"

It is:

> two threads touch the same mutable state, and the final result breaks the rule
> the system was supposed to protect

Typical examples:

- a counter ends up too low
- a flag change is not seen by another thread
- two steps that should move together get interleaved
- a thread pool is full of blocked work and latency spikes

Short rule:

> concurrency is mostly about shared mutable state, visibility, and blocking

---

## 2. The Smallest Broken Example

This is the classic race condition:

```kotlin
class CounterService {
    private var counter = 0

    fun increment() {
        counter++ // read, add, write
    }

    fun get(): Int = counter
}
```

<details>
<summary>Java version</summary>

```java
public final class CounterService {
    private int counter = 0;

    public void increment() {
        counter++; // read, add, write
    }

    public int get() {
        return counter;
    }
}
```

</details>

Why it is broken:

- `counter++` is not one indivisible step
- two threads can both read the same old value
- one update can overwrite the other

That is the first mental model to keep warm.

---

## 3. `synchronized` vs Atomics

### 3.1 `synchronized`

Use it when you need one clear critical section.

```kotlin
class CounterService {
    private var counter = 0

    @Synchronized
    fun increment() {
        counter++ // only one thread can do the read-modify-write at a time
    }

    @Synchronized
    fun get(): Int = counter // read is inside the same synchronized boundary
}
```

<details>
<summary>Java version</summary>

```java
public final class CounterService {
    private int counter = 0;

    public synchronized void increment() {
        counter++; // only one thread can do the read-modify-write at a time
    }

    public synchronized int get() {
        return counter; // read is inside the same synchronized boundary
    }
}
```

</details>

What it gives you:

- only one thread enters the critical section at a time
- visibility is also handled for that protected state

Good fit:

- small shared state
- simple read-modify-write logic

### 3.2 `AtomicInteger`

Use it when one value can change independently.

```kotlin
import java.util.concurrent.atomic.AtomicInteger

class CounterService {
    private val counter = AtomicInteger()

    fun incrementAndGet(): Int =
        counter.incrementAndGet() // atomically add 1 and return the new value

    fun get(): Int = counter.get() // safe current read of the latest counter value
}
```

<details>
<summary>Java version</summary>

```java
import java.util.concurrent.atomic.AtomicInteger;

public final class CounterService {
    private final AtomicInteger counter = new AtomicInteger();

    public int incrementAndGet() {
        return counter.incrementAndGet(); // atomically add 1 and return the new value
    }

    public int get() {
        return counter.get(); // safe current read of the latest counter value
    }
}
```

</details>

Good fit:

- counters
- simple flags
- compare-and-set state changes

Important rule:

> atomics are good for one value; they do not protect a whole multi-step business rule

If two values must stay consistent together, a plain atomic is usually not enough.

---

## 4. `volatile`: Visibility, Not Full Safety

`volatile` is useful when one thread must see the latest value written by another.

Good minimal example:

```kotlin
class Worker {
    @Volatile
    private var stopped = false

    fun stop() {
        stopped = true // write latest value so the worker thread can see it
    }

    fun runLoop() {
        while (!stopped) {
            doWork() // loop keeps running until another thread flips stopped to true
        }
    }

    private fun doWork() {
        // work here
    }
}
```

<details>
<summary>Java version</summary>

```java
public final class Worker {
    private volatile boolean stopped = false;

    public void stop() {
        stopped = true; // write latest value so the worker thread can see it
    }

    public void runLoop() {
        while (!stopped) {
            doWork(); // loop keeps running until another thread flips stopped to true
        }
    }

    private void doWork() {
        // work here
    }
}
```

</details>

Why this works:

- one thread sets `stopped = true`
- the worker thread can see that change promptly

Bad example:

```kotlin
@Volatile
private var counter = 0

fun increment() {
    counter++
}
```

<details>
<summary>Java version</summary>

```java
private volatile int counter = 0;

public void increment() {
    counter++;
}
```

</details>

That is still broken because `counter++` is still read, add, write.

Short rule:

> `volatile` helps one thread see the latest value; it does not make multi-step updates atomic

---

## 5. The JMM In Plain English

The Java Memory Model is the set of rules that says:

- when one thread can see another thread's write
- what ordering Java and the CPU are allowed to change internally
- which synchronization actions create a safe visibility boundary

The two terms worth keeping are:

- `happens-before`: one action is guaranteed to be visible before another
- `safe publication`: an object is shared only after it has been fully constructed

Practical translation:

- entering and leaving `synchronized` creates visibility guarantees
- `volatile` reads and writes create visibility guarantees for that variable
- immutable objects reduce the problem a lot because they stop changing after construction

You do not need the full spec.
You do need to understand this failure:

- thread A builds or updates state
- thread B reads stale or partially visible state
- the bug appears only sometimes under load

---

## 6. Concurrent Collections

Do not put normal collections behind many threads and hope for the best.

Use the collection that matches the access pattern.

- `ConcurrentHashMap`: shared map with concurrent reads and writes
- `CopyOnWriteArrayList`: many reads, rare writes
- `ConcurrentLinkedQueue`: non-blocking FIFO queue across threads
- `ConcurrentLinkedDeque`: non-blocking deque, useful for stack-like or queue-like access
- `BlockingQueue`: producer-consumer handoff where threads may need to wait
- `PriorityBlockingQueue`: concurrent priority queue when removal should follow priority, not arrival order

Useful correction:

- `ArrayDeque`, `LinkedList`, and `PriorityQueue` are not thread-safe
- `BlockingQueue` is not just "a thread-safe queue"; it also gives waiting semantics, which is useful for producer-consumer work
- `PriorityBlockingQueue` gives concurrent priority ordering, but it is not a bounded backpressure mechanism by itself

Small example:

```kotlin
import java.util.concurrent.ConcurrentHashMap

class SessionStore {
    private val sessions = ConcurrentHashMap<String, String>()

    fun put(token: String, userId: String) {
        sessions[token] = userId // thread-safe single map write
    }

    fun findUser(token: String): String? =
        sessions[token] // thread-safe single map read
}
```

<details>
<summary>Java version</summary>

```java
import java.util.concurrent.ConcurrentHashMap;

public final class SessionStore {
    private final ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();

    public void put(String token, String userId) {
        sessions.put(token, userId); // thread-safe single map write
    }

    public String findUser(String token) {
        return sessions.get(token); // thread-safe single map read
    }
}
```

</details>

Important nuance:

> a concurrent collection protects its own operations, not your whole business workflow

If you do `get`, then make a decision, then `put`, you still need to think about races.

---

## 7. Thread Pools, Blocking Work, and `CompletableFuture`

Thread pools are not free.

The first question is what kind of work the pool is doing:

- CPU-bound work
- blocking I/O
- mixed work that should probably be separated

Short rule:

> CPU-bound work and blocking I/O should not share sizing assumptions

Minimal `CompletableFuture` example:

```kotlin
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun buildCheckout(userId: String): CheckoutView = coroutineScope {
    val cart = async { cartClient.getCart(userId) } // start cart fetch in parallel
    val pricing = async { pricingClient.getPricing(userId) } // start pricing fetch in parallel
    CheckoutView(cart.await(), pricing.await()) // wait for both results, then build one response
}
```

<details>
<summary>Java version</summary>

```java
import java.util.concurrent.CompletableFuture;

public CompletableFuture<CheckoutView> buildCheckout(String userId) {
    CompletableFuture<Cart> cartFuture =
        CompletableFuture.supplyAsync(() -> cartClient.getCart(userId)); // start cart fetch in parallel
    CompletableFuture<Pricing> pricingFuture =
        CompletableFuture.supplyAsync(() -> pricingClient.getPricing(userId)); // start pricing fetch in parallel

    return cartFuture.thenCombine(pricingFuture, CheckoutView::new); // wait for both results, then build one response
}
```

</details>

Why this is useful:

- independent remote calls can run in parallel
- the code can stay clearer than nested callbacks

What to say carefully:

- do not hide blocking calls inside a fancy async chain
- know which executor runs the work
- timeouts and fallbacks still matter

---

## 8. Virtual Threads

Virtual threads make blocking Java code scale better.

The practical idea is simple:

- you keep a straightforward blocking style
- the JVM can schedule many lightweight virtual threads on fewer platform threads

Minimal example:

```kotlin
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun authorizeCheckout(): Unit = coroutineScope {
    val payment = async { paymentClient.authorize() } // run one remote call concurrently
    val inventory = async { inventoryClient.reserve() } // run another call without a platform-thread-per-task model
    payment.await()
    inventory.await()
}
```

<details>
<summary>Java version</summary>

```java
import java.util.concurrent.Executors;

try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> paymentClient.authorize()); // run one blocking remote call in its own virtual thread
    executor.submit(() -> inventoryClient.reserve()); // run another blocking remote call without tying up a platform thread
}
```

</details>

What virtual threads do help with:

- JDBC-heavy code
- service-to-service calls
- request fan-out with blocking clients

What they do not solve:

- race conditions
- bad locking
- missing backpressure

Short rule:

> virtual threads improve the cost of blocking; they do not remove the need to design correctness

---

## 9. How To Compare Java, Kotlin, and Go Briefly

Safe short comparison:

- Java: explicit concurrency primitives, mature libraries, virtual threads for scalable blocking I/O
- Kotlin coroutines: language-level async model with explicit suspension points
- Go: lightweight goroutines and a simpler concurrency story for many small services

Short explanation:

> Coroutines are mainly an async programming model. Virtual threads are mainly a cheaper runtime model for blocking work. Both can help, but they are not the same tool.

---

## 10. Key Explanations To Keep Ready

- "I start by asking what state is shared and what rule must never be violated."
- "If one value changes alone, an atomic may be enough. If several values must move together, I think about locking or redesigning the state."
- "`volatile` helps with visibility, not with multi-step consistency."
- "For backend systems, concurrency is really about correctness, throughput, and blocking behavior together."

---

## 11. What To Internalize

- the first bug to explain is usually a race on shared mutable state
- `counter++` is not atomic
- `synchronized` is often the simplest correct answer
- atomics help for single-value coordination
- `volatile` is about visibility, not full safety
- concurrent collections help, but they do not replace workflow-level reasoning
- thread-pool sizing depends on workload type
- virtual threads improve scalable blocking I/O, not correctness

---

## 12. Further Reading

- Java Virtual Threads: https://openjdk.org/jeps/444
