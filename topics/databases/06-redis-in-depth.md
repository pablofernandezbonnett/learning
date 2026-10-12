# Redis: Shared State and Operational Choices

Redis stores data structures in memory and gives application instances fast access
to shared state. Choose it for a specific access pattern and decide what happens
if a key expires, is evicted, or is lost during failover.

## Start With the State

| Need | Redis fit | Decision to make |
|---|---|---|
| Hot read | Store a cached result with a TTL | How stale may it be, and how is it invalidated? |
| Shared counter | Use `INCR` on one key | When does the counter reset, and is loss acceptable? |
| Membership or ranking | Use a set or sorted set | How large can the structure grow? |
| Temporary coordination | Use an expiring key | What if the holder pauses or the lease expires? |

Redis can hold primary data when its data model and durability fit the
requirement. For orders, payments, or other facts that require durable
multi-record transactions, choose an owner and recovery model explicitly before
putting that state in Redis.

## Choose a Data Structure

| Structure | Typical use | Example command |
|---|---|---|
| String | Whole value or counter | `SET product:123 "..." EX 300`, `INCR requests:42` |
| Hash | Fields read or updated together | `HSET user:42 tier gold` |
| Set | Unique membership | `SADD online-users 42` |
| Sorted set | Ranking or time-scored entries | `ZADD leaderboard 980 user:42` |

Name keys by purpose and scope, for example `catalog:product:123`. Include a
tenant or environment identifier where keys must be isolated. Apply the same
access control and retention decisions to secrets or personal data that you
would apply to any other store.

## Plan Memory and Expiry

- Give temporary keys a TTL. A TTL limits lifetime; it does not guarantee that
  a cached value stays fresh until it expires.
- Set a memory limit and choose an eviction policy for the workload. A cache
  may evict keys under pressure; `noeviction` returns errors for writes that
  would exceed the limit.
- Bound sets and sorted sets that accumulate entries. Monitor memory use,
  evictions, latency, and failed writes.
- For cache invalidation, stampedes, and stale reads, use
  [Caching strategies](../architecture/07-caching-strategies.md).

## Choose Durability Deliberately

Redis supports RDB snapshots and an append-only file (AOF); they can be used
together. Snapshots can lose writes since the last snapshot. AOF durability
depends on its sync policy. Replication and failover also affect which recent
writes survive. Test the configured recovery path against the data-loss window
the system can tolerate.

If Redis is a disposable cache, rebuilding it may be enough. If it holds
sessions, work state, or business data, define persistence, backup, restore,
and failover behavior before relying on it.

## Keep Multi-Step Operations Safe

Individual Redis commands are atomic, but a sequence of commands can interleave
with another client. For a rate limit, checking the current count, incrementing
it, and setting expiry must form one atomic decision, for example through a Lua
script. Choose fixed window, sliding window, or token bucket based on the burst
behavior you need.

An expiring lock key can coordinate workers, but expiry can let another worker
start while the first is still running. Use conditional release and, where a
duplicate effect would be harmful, protect the underlying resource with fencing
or another correctness mechanism. See
[Consensus and leader election](../architecture/12-consensus-and-leader-election.md).

For Redis messaging, see
[Pub/Sub and Streams](./11-redis-streams-and-delivery-tradeoffs.md). For Spring
configuration and cache annotations, see
[Caching and Redis in Spring](../spring-boot/12-caching-and-redis.md).

## Further Reading

- [Redis data types](https://redis.io/docs/latest/develop/data-types/)
- [Key eviction](https://redis.io/docs/latest/develop/reference/eviction/)
- [Persistence](https://redis.io/docs/latest/management/persistence/)
