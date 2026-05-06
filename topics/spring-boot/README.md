# Spring Boot for Backend Engineers

Use this folder for practical Spring and Spring Boot refresh.

Why this folder matters:

- Spring knowledge goes stale fastest where runtime behavior is hidden behind
  convenience
- many real issues come from misunderstanding proxies, persistence behavior,
  configuration, or boundary placement rather than from missing syntax
- this folder keeps the refresh anchored in production-minded Spring behavior

Focus:

- container and proxy behavior
- web and API boundaries
- transactions and persistence
- error handling
- JPA and Hibernate tradeoffs
- platform wiring, configuration, and Spring cloud-facing concerns

Working style:

- explain the runtime meaning behind Spring annotations instead of stopping at syntax
- keep bean lifecycle, proxy behavior, transactions, and persistence connected as one mental model
- connect framework features back to correctness, latency, and production behavior

Smallest mental model:

- Spring Framework owns the container and runtime behavior
- Spring Boot adds defaults, packaging, and wiring conventions
- the useful refresh is about boundaries: web, transaction, proxy, data, and
  configuration boundaries

## Recommended Order

1. [01-spring-boot-fast-review.md](./01-spring-boot-fast-review.md): compact map of the main Spring Boot areas
2. [02-exception-handling.md](./02-exception-handling.md): consistent error handling with `@RestControllerAdvice` and `ProblemDetail`
3. [03-transactions-and-isolation.md](./03-transactions-and-isolation.md): local transaction boundaries, isolation, and propagation
4. [04-jpa-hibernate-performance-traps.md](./04-jpa-hibernate-performance-traps.md): ORM behavior, query shape, and common production traps

## Refresh

- [01-spring-boot-fast-review.md](./01-spring-boot-fast-review.md)
- [02-exception-handling.md](./02-exception-handling.md)
- [03-transactions-and-isolation.md](./03-transactions-and-isolation.md)
- [04-jpa-hibernate-performance-traps.md](./04-jpa-hibernate-performance-traps.md)

## Required

- [05-ioc-deep-dive.md](./05-ioc-deep-dive.md)
- [06-bean-lifecycle.md](./06-bean-lifecycle.md)
- [07-proxies-and-aop.md](./07-proxies-and-aop.md)
- [08-auto-configuration.md](./08-auto-configuration.md)
- [09-conditional-beans.md](./09-conditional-beans.md)
- [10-profiles.md](./10-profiles.md)
- [11-web-annotations.md](./11-web-annotations.md)
- [12-caching-and-redis.md](./12-caching-and-redis.md)
- [13-spring-data.md](./13-spring-data.md)
- [19-flyway-and-schema-migrations.md](./19-flyway-and-schema-migrations.md)

## Growth

- [14-datastore-choice-postgres-mongo-redis.md](./14-datastore-choice-postgres-mongo-redis.md)
- [15-kotlin-spring-idioms.md](./15-kotlin-spring-idioms.md)
- [16-appsec-authz-lab.md](./16-appsec-authz-lab.md)
- [17-webhook-idempotency-lab.md](./17-webhook-idempotency-lab.md)
- [18-threat-modeling-lab.md](./18-threat-modeling-lab.md)
- [20-spring-cloud-and-service-integration.md](./20-spring-cloud-and-service-integration.md)

## Companion Lab

Use [../../labs/spring-boot-sample/README.md](../../labs/spring-boot-sample/README.md)
for a small Kotlin + Spring Boot sample that covers web, JPA, config, caching,
and coroutine-oriented examples.

## Core Rule

- keep the mental model focused on runtime behavior, not annotations alone
- remember that proxies explain many Spring surprises
- treat JPA convenience and SQL reality as two different layers
