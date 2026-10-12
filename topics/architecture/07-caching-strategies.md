# Choosing a Cache Strategy

A cache avoids repeated work by keeping a copy of data near a read path. It
trades lower latency and origin load for staleness, invalidation work, and a
new failure mode. Decide what may be stale before choosing Redis, an
in-process cache, or an annotation.

## Start With the Business Read

For each candidate, answer:

1. Which system owns the authoritative value?
2. How old may the copy be, including after a write?
3. Who can change the value, and how will each cache entry be invalidated?
4. What happens on a miss or cache outage at peak traffic?
5. Does the key include every input and access scope that changes the result?

Example: product descriptions may tolerate a short delay; checkout stock
reservation must check the authoritative write path. A cached availability
hint can help browsing but cannot confirm the final reservation.

## Choose the Read and Write Flow

| Pattern | Flow | Cost to plan for |
| --- | --- | --- |
| Cache-aside | Application reads cache, loads from owner on miss, then stores a copy | Miss latency, invalidation, and a possible miss surge |
| Read-through | Cache layer loads from owner on miss | Loading and failure policy are hidden behind the cache layer |
| Write-through | Write path updates owner and cache | Two systems still need failure and ordering rules |

Cache-aside is a practical starting point when the application owns the read
path. After an authoritative write commits, invalidate affected keys. A
time-to-live (TTL) expires entries after a bounded period and helps recover
from missed invalidations. Neither step makes the database and cache one
atomic transaction. A concurrent read can repopulate an old value during an
invalidation race; use versioned entries or a stronger read path when that
window is unacceptable.

Write-through can make recent writes visible sooner, but writing the database
and cache in one application flow does not guarantee they agree after a
crash. Read-through moves miss handling into a cache library or service; it
does not remove the need to define behavior when that layer fails.

## Keep a Miss From Becoming an Outage

A **stampede** happens when a popular entry expires and many callers rebuild
it at once. For hot keys, allow one bounded refresh per key where practical,
spread expiry times, or briefly serve an older value if the product permits
it. Limit refresh concurrency so a cache outage does not send every caller
to the same database. If stale data is unsafe, reject or use a tightly
bounded authoritative read instead.

Cache keys must include normalized filters and the caller's authorized scope.
Otherwise a reused search result can expose one tenant's data to another.
Caching identical queries reduces repeated work; it does not replace request
limits or make unlimited concurrent connections cheap. See [capacity and load
shedding](../sre/05-capacity-planning-and-load-shedding.md).

## Pick the Cache Location

- An in-process cache avoids a network hop but each application instance has
  its own copy. Invalidation across instances needs a plan.
- A shared cache lets instances reuse entries but adds a network dependency
  and shared capacity limit.
- An HTTP cache reuses responses in clients or intermediaries. Its headers,
  validation, and private/public scope are covered in [HTTP caching](../api/09-http-caching-etag-and-compression.md).

For Spring annotations and Redis configuration, see [Spring caching and
Redis](../spring-boot/12-caching-and-redis.md). For distributing keys across
cache nodes, see [consistent hashing and hash slots](./10-consistent-hashing.md).

## Decision Rule

Cache repeated reads only when their staleness and access scope are explicit.
Keep final business writes with their owner, bound miss traffic, and test
invalidation races and cache failure before relying on the latency gain.
