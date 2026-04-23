# Kotlin for Backend Engineers Coming from Java

> Primary fit: `Shared core`


Kotlin should not feel like a language reset for a Java backend engineer.

The useful question is:

> what changes in the way I express backend code, and which changes actually improve it?

---

## 1. What Carries Over From Java

Most backend judgment does not change:

- API design
- transaction boundaries
- concurrency tradeoffs
- source of truth decisions
- debugging and production reasoning

Kotlin mostly changes:

- how nullability is expressed
- how much ceremony DTO and state modeling requires
- how async code can be written

---

## 2. The Smallest Useful Kotlin Example

This is a good minimal contrast with Java DTO and null handling:

```kotlin
data class ProductDto(
    val id: String,
    val name: String,
    val priceYen: Int,
)

fun findProduct(id: String): ProductDto? = repository.findById(id)

val product = findProduct("sku-1") ?: throw ProductNotFoundException("sku-1")
```

What this shows immediately:

- `data class` removes DTO ceremony
- `ProductDto?` makes nullability explicit
- Elvis operator makes the not-found path compact and clear

That is the core of why Kotlin often feels better for backend code before you even touch
coroutines.

---

## 3. The Biggest Improvements Over Java

### Null safety

This is the highest-value difference.

- `String` means non-null
- `String?` means nullable

That improves:

- repository return types
- DTO contracts
- service branching

### Data classes

Great for:

- request and response objects
- projections
- simple state holders

### Sealed classes

Very strong for:

- payment results
- validation outcomes
- workflow states

### Extension functions

Useful when restrained.
Dangerous when overused.

Short rule:

> extension functions should improve readability, not hide important behavior

---

## 4. Spring + Kotlin Reality

Kotlin plus Spring is productive, but there are a few rules you must remember:

- Spring proxies need `plugin.spring`
- JPA entities usually need `plugin.jpa`
- validation annotations on data classes often need `@field:`
- Jackson needs `jackson-module-kotlin`

That means the language is cleaner, but the framework still has runtime rules.

---

## 5. Coroutines: Useful, Not Mandatory Everywhere

Coroutines are one of Kotlin's strongest features, but they are not a reason to rewrite
every backend in async style.

Good use cases:

- coordinating multiple independent I/O calls
- replacing awkward callback or future orchestration
- writing clearer async flows than a heavy reactive chain

Be careful with:

- blocking libraries
- transaction boundaries
- thread-local assumptions

Short rule:

> use coroutines where they simplify concurrency, not where they only add novelty

---

## 6. Where Java Is Still A Good Choice

A strong answer is not "Kotlin good, Java bad."

Better framing:

- Java is more verbose but still very strong
- Kotlin improves correctness and readability in many service layers
- the choice depends on team habits, platform conservatism, and existing codebase shape

That is both more accurate and more useful in practice.

---

## 7. 20-Second Answer

> My Java backend experience transfers directly to Kotlin. The main improvements are
> explicit null safety, less DTO ceremony with data classes, and stronger workflow modeling
> with sealed classes. I see coroutines as a useful concurrency tool when they simplify
> async orchestration, not as a reason to force every backend path into async style.

---

## 8. 1-Minute Answer

> Kotlin does not replace backend engineering judgment; it changes how clearly and safely I
> express it. The biggest practical gains over Java are null safety in the type system,
> lighter DTO and state modeling with data classes, and very strong sealed-class modeling
> for validation or payment outcomes. In Spring, Kotlin works well, but I still remember
> the framework rules like `plugin.spring`, `plugin.jpa`, `@field:` validation targets,
> and Jackson Kotlin support. I also treat coroutines pragmatically: they are excellent
> when they simplify coordination of independent I/O work, but I do not use them just to
> look modern. My framing is that I am a JVM backend engineer extending strong Java and
> Spring instincts into Kotlin, not starting over in a new ecosystem.

---

## 9. Further Reading

- Kotlin null safety:
  https://kotlinlang.org/docs/null-safety.html
- Kotlin coroutines guide:
  https://kotlinlang.org/docs/coroutines-guide.html
- Spring Boot Kotlin support:
  https://docs.spring.io/spring-boot/reference/features/kotlin.html
