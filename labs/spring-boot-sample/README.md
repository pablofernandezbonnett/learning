# Spring Boot Sample

This lab is a small Kotlin + Spring Boot sample used as a companion for the
Spring Boot topic.

Why this lab matters:

- it turns several Spring notes into one runnable slice
- it is small enough to inspect without losing the boundaries
- it helps connect container wiring, request flow, JPA, caching, and coroutines

It includes examples for:

- bean lifecycle and scopes
- configuration and conditional beans
- web and exception handling
- JPA and repository basics
- caching
- coroutine-oriented service examples

## How To Run

From this folder:

```bash
gradle bootRun
gradle test
```

This lab currently ships with Gradle build files but not the Gradle wrapper.
If you add the wrapper later, the equivalent commands become:

```bash
./gradlew bootRun
./gradlew test
```

The sample uses H2 for local runs and tests.

Use it after:

- [../../topics/spring-boot/README.md](../../topics/spring-boot/README.md)
- `01-spring-boot-fundamentals.md`
- `02-exception-handling.md`
- `03-validation.md`
- `04-jpa-hibernate-performance-traps.md`
- `05-ioc-deep-dive.md`
- `15-kotlin-with-spring.md`

Strong default:

- use this sample to inspect request flow, bean wiring, and framework boundaries
- do not treat it as a production template or as the only source of truth for the topic
