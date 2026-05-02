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
- idempotency and locking examples where you can see what happens when the same request repeats or two writes overlap
- JVM concurrency and runtime tradeoffs with actual execution
- clean-code examples where state changes, external calls, and module boundaries stay easy to see

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
./run-topic.sh integration/kafka-patterns
./run-topic.sh data/cache
./run-topic.sh jvm/concurrency
./run-topic.sh jvm/concurrency-production
./run-topic.sh jvm/modeling
./run-topic.sh quality/clean-code
./run-topic.sh all
```

## Topic Map

### `algorithms/patterns`

Companion for:

- `../../topics/algorithms/01-core-patterns.md`

Focus:

- hash map lookup for fast counting, membership checks, and last-seen tracking
- sliding window for substring and contiguous-range problems where the active range grows and shrinks
- two pointers for problems that become simpler when you move from both ends or merge two sorted views
- linked-list basics for pointer updates, traversal, and in-place mutation
- heap / priority queue for "keep the smallest or largest few items ready" problems
- BFS / DFS for tree and graph traversal, where you either explore level by level or follow one path deeply first
- DP / memoization for problems where the same subproblem repeats and cached answers save work

### `algorithms/drills`

Companion for:

- `../../topics/algorithms/02-coding-round-drills.md`

Focus:

- timed coding shapes with the kind of edge cases that usually cause mistakes under interview pressure
- one runnable drill per major pattern family so the set stays small and repeatable

### `correctness/idempotency`

Companion for:

- `../../topics/databases/01-idempotency-and-transaction-safety.md`

Focus:

- natural idempotency, where repeating the same action is harmless because the final state is already the same
- request-key deduplication, where the system stores a client or request key to stop the same business action from being applied twice
- "processing" vs final result, meaning the difference between "work has started" and "the final durable answer is ready"
- local transaction boundary mental model, meaning what one database transaction can protect and what it cannot protect outside that database

### `correctness/locking`

Companion for:

- `../../topics/databases/02-database-locks-and-concurrency.md`

Focus:

- lost update, where one write silently overwrites another because both started from stale data
- optimistic locking, where you detect a conflicting write at save time and retry or reject it
- pessimistic locking, where you lock the row early because the business rule is too important to risk concurrent change

### `jvm/concurrency`

Companion for:

- `../../topics/java/02-java-concurrency-and-jmm.md`

Focus:

- broken shared state, where several threads mutate the same data and the result becomes inconsistent
- `synchronized` vs atomics, meaning when you need a full critical section versus one small thread-safe variable update
- `volatile`, which gives visibility of the latest value across threads but does not make compound updates atomic
- concurrent collections that stay safer under multi-threaded access than plain collections
- `CompletableFuture` for composing asynchronous work without blocking the caller thread the whole time
- virtual threads for high-concurrency blocking-style code without paying for one heavyweight platform thread per task

### `jvm/concurrency-production`

Companion for:

- `../../topics/java/05-concurrency-in-production.md`
- `../../topics/databases/02-database-locks-and-concurrency.md`

Focus:

- pool saturation, meaning a small worker pool can quietly turn blocking calls into queueing latency
- admission control, meaning you bound expensive work instead of letting every request pile up
- request budgets, meaning the whole fan-out has one timeout budget rather than several unrelated waits
- local lock vs shared truth, meaning one JVM mutex does not protect a database row across several app instances

### `integration/async-boundaries`

Companion for:

- `../../topics/api/03-webhooks-basics.md`
- `../../topics/api/02-message-brokers-and-delivery-semantics.md`
- `../../topics/architecture/03-distributed-transactions-and-events.md`
- `../../topics/architecture/06-reactive-and-event-driven-basics.md`
- `../../topics/architecture/02-resiliency-patterns.md`

Focus:

- webhook deduplication, meaning a repeated callback from a provider should not trigger the same business action twice
- at-least-once event handling, meaning the system assumes an event may arrive more than once and still processes it safely
- outbox-style thinking, meaning "save the business write and the event record together, then publish later"
- retry and fallback mindset, meaning you decide which failures should be retried, which should stop, and what reduced behavior is acceptable meanwhile

### `integration/kafka-patterns`

Companion for:

- `../../topics/api/06-kafka-practical-foundations.md`
- `../../topics/api/02-message-brokers-and-delivery-semantics.md`

Focus:

- partitioning by key, meaning the key decides where local ordering is preserved
- consumer-group assignment, meaning partitions are shared across consumers rather than every consumer reading everything in one group
- duplicate delivery after crash-before-commit, meaning the same event can be observed again after restart
- retry and DLT flow, meaning not every failure should be retried forever

### `data/cache`

Companion for:

- `../../topics/spring-boot/12-caching-and-redis.md`
- `../../topics/databases/06-redis-in-depth.md`

Focus:

- cache-aside, meaning the app reads from cache first and falls back to the database when the cache misses
- hot-read path, meaning the request path that is read so often that latency and database load become meaningful
- cache invalidation, meaning how cached data gets updated or removed when the source data changes
- simple stampede control, meaning basic protection against many requests all missing cache and hitting the database at once

### `jvm/modeling`

Companion for:

- `../../topics/java/03-modern-java-for-backend-engineers.md`

Focus:

- Kotlin equivalents of modern JVM modeling ideas
- data carriers, meaning small types whose job is to hold and move data clearly rather than hide behavior
- sealed result modeling, meaning encode the allowed outcomes explicitly instead of returning vague nulls or generic booleans
- clearer state space as a design value, meaning the type system should make valid and invalid states easier to tell apart

### `quality/clean-code`

Companion for:

- `../../topics/testing/02-clean-code-and-code-review.md`

Focus:

- bad flow vs better flow, meaning compare tangled control flow with versions that are easier to reason about
- explicit side effects, meaning database writes, network calls, and mutations are visible instead of hidden in surprising places
- naming and boundaries, meaning each function and module says clearly what it owns and what it changes
- code-review-safe structure, meaning the code is easy to scan, question, and change without hidden coupling

## Rule

Read the source doc first.
Then run the matching topic to reopen the mental model with real output.
