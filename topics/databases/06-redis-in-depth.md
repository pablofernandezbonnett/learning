# Redis In-Depth for Backend Engineers

> Primary fit: `Shared core`


Redis is one of the highest-return backend topics because it shows up in system
design constantly:

- cache hot data
- coordinate work across app instances
- protect shared resources
- fan out ephemeral events

You do not need to know every Redis feature.
You do need to know what problem it solves, where it is a good fit, and where it is the
wrong tool.

---

## 1. What Redis Actually Is

Redis is an in-memory key-value data store optimized for very fast reads and writes.

Smallest mental model:

- data lives in RAM
- operations are very fast
- single commands are effectively atomic from the client point of view
- RAM is limited and expensive

Important nuance:

> Redis makes small shared-state operations easy. It does not magically make multi-step
> workflows race-free or durable enough for money-like state on its own.

---

## 2. What Problem Redis Solves

Redis is useful when you need one of these:

1. very low-latency lookups
2. temporary shared state across app instances
3. small coordination primitives
4. ephemeral one-to-many messaging, where one published message is sent to many current subscribers

Typical use cases:

- caching product or pricing reads
- session storage
- rate limiting
- distributed locks
- leaderboard or ranking data
- Pub/Sub for cache invalidation or live dashboards

Good practical line:

> I reach for Redis when I need fast shared state or cache semantics, not when I need
> relational modeling or strong transactional workflows across many entities.

Pros:

- extremely fast for the right access patterns
- useful shared state across multiple app instances
- simple primitives for cache, counters, and coordination

Tradeoffs / Cons:

- RAM is limited and expensive
- durability and consistency are weaker than a relational source of truth
- misuse creates stale-state and correctness problems quickly

---

## 3. Minimal Example

### 3.1 Cache a hot read

```text
GET product:123 -> hit -> return cached JSON
GET product:123 -> miss -> load from Postgres -> SETEX product:123 300 ...
```

### 3.2 Shared counter

```text
INCR rate:user:42
EXPIRE rate:user:42 60
```

These two examples already explain why Redis is useful:

- fast lookup
- cheap shared coordination

---

## 4. When Redis Fits And When It Does Not

### Good fit

- read-heavy hot data
- temporary state
- counters
- rate limits
- locks with TTL
- ephemeral notifications

### Bad fit

- relational joins
- complex ad hoc queries
- long-lived source of truth for money-sensitive data
- workflows where durable multi-step consistency is the main concern

Commerce example:

- cache product details or stock snapshots for read-heavy catalog paths
- do not make Redis the only source of truth for final order or financial state

Payments example:

- use Redis for rate limits or temporary coordination
- do not store final payment correctness only in Redis

---

## 5. The Core Data Structures You Actually Need

Knowing the structure choice is a classic follow-up question.

| Structure | Use it for | Small example |
|---|---|---|
| `String` | cache blob, simple counter, token | `SET product:123 ...`, `INCR login:42` |
| `Hash` | object-like field updates | `HSET user:42 name "John" tier "gold"` |
| `Set` | membership, uniqueness | `SADD order:42:seen webhook-123` |
| `Sorted Set` | ranking, time-scored windows | `ZADD rate:user:42 1711954800 req-1` |

Practical choice rules:

- use `String` when the whole value moves together
- use `Hash` when you want object-like field updates
- use `Set` when existence/uniqueness matters
- use `Sorted Set` when score or time ordering matters

---

## 6. TTL, Memory, And Eviction

Because Redis lives in RAM, memory behavior matters from day one.

### TTL

TTL means the key expires automatically after a time limit.

Example:

```text
SET product:123 "{...}" EX 300
```

Why it matters:

- prevents stale cache living forever
- prevents memory leaks in temporary-state patterns

### Eviction

If Redis runs out of memory, the configured eviction policy decides what happens.

Common safe choices:

- `allkeys-lru` -> good default for cache-heavy use
- `allkeys-lfu` -> useful when frequent access matters more than recency
- `noeviction` -> write errors instead of eviction

Short rule:

> If Redis is acting mainly as a cache, TTL and eviction policy are part of the design,
> not an afterthought.

---

## 7. Persistence: RDB vs AOF

Redis is in memory first, but it can also persist to disk.

### RDB snapshot

- periodic dataset snapshot
- compact
- fast restart
- can lose writes since last snapshot

### AOF

- append every write operation
- better durability
- slower restart
- larger file over time

Practical answer:

> For cache-only Redis, some data loss may be acceptable. For session or coordination
> data, AOF usually gives a better durability tradeoff. Using both RDB and AOF is a
> common balanced setup.

Pros:

- lets Redis recover after crashes
- AOF improves durability for non-cache use cases

Tradeoffs / Cons:

- persistence is still a tradeoff against speed and operational simplicity
- stronger durability does not suddenly make Redis the best primary system of record

---

## 8. Pub/Sub

Redis Pub/Sub is fire-and-forget one-to-many delivery.

Smallest mental model:

- publisher sends to a channel
- current subscribers receive it
- offline subscribers miss it
- there is no replay

Good use cases:

- live dashboard updates
- cache invalidation broadcast
- lightweight internal notifications

Bad use cases:

- anything where delivery or replay really matters

That is the boundary with Kafka:

> Redis Pub/Sub is for ephemeral notifications. Kafka is for durable event processing.

### Minimal Spring shape

```kotlin
@Service
class CacheInvalidationPublisher(private val redisTemplate: StringRedisTemplate) {
    fun invalidate(productId: String) {
        redisTemplate.convertAndSend("cache:invalidation", "product:$productId") // publish one invalidation message to all current subscribers
    }
}
```

---

## 9. Distributed Locks

### What problem they solve

Sometimes only one instance should do the work:

- one settlement job
- one daily reconciliation
- one leader-only task

### The naive implementation

Bad shape:

```text
GET lock -> null
SET lock
```

Two instances can both observe "no lock" and both proceed.

### Minimum safe Redis shape

Use one atomic acquire step with TTL:

```text
SET lock:daily-settlement instance-1 NX PX 30000
```

That means:

- `NX` -> only if absent
- `PX` -> auto-expire if holder crashes

### Practical Spring example

```kotlin
val acquired = redisTemplate.opsForValue()
    .setIfAbsent("lock:daily-settlement", instanceId, Duration.ofSeconds(30)) // set only if the lock key does not exist yet, with a 30s TTL
```

Important nuance:

- release only if you still own the lock
- if the task can exceed the TTL, you need renewal or a stronger lock library

### Interview conversion

> Redis locks are useful for coordination, but I treat them carefully. I acquire with
> `SET NX PX`, store owner identity, and release conditionally so one instance does not
> delete another instance's lock.

---

## 10. Rate Limiting

Redis is a common place to implement rate limits because counters and small windows are fast.

### Fixed window

Minimal idea:

```text
INCR rate:user:42
EXPIRE rate:user:42 60
```

Good:

- simple
- cheap

Weakness:

- burst at window boundaries

### Sliding window with Sorted Set

Use a Sorted Set when you need a more accurate rolling window.

```kotlin
fun isAllowed(userId: String, limitPerMinute: Int): Boolean {
    val key = "rate:$userId"
    val now = System.currentTimeMillis() // use current time as the sorted-set score
    val windowStart = now - 60_000L // keep only the last 60 seconds

    redisTemplate.opsForZSet().removeRangeByScore(key, 0.0, windowStart.toDouble()) // drop old requests that are outside the rolling window
    redisTemplate.opsForZSet().add(key, "$now-${UUID.randomUUID()}", now.toDouble()) // add this request with its timestamp as the score
    redisTemplate.expire(key, Duration.ofMinutes(2)) // clean up the Redis key if traffic stops

    val count = redisTemplate.opsForZSet().count(key, windowStart.toDouble(), now.toDouble()) // count requests still inside the window
    return (count ?: 0) <= limitPerMinute
}
```

Interview rule:

- fixed window -> simpler
- sliding window -> more accurate
- token bucket -> controlled bursts

---

## 11. Real Backend Use Cases

### Commerce backend

- cache product and pricing reads
- invalidate cache on catalog update
- rate-limit search or partner APIs
- do not put final order or stock correctness only in Redis

### Payments backend

- use Redis for idempotency-adjacent coordination, short-lived state, or throttling
- do not treat Redis as the only source of truth for payment finality

### Telemetry / realtime backend

- Pub/Sub for lightweight one-to-many updates
- cache latest state
- sorted sets for time-scored windows

---

## 12. The Big Traps

1. **Treating Redis as a durable source of truth by accident**
   Example: final payment state exists only in Redis and is lost on failure.

2. **Assuming atomic commands make whole workflows safe**
   Example: `GET` then `SET` then external call still races across instances.

3. **No TTL on temporary data**
   Example: cache keys or rate-limit keys never expire and memory grows forever.

4. **Using Pub/Sub when delivery matters**
   Example: settlement event missed because the consumer was offline.

5. **Choosing Redis just because it is fast**
   Example: using Redis where relational queries or durable joins are the real need.

---

## 13. Practical Summary

Good short answer:

> Redis is great for fast shared state: cache, counters, rate limits, short-lived
> coordination, and ephemeral one-to-many messaging. I would not use it as the primary durable source
> of truth for money-like workflows, but it is an excellent support layer around a
> relational system.
