# General Refresh Path

Use this path when you want a broad backend refresh rather than a single-topic
deep dive.

The point is not to complete every document in one pass.
The point is to reopen the core mental models until they feel usable again.

## Recommended Order

1. [../topics/java/README.md](../topics/java/README.md): refresh JVM runtime, memory, and concurrency first
2. [../topics/kotlin/README.md](../topics/kotlin/README.md): translate strong Java instincts into practical Kotlin
3. [../topics/spring-boot/README.md](../topics/spring-boot/README.md): reconnect framework behavior with transactions, proxies, and persistence
4. [../topics/databases/README.md](../topics/databases/README.md): revisit the data and correctness rules that break real systems
5. [../topics/api/README.md](../topics/api/README.md): reopen contract, retry, and integration behavior
6. [../topics/architecture/README.md](../topics/architecture/README.md): rebuild distributed-systems judgment around boundaries and failure
7. [../topics/system-design/README.md](../topics/system-design/README.md): move from isolated topics to end-to-end backend reasoning
8. [../topics/security/README.md](../topics/security/README.md): refresh trust boundaries, auth, and workflow abuse
9. [../topics/cloud/README.md](../topics/cloud/README.md): understand responsibility boundaries and runtime tradeoffs
10. [../topics/devops/README.md](../topics/devops/README.md): connect backend ownership with rollout, observability, and recovery
11. [../topics/testing/README.md](../topics/testing/README.md): tighten judgment about confidence, risk, and code shape
12. [../topics/algorithms/README.md](../topics/algorithms/README.md): keep the core interview and debugging patterns warm

## Working Rule

For each topic:

1. reopen the smallest mental model
2. review one practical example
3. note the main tradeoff or failure mode
4. stop once the topic feels usable again

## How To Use This Path

This path is for refresh and practical understanding, not for exhaustive study.
Once you enter a topic folder, use it in this order:

1. `Refresh`
2. `Required`
3. `Growth`

If a topic already feels warm, skim the `Refresh` layer and move on.
The path works best when it keeps momentum instead of turning into a checklist marathon.

If your goal is a role shift rather than a broad refresh, use one of the more
targeted paths instead:

- [sre-from-backend-engineers.md](./sre-from-backend-engineers.md)
- [solutions-architect-from-backend.md](./solutions-architect-from-backend.md)
- [appsec-for-software-engineers.md](./appsec-for-software-engineers.md)
