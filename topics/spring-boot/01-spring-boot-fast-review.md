# Spring Boot Fast Review

Use this page when you need to reopen a Spring service quickly. Spring manages
objects and their lifecycle; Spring Boot supplies defaults and configuration
for running them. Most surprising behavior appears at a boundary: an HTTP
request, a proxied method call, a database transaction, a remote dependency,
or the runtime configuration.

## Follow One Request

Pick a representative endpoint and trace it from HTTP to the database or
remote service. Ask:

1. **Entry:** What request body, validation, authorization, and error response
   does the API promise?
2. **Call path:** Which Spring bean calls which other bean? Does a proxy need
   to intercept a method for transactions, caching, or security to apply?
3. **State change:** Which database write must be atomic? What happens on a
   retry or concurrent request?
4. **Read cost:** How many SQL statements and rows does the whole request
   produce, including lazy loads?
5. **Dependency:** What happens if a remote call is slow, fails, or succeeds
   while a later local step fails?
6. **Runtime:** Which configuration is active, and what health, metrics, logs,
   and traces show whether this path works?

This exercise exposes the boundary that needs attention before you choose a
Spring feature or tune an annotation.

## Open the Guide for the Problem

- **A bean or annotation behaves unexpectedly:** check creation, lifecycle,
  and the proxy call path. Open [IoC](./05-ioc-deep-dive.md),
  [bean lifecycle](./06-bean-lifecycle.md), and [proxies](./07-proxies-and-aop.md).
- **Behavior differs between environments:** check active configuration and
  conditions. Open [auto-configuration](./08-auto-configuration.md),
  [conditional beans](./09-conditional-beans.md), and [profiles](./10-profiles.md).
- **An endpoint leaks details or gives unclear errors:** check the HTTP contract
  and validation. Open [web boundaries](./11-web-annotations.md) and
  [exception handling](./02-exception-handling.md).
- **A write is inconsistent or a transaction seems absent:** check the entry
  point, rollback rule, and concurrency rule. Open
  [Spring transactions](./03-transactions-and-isolation.md) and
  [write safety](../databases/01-idempotency-and-transaction-safety.md).
- **A data path is slow:** count queries and rows before changing code. Open
  [Spring Data](./13-spring-data.md), [JPA/Hibernate](./04-jpa-hibernate-performance-traps.md),
  and [query optimization](../databases/08-query-optimization.md).
- **A browser or API route has an access problem:** check authentication,
  authorization, CSRF, and exposed routes. Open
  [security baseline](./24-spring-security-actuator-and-testing-baseline.md) and
  [Spring AppSec](../security/03-spring-and-jvm-appsec.md).
- **A remote dependency causes failures:** check timeouts, retry safety, and
  the failure boundary. Open [service integration](./20-spring-cloud-and-service-integration.md)
  and [resilience](../architecture/02-resiliency-patterns.md).
- **A service runs but is hard to operate:** check health exposure, metrics,
  and request correlation. Open
  [Actuator baseline](./24-spring-security-actuator-and-testing-baseline.md)
  and [observability](../devops/03-observability-and-monitoring.md).

## Keep the Models Separate

- A repository simplifies data access; SQL shape and database guarantees still
  decide cost and correctness. Use [data-store choice](../databases/04-sql-vs-nosql.md)
  when deciding between relational, document, and fast shared state.
- A cache can shorten a read, but its owner, expiry, and invalidation still need
  a rule. Use [Spring caching](./12-caching-and-redis.md) for the framework
  boundary and [caching strategies](../architecture/07-caching-strategies.md)
  for the system decision.
- A local transaction protects local data. It does not make a remote call
  atomic with that data; use [workflow patterns](../architecture/16-distributed-workflow-pattern-choice.md)
  when several systems must participate.
- A healthy JVM is not proof that the service can take traffic. Readiness
  should describe whether this instance can serve requests; add shared
  downstream checks only after considering what happens if every instance
  becomes unready during one dependency outage. Expose only the operational
  endpoints the platform needs.

For Kotlin-specific proxy, nullability, and interoperability choices, use
[Kotlin and Spring](./15-kotlin-spring-idioms.md). For schema changes, use
[Flyway and migrations](./19-flyway-and-schema-migrations.md).
