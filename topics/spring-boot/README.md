# Spring Boot

Use this block to understand what a Spring service does at runtime: how beans
are created, where proxies apply behavior, how HTTP and data boundaries work,
and how configuration and operations affect the running service.

Start with the [fast review](./01-spring-boot-fast-review.md) if you need to
locate a problem. Open the relevant topic below for the mechanism and examples.

## Container and Configuration

- [IoC and dependency injection](./05-ioc-deep-dive.md): how Spring builds the object graph.
- [Bean lifecycle](./06-bean-lifecycle.md): initialization, shutdown, and resource ownership.
- [Proxies and AOP](./07-proxies-and-aop.md): when a method call receives framework behavior.
- [Auto-configuration](./08-auto-configuration.md): how Boot supplies defaults.
- [Conditional beans](./09-conditional-beans.md): why a bean appears in one setup and not another.
- [Profiles](./10-profiles.md): environment-specific configuration and its limits.

## HTTP and Security Boundaries

- [Web annotations](./11-web-annotations.md): request mapping, validation, and response behavior.
- [Exception handling](./02-exception-handling.md): consistent HTTP errors.
- [Security, Actuator, and tests](./24-spring-security-actuator-and-testing-baseline.md): route policy, exposed endpoints, and service checks.

## Data and Background Work

- [Spring Data](./13-spring-data.md): repositories, projections, filters, and auditing.
- [Transactions](./03-transactions-and-isolation.md): local atomicity and proxy boundaries.
- [JPA and Hibernate](./04-jpa-hibernate-performance-traps.md): fetch plans and persistence-context costs.
- [Caching and Redis](./12-caching-and-redis.md): cache behavior and invalidation.
- [Flyway and schema migrations](./19-flyway-and-schema-migrations.md): safe database change.
- [Spring Batch](./25-spring-batch-production-jobs.md): bounded jobs, restart, and failure handling.

## Integration and Application Structure

- [Spring Cloud and service integration](./20-spring-cloud-and-service-integration.md): framework choices around remote services.
- [DDD in Spring](./21-practical-ddd-in-spring.md): domain rules and service boundaries.
- [CQRS and read models](./22-practical-cqrs-and-read-models.md): separate reads when their shape demands it.
- [Modular monoliths](./23-practical-modular-monolith-in-spring.md): module ownership inside one deployment.
- [Kotlin and Spring](./15-kotlin-spring-idioms.md): proxy, nullability, and Java interoperability.

## Practice

- [Authorization lab](./16-appsec-authz-lab.md): enforce business permissions.
- [Webhook idempotency lab](./17-webhook-idempotency-lab.md): handle repeated delivery.
- [Threat modeling lab](./18-threat-modeling-lab.md): identify and test security assumptions.
- [Spring Boot service slice lab](../../labs/spring-boot-sample/README.md): build a small request flow across web, JPA, configuration, and caching.

For storage selection, use [databases](../databases/README.md). For reliability
and delivery outside the framework, use [operations](../devops/README.md) and
[reliability](../sre/README.md).
