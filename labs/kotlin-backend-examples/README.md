# Kotlin Backend Examples

> Role: `Companion support for backend learning notes`

This folder is not a new study path.
It is the runnable companion for the Kotlin snippets that support the active
backend learning flow.

Use it after reading the main notes, not instead of them.

## Why This Exists

The docs are good for fast refresh.
This folder exists so you can also run the ideas and watch the behavior:

- DSA patterns and drills without copy/paste
- idempotency and locking examples with visible flow
- JVM concurrency and runtime tradeoffs with actual execution
- clean-code examples with explicit side effects and boundaries

## How To Run

This module is intentionally simple:

- it uses `kotlinc` and `java`
- it does not require Gradle for the basic run path
- it also includes `build.gradle.kts` for IDE import or future expansion

Run one topic:

```bash
./run-topic.sh list
./run-topic.sh algorithms/patterns
./run-topic.sh algorithms/drills
./run-topic.sh correctness/idempotency
./run-topic.sh correctness/locking
./run-topic.sh integration/async-boundaries
./run-topic.sh data/cache
./run-topic.sh jvm/concurrency
./run-topic.sh jvm/modeling
./run-topic.sh quality/clean-code
./run-topic.sh all
```

## Topic Map

### `algorithms/patterns`

Companion for:

- `../../topics/algorithms/01-core-patterns.md`

Focus:

- hash map lookup
- sliding window
- two pointers
- linked-list basics
- heap / priority queue
- BFS / DFS
- DP / memoization

### `algorithms/drills`

Companion for:

- `../../topics/algorithms/02-coding-round-drills.md`

Focus:

- timed coding shapes with edge cases
- one runnable drill per major pattern family

### `correctness/idempotency`

Companion for:

- `../../topics/databases/01-idempotency-and-transaction-safety.md`

Focus:

- natural idempotency
- request-key deduplication
- "processing" vs final result
- local transaction boundary mental model

### `correctness/locking`

Companion for:

- `../../topics/databases/02-database-locks-and-concurrency.md`

Focus:

- lost update
- optimistic locking
- pessimistic locking

### `jvm/concurrency`

Companion for:

- `../../topics/java/02-java-concurrency-and-jmm.md`

Focus:

- broken shared state
- `synchronized` vs atomics
- `volatile`
- concurrent collections
- `CompletableFuture`
- virtual threads

### `integration/async-boundaries`

Companion for:

- `../../topics/api/03-webhooks-basics.md`
- `../../topics/api/02-message-brokers-and-delivery-semantics.md`
- `../../topics/architecture/03-distributed-transactions-and-events.md`
- `../../topics/architecture/06-reactive-and-event-driven-basics.md`
- `../../topics/architecture/02-resiliency-patterns.md`

Focus:

- webhook deduplication
- at-least-once event handling
- outbox-style thinking
- retry and fallback mindset

### `data/cache`

Companion for:

- `../../topics/spring-boot/12-caching-and-redis.md`
- `../../topics/databases/06-redis-in-depth.md`

Focus:

- cache-aside
- hot-read path
- cache invalidation
- simple stampede control

### `jvm/modeling`

Companion for:

- `../../topics/java/03-modern-java-for-backend-engineers.md`

Focus:

- Kotlin equivalents of modern JVM modeling ideas
- data carriers
- sealed result modeling
- clearer state space as a design value

### `quality/clean-code`

Companion for:

- `../../topics/testing/02-clean-code-and-code-review.md`

Focus:

- bad flow vs better flow
- explicit side effects
- naming and boundaries
- code-review-safe structure

## Rule

Read the source doc first.
Then run the matching topic to reopen the mental model with real output.
