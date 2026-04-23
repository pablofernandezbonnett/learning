# 10. Spring Data: Repositories, Query Shape, and Auditing

> Primary fit: `Shared core`


Spring Data gives you a similar repository programming model across JPA (`Java Persistence API`), MongoDB, and Redis, but the underlying storage behavior is still very different.

This note is not about memorizing advanced names.
It is about knowing when the repository abstraction is enough and when you need something more explicit.

Important reminder:

> The programming model may look similar, but JPA, Mongo, and Redis do not
> behave like interchangeable backends. The repository abstraction is shared.
> The persistence model is not.

### 1. Interface-based Projections

Projection means:

- do not fetch the whole row or entity if you only need a few fields
- ask the database for the smaller shape you actually need

Don't fetch the whole Entity if you only need the `name` and `email`.
```kotlin
interface UserSummary {
    fun getName(): String
    fun getEmail(): String
}

// In Repository
fun findByRole(role: UserRole): List<UserSummary>
// Result: Spring generates a SQL that SELECTs only 'name' and 'email'.
```

Kotlin note: Spring Data's projection proxies work with Kotlin `fun` declarations
(not properties). Use `fun getName()` not `val name: String` in projection interfaces.

### 2. Derived Query Methods

Spring parses the method name to generate queries.
- `findFirstByOrderByCreatedAtDesc()`: Perfect for "latest" record.
- `existsByEmail(email: String)`: More efficient than `findByEmail() != null`.
- Return `T?` (nullable) instead of `Optional<T>` — Spring Data handles this natively in Kotlin.

Practical rule:

- use derived queries for straightforward lookup patterns
- stop when the method name starts becoming a sentence

### 3. Entity Auditing

Auditing means automatically recording metadata such as:

- when the row was created
- when it was last changed
- who changed it

Don't manually set `updatedAt` or `createdBy`. Use `@EnableJpaAuditing` and:
- `@CreatedDate`, `@LastModifiedDate` (handled automatically).
- `@CreatedBy`, `@LastModifiedBy` (integrates with Spring Security via `AuditorAware<String>`).

```kotlin
// In your @Configuration or @SpringBootApplication class:
@Bean
fun auditorProvider(): AuditorAware<String> = AuditorAware {
    // Replace with: SecurityContextHolder.getContext().authentication?.name
    Optional.of("system")
}
```

### 4. Specifications For Dynamic Filters

When a search screen has many optional filters, a single derived query method stops being practical.
That is where `Specification` helps.

It means:

- build the query piece by piece
- include only the conditions that are actually present
- avoid one giant hard-coded query method name

```kotlin
object UserSpecifications {
    fun isActive(): Specification<User> =
        Specification { root, _, cb -> cb.isTrue(root.get("active")) }

    fun hasRole(role: UserRole?): Specification<User> =
        Specification { root, _, cb -> role?.let { cb.equal(root.get<UserRole>("role"), it) } }
}

// Usage: null-safe composition
val spec = Specification.where(UserSpecifications.isActive())
    .and(UserSpecifications.hasRole(role))   // ignored if role is null
```

See `src/main/kotlin/com/learning/mastery/data/` for full examples.

### 5. Know When To Leave the Repository Abstraction

Repositories are productive, but they are not always the right final tool.

- **JPA / Postgres**:
  - stay with repositories for normal aggregate CRUD (create, read, update, delete) and conventional filters
  - move to projections, JPQL (Java Persistence Query Language), native SQL, or `JdbcTemplate` when query shape and performance matter more than repository convenience
- **MongoDB**:
  - repositories are fine for basic document access
  - use `MongoTemplate` for aggregations, explicit updates, and more complex document operations
- **Redis**:
  - repository-style access is not the main value
  - `RedisTemplate` or Spring Cache is often the better fit for counters, sessions, rate limits, and cache-aside patterns

Good senior rule:

> Use the repository abstraction when it keeps the code simple. Step outside it
> as soon as the storage model or query shape starts demanding more explicit
> control.
