# 12. Kotlin + Spring Boot Idioms

> Primary fit: `Supporting reference for Kotlin-based Spring work`


Topics that don't appear in docs 01–11 but are critical when using Kotlin with Spring Boot 3.
This is not the main Spring path, but it is a high-value supporting
reference when the stack is Kotlin-heavy.

---

## 1. Why `plugin.spring` Is Required

All Kotlin classes are `final` by default. Spring's CGLIB proxy mechanism works by **subclassing** your beans at runtime. If a class is `final`, CGLIB cannot subclass it, and Spring throws:

```
Cannot subclass final class com.learning.mastery.web.ProductController
```

The `kotlin("plugin.spring")` Gradle plugin auto-opens classes annotated with:
- `@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`
- `@Configuration`
- `@Transactional`, `@Cacheable`, `@Async`

**Without the plugin**, you would have to manually mark every Spring-managed class with `open`:

```kotlin
// Without plugin.spring — verbose and error-prone
open class ProductController(...) { ... }

// With plugin.spring — Kotlin compiler opens it for you
class ProductController(...) { ... }   // ← looks final, but plugin.spring opens it
```

---

## 2. `plugin.jpa` for Entity No-Arg Constructors

JPA (Hibernate) requires a **no-arg constructor** to instantiate entities via reflection. Kotlin data classes don't have one by default unless all properties have defaults.

The `kotlin("plugin.jpa")` plugin generates a synthetic no-arg constructor for classes annotated with:
- `@Entity`
- `@MappedSuperclass`
- `@Embeddable`

```kotlin
// Without plugin.jpa:
// Hibernate throws: "No default constructor for entity: User"

@Entity
class User {
    @Id var id: Long? = null
    var email: String = ""
    var name: String = ""
    // plugin.jpa generates: User() { } behind the scenes
}
```

Use `var` properties (not `val`) on entities — Hibernate needs to set them after calling the no-arg constructor.

---

## 3. Data Classes as DTOs — `@field:` Prefix

Kotlin data classes replace Java records for DTOs. However, Bean Validation annotations must use the `@field:` use-site target to reach the backing field:

```kotlin
// WRONG — annotation is on the constructor parameter, Bean Validation ignores it
data class CreateProductRequest(
    @NotBlank val name: String,          // ← does nothing
)

// CORRECT — @field: targets the backing field
data class CreateProductRequest(
    @field:NotBlank val name: String,    // ← Bean Validation picks this up
    @field:DecimalMin("1") val priceYen: BigDecimal,
    @field:Min(0) val initialStock: Int,
)
```

Why: Kotlin generates a field, a constructor parameter, and a getter for each `val`. `@NotBlank` without `@field:` defaults to the constructor parameter, which Bean Validation doesn't inspect.

---

## 4. Sealed Classes for Domain State vs Java Sealed Classes

Kotlin sealed classes are more powerful than Java sealed classes (Java 21+) for domain modeling:

```kotlin
sealed class PaymentResult {
    data class Success(val transactionId: String) : PaymentResult()
    data class Declined(val reason: String) : PaymentResult()
    data class Error(val cause: Throwable) : PaymentResult()
}

// Exhaustive when — compiler enforces all branches
fun handlePayment(result: PaymentResult): String = when (result) {
    is PaymentResult.Success  -> "Charged: ${result.transactionId}"
    is PaymentResult.Declined -> "Declined: ${result.reason}"
    is PaymentResult.Error    -> "Error: ${result.cause.message}"
    // No else needed — compiler verifies all cases are covered
}
```

Compare to throwing exceptions from a service method — sealed classes make the error path explicit in the type system and force callers to handle all cases.

---

## 5. Coroutines with `@Transactional`

Spring 6+ supports `@Transactional` on `suspend` functions when `spring-tx` and `kotlinx-coroutines-reactor` are on the classpath:

```kotlin
@Service
class OrderService(private val repo: OrderRepository) {

    @Transactional
    suspend fun placeOrder(order: Order) {
        repo.save(order)                    // both in the same transaction
        repo.decrementStock(order.items)    // rolled back if this throws
    }
}
```

**What works:**
- `@Transactional` on `suspend` functions in `@Service` beans.
- Rollback on exception still works.

**What doesn't work:**
- Switching dispatchers (`withContext(Dispatchers.IO)`) inside a `@Transactional suspend` function. The transaction is bound to a coroutine context, not a thread. Switching to `Dispatchers.IO` may break the transaction association.
- `@Async` + coroutines together — use coroutines instead of `@Async` when you need non-blocking behavior.

**Safe pattern for blocking DB calls:**

```kotlin
// Use a transactional wrapper, then switch to IO inside a non-transactional scope
@Transactional
suspend fun findAndUpdateUser(userId: Long): User {
    val user = withContext(Dispatchers.IO) { userRepository.findById(userId) }
        ?: throw UserNotFoundException(userId)
    user.lastActive = Instant.now()
    // withContext(Dispatchers.IO) is safe HERE only if the JPA session is thread-bound.
    // For real safety, call a separate @Transactional method from a coroutineScope.
    return user
}
```

---

## 6. `suspend` Functions in Controllers

Spring MVC supports `suspend` controller functions via the `kotlinx-coroutines-reactor` bridge:

```kotlin
@RestController
@RequestMapping("/api/products")
class ProductController(private val aggregator: ProductAggregatorService) {

    // Spring MVC wraps this suspend call in a Mono and handles it correctly
    @GetMapping("/{id}/bundle")
    suspend fun getBundle(@PathVariable id: String): ProductBundle =
        aggregator.getBundleForProduct(id)
}
```

The bridge converts the `suspend` function into a `Mono<T>` transparently. No additional configuration needed beyond having `kotlinx-coroutines-reactor` on the classpath.

---

## 7. Coroutines vs Virtual Threads

Do not explain these as if one simply replaces the other.

They solve different problems at different layers:

- **Kotlin coroutines**: language-level concurrency model with `suspend`,
  structured concurrency, cancellation propagation, and coroutine-aware APIs
- **Java virtual threads**: JVM runtime model that makes blocking code cheaper
  to schedule, especially in Java 21+ server applications

Why coroutines are strong:

- mature and production-proven in Kotlin systems
- natural when your service layer, clients, and team already work in Kotlin
- better fit when you want structured concurrency for fan-out/fan-in flows
- expressive for async orchestration without dropping into callback style

Why virtual threads are strong:

- very practical for existing blocking Spring MVC, JDBC, and Java-heavy stacks
- lower migration cost when you want to keep imperative code style
- useful when most dependencies are still blocking and you do not want a larger async model shift

Where people get the comparison wrong:

- virtual threads do not magically turn blocking I/O into non-blocking I/O
- coroutines are not just "lighter threads"; they carry a different programming model
- JPA, JDBC, and many client libraries still have their own runtime limits no matter which model you use

Simple decision rule:

- Kotlin-heavy service with real coroutine usage and structured concurrency needs -> coroutines are a strong default
- Java-heavy or blocking Spring service where you want simpler adoption on Java 21+ -> virtual threads are often the easier win
- mixed team or unclear benefit -> do not force either model as ideology

Interview-safe answer:

> In Kotlin services, coroutines are a mature and powerful option because they
> give me structured concurrency and clear async composition. Virtual threads
> are also strong, but they solve a different problem: they make blocking code
> cheaper to run on the JVM. I choose based on the stack and libraries, not by
> treating them as interchangeable.

---

## 8. Kotlin-Idiomatic Null Handling in Spring

Avoid Java's `@Nullable`/`@NonNull` — Kotlin's type system handles this:

```kotlin
// Java Spring Data — Optional<User>
Optional<User> findByEmail(String email);

// Kotlin Spring Data — nullable return type
fun findByEmail(email: String): User?   // null = not found, no Optional wrapper needed

// Usage: Kotlin's Elvis operator instead of Optional.orElseThrow()
val user = userRepository.findByEmail(email) ?: throw UserNotFoundException(email)
```

Configure strict null safety for Spring's own annotations in `build.gradle.kts`:

```kotlin
tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")  // Spring's @Nullable → Kotlin nullable
    }
}
```

---

## 9. Jackson + Kotlin: Why `jackson-module-kotlin` Is Required

Jackson, by default, requires a no-arg constructor and setters to deserialize JSON into an object. Kotlin data classes have neither — they use a primary constructor with `val` properties.

`jackson-module-kotlin` adds:
1. Support for primary constructor deserialization (uses constructor parameters, not setters).
2. Handling of Kotlin's `null` safety — throws on missing non-nullable fields.
3. Correct serialization of `object` singletons and `sealed` classes.

```kotlin
// Without jackson-module-kotlin:
// InvalidDefinitionException: No suitable constructor found for CreateProductRequest

// With jackson-module-kotlin (declared in build.gradle.kts):
// Works correctly — Jackson uses the primary constructor
data class CreateProductRequest(
    val name: String,
    val priceYen: BigDecimal,
)
```

Spring Boot auto-configures `jackson-module-kotlin` when it detects the dependency. No manual `ObjectMapper` configuration needed.

---

## Quick Reference: Java → Kotlin Conversion Table

| Java pattern | Kotlin equivalent |
|---|---|
| `record Dto(String name) {}` | `data class Dto(val name: String)` |
| `@NotBlank String name` | `@field:NotBlank val name: String` |
| `Optional<User>` return | `User?` return |
| `List.of("a", "b")` | `listOf("a", "b")` |
| `Map.of("k", "v")` | `mapOf("k" to "v")` |
| `instanceof` check | `is` + smart cast |
| `static` method | `companion object { fun ... }` |
| String concatenation `"Hello " + name` | String template `"Hello $name"` |
| `class Foo extends Bar implements Baz` | `class Foo : Bar(), Baz` |
| `@ConditionalOnClass(Foo.class)` | `@ConditionalOnClass(Foo::class)` |
| `@ConditionalOnMissingBean(X.class)` | `@ConditionalOnMissingBean(X::class)` |
| `getters/setters` | Kotlin properties (auto-generated) |
