# Advanced Cache Patterns (Redis)

Everyone says "let's add Redis", but very few can explain *how* they will keep
Redis in sync with Postgres without showing stale or incorrect data to users.

If you have Redis experience, you must be able to explain these three main strategies.

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
- If Redis goes down, the application keeps working (falling back to the database, though slower).

**Cons:**
- The first user to request an uncached item takes the "Miss" and experiences latency.
- Mitigation (Cache Stampede): Pre-warm the cache — if a product launch is scheduled at noon,
  a script fills Redis at 11:59 before traffic arrives.

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

## The Big Cache Problem: Cache Stampede (The Thundering Herd)

**Interview Scenario:**

You have a product that expires from cache every 10 minutes. It is the main product on your
homepage. It happens to expire. In that millisecond, the cache is empty. At that exact moment,
5,000 users hit the homepage.

Your application (Cache-Aside) says: "It's not in Redis. I'll ask Postgres." **All 5,000 threads
hit Postgres simultaneously requesting the same product.** Postgres is overwhelmed, connections
collapse, and your site goes down.

**How do you solve it?**

Mention **Distributed Locks (Mutex) in Redis**:

*"If a cache miss occurs on something critical and high-demand, my thread would attempt to acquire
a lock in Redis. Only 1 of the 5,000 threads would succeed in going to Postgres, running the
SELECT, and refilling Redis. The other 4,999 threads would wait briefly polling Redis until the
first thread filled the cache, protecting the relational database from the destructive spike."*

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
