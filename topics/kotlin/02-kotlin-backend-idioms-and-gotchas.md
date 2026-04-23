# Kotlin Backend Idioms and Gotchas

This is the Kotlin refresher that usually matters after the basics.

If you already understand:

- null safety
- data classes
- sealed classes
- coroutines

the next useful layer is:

- how to write Kotlin that stays clear in backend code
- how to avoid "clever Kotlin"
- which features actually help on the JVM

---

## 1. Value Classes

Kotlin value classes are useful for lightweight domain wrappers.

Typical examples:

- `UserId`
- `OrderId`
- `EmailAddress`

Why they matter:

- stronger domain typing
- less accidental mixing of raw `String` or `Long` values
- better readability at service boundaries

Be careful with:

- frameworks and serialization boundaries
- overusing them everywhere
- assuming they remove all runtime nuance automatically

Good rule:

> Use value classes for a few high-value domain identifiers, not as a blanket style.

Tiny example:

```kotlin
@JvmInline
value class ProductId(val value: String)
```

Useful in real backend code:

- service method signatures
- domain commands
- adapters where raw IDs are easy to mix up

---

## 2. `Result` and `runCatching`

Kotlin `Result` is useful, but it is easy to overuse badly.

Good uses:

- infrastructure boundaries
- wrapping external calls when a result style is clearer
- transforming success/failure in a pipeline

Be careful with:

- replacing all exceptions in business logic
- hiding meaningful domain states inside generic failures
- `runCatching` catching too broadly when you really want explicit control

Good rule:

> Use `Result` when it improves the boundary. Use domain types or exceptions when they express the situation more clearly.

Tiny example:

```kotlin
fun fetchProfile(id: UserId): Result<ProfileDto> =
    runCatching { client.getProfile(id.value) }
```

Useful in real backend code:

- HTTP or messaging adapters
- infrastructure wrappers
- places where you want to map low-level failures before they enter service logic

---

## 3. Scope Functions

The main scope functions are:

- `let`
- `run`
- `with`
- `apply`
- `also`

They are useful, but they are also one of the fastest ways to make Kotlin code harder to read.

Good rule of thumb:

- `apply` for object configuration
- `also` for side effects like logging
- `let` for nullable chains or local scoping

Avoid:

- nesting several scope functions
- switching between `this` and `it` in confusing ways
- writing code that reads like puzzle syntax

---

## 4. Java Interop and Platform Types

Kotlin null safety gets weaker at Java boundaries.

That is where platform types appear.

Practical meaning:

- a Java API may arrive in Kotlin as `String!`
- the compiler cannot fully guarantee whether it is nullable or not
- framework code, older libraries, and mixed Java/Kotlin projects still need explicit care

Backend rule:

> Be strict at boundaries. Adapt Java APIs into explicit Kotlin types early
> instead of letting platform types leak through service code.

This matters in:

- Spring or library callbacks
- mapper code
- legacy Java modules
- repository and integration boundaries

---

## 5. Extension Functions and Defaults

Extension functions are great when they make local intent clearer.

Good uses:

- mapper helpers
- small formatting helpers
- domain-specific convenience around existing types

Be careful with:

- hiding important behavior behind extensions
- adding business logic far away from the real type
- creating a pseudo-framework out of helpers

Good rule:

> Use extension functions to reduce friction, not to make the codebase harder
> to trace.

Tiny example:

```kotlin
fun ProductEntity.toDto(): ProductDto = ProductDto(id = id, name = name)
```

Useful in real backend code:

- mapping
- small formatting / conversion helpers
- keeping controllers and services less noisy

---

## 6. Collections and Immutability

Kotlin collection APIs are expressive and useful.

But remember:

- expressive pipelines can still allocate a lot
- readability matters more than chaining everything
- "read-only" collection types are not the same as deeply immutable data structures

Backend rule:

> Prefer clear collection pipelines, but do not forget the cost of extra intermediate transformations in hot paths.

---

## 7. Kotlin and Spring Boundaries

The main Kotlin + Spring traps are already familiar:

- proxies
- JPA entity requirements
- validation annotations
- coroutine + transaction boundaries

Additional practical rule:

> Keep Kotlin expressive at the edges and boring in the middle. DTOs, mapping, and state modeling benefit a lot. Core service logic should still stay obvious to the next engineer.

---

## 8. What To Practice

If you want a practical Kotlin refresh:

1. introduce one value class such as `ProductId` or `OrderId` in a personal project
2. refactor one nested scope-function chain into straightforward service code
3. wrap one flaky integration call with `Result` at the adapter boundary
4. check one Java-facing boundary and make platform-type nullability explicit

The goal is not "more Kotlin".
The goal is clearer backend code.

---

## 9. What To Keep Ready

- value classes for stronger IDs and small domain wrappers
- `Result` as a boundary tool, not a religion
- scope functions used sparingly
- Java interop and platform types under control
- extension functions used as helpers, not hidden architecture
- clear `when` over sealed types
- coroutines where they simplify flow, not where they create magic

---

## 10. Short Lines

- "What I want from Kotlin in backend work is stronger modeling and less ceremony, not clever syntax."
- "Value classes are useful for domain IDs, but I use them selectively."
- "I treat `Result` as a boundary tool. For many business cases, a sealed domain result is clearer."
- "Scope functions are helpful, but overuse makes Kotlin code harder to maintain."
- "Kotlin null safety is strongest when Java interop is kept under control and platform types do not leak too far."

---

## 11. Further Reading

- [Inline Value Classes](https://kotlinlang.org/docs/inline-classes.html)
- [Scope Functions](https://kotlinlang.org/docs/scope-functions.html)
- [Kotlin Result API](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-result/)
- [runCatching](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/run-catching.html)
- [Java Interoperability](https://kotlinlang.org/docs/java-interop.html)
- [Null Safety](https://kotlinlang.org/docs/null-safety.html)
