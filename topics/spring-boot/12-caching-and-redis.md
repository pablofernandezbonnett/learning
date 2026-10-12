# Caching With Spring and Redis

Choose what may be cached, how stale it may be, and what happens on a miss
before adding annotations. The general decision belongs in [cache
strategy](../architecture/07-caching-strategies.md); this note covers the
Spring implementation boundary.

## What the Annotations Do

| Annotation | Behavior | Check before using |
| --- | --- | --- |
| `@Cacheable` | Returns a cached value for a key, or runs the method and stores its result | Is the key complete, including tenant or authorization scope? |
| `@CachePut` | Always runs the method and puts its result in the cache | Can a database/cache failure leave the values different? |
| `@CacheEvict` | Removes entries according to its key or cache rule | Does eviction happen after the authoritative change commits? |

For a read-only product view, the smallest useful shape is:

```java
@Service
public class ProductReader {
    private final ProductRepository repository;

    public ProductReader(ProductRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = "products", key = "#productId")
    public ProductView find(String productId) {
        return repository.loadView(productId);
    }
}
```

If the view depends on tenant, currency, language, or filters, include those
values in the key. Never reuse a cached authorized result across callers
with different access scopes.

## Know What the Annotation Does Not Guarantee

Spring caching commonly works through a proxy around bean method calls. A
method calling its own `@Cacheable` method directly can bypass the cache
advice. Test the actual call path rather than assuming the annotation ran.

`@CachePut` and `@CacheEvict` do not make a database transaction and a cache
change atomic. A write can commit while cache work fails, or cache work can
happen before the transaction outcome is final. Choose an after-commit
invalidation or another explicit strategy when that ordering matters, and
keep a TTL as a recovery bound for missed invalidations.

Spring's `sync = true` option can reduce duplicate cache loads for one key,
but its behavior depends on the cache provider. It is not a general
distributed-lock guarantee. Bound the origin load and test a hot-key miss
with the provider you actually run.

## Choose the Cache Manager and TTL

An in-process cache keeps values inside each application instance. Redis
shares them across instances but adds a network dependency. Choose from the
read pattern and failure behavior, not only lookup speed.

With Spring Data Redis, `RedisCacheManager` can set a TTL per cache. A TTL
expires an entry after its configured lifetime; ordinary reads do not
necessarily extend it. Pick the duration from the allowed staleness of that
view, not a universal value. For example, a product description and a
short-lived promotion need different policies.

If Redis is unavailable, a fallback to the database may overload it. Test
cache failure and stampede behavior alongside normal cache hits. Use
[capacity planning](../sre/05-capacity-planning-and-load-shedding.md) for
limits on miss traffic.

## Review the Implementation

1. Is the cached method a read with a stable, complete key?
2. Does the call cross the Spring proxy as expected?
3. Does the configured cache manager use the intended local or shared store?
4. Does TTL match the allowed age, and how are writes invalidated?
5. Can a miss surge or cache outage overwhelm the source of truth?

## Further Reading

- [Spring cache annotations](https://docs.spring.io/spring-framework/reference/integration/cache/annotations.html).
- [Spring Data Redis cache expiration](https://docs.spring.io/spring-data/redis/reference/redis/redis-cache.html).
