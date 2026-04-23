# 9. Caching in Spring Boot and Redis

> Primary fit: `Shared core`

Caching is one of the easiest topics to explain badly.
People often jump straight to `@Cacheable` without first deciding whether the data is even
safe to cache.

This note starts from the concept, then moves to the Spring and Redis shapes that matter
in real backend work.

---

## 1. What Caching Actually Solves

A cache exists to avoid repeating expensive reads.

Smallest example:

- product detail is read thousands of times
- product metadata changes a few times per day
- DB is the source of truth
- cache stores a short-lived copy of the read result

If the cache hits, you avoid:

- database latency
- database load
- repeated serialization or aggregation work

Short rule:

> cache stable or expensive read models, not write-critical truth

In plain language:

- the database still owns the real answer
- the cache keeps a temporary copy because many requests ask the same question
- if the cache disappears, the system should still be correct, only slower

Pros:

- lower read latency
- lower database load
- better throughput on hot read paths

Tradeoffs / Cons:

- stale data risk
- invalidation complexity
- more moving parts than "just add `@Cacheable`"

---

## 2. What A Cache Is Not

A cache is usually not:

- the source of truth
- the safest place for transactional state
- a substitute for correct query design

Examples of things to be careful with:

- real-time stock at checkout
- payment state
- order confirmation state

Why:

- stale cache can break correctness
- invalidation can be harder than the read itself

If you cannot say:

- where truth lives
- how staleness is bounded
- how invalidation happens

then you are not done designing the cache.

---

## 3. The Smallest Useful Example

Before Spring annotations, the core caching idea looks like this:

```kotlin
class ProductService(private val repository: ProductRepository) {
    private val cache = mutableMapOf<String, ProductDto>() // local in-memory cache for this Java Virtual Machine (JVM) process only

    fun findById(productId: String): ProductDto {
        cache[productId]?.let { return it } // cache hit: return without touching the repository

        val product = repository.findById(productId)
        cache[productId] = product // cache miss: save loaded value for later calls
        return product
    }
}
```

That already teaches the key idea:

- check cache first
- read from source on miss
- store result

This is the **cache-aside** pattern in its simplest form.

That name sounds more complicated than it is.
It only means:

1. look in the cache first
2. if missing, read from the real data store
3. save the result in the cache for later requests

Why it is not production-ready:

- no TTL (time to live, meaning no automatic expiration)
- no distributed sharing across instances
- no invalidation
- no stampede protection

But it is the right place to start conceptually.

Pros:

- easy to explain
- captures the core cache-aside idea cleanly

Tradeoffs / Cons:

- not distributed
- no TTL or invalidation
- no stampede protection

---

## 4. Spring Cache Abstraction

Spring's cache abstraction is useful because it lets you describe the caching behavior
without wiring every Redis call by hand.

### `@Cacheable`

**Kotlin**
```kotlin
@Service
class ProductService(private val repository: ProductRepository) {

    @Cacheable(value = ["products"], key = "#productId")
    fun findById(productId: String): ProductDto {
        return repository.findById(productId) // this line runs only on a cache miss
    }
}
```

<details>
<summary>Java version</summary>

```java
@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "products", key = "#productId")
    public ProductDto findById(String productId) {
        return repository.findById(productId); // this line runs only on a cache miss
    }
}
```

</details>

Meaning:

- on first call, method runs and result is cached
- on later calls with same key, cached value is returned

Useful options:

```kotlin
@Cacheable(
    value = ["products"],
    key = "#productId",
    unless = "#result.discontinued",
    sync = true,
)
fun findProduct(productId: String): ProductDto = loadProduct(productId)
```

Important nuance:

- `sync = true` reduces duplicate local cache-miss work
- it is local JVM coordination, not a global distributed lock

### `@CachePut`

```kotlin
@CachePut(value = ["products"], key = "#product.id")
fun update(product: ProductDto): ProductDto = repository.save(product) // method always runs, then cache is refreshed
```

Meaning:

- method always runs
- cache is refreshed with the returned value

### `@CacheEvict`

```kotlin
@CacheEvict(value = ["products"], key = "#productId")
fun delete(productId: String) {
    repository.deleteById(productId) // remove DB row first; Spring evicts cache entry around this call
}
```

Meaning:

- remove stale cache entry after the write

Important Spring nuance:

> `@Cacheable` is proxy-based, so the same self-invocation caveat as `@Transactional`
> applies.

Plain English version:

- Spring usually applies caching behavior around a bean method call, not inside a direct self-call
- if one method in the same class calls another `@Cacheable` method directly, the cache behavior may be skipped

Pros:

- fast to adopt in Spring
- cleanly separates cache intent from low-level Redis calls

Tradeoffs / Cons:

- easy to overuse without first proving the data is safe to cache
- hides complexity if TTL, invalidation, or ownership are still unclear

---

## 5. TTL, Invalidation, And Ownership

This is where caching becomes real engineering.

Three core questions:

1. how long can the value be stale?
2. who is allowed to invalidate it?
3. what happens if another service writes the same data?

`TTL` means `time to live`.
It is simply the expiration time for a cache entry.

Basic Redis TTL configuration in Spring:

**Kotlin**
```kotlin
@Configuration
@EnableCaching
class CacheConfiguration {

    @Bean
    fun cacheManager(factory: RedisConnectionFactory): RedisCacheManager {
        val defaults = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .disableCachingNullValues()

        val cacheConfigs = mapOf(
            "products" to defaults.entryTtl(Duration.ofHours(1)),
            "promotions" to defaults.entryTtl(Duration.ofMinutes(1)),
            "product-catalog" to defaults.entryTtl(Duration.ofMinutes(5)),
        )

        return RedisCacheManager.builder(factory)
            .cacheDefaults(defaults)
            .withInitialCacheConfigurations(cacheConfigs)
            .build()
    }
}
```

<details>
<summary>Java version</summary>

```java
@Configuration
@EnableCaching
public class CacheConfiguration {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30)) // default TTL for caches that do not override it
            .disableCachingNullValues(); // avoid filling cache with "not found" placeholders by default

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
            "products", defaults.entryTtl(Duration.ofHours(1)), // stable product data can stay longer
            "promotions", defaults.entryTtl(Duration.ofMinutes(1)) // promotions change faster, so TTL is shorter
        );

        return RedisCacheManager.builder(factory)
            .cacheDefaults(defaults)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
```

</details>

Practical TTL rule:

- stable product metadata -> longer TTL
- pricing or promotions -> shorter TTL
- live stock or payment truth -> avoid cache or keep extremely tight rules

Pros:

- bounded staleness
- simpler memory control

Tradeoffs / Cons:

- short TTL can reduce cache value
- long TTL increases stale-read risk

If your service is not the only writer, `@CacheEvict` is often not enough.
Then you may need:

- short TTL with tolerated staleness
- event-driven invalidation
- explicit ownership rules

`invalidation` simply means "how the stale cache entry gets removed or replaced."
`ownership` means "which service is allowed to decide that the cached value is now stale."

Smallest event-driven invalidation shape:

```text
Catalog Service updates product
-> publishes ProductUpdated
-> cache consumer deletes product:123
```

Why it matters:

- another service may change the same truth
- local `@CacheEvict` only sees local writes

---

## 6. What To Cache And What Not To Cache

Good cache candidates:

- product details
- catalog views
- reference data
- expensive read projections
- read-heavy aggregated responses

Bad or risky cache candidates:

- payment state
- final order confirmation
- highly contested inventory
- data that changes under strict transaction boundaries

Rule of thumb:

- cache DTOs, projections, or read models
- do not casually cache live JPA (Java Persistence API) entities

Why:

- lazy loading surprises
- stale entity graphs
- serialization traps

---

## 7. Cache Stampede And Hot Keys

If a hot key expires and many requests miss at once, the database can get hammered.

Two terms worth knowing:

- `hot key`: one cache entry that many requests ask for, such as a best-selling product page
- `cache stampede`: many requests all miss at the same time and all rush to rebuild the same value

Common mitigations:

- `sync = true` for local coordination
- TTL jitter so keys do not all expire together
- one writer or distributed lock on repopulation if the risk is high
- pre-warming a few very hot read models

Example TTL jitter:

```kotlin
val ttl = Duration.ofMinutes(30).plusSeconds(Random.nextLong(60)) // add small random jitter so hot keys do not all expire at once
```

Short rule:

> hot keys need expiration strategy, not just expiration time

---

## 8. Redis vs Caffeine

This distinction comes up often in practice.

- **Redis**: shared across instances, network hop, good for distributed cache
- **Caffeine**: in-process, faster per lookup, but each JVM has its own copy

Use Caffeine when:

- one JVM or per-instance cache is enough
- ultra-low local latency matters

Use Redis when:

- multiple application instances need the same cache
- you need a distributed view of cached state

---

## 9. 20-Second Answer

> Caching is an acceleration layer for read-heavy or expensive data, not a substitute for
> a source of truth. I first decide whether the data is safe to be stale and how invalidation
> will work. In Spring I use `@Cacheable`, `@CachePut`, and `@CacheEvict` for simple cache
> flows, but I still care more about TTL, ownership, and whether another service can mutate
> the same data. I avoid caching payment truth or highly contested inventory casually.

---

## 10. 1-Minute Answer

> I frame caching around three questions: what is the source of truth, how stale can the
> data be, and who invalidates it. The simplest pattern is cache-aside: check cache, load
> from the database on miss, then populate the cache. In Spring that usually maps to
> `@Cacheable` for reads, `@CachePut` for refresh-on-write, and `@CacheEvict` for invalidation.
> But the annotation is the easy part. The real engineering is TTL and ownership. Stable
> product metadata can tolerate longer TTLs, pricing or promotions need shorter TTLs, and
> payment state or highly contested stock should usually not be cached casually at all. I
> also watch for stampedes on hot keys and remember that Redis and Caffeine solve different
> problems: Redis is shared and distributed; Caffeine is local and per-JVM.

---

## 11. What To Internalize

- caching is about accelerating reads, not redefining truth
- the first design question is whether staleness is acceptable
- TTL and invalidation matter more than the annotation syntax
- `@Cacheable` is proxy-based and has the same self-call caveat as `@Transactional`
- cache DTOs or read models, not live JPA entities by default
- Redis and Caffeine are different tools with different failure and sharing models
