# Modern Java for Backend Engineers

> Primary fit: `Shared core`


You do not need an exhaustive Java language tour.

If most of your professional Java experience is anchored in Java 8 or 11, what
you need is a practical refresh of the changes that affect backend code and how
you talk about them.

You need to know:

- what actually changed for backend code
- which features improve clarity
- which features affect runtime behavior
- how to explain them clearly

Code style rule for this repo:

- Java examples should stay inside the practical `17-21` baseline
- if a feature needs `21`, say that clearly
- avoid examples that depend on newer preview-era features as the default answer

Reading rule:

- use the Kotlin sketch as the fast mental anchor
- open the Java block when you want the direct `17-21` comparison

---

## 1. What Changed In The Practical Backend Baseline

The most useful modern-Java features for backend work are:

- records
- sealed types
- pattern matching
- virtual threads

Why these matter:

- better DTO and state modeling
- clearer branching
- less ceremony
- better concurrency ergonomics for blocking service code

Everything else is secondary until this baseline is clear.

---

## 2. Records: The Smallest Useful Example

Records are best when the type is mainly data.

```kotlin
data class ProductView(
    val id: String,
    val name: String,
    val priceYen: Int,
)
```

<details>
<summary>Java version</summary>

```java
// record = compact immutable data carrier
public record ProductView(String id, String name, int priceYen) { }
```

</details>

Why this matters:

- immutable by default
- less boilerplate
- very good for DTOs and projections

Good backend use:

- API request and response models
- query projections
- command objects

Bad use:

- forcing records into rich mutable domain entities
- assuming they are a good JPA entity default

Short rule:

> use records for data carriers, not as a substitute for all domain modeling

---

## 3. Sealed Types And Pattern Matching

These features work especially well together.

```kotlin
sealed interface PaymentResult

data class Success(val transactionId: String) : PaymentResult
data class Declined(val reason: String) : PaymentResult
data class Error(val message: String) : PaymentResult

fun describe(result: PaymentResult): String =
    when (result) {
        is Success -> "charged ${result.transactionId}"
        is Declined -> "declined: ${result.reason}"
        is Error -> "error: ${result.message}"
    }
```

<details>
<summary>Java version</summary>

```java
sealed interface PaymentResult permits Success, Declined, Error { }

record Success(String transactionId) implements PaymentResult { }
record Declined(String reason) implements PaymentResult { }
record Error(String message) implements PaymentResult { }
```

```java
// This switch pattern-matching style is part of the Java 21 baseline.
// If your team is still on Java 17 without this syntax enabled,
// use instanceof checks plus casts instead.
String describe(PaymentResult result) {
    return switch (result) {
        case Success s -> "charged " + s.transactionId(); // pattern matching binds result to typed variable s
        case Declined d -> "declined: " + d.reason(); // d is already a Declined, no extra cast needed
        case Error e -> "error: " + e.message(); // e is already typed as Error
    };
}
```

</details>

Why this matters:

- state space becomes explicit
- compiler helps enforce handling
- branching gets cleaner

Great backend fit:

- payment outcomes
- order workflow state
- validation results

Short rule:

> sealed hierarchies plus pattern matching are often better than strings, flags, and ad-hoc enums

---

## 4. Virtual Threads: The Main Operational Shift

Virtual threads matter because they change the cost model of blocking-style concurrency.

Smallest example:

```kotlin
suspend fun buildProductBundle(productId: String): ProductBundle = coroutineScope {
    val pricing = async { pricingClient.fetch(productId) }
    val stock = async { stockClient.fetch(productId) }
    ProductBundle(
        pricing = pricing.await(),
        stock = stock.await(),
    )
}
```

<details>
<summary>Java version</summary>

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    Future<Pricing> pricing = executor.submit(() -> pricingClient.fetch(productId)); // start one blocking remote call
    Future<Stock> stock = executor.submit(() -> stockClient.fetch(productId)); // start another blocking remote call in parallel
    return new ProductBundle(
        pricing.get(), // wait for pricing result
        stock.get()    // wait for stock result
    );
}
```

</details>

Why backend engineers care:

- easier concurrency than callback-heavy code
- useful with blocking I/O
- easier migration path for existing service code

What virtual threads do **not** solve:

- bad locking design
- unbounded downstream latency
- poor backpressure strategy

Short rule:

> virtual threads make blocking code cheaper, not automatically correct

---

## 5. Where Modern Java Helps Most

The highest-value uses are usually:

- records for DTOs
- sealed types for workflow states
- pattern matching for branching on results
- virtual threads for I/O-heavy service concurrency

Good senior rule:

> adopt modern Java when it improves clarity or concurrency ergonomics, not just because the feature exists

---

## 6. Where Java Is Still Not “Kotlin But Worse”

A lot of engineers still compare modern Java to an old Java 8 memory.
That is outdated.

Modern Java is still:

- more verbose than Kotlin
- very strong for conservative enterprise stacks
- a good fit for gradual modernization

Practical comparison:

- Kotlin is usually more concise
- modern Java is much less ceremonious than it used to be
- the gap matters less when the team values stability and broad familiarity

---

## 7. 20-Second Answer

> The practical modern-Java baseline for backend work is records, sealed types, pattern
> matching, and virtual threads. Records improve DTO and projection modeling, sealed types
> plus pattern matching improve workflow and result modeling, and virtual threads make
> blocking-style concurrency much more attractive without forcing a reactive rewrite.

---

## 8. 1-Minute Answer

> For backend work I do not try to memorize every recent Java feature. I keep a practical
> baseline: records for immutable data carriers, sealed types and pattern matching for safer
> workflow modeling, and virtual threads for scaling blocking-style concurrency more cleanly.
> Records are great for request/response DTOs and projections, but I would not force them
> into richer mutable domains. Sealed types help make payment, validation, or order states
> explicit instead of hiding them in flags. Virtual threads are the biggest operational shift
> because they improve the economics of blocking I/O, but they do not remove the need to
> think about locks, contention, or downstream latency. My general rule is that modern Java
> should reduce ceremony and improve clarity, not become feature collecting.

---

## 9. Further Reading

- Java Records: https://openjdk.org/jeps/395
- Sealed Classes: https://openjdk.org/jeps/409
- Pattern Matching for `switch`: https://openjdk.org/jeps/441
- Virtual Threads: https://openjdk.org/jeps/444
