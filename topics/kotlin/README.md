# Kotlin Refresh for Java and Spring Engineers

Use this folder as a Kotlin bridge for someone who already knows Java, Spring,
and backend service design.

The goal is not to relearn backend fundamentals from zero.
The goal is to translate strong Java and Spring instincts into idiomatic Kotlin
without overcomplicating the code.

Focus:

- null safety and type-system differences that change everyday backend code
- coroutines, meaning lightweight asynchronous code that can replace some thread-heavy flows when the stack and use case justify them
- simple Kotlin idioms that improve clarity without turning the codebase into a DSL

Working style:

- keep mapping Kotlin features back to familiar Java and Spring use cases
- prefer the smallest useful Kotlin feature over clever language tricks
- treat readability and correctness as more important than idiomatic flair

## Stable Practical Baseline

If your goal is to become productive in Kotlin backend work quickly, keep this
baseline warm first:

- null safety in signatures and service logic
- data classes for DTOs and simple state
- sealed classes plus `when` for closed result or workflow states
- Java and Spring interop details such as validation targets, Jackson support, and proxy-aware setup
- coroutines as an optional concurrency tool, not a requirement to write normal Kotlin services well

That baseline already covers most of the day-to-day value Kotlin brings to a
Spring or JVM backend codebase.

## Recommended Order

1. [01-kotlin-for-backend-vs-java.md](./01-kotlin-for-backend-vs-java.md): translate Java and Spring instincts into Kotlin
2. [02-kotlin-backend-idioms-and-gotchas.md](./02-kotlin-backend-idioms-and-gotchas.md): value classes, `Result` as a success-or-failure wrapper, Java interop, and avoiding clever Kotlin
3. [03-kotlin-coroutines-for-backend.md](./03-kotlin-coroutines-for-backend.md): structured async orchestration, dispatchers, cancellation, and backend coroutine judgment
4. [../../labs/kotlin-basics/README.md](../../labs/kotlin-basics/README.md): language basics, null safety, collections, and coroutines

## If You Want To Get Productive Fast

1. read [01-kotlin-for-backend-vs-java.md](./01-kotlin-for-backend-vs-java.md)
2. read [02-kotlin-backend-idioms-and-gotchas.md](./02-kotlin-backend-idioms-and-gotchas.md)
3. go to [../../topics/spring-boot/15-kotlin-spring-idioms.md](../../topics/spring-boot/15-kotlin-spring-idioms.md) for Spring-specific usage
4. read [03-kotlin-coroutines-for-backend.md](./03-kotlin-coroutines-for-backend.md) only when you need async orchestration, suspension, or coroutine boundaries

This keeps the initial Kotlin path focused on writing normal backend code well
before you optimize the concurrency model.

## Working Loop

1. read the backend bridge doc first
2. run the small labs selectively
3. keep mapping Kotlin features back to Java and Spring use cases
4. stop once the new syntax stops feeling like the main challenge

If a Kotlin feature feels clever but does not improve clarity or correctness,
this repo treats that as a warning sign rather than a style goal.

## Coroutines Companion

Use [03-kotlin-coroutines-for-backend.md](./03-kotlin-coroutines-for-backend.md)
as the main note.

Do not treat it as mandatory before writing ordinary Kotlin services.
Many teams get clear value from Kotlin first through null safety, cleaner DTOs,
sealed-state modeling, and better Java interoperability.

Use the lab after that when you want runnable examples:

- [../../labs/kotlin-basics/README.md](../../labs/kotlin-basics/README.md)
- [../../topics/spring-boot/15-kotlin-spring-idioms.md](../../topics/spring-boot/15-kotlin-spring-idioms.md) for Spring-specific coroutine boundaries

## Core Rule

- Kotlin is most valuable when it improves correctness and readability
- null safety is the first mindset shift; coroutines are the second, optional one
- Kotlin backend code should stay simple; clever DSL-style code is rarely the goal
- value classes and scope functions are useful, but only with restraint
