# Advanced Cache Patterns (Redis)

Everyone says "let's add Redis", but very few can explain *how* they will keep
Redis in sync with Postgres without showing stale or incorrect data to users.

If you have Redis experience, you must be able to explain these three main strategies.

---

## Why This Matters

Caching is one of the easiest ways to improve latency and one of the easiest
ways to quietly damage correctness.

This topic matters because many backend and interview answers treat Redis as a
free performance button. In real systems, the hard part is not adding a cache.
It is knowing what can be stale, what must stay authoritative, and what failure
mode each cache pattern introduces.

## Smallest Mental Model

Cache is a speed layer in front of a primary source of truth.

The main decision is not "should we use Redis?" but:

- who owns final truth
- when cache entries appear or disappear
- how stale data can be
- what happens under misses, bursts, and write races

## Bad Mental Model vs Better Mental Model

Bad mental model:

- Redis makes reads fast, so the system is better
- once data is in cache, the hard part is solved
- cache consistency is a small implementation detail

Better mental model:

- Redis trades simpler reads for coherence risk
- every cache pattern is really a policy for handling stale data and failure
- the source of truth must remain explicit even when Redis is hot

Small concrete example:

- weak approach: product stock lives in Postgres, but the app trusts cached
  availability during checkout
- better approach: cache helps product browsing, while final reservation still
  checks the authoritative write path

Smallest code example:

```kotlin
fun getProduct(productId: Long): Product {
    val cached = cache.get(productId)
    if (cached != null) return cached

    val product = repository.findById(productId)
    cache.put(productId, product)
    return product
}

fun updateProduct(product: Product) {
    repository.save(product)
    cache.remove(product.id)
}
```

This is the smallest useful cache-aside shape:

- reads try cache first
- misses fall back to the source of truth
- writes update the source of truth first
- cache entries are refreshed or invalidated around the authoritative write

Production translation:

- Redis replaces the in-memory cache
- `TTL` and invalidation policy decide how stale reads may become
- correctness-critical writes still go through the primary write path

Strong default:

- cache read-heavy data whose slight staleness is acceptable
- do not let cache become the final authority for money, final order state, or
  high-contention write decisions unless that is a deliberate system design

### Search Result Cache Boundaries

Search results often mix data with different freshness needs. For example,
hotel descriptions can be cached longer than price or availability. Keep those
decisions separate instead of caching the complete response blindly.

For a short-lived search-result cache:

- build the cache key from the same filters, ordering, and authorized tenant or
  agency; this prevents one caller receiving another caller's contracted result
- use it only when the product accepts the resulting staleness
- when the cache is empty and many identical searches arrive together, run one
  database query and let the others reuse its result instead of running many;
  this is called request coalescing, meaning “do identical work once”
- revalidate final price, capacity, or other correctness-critical state on the
  later commit path

This reduces repeated read work. It does not replace rate limiting, request
deadlines, or a correct database query.

Important limit:

> Caching and sharing one identical query help only when callers ask for the
> same safe result. They do not make 10,000 open HTTP requests free: cap how
> many callers may wait for one unfinished result. They also do not stop a
> direct API client from sending many different queries, so they make repeated
> work cheaper but do not control who may start work.

Reusable takeaway:

> I treat caching as a latency optimization over a clear source of truth. The
> real decision is how much staleness the business can tolerate and how the
> system behaves on misses, races, and bursty traffic.

---

## 1. Pattern: Cache-Aside (Lazy Loading)

The most common pattern. The application (your Spring Boot / Kotlin code) acts as the
intermediary between the cache and the primary database.

**Read Flow:**
1. App asks Redis: *"Give me product 123"*.
2. Redis says: *"I don't have it (Cache Miss)"*.
3. App asks Postgres: *"Give me product 123"*.
4. Postgres returns the product.
5. App returns the product to the client and simultaneously stores a copy in Redis with a
   **TTL** (Time To Live, e.g., 10 minutes).

**Write Flow:**
1. App saves the product 123 update in Postgres.
2. App **Invalidates (Deletes)** product 123 from Redis.
   *(Note: Deleting is safer than updating Redis directly due to concurrency issues.)*

**Pros:**
- Redis only holds data that is actually being used (memory savings).
- If Redis goes down, the application can fall back to the database when that
  database has enough spare capacity; otherwise use a bounded stale fallback or
  controlled rejection rather than sending every caller to the database at once.

**Cons:**
- The first user to request an uncached item takes the "Miss" and experiences latency.
- For an expected spike, pre-warm important keys gradually and with a limit on
  refresh work. A sudden bulk warm-up can overload the same database the cache
  was meant to protect.

---

## 2. Pattern: Write-Through (The Cautious Accelerator)

The cache is updated as part of the write path, so reads are much less likely to see stale
data immediately after a successful write.

**Write Flow:**
1. App writes the new data to the primary store and updates the cache in the same application
   flow, or through a tightly coupled write-through mechanism.

**Pros:**
- Fresh reads are more likely right after writes because the cache is updated proactively.
- Fewer cache misses on recently written data because the cache is updated proactively.

**Cons:**
- Writes are doubled. All calls to modify data now take longer.
- Redis RAM fills with data that may never be queried.
- If the database write and cache write are not coordinated carefully, you can still get
  inconsistency.

Important nuance:

- "write-through" does **not** mean global perfect consistency
- it means the cache is part of the write path
- you still need to reason about failures between the primary write and the cache update

---

## 3. Pattern: Read-Through (The Smart Cache)

The application no longer communicates with the database for reads. It sends ALL reads to the
cache, and the cache is responsible for going to Postgres if it does not have the data.

*At the code level, some libraries and proxy-style databases implement this pattern.*

**Pros:**
- Your application code is much simpler. The app does not care whether data came from memory or
  disk.

**Cons:**
- Every read has the cache as a single point of failure. If the cache library's internal policy
  fails, the app is blind because it does not know how to go to disk on its own.

---

## Refreshing Cache Without Causing A Cascade

A cache can fail safely only if its refresh path is bounded. The dangerous case
is a **cache stampede**: a popular key expires, many callers all miss at once,
and they flood the database or a slow dependency. That new overload can make
the whole application fail, even though the original problem was one empty
cache entry.

The default refresh rule for cache-aside is simple:

1. write the source of truth first
2. after that write succeeds, remove the affected cache key
3. the next read loads the new value and puts it back into cache

Removing a key is usually safer than trying to update several cache entries in
the write request. For complex or cross-service writes, publish a durable
post-commit event and let a consumer remove or refresh affected keys. A `TTL`
(the maximum age of a cached value) is still useful as a safety net if an
invalidation message is delayed or missed.

For a very hot key, add these protections:

- **one refresh per key:** when a value is missing, one caller refreshes it;
  matching callers share that work or wait only for a short, bounded time
- **serve slightly old data while refreshing:** keep a last known good value
  for a short extra window. One caller refreshes in the background while other
  callers receive that older value. This works only when the business accepts
  the temporary staleness.
- **vary expiry times:** add small random variation to `TTL`s so many keys do
  not expire in the same second after a deployment or bulk warm-up
- **bound refresh work:** use short deadlines and a small limit on concurrent
  cache refreshes, so a dependency outage cannot consume every app worker or
  database connection

If the refresh fails, do not retry endlessly. Serve the last known good value
only until its agreed maximum age. After that, return a clear temporary failure
or a reduced response rather than turning every caller into another database
retry. For data where old values are unsafe, do not serve stale data: fail fast
or use the authoritative path with strict capacity limits.

Bad mental model: “when the cache expires, every request can just reload it.”

Better mental model: “one controlled refresh is allowed; other requests get a
bounded wait, an acceptable older value, or an explicit failure.”

Strong interview answer:

> I use cache-aside by default: write the database first and invalidate the
> cached key after success. For hot keys, only one request refreshes a missing
> key, expiry times are staggered, and callers can receive a briefly stale last
> known good value while refresh happens if the product allows it. Refresh work
> has timeouts and concurrency limits, so a cache miss cannot cascade into a
> database outage.

---

## Expiration / Memory Eviction Policies

If Redis fills to its RAM limit, it must decide what to delete to make room for new data. You
need to know what policy you are using.

- **LRU (Least Recently Used):** The most common. Deletes the object that has not been accessed
  for the longest time.
- **LFU (Least Frequently Used):** Deletes the object that has been requested the fewest times
  historically, even if someone just requested it 1 second ago.

**Answer to use:** "For the product catalog I would configure Redis with LRU, ensuring that
abandoned products — old SKUs nobody searches for — expire on their own without manual cleanup."
