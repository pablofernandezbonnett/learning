# Distributing Keys When Nodes Change

When a cache or storage system has several nodes, it needs a rule that maps
each key to an owner. A good rule balances work and limits how many keys move
when nodes join or leave. Moving too many keys at once can cause misses,
network transfer, and extra load on the source of truth.

## Why Simple Modulo Is Awkward

With `hash(key) % nodeCount`, changing `nodeCount` changes the calculation
for many keys. Adding one node can therefore remap far more data than the
new node needs. This is a distribution problem, separate from cache TTL or
invalidation.

## The Consistent-Hashing Ring

Place node positions and key positions on a logical ring. Assign each key to
the next node clockwise. When a node joins, it takes a section of keys from
its successor; other sections keep their owners. When a node leaves, its
keys move to its successor. The exact amount moved depends on node placement
and capacity; it is not a fixed promise for every resize.

One physical node can hold several **virtual nodes** (positions) on the
ring. This spreads ownership more evenly and lets a larger node take more
positions. It does not solve a single hot key: one key can still receive far
more requests than its neighbors.

Consistent hashing chooses placement. It does not provide replication,
failover, consistency, or a migration protocol by itself. Those need separate
design and operational checks.

## Do Not Confuse It With Redis Cluster

Redis Cluster uses **16,384 hash slots**, not the classic consistent-hashing
ring. A key maps to a slot, and nodes own sets of slots. During a resize,
the cluster moves selected slots between nodes. Both approaches let an
operator move part of the keyspace, but their placement and migration rules
are different.

If several Redis keys must participate in one supported multi-key operation,
check whether they need the same slot. Do not group unrelated hot keys under
one hash tag merely to make them colocate: that can create a hotspot.

## Decide Whether You Need to Own Placement

Before implementing a ring, ask:

- Does the chosen datastore or cache already manage key placement and
  migration? Use its model rather than layering a second one blindly.
- How will clients learn the current owners during a change?
- What happens to reads and writes while keys move?
- How are replicas and failures handled separately from placement?
- Will a few hot keys dominate traffic despite an even key count?

For cache freshness and miss behavior, see [cache strategy](./07-caching-strategies.md).

## Further Reading

- [Original consistent-hashing paper](https://doi.org/10.1145/258533.258660).
- [Redis Cluster scaling model](https://redis.io/docs/latest/operate/oss_and_stack/management/scaling/).
