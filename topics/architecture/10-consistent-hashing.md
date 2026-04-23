# Consistent Hashing

## The Problem: Naive Modulo Sharding Breaks on Resize

Imagine you have 3 cache nodes and use the formula `node = hash(key) % 3` to decide where to store data.

- `hash("product:123") % 3 = 1` → Node 1
- `hash("cart:abc")    % 3 = 2` → Node 2

This works until you add a 4th node. Now `% 4` produces completely different results. Effectively **every key maps to a new node** — your entire cache becomes invalid simultaneously. For a retail platform with millions of sessions and product caches, this means a thundering herd of cache misses hitting the database all at once.

**Consistent hashing solves this.** When you add or remove a node, only the keys that were assigned to that specific node need to be remapped.

---

## How It Works

### The Hash Ring

Imagine a circle (ring) from 0 to 2³² − 1. Both nodes and keys are hashed to a position on this ring.

```
                    0 / 2^32
                      *
             *                 *
     Node A (12%)         Node C (58%)
                   ring
             *                 *
          Node B (35%)       (empty)
                      *
```

**Lookup rule:** To find which node owns a key, hash the key to a ring position, then walk **clockwise** until you hit a node. That node owns the key.

### Adding a Node

When you add Node D at position 45%:
- Only the keys between 35% (Node B) and 45% (Node D) move from Node C to Node D.
- All other keys are unaffected.
- **Fraction of keys remapped = 1 / (n+1)** on average.

### Removing a Node

When Node B fails:
- Only the keys owned by Node B move to the next clockwise node (Node D or Node C).
- All other keys stay put.

---

## Virtual Nodes (vnodes) — The Critical Detail

Plain consistent hashing has a problem: with 3 nodes, each node gets roughly 33% of the ring, but hash functions are not perfectly uniform. One node might get 50%, another 15% — **hotspot**.

**Virtual nodes** fix this. Instead of placing each physical node once on the ring, you place it **many times** (e.g., 150 virtual positions per node). The virtual nodes are distributed across the ring, so the load is statistically even.

```
Ring positions (example with 3 nodes, 3 vnodes each):
A1  B2  C1  A2  C3  B1  A3  C2  B3
```

- Node A owns keys that land between B3→A1, A2→C3 (non-contiguous but balanced).
- When Node B is removed, its load is spread across A and C proportionally — not all dumped onto one node.

**Interview answer:** "We use virtual nodes to ensure even load distribution even when nodes have different capacities. A larger node gets more virtual nodes."

---

## Where It's Used in Practice

### Redis Cluster

Redis Cluster divides the key space into **16,384 hash slots** (not a continuous ring, but the same principle). Each master node owns a range of slots. `CLUSTER KEYSLOT mykey` tells you which slot a key maps to.

When you add a Redis node, it imports a subset of hash slots from existing nodes — only those keys migrate. Sessions, carts, rate-limit counters: only the affected keys move.

```
Node A: slots 0–5460
Node B: slots 5461–10922
Node C: slots 10923–16383

Add Node D → redistribute some slots from A, B, C to D
```

**Multi-key operations** (MGET, pipeline) require all keys to be in the same slot. Force this with hash tags: `{user:123}:cart` and `{user:123}:session` both hash on `user:123` → same slot.

### Cassandra

Cassandra uses consistent hashing on the partition key of each row. Each node owns a token range on the ring. The replication factor (e.g., RF=3) means each partition is stored on 3 consecutive nodes clockwise.

```kotlin
// In a Cassandra table:
// PRIMARY KEY (store_id, product_sku)
// store_id is the partition key → hashed to a ring position
// product_sku is the clustering key → sorted within the partition
```

### Load Balancers (Sticky Sessions)

L7 load balancers (HAProxy, Nginx, AWS ALB) can use consistent hashing on a session cookie or user ID to route requests to the same backend node. This is important for:
- In-memory session stores (if you're not using external Redis)
- Websocket connections that must stay on the same instance
- Stateful streaming processes

---

## Practical Scenario

**Question:** "How would you design the caching layer for a very large product catalog, with zero downtime during cache node scaling?"

**Answer using consistent hashing:**

> We'd use Redis Cluster with consistent hashing across, say, 6 master nodes with RF=1 (plus 6 replicas). Product data is partitioned by SKU using hash tags grouped by category: `{outerwear}:product:UT-WHITE-M`, so related products hash to the same slot enabling multi-key pipeline reads.
>
> When we need to add capacity (say, before a major sale campaign), we add 2 new nodes. Redis Cluster migrates only the ~25% of hash slots assigned to the new nodes — no full cache invalidation. During migration, reads are served from the old nodes for unmigrated slots and from new nodes for migrated ones. No downtime.
>
> For hot SKUs during a major launch, we'd use a local in-process cache (Caffeine) as L1 with a 1-second TTL to absorb the spike, falling back to Redis Cluster as L2.

---

## Quick Reference

| Aspect | Value |
|---|---|
| Keys remapped on resize | 1/n on average (vs 100% with modulo) |
| Virtual nodes purpose | Even load distribution |
| Redis Cluster hash slots | 16,384 |
| Multi-key operation trick | Hash tags `{prefix}:key` |
| Cassandra equivalent | Token ranges, partition key hashing |
| Load balancer use case | Sticky sessions by user/session ID |

---

## Common Interview Mistakes

- Saying "we use modulo hashing" for distributed caches — always say "consistent hashing" or "Redis Cluster"
- Forgetting virtual nodes — plain ring is almost never used in production
- Not mentioning what happens during a node failure (keys owned by failed node → next clockwise node; replicas promote)
- Confusing hash slots (Redis Cluster) with the continuous ring (Cassandra) — both are consistent hashing, different implementations
