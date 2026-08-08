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
- runtime readiness, health, and observability basics for real services
- service baseline discipline around security, actuator exposure, and testing
- batch execution, restart, reconciliation, and failure handling when the
  product has imports, settlement, reporting, or scheduled processing

Working style:

- explain the runtime meaning behind Spring annotations instead of stopping at syntax
- keep bean lifecycle, proxy behavior, transactions, and persistence connected as one mental model
- connect framework features back to correctness, latency, and production behavior

Smallest mental model:

- Spring Framework owns the container and runtime behavior
- Spring Boot adds defaults, packaging, and wiring conventions
- the useful refresh is about boundaries: web, transaction, proxy, data, and
  configuration boundaries
- a production-ready Spring service also needs health, metrics, and trace-aware runtime thinking

## Recommended Order

1. [01-spring-boot-fast-review.md](./01-spring-boot-fast-review.md): compact map of the main Spring Boot areas
2. [02-exception-handling.md](./02-exception-handling.md): consistent error handling with `@RestControllerAdvice` and `ProblemDetail`
3. [03-transactions-and-isolation.md](./03-transactions-and-isolation.md): local transaction boundaries, isolation, and propagation
4. [04-jpa-hibernate-performance-traps.md](./04-jpa-hibernate-performance-traps.md): ORM behavior, query shape, and common production traps
5. [24-spring-security-actuator-and-testing-baseline.md](./24-spring-security-actuator-and-testing-baseline.md): minimum credible service posture for route security, actuator exposure, and verification

## If You Want To Get Productive Fast

For the shortest "I can work in a Spring Boot service again" loop:

1. [01-spring-boot-fast-review.md](./01-spring-boot-fast-review.md)
2. [11-web-annotations.md](./11-web-annotations.md)
3. [02-exception-handling.md](./02-exception-handling.md)
4. [03-transactions-and-isolation.md](./03-transactions-and-isolation.md)
5. [04-jpa-hibernate-performance-traps.md](./04-jpa-hibernate-performance-traps.md)
6. [24-spring-security-actuator-and-testing-baseline.md](./24-spring-security-actuator-and-testing-baseline.md)
7. [12-caching-and-redis.md](./12-caching-and-redis.md)
8. [16-appsec-authz-lab.md](./16-appsec-authz-lab.md)
9. [19-flyway-and-schema-migrations.md](./19-flyway-and-schema-migrations.md)
10. [20-spring-cloud-and-service-integration.md](./20-spring-cloud-and-service-integration.md)

That path covers the questions that most often decide whether a Spring service
is merely running or actually trustworthy:

- how HTTP boundaries are shaped
- how failures are returned
- how data changes stay correct
- how persistence behaves under load
- how route policy, actuator exposure, and tests hold the service boundary together
- what may be cached safely
- how authz is enforced
- how schema change stays safe
- how remote dependencies and runtime concerns are handled

## Refresh

- [01-spring-boot-fast-review.md](./01-spring-boot-fast-review.md)
- [02-exception-handling.md](./02-exception-handling.md)
- [03-transactions-and-isolation.md](./03-transactions-and-isolation.md)
- [04-jpa-hibernate-performance-traps.md](./04-jpa-hibernate-performance-traps.md)
- [24-spring-security-actuator-and-testing-baseline.md](./24-spring-security-actuator-and-testing-baseline.md)

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
- [24-spring-security-actuator-and-testing-baseline.md](./24-spring-security-actuator-and-testing-baseline.md)

## Growth

- [14-datastore-choice-postgres-mongo-redis.md](./14-datastore-choice-postgres-mongo-redis.md)
- [15-kotlin-spring-idioms.md](./15-kotlin-spring-idioms.md)
- [16-appsec-authz-lab.md](./16-appsec-authz-lab.md)
- [17-webhook-idempotency-lab.md](./17-webhook-idempotency-lab.md)
- [18-threat-modeling-lab.md](./18-threat-modeling-lab.md)
- [20-spring-cloud-and-service-integration.md](./20-spring-cloud-and-service-integration.md)
- [21-practical-ddd-in-spring.md](./21-practical-ddd-in-spring.md)
- [22-practical-cqrs-and-read-models.md](./22-practical-cqrs-and-read-models.md)
- [23-practical-modular-monolith-in-spring.md](./23-practical-modular-monolith-in-spring.md)
- [25-spring-batch-production-jobs.md](./25-spring-batch-production-jobs.md):
  bounded imports, settlement, restart, idempotency, observability, and safe
  failure handling for production jobs

## Companion Lab

Use [../../labs/spring-boot-sample/README.md](../../labs/spring-boot-sample/README.md)
for a small Kotlin-first Spring Boot sample that covers web, JPA, config,
caching, and service-boundary basics.

Keep the topic notes themselves friendly to Java-first comparison when a Spring
concept benefits from seeing both languages.

## Core Rule

- keep the mental model focused on runtime behavior, not annotations alone
- remember that proxies explain many Spring surprises
- treat JPA convenience and SQL reality as two different layers
- treat health, metrics, and request tracing as part of the service baseline, not as afterthoughts
