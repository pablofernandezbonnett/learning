# Kotlin Refresh for Java and Spring Engineers

Use this folder as a Kotlin bridge for someone who already knows Java, Spring,
and backend service design.

The goal is not to relearn backend fundamentals from zero.
The goal is to translate strong Java and Spring instincts into idiomatic Kotlin
without overcomplicating the code.

Focus:

- null safety and type-system differences that change everyday backend code
- coroutines, meaning lightweight asynchronous code that can replace some thread-heavy flows
- simple Kotlin idioms that improve clarity without turning the codebase into a DSL

Working style:

- keep mapping Kotlin features back to familiar Java and Spring use cases
- prefer the smallest useful Kotlin feature over clever language tricks
- treat readability and correctness as more important than idiomatic flair

## Recommended Order

1. [01-kotlin-for-backend-vs-java.md](./01-kotlin-for-backend-vs-java.md): translate Java and Spring instincts into Kotlin
2. [../../labs/kotlin-basics/README.md](../../labs/kotlin-basics/README.md): language basics, null safety, collections, and coroutines
3. [02-kotlin-backend-idioms-and-gotchas.md](./02-kotlin-backend-idioms-and-gotchas.md): value classes, `Result` as a success-or-failure wrapper, Java interop, and avoiding clever Kotlin

## Working Loop

1. read the backend bridge doc first
2. run the small labs selectively
3. keep mapping Kotlin features back to Java and Spring use cases
4. stop once the new syntax stops feeling like the main challenge

If a Kotlin feature feels clever but does not improve clarity or correctness,
this repo treats that as a warning sign rather than a style goal.

## Core Rule

- Kotlin is most valuable when it improves correctness and readability
- null safety and coroutines are the biggest mindset shifts
- Kotlin backend code should stay simple; clever DSL-style code is rarely the goal
- value classes and scope functions are useful, but only with restraint
