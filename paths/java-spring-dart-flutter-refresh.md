# Integrated Java, Spring, Dart, and Flutter Refresh Path

Use this path when you want one common study order across backend Java, Spring
Boot, APIs, security, operations, and app-facing/mobile work.

The point is not to study each folder in isolation.
The point is to follow the same shape that real product engineering work has:

- runtime
- request handling
- data correctness
- API design
- secure flows
- integrations
- production ownership
- app-facing and mobile boundaries

## Goal

Build enough depth to:

- feel current again in modern Java and Spring Boot
- design, implement, and secure APIs as one connected skill
- reason clearly about transactions, persistence, caching, auth, async boundaries, and abuse cases
- reconnect backend engineering with delivery, observability, and secure SDLC habits
- reopen modern Dart and mobile/app-facing judgment without treating client work as a separate world

## What Already Transfers Well

Your older backend and mobile experience still transfers well in:

- request and response thinking
- CRUD and business-flow modeling
- debugging correctness bugs
- API and database intuition
- knowing where complexity usually hides in frameworks

That is why the right path is not:

- syntax first
- framework trivia first
- one path for APIs and another separate path for security

It is:

- rebuild the runtime and boundary mental models
- then study related engineering concerns together

## Current Repo Boundary

This repo is now strong on Java, Spring Boot, backend correctness, APIs,
security, architecture, production ownership, and app-facing/mobile judgment.

The Flutter coverage is still more product-flow and architecture oriented than
framework-exhaustive, which is a good fit for your broader goal.

So this path uses the existing repo to do two things well:

- rebuild the backend and security depth that gives the highest return now
- keep Flutter and mobile work connected to contracts, flows, and public-client boundaries

## Core Rule

Study by engineering boundary, not by repo folder.

If two concerns belong to the same real flow, study them together.

Examples:

- API contract + auth model + authorization + Spring implementation
- webhook design + idempotency + replay defense + delivery semantics
- mobile login flow + token handling + backend trust boundaries

## Working Rule

For each phase:

1. read the notes in order
2. run the matching lab where one exists
3. write a short recap in your own words
4. stop only when you can explain the tradeoff or failure mode without rereading

## Recommended Order

### Phase 1. Rebuild the JVM and modern Java baseline

1. [../topics/java/01-jvm-memory-and-gc.md](../topics/java/01-jvm-memory-and-gc.md)
2. [../topics/java/02-java-concurrency-and-jmm.md](../topics/java/02-java-concurrency-and-jmm.md)
3. [../topics/java/03-modern-java-for-backend-engineers.md](../topics/java/03-modern-java-for-backend-engineers.md)
4. [../topics/java/04-modern-java-21-plus-notes.md](../topics/java/04-modern-java-21-plus-notes.md)
5. [../topics/java/05-concurrency-in-production.md](../topics/java/05-concurrency-in-production.md)
6. [../labs/java-modern-features/README.md](../labs/java-modern-features/README.md)
7. [../labs/kotlin-backend-examples/README.md](../labs/kotlin-backend-examples/README.md)

Use these runnable topics:

- `jvm/concurrency`
- `jvm/concurrency-production`
- `jvm/modeling`

Outcome:

- you recover the runtime and concurrency judgment that modern backend work still depends on even when the framework hides most details

### Phase 2. Add the Kotlin bridge so the rest of the repo becomes easier to use

1. [../topics/kotlin/01-kotlin-for-backend-vs-java.md](../topics/kotlin/01-kotlin-for-backend-vs-java.md)
2. [../labs/kotlin-basics/README.md](../labs/kotlin-basics/README.md)
3. [../topics/kotlin/02-kotlin-backend-idioms-and-gotchas.md](../topics/kotlin/02-kotlin-backend-idioms-and-gotchas.md)

Outcome:

- you can read the Kotlin-based Spring and backend labs in this repo without syntax friction becoming the main problem

### Phase 3. Refresh Spring as request flow and container behavior

1. [../topics/spring-boot/01-spring-boot-fast-review.md](../topics/spring-boot/01-spring-boot-fast-review.md)
2. [../topics/spring-boot/05-ioc-deep-dive.md](../topics/spring-boot/05-ioc-deep-dive.md)
3. [../topics/spring-boot/06-bean-lifecycle.md](../topics/spring-boot/06-bean-lifecycle.md)
4. [../topics/spring-boot/07-proxies-and-aop.md](../topics/spring-boot/07-proxies-and-aop.md)
5. [../topics/spring-boot/08-auto-configuration.md](../topics/spring-boot/08-auto-configuration.md)
6. [../topics/spring-boot/09-conditional-beans.md](../topics/spring-boot/09-conditional-beans.md)
7. [../topics/spring-boot/10-profiles.md](../topics/spring-boot/10-profiles.md)
8. [../topics/spring-boot/11-web-annotations.md](../topics/spring-boot/11-web-annotations.md)
9. [../topics/spring-boot/02-exception-handling.md](../topics/spring-boot/02-exception-handling.md)
10. [../labs/spring-boot-sample/README.md](../labs/spring-boot-sample/README.md)
11. [../topics/spring-boot/21-practical-ddd-in-spring.md](../topics/spring-boot/21-practical-ddd-in-spring.md)
12. [../topics/spring-boot/22-practical-cqrs-and-read-models.md](../topics/spring-boot/22-practical-cqrs-and-read-models.md)
13. [../topics/spring-boot/23-practical-modular-monolith-in-spring.md](../topics/spring-boot/23-practical-modular-monolith-in-spring.md)

Outcome:

- you stop treating Spring as a bag of annotations and start seeing container behavior, proxy boundaries, configuration wiring, and request handling as one runtime model

### Phase 4. Rebuild persistence, correctness, and the data-side failure modes

1. [../topics/spring-boot/03-transactions-and-isolation.md](../topics/spring-boot/03-transactions-and-isolation.md)
2. [../topics/spring-boot/04-jpa-hibernate-performance-traps.md](../topics/spring-boot/04-jpa-hibernate-performance-traps.md)
3. [../topics/spring-boot/13-spring-data.md](../topics/spring-boot/13-spring-data.md)
4. [../topics/spring-boot/19-flyway-and-schema-migrations.md](../topics/spring-boot/19-flyway-and-schema-migrations.md)
5. [../topics/spring-boot/12-caching-and-redis.md](../topics/spring-boot/12-caching-and-redis.md)
6. [../topics/spring-boot/14-datastore-choice-postgres-mongo-redis.md](../topics/spring-boot/14-datastore-choice-postgres-mongo-redis.md)
7. [../topics/databases/01-idempotency-and-transaction-safety.md](../topics/databases/01-idempotency-and-transaction-safety.md)
8. [../topics/databases/02-database-locks-and-concurrency.md](../topics/databases/02-database-locks-and-concurrency.md)
9. [../topics/databases/03-sql-refresh-for-backend-engineers.md](../topics/databases/03-sql-refresh-for-backend-engineers.md)
10. [../topics/appsec/07-sql-injection.md](../topics/appsec/07-sql-injection.md)
11. [../topics/databases/06-redis-in-depth.md](../topics/databases/06-redis-in-depth.md)
12. [../topics/databases/08-query-optimization.md](../topics/databases/08-query-optimization.md)
13. [../topics/databases/10-postgres-in-depth.md](../topics/databases/10-postgres-in-depth.md)
14. [../labs/kotlin-backend-examples/README.md](../labs/kotlin-backend-examples/README.md)

Use these runnable topics:

- `correctness/idempotency`
- `correctness/locking`
- `data/cache`

Outcome:

- you can reason about what one transaction protects, where duplicates still leak through, how ORM convenience turns into SQL cost, and how unsafe query habits become both correctness and security problems

### Phase 5. Study API design, auth, and secure request flows together

1. [../topics/api/00-rest-vs-graphql.md](../topics/api/00-rest-vs-graphql.md)
2. [../topics/api/01-advanced-api-design.md](../topics/api/01-advanced-api-design.md)
3. [../topics/architecture/17-gateway-vs-bff-vs-edge-patterns.md](../topics/architecture/17-gateway-vs-bff-vs-edge-patterns.md)
4. [../topics/api/08-contract-testing-and-api-evolution.md](../topics/api/08-contract-testing-and-api-evolution.md)
5. [../topics/appsec/04-authentication-vs-authorization.md](../topics/appsec/04-authentication-vs-authorization.md)
6. [../topics/appsec/03-basic-auth-bearer-and-api-keys.md](../topics/appsec/03-basic-auth-bearer-and-api-keys.md)
7. [../topics/security/01-auth-sessions-vs-jwt.md](../topics/security/01-auth-sessions-vs-jwt.md)
8. [../topics/appsec/05-jwt.md](../topics/appsec/05-jwt.md)
9. [../topics/appsec/06-oauth2-and-openid-connect.md](../topics/appsec/06-oauth2-and-openid-connect.md)
10. [../topics/security/02-web-and-api-security.md](../topics/security/02-web-and-api-security.md)
11. [../topics/security/03-spring-and-jvm-appsec.md](../topics/security/03-spring-and-jvm-appsec.md)
12. [../topics/appsec/10-access-control-and-idor.md](../topics/appsec/10-access-control-and-idor.md)
13. [../topics/appsec/08-cross-site-scripting-xss.md](../topics/appsec/08-cross-site-scripting-xss.md)
14. [../topics/appsec/09-csrf.md](../topics/appsec/09-csrf.md)
15. [../topics/spring-boot/16-appsec-authz-lab.md](../topics/spring-boot/16-appsec-authz-lab.md)

Outcome:

- you can design an API, choose an auth model, implement it in Spring, and ask the right security questions in the same pass instead of treating them as separate topics

### Phase 6. Rebuild integrations, webhooks, and abuse-resistant workflows

1. [../topics/api/02-message-brokers-and-delivery-semantics.md](../topics/api/02-message-brokers-and-delivery-semantics.md)
2. [../topics/api/03-webhooks-basics.md](../topics/api/03-webhooks-basics.md)
3. [../topics/security/05-payment-integration-patterns.md](../topics/security/05-payment-integration-patterns.md)
4. [../topics/architecture/02-resiliency-patterns.md](../topics/architecture/02-resiliency-patterns.md)
5. [../topics/architecture/03-distributed-transactions-and-events.md](../topics/architecture/03-distributed-transactions-and-events.md)
6. [../topics/api/06-kafka-practical-foundations.md](../topics/api/06-kafka-practical-foundations.md)
7. [../topics/api/07-sync-vs-async-integration-choice.md](../topics/api/07-sync-vs-async-integration-choice.md)
8. [../topics/architecture/06-reactive-and-event-driven-basics.md](../topics/architecture/06-reactive-and-event-driven-basics.md)
9. [../topics/architecture/13-enterprise-integration-patterns.md](../topics/architecture/13-enterprise-integration-patterns.md)
10. [../topics/architecture/18-shared-db-and-independent-deployments.md](../topics/architecture/18-shared-db-and-independent-deployments.md)
11. [../topics/architecture/19-database-per-service-and-read-model-migration.md](../topics/architecture/19-database-per-service-and-read-model-migration.md)
12. [../topics/security/04-advanced-auth-and-sso.md](../topics/security/04-advanced-auth-and-sso.md)
13. [../topics/security/06-threat-modeling-and-business-abuse.md](../topics/security/06-threat-modeling-and-business-abuse.md)
14. [../topics/spring-boot/17-webhook-idempotency-lab.md](../topics/spring-boot/17-webhook-idempotency-lab.md)
15. [../topics/spring-boot/18-threat-modeling-lab.md](../topics/spring-boot/18-threat-modeling-lab.md)
16. [../labs/kotlin-backend-examples/README.md](../labs/kotlin-backend-examples/README.md)
17. [../topics/system-design/checkout-to-fulfillment-end-to-end.md](../topics/system-design/checkout-to-fulfillment-end-to-end.md)

Use these runnable topics:

- `integration/async-boundaries`
- `integration/kafka-patterns`

Outcome:

- you can defend contract shape, retry behavior, async boundaries, replay safety, sensitive business flows, and integration failure handling as one design problem

### Phase 7. Add standards, secure delivery, and production ownership

1. [../topics/security/09-appsec-standards-and-resources.md](../topics/security/09-appsec-standards-and-resources.md)
2. [../topics/security/07-secrets-logging-and-secure-sdlc.md](../topics/security/07-secrets-logging-and-secure-sdlc.md)
3. [../topics/devops/02-zero-downtime-deployments.md](../topics/devops/02-zero-downtime-deployments.md)
4. [../topics/devops/03-observability-and-monitoring.md](../topics/devops/03-observability-and-monitoring.md)
5. [../topics/devops/05-docker-runtime-practices.md](../topics/devops/05-docker-runtime-practices.md)
6. [../topics/cloud/01-cloud-basics.md](../topics/cloud/01-cloud-basics.md)
7. [../topics/cloud/04-container-sizing-and-observability.md](../topics/cloud/04-container-sizing-and-observability.md)
8. [../topics/sre/02-sli-slo-and-error-budgets.md](../topics/sre/02-sli-slo-and-error-budgets.md)
9. [../topics/sre/03-alerting-and-on-call.md](../topics/sre/03-alerting-and-on-call.md)
10. [../topics/sre/04-incident-response-and-triage.md](../topics/sre/04-incident-response-and-triage.md)
11. [../topics/sre/05-capacity-planning-and-load-shedding.md](../topics/sre/05-capacity-planning-and-load-shedding.md)
12. [../topics/sre/06-postmortems-and-operational-review.md](../topics/sre/06-postmortems-and-operational-review.md)

Outcome:

- you reconnect coding decisions with deploy safety, detection, incident response, security requirements, and abuse-resistant delivery habits

### Phase 8. Refresh Dart and mobile/app-facing boundaries

1. [../topics/dart/README.md](../topics/dart/README.md)
2. [../topics/dart/01-dart3-features.md](../topics/dart/01-dart3-features.md)
3. [../topics/flutter/README.md](../topics/flutter/README.md)
4. [../topics/flutter/01-flutter-architecture-and-project-shape.md](../topics/flutter/01-flutter-architecture-and-project-shape.md)
5. [../topics/flutter/02-layout-navigation-and-async-ui.md](../topics/flutter/02-layout-navigation-and-async-ui.md)
6. [../topics/flutter/03-auth-api-and-mobile-boundaries.md](../topics/flutter/03-auth-api-and-mobile-boundaries.md)
7. [../topics/flutter/04-platform-integration-and-testing.md](../topics/flutter/04-platform-integration-and-testing.md)
8. [../topics/security/08-mobile-appsec-basics.md](../topics/security/08-mobile-appsec-basics.md)
9. [../topics/testing/01-testing-strategies.md](../topics/testing/01-testing-strategies.md)
10. [../topics/testing/02-clean-code-and-code-review.md](../topics/testing/02-clean-code-and-code-review.md)
11. [../topics/architecture/07-caching-strategies.md](../topics/architecture/07-caching-strategies.md)
12. [../topics/system-design/lifecycles-and-flows-cheatsheet.md](../topics/system-design/lifecycles-and-flows-cheatsheet.md)
13. [../topics/system-design/practical-checkout-design.md](../topics/system-design/practical-checkout-design.md)

Working focus:

- records
- patterns
- sealed-state modeling
- public-client constraints
- token handling and mobile trust boundaries
- app-to-backend contract quality

Outcome:

- you strengthen the part of Flutter and mobile work that is really about data flow, auth, retries, cache boundaries, error handling, and public-client security

### Phase 9. Consolidate with end-to-end backend exercises

1. [../labs/spring-boot-sample/README.md](../labs/spring-boot-sample/README.md)
2. [../labs/kotlin-backend-examples/README.md](../labs/kotlin-backend-examples/README.md)
3. [../topics/system-design/system-design-drills.md](../topics/system-design/system-design-drills.md)
4. [../topics/system-design/worked-diagrams.md](../topics/system-design/worked-diagrams.md)
5. [../topics/system-design/system-design-decision-cheatsheet.md](../topics/system-design/system-design-decision-cheatsheet.md)

Outcome:

- you finish the path able to explain not just framework syntax, but whole flows, tradeoffs, failure modes, safe defaults, and where security belongs in normal product engineering

## Practical Rule For This Path

Do not split closely related skills into separate study tracks.

The highest-return order in this repo is:

1. Java and JVM
2. Kotlin bridge
3. Spring runtime and request flow
4. persistence and correctness
5. API design plus auth plus security
6. integrations plus abuse-resistant workflows
7. secure delivery plus production ownership
8. Dart plus mobile/app-facing boundaries

That order is closer to how real systems are built, reviewed, broken, and improved.

## Supporting Focused Paths

The other paths in this repo are now better treated as references, not as
separate main study plans, if you want one common path:

- [appsec-for-software-engineers.md](./appsec-for-software-engineers.md): focused web-first security recap
- [appsec-in-product-teams.md](./appsec-in-product-teams.md): focused AppSec deepening inside product teams
- [solutions-architect-from-backend.md](./solutions-architect-from-backend.md): solution-shaping expansion
- [sre-from-backend-engineers.md](./sre-from-backend-engineers.md): operations-heavy expansion

## Current Boundary

This path is now much more self-contained than before.

The remaining gap is not "missing basic topics" so much as "more end-to-end
applied examples" if you later want to deepen it further.

The next highest-value additions would be:

1. one runnable end-to-end sample that mirrors the checkout-to-fulfillment study
2. one deeper Flutter sample app that shows the folder structure, auth-aware API client, and testing layers in code
3. one contract-testing or provider-consumer example wired into a small Spring sample
