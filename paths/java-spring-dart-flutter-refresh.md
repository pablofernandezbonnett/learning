# Java, Spring, Dart, and Flutter Refresh Path

Use this path when you are rusty across backend Java, Spring Boot, and
Dart/Flutter work and want one deep refresh order using the materials that
already exist in this repo.

This is not a restart from zero.
The path works best when you rebuild the backend and runtime mental models
first, then reopen the language and app-side layers on top of that.

## Goal

Build enough depth to:

- feel current again in modern Java and Spring Boot
- reason clearly about transactions, persistence, caching, async boundaries, and production behavior
- reopen modern Dart instead of carrying old pre-Dart-3 habits
- rebuild stronger app-facing judgment for Flutter work through API, auth, state, and flow design

## What Already Transfers Well

Your older backend and mobile experience still transfers well in:

- request and response thinking
- CRUD and business-flow modeling
- debugging correctness bugs
- API and database intuition
- knowing where complexity usually hides in frameworks

That is why the right path is not "learn the newest syntax everywhere first".
It is "rebuild the runtime and boundary mental models, then update the syntax
and framework layer on top".

## Current Repo Boundary

This repo is already strong on Java, Spring Boot, backend correctness,
architecture, security, and production ownership.

The main gap is that there is not yet a dedicated `Flutter` topic folder for:

- widget tree and render pipeline refresh
- layout and constraints
- navigation APIs
- state-management options in Flutter itself
- platform integration and package ecosystem refresh

So this path uses the existing repo to do two things well first:

- rebuild the backend and language depth that gives the highest immediate return
- strengthen the app-facing architectural judgment that a strong Flutter engineer still needs

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

### Phase 2. Add the Kotlin bridge so more of the repo becomes usable

1. [../topics/kotlin/01-kotlin-for-backend-vs-java.md](../topics/kotlin/01-kotlin-for-backend-vs-java.md)
2. [../labs/kotlin-basics/README.md](../labs/kotlin-basics/README.md)
3. [../topics/kotlin/02-kotlin-backend-idioms-and-gotchas.md](../topics/kotlin/02-kotlin-backend-idioms-and-gotchas.md)

Outcome:

- you can read the Kotlin-based Spring and backend labs in this repo without syntax friction becoming the main problem

### Phase 3. Refresh Spring and Spring Boot as runtime behavior, not annotation memory

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

Outcome:

- you stop treating Spring as a bag of annotations and start seeing container behavior, proxy boundaries, configuration wiring, and web behavior as one runtime model

### Phase 4. Rebuild persistence and correctness judgment

1. [../topics/spring-boot/03-transactions-and-isolation.md](../topics/spring-boot/03-transactions-and-isolation.md)
2. [../topics/spring-boot/04-jpa-hibernate-performance-traps.md](../topics/spring-boot/04-jpa-hibernate-performance-traps.md)
3. [../topics/spring-boot/13-spring-data.md](../topics/spring-boot/13-spring-data.md)
4. [../topics/spring-boot/19-flyway-and-schema-migrations.md](../topics/spring-boot/19-flyway-and-schema-migrations.md)
5. [../topics/spring-boot/12-caching-and-redis.md](../topics/spring-boot/12-caching-and-redis.md)
6. [../topics/spring-boot/14-datastore-choice-postgres-mongo-redis.md](../topics/spring-boot/14-datastore-choice-postgres-mongo-redis.md)
7. [../topics/databases/01-idempotency-and-transaction-safety.md](../topics/databases/01-idempotency-and-transaction-safety.md)
8. [../topics/databases/02-database-locks-and-concurrency.md](../topics/databases/02-database-locks-and-concurrency.md)
9. [../topics/databases/03-sql-refresh-for-backend-engineers.md](../topics/databases/03-sql-refresh-for-backend-engineers.md)
10. [../topics/databases/06-redis-in-depth.md](../topics/databases/06-redis-in-depth.md)
11. [../topics/databases/08-query-optimization.md](../topics/databases/08-query-optimization.md)
12. [../topics/databases/10-postgres-in-depth.md](../topics/databases/10-postgres-in-depth.md)
13. [../labs/kotlin-backend-examples/README.md](../labs/kotlin-backend-examples/README.md)

Use these runnable topics:

- `correctness/idempotency`
- `correctness/locking`
- `data/cache`

Outcome:

- you can reason about what one transaction protects, where duplicates still leak through, how ORM convenience turns into SQL cost, and where caching helps or hurts

### Phase 5. Rebuild API and async integration depth

1. [../topics/api/00-rest-vs-graphql.md](../topics/api/00-rest-vs-graphql.md)
2. [../topics/api/01-advanced-api-design.md](../topics/api/01-advanced-api-design.md)
3. [../topics/api/02-message-brokers-and-delivery-semantics.md](../topics/api/02-message-brokers-and-delivery-semantics.md)
4. [../topics/api/03-webhooks-basics.md](../topics/api/03-webhooks-basics.md)
5. [../topics/api/06-kafka-practical-foundations.md](../topics/api/06-kafka-practical-foundations.md)
6. [../topics/api/07-sync-vs-async-integration-choice.md](../topics/api/07-sync-vs-async-integration-choice.md)
7. [../topics/architecture/02-resiliency-patterns.md](../topics/architecture/02-resiliency-patterns.md)
8. [../topics/architecture/03-distributed-transactions-and-events.md](../topics/architecture/03-distributed-transactions-and-events.md)
9. [../topics/architecture/06-reactive-and-event-driven-basics.md](../topics/architecture/06-reactive-and-event-driven-basics.md)
10. [../topics/architecture/13-enterprise-integration-patterns.md](../topics/architecture/13-enterprise-integration-patterns.md)
11. [../labs/kotlin-backend-examples/README.md](../labs/kotlin-backend-examples/README.md)

Use these runnable topics:

- `integration/async-boundaries`
- `integration/kafka-patterns`

Outcome:

- you can defend contract shape, retry behavior, async boundaries, delivery guarantees, and integration failure handling instead of only wiring endpoints together

### Phase 6. Add production ownership back into your backend mental model

1. [../topics/security/02-web-and-api-security.md](../topics/security/02-web-and-api-security.md)
2. [../topics/security/03-spring-and-jvm-appsec.md](../topics/security/03-spring-and-jvm-appsec.md)
3. [../topics/security/04-advanced-auth-and-sso.md](../topics/security/04-advanced-auth-and-sso.md)
4. [../topics/security/06-threat-modeling-and-business-abuse.md](../topics/security/06-threat-modeling-and-business-abuse.md)
5. [../topics/security/07-secrets-logging-and-secure-sdlc.md](../topics/security/07-secrets-logging-and-secure-sdlc.md)
6. [../topics/devops/02-zero-downtime-deployments.md](../topics/devops/02-zero-downtime-deployments.md)
7. [../topics/devops/03-observability-and-monitoring.md](../topics/devops/03-observability-and-monitoring.md)
8. [../topics/devops/05-docker-runtime-practices.md](../topics/devops/05-docker-runtime-practices.md)
9. [../topics/cloud/01-cloud-basics.md](../topics/cloud/01-cloud-basics.md)
10. [../topics/cloud/04-container-sizing-and-observability.md](../topics/cloud/04-container-sizing-and-observability.md)
11. [../topics/sre/02-sli-slo-and-error-budgets.md](../topics/sre/02-sli-slo-and-error-budgets.md)
12. [../topics/sre/03-alerting-and-on-call.md](../topics/sre/03-alerting-and-on-call.md)
13. [../topics/sre/04-incident-response-and-triage.md](../topics/sre/04-incident-response-and-triage.md)
14. [../topics/sre/05-capacity-planning-and-load-shedding.md](../topics/sre/05-capacity-planning-and-load-shedding.md)
15. [../topics/sre/06-postmortems-and-operational-review.md](../topics/sre/06-postmortems-and-operational-review.md)

Outcome:

- you reconnect coding decisions with deploy safety, detection, incident response, and abuse-resistant design

### Phase 7. Refresh Dart before trying to refresh Flutter deeply

1. [../topics/dart/README.md](../topics/dart/README.md)
2. [../topics/dart/01-dart3-features.md](../topics/dart/01-dart3-features.md)

Working focus:

- records
- patterns
- sealed-state modeling
- class modifiers

Outcome:

- you stop bringing older Dart habits into newer code and regain the language features that matter most in modern Flutter codebases

### Phase 8. Use the repo to rebuild app-facing judgment for Flutter work

1. [../topics/api/00-rest-vs-graphql.md](../topics/api/00-rest-vs-graphql.md)
2. [../topics/api/01-advanced-api-design.md](../topics/api/01-advanced-api-design.md)
3. [../topics/security/01-auth-sessions-vs-jwt.md](../topics/security/01-auth-sessions-vs-jwt.md)
4. [../topics/security/04-advanced-auth-and-sso.md](../topics/security/04-advanced-auth-and-sso.md)
5. [../topics/architecture/02-resiliency-patterns.md](../topics/architecture/02-resiliency-patterns.md)
6. [../topics/architecture/07-caching-strategies.md](../topics/architecture/07-caching-strategies.md)
7. [../topics/testing/01-testing-strategies.md](../topics/testing/01-testing-strategies.md)
8. [../topics/testing/02-clean-code-and-code-review.md](../topics/testing/02-clean-code-and-code-review.md)
9. [../topics/system-design/lifecycles-and-flows-cheatsheet.md](../topics/system-design/lifecycles-and-flows-cheatsheet.md)
10. [../topics/system-design/practical-checkout-design.md](../topics/system-design/practical-checkout-design.md)

Outcome:

- you strengthen the part of Flutter work that is really about data flow, auth, retries, cache boundaries, error handling, and screen-to-backend contract design

### Phase 9. Consolidate with end-to-end backend exercises

1. [../labs/spring-boot-sample/README.md](../labs/spring-boot-sample/README.md)
2. [../labs/kotlin-backend-examples/README.md](../labs/kotlin-backend-examples/README.md)
3. [../topics/system-design/system-design-drills.md](../topics/system-design/system-design-drills.md)
4. [../topics/system-design/worked-diagrams.md](../topics/system-design/worked-diagrams.md)
5. [../topics/system-design/system-design-decision-cheatsheet.md](../topics/system-design/system-design-decision-cheatsheet.md)

Outcome:

- you finish the path able to explain not just framework syntax, but whole flows, tradeoffs, failure modes, and safe defaults across backend and app-facing work

## Practical Rule For This Path

Do not jump into Flutter-specific surface refresh too early if the deeper
runtime and contract instincts still feel rusty.

The highest-return order in this repo is:

1. Java and JVM
2. Kotlin bridge
3. Spring runtime model
4. persistence and correctness
5. APIs and async integration
6. production and security
7. Dart
8. app-facing Flutter prep

That order restores the biggest gaps first and makes the later Flutter refresh
more useful instead of more confusing.

## Next Repo Gap To Fill

If you want this path to become fully self-contained for Flutter, the next topic
folder to add is:

1. Flutter architecture and project structure
2. widget tree, element tree, and render pipeline
3. layout and constraints
4. navigation and routing
5. state management choices
6. async UI, error, and loading states
7. platform channels and package boundaries
8. Flutter testing layers
