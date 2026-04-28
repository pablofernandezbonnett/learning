# Kotlin Refresh for Java and Spring Engineers

Use this folder as a Kotlin bridge for someone who already knows Java, Spring,
and backend service design.

## Recommended Order

1. [01-kotlin-for-backend-vs-java.md](./01-kotlin-for-backend-vs-java.md): translate Java and Spring instincts into Kotlin
2. [../../labs/kotlin-basics/README.md](../../labs/kotlin-basics/README.md): language basics, null safety, collections, and coroutines
3. [02-kotlin-backend-idioms-and-gotchas.md](./02-kotlin-backend-idioms-and-gotchas.md): value classes, `Result`, interop, and avoiding clever Kotlin

## Working Loop

1. read the backend bridge doc first
2. run the small labs selectively
3. keep mapping Kotlin features back to Java and Spring use cases
4. stop once the new syntax stops feeling like the main challenge

## Core Rule

- Kotlin is most valuable when it improves correctness and readability
- null safety and coroutines are the biggest mindset shifts
- Kotlin backend code should stay simple; clever DSL-style code is rarely the goal
- value classes and scope functions are useful, but only with restraint
