# Kotlin With Spring Boot

Kotlin makes nullability and data shapes explicit, but Spring proxies,
persistence, transactions, and JSON still follow their own runtime rules.
Use this note when those boundaries meet.

## Proxies and Open Classes

Kotlin classes and members are final by default. Spring features that need a
class-based proxy cannot subclass a final class. The `kotlin("plugin.spring")`
compiler plugin opens classes and members annotated with common Spring
stereotypes and related annotations, including meta-annotated stereotypes
such as `@Service`.

```kotlin
plugins {
    kotlin("plugin.spring")
}
```

This is a useful default for a Kotlin Spring application. A class does not
need a proxy merely because Spring constructs it, and interface-based proxies
have different constraints. Check the actual proxy boundary when
`@Transactional`, caching, or async behavior is missing. Calls from one method
to another on the same object can bypass proxy advice.

## JPA Entities and Constructors

JPA needs a suitable no-argument constructor for entity creation. The
`kotlin("plugin.jpa")` compiler plugin generates one for `@Entity`,
`@Embeddable`, and `@MappedSuperclass` classes. Hibernate proxying and lazy
loading also depend on whether classes and members can be subclassed; check
the plugin behavior for the Kotlin and persistence versions in the project.

Use entity classes for persistence state and data classes for ordinary DTOs.
Avoid assuming that a data class's generated `equals`, `hashCode`, and
`toString` are safe for a mutable entity or lazy association. Choose property
mutability from the mapping and domain rule; `var` is common, but “every entity
field must be `var`” is too broad.

## Validation and JSON Boundaries

A Kotlin constructor property can produce a parameter, field, and getter. Put
a validation annotation on the element the validator actually inspects. For
field-based Bean Validation, make that target explicit:

```kotlin
data class CreateProductRequest(
    @field:NotBlank val name: String,
    @field:Min(0) val initialStock: Int,
)
```

Do not assume an unqualified annotation lands on the field; target selection
depends on the annotation and Kotlin compiler behavior. Test validation
through the HTTP boundary, including a missing field and a wrong type.

Jackson's Kotlin module supports Kotlin constructor and nullability metadata
for common DTO deserialization. Add the module when using Jackson with Kotlin
and test malformed JSON rather than assuming every null or absent value is
rejected in the same way.

## Coroutines and Transactions

Spring MVC and WebFlux can accept suspending controller functions when the
coroutine integration is configured. A `suspend` declaration makes suspension
possible; a blocking JPA or JDBC call remains blocking.

The transaction choice matters more than the syntax:

- **Imperative JPA/JDBC:** `PlatformTransactionManager` transactions are
  normally bound to the current thread. Keep blocking persistence work inside
  a proxied transactional service method. Do not assume a coroutine dispatcher
  switch or a new coroutine carries that transaction.
- **Reactive data access:** `ReactiveTransactionManager` uses Reactor context.
  For suspending work, Spring documents `TransactionalOperator.executeAndAwait`
  as a transaction boundary; participating data access must use the same
  reactive context.

```kotlin
// Sketch for coroutine-aware reactive data access, not for JPA:
suspend fun updateBoth(operator: TransactionalOperator) =
    operator.executeAndAwait {
        updateOrder()
        updateInventory()
    }
```

The calls in that sketch must participate in the reactive transaction; an
external provider call does not become part of it. See
[transactions](./03-transactions-and-isolation.md) for the local database
boundary and
[distributed workflows](../architecture/03-distributed-transactions-and-events.md)
for remote effects.

## Coroutines or Virtual Threads?

Coroutines help compose suspension and cancellation when the client and data
APIs support them. Virtual threads let blocking-style Java code wait without
occupying a platform thread for the whole wait in supported cases. Neither
removes connection-pool limits, authorization checks, or the need to bound
concurrency. Choose from the stack's actual I/O model and measure the result.

## Further Reading

- [Spring Kotlin coroutine support](https://docs.spring.io/spring-framework/reference/languages/kotlin/coroutines.html): controllers and reactive transactions.
- [Spring transaction context](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-decl-explained.html): thread-bound and reactive boundaries.
- [Kotlin Spring compiler plugin](https://kotlinlang.org/docs/all-open-plugin.html#spring-support): class opening for proxies.
- [Kotlin JPA compiler plugin](https://kotlinlang.org/docs/no-arg-plugin.html#jpa-support): generated no-argument constructors.
- [Kotlin annotation targets](https://kotlinlang.org/docs/annotations.html#annotation-use-site-targets): field, getter, and parameter placement.
