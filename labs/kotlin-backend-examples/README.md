# Kotlin Backend Boundary Exercises

Work through these scenarios in Kotlin after reading the linked topic. State the invariant, sketch the data and API boundary, implement it yourself, and check the failure cases. The guides below describe the decision rather than supply a finished program.

| Exercise | Task | Solution guide | Check |
| --- | --- | --- | --- |
| [Algorithm patterns](../../topics/algorithms/02-coding-round-drills.md) | Choose one map, window, heap, and graph drill. | Use the pattern and invariant from the canonical drills. | Empty and repeated input, complexity. |
| [Idempotency](../../topics/databases/01-idempotency-and-transaction-safety.md) | Apply the same payment request twice. | Unique request key and business write belong in one local transaction; return the stored outcome on replay. | Concurrent replay and crash before commit. |
| [Locking](../../topics/databases/02-database-locks-and-concurrency.md) | Prevent two buyers from claiming the last unit. | Guard the final inventory write with an atomic condition or version check; decide retry policy. | Two concurrent buyers and rejected stale write. |
| [JVM concurrency](../../topics/java/02-java-concurrency-and-jmm.md) | Count requests across workers. | Choose an atomic counter for one value; use a lock if several values must change together. | Lost updates and shutdown. |
| [Production concurrency](../../topics/java/05-concurrency-in-production.md) | Fan out to three blocking services under one deadline. | Bound admission and each dependency; propagate remaining budget. | Queue saturation, slow dependency, cancellation. |
| [Async boundary](../../topics/api/02-message-brokers-and-delivery-semantics.md) | Accept an event and update local state once. | Authenticate input, deduplicate by event ID, commit the local effect, acknowledge after success. | Duplicate and crash-before-ack. |
| [Kafka delivery](../../topics/api/06-kafka-practical-foundations.md) | Process ordered events for one account. | Partition by account key; deduplicate effects locally; route bounded failures to a recoverable path. | Rebalance, duplicate, poison event. |
| [Cache aside](../../topics/databases/06-redis-in-depth.md) | Speed up a hot read. | Source of truth stays in the database; cache misses refill, writes invalidate, TTL bounds staleness. | Stampede and stale value. |
| [AI boundary](../../topics/ai/05-serving-and-rollout.md) | Add an optional model-based classification. | Bound time and concurrency; validate output with deterministic policy before any side effect. | Slow model, malformed output, fallback. |
| [JVM modeling](../../topics/java/03-modern-java-for-backend-engineers.md) | Represent success, rejection, and pending. | Use explicit result variants; callers handle every outcome. | Add a new variant. |
| [Clean code](../../topics/testing/02-clean-code-and-code-review.md) | Refactor a function mixing validation, database writes, and remote calls. | Make the sequence and side effects visible; give each boundary one owner. | A failure between local and remote work. |

For an exercise with several valid designs, explain why yours fits the stated failure and load assumptions. Keep the implementation outside this repository if you want to practise it.
