# Spring Boot Service Slice Lab

Build a small product API yourself after reading the [Spring Boot topic](../../topics/spring-boot/README.md). The goal is to connect request handling, persistence, configuration, security, and tests without copying a sample application.

## Task

Start with `POST /products`, `GET /products/{id}`, and a filtered, paged list. Validate create input, return a location for a new product, and map missing products to a clear error. Then add these focused extensions:

- adjust stock without allowing an invalid final quantity;
- map a small JPA entity, inspect the query shape, and add one cached read with invalidation on update;
- add auditing timestamps and check when they are written;
- select one payment gateway from configuration and fail clearly if no real gateway is configured for a real environment;
- import products as background work with an explicit job ID and status;
- compare a singleton bean with a request-scoped bean and observe lifecycle hooks;
- expose a health endpoint with a deliberate security rule.

## Guide

```text
HTTP boundary:
  parse and validate input
  resolve authenticated principal
  call application service
  map result or error to HTTP

Application service:
  enforce business and object-access rules
  commit each product or stock change in its intended transaction
  return an explicit result

Persistence:
  own database mapping and query shape
  check generated SQL when fetching orders and items
```

Keep configuration in one place, inject collaborators through constructors, and explain which Spring-managed call crosses a transactional or caching proxy. Cache only reads whose staleness rule is acceptable; invalidate on writes. Expose health information deliberately and protect operational endpoints according to their audience.

## Checks

- Valid create and read; invalid create and unknown product.
- Filtered list with an empty result and stable paging.
- Stock adjustment at and beyond the allowed boundary.
- Duplicate create request if you add an idempotency key.
- A database failure and the HTTP response it produces.
- Query count when reading related entities.
- A cache hit, a write, and the next read.
- Auditing values on create and update; bean creation and destruction at the expected scope.
- Gateway selection under different configurations, including a missing real gateway.
- Import job accepted, failed, and queried by ID; cancellation or timeout at an asynchronous boundary.
- Health endpoint access for an unauthenticated caller.

Use [exception handling](../../topics/spring-boot/02-exception-handling.md), [transactions](../../topics/spring-boot/03-transactions-and-isolation.md), [JPA performance](../../topics/spring-boot/04-jpa-hibernate-performance-traps.md), and the [security baseline](../../topics/spring-boot/24-spring-security-actuator-and-testing-baseline.md) for the underlying rules.
