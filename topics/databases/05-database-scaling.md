# Scale a Database From the Measured Limit

"The database is slow" does not identify the bottleneck. Locate the cost first:
the request may wait for a connection, execute one expensive query, issue too
many small queries, contend on a write, or overload storage. Each cause needs
a different change.

## Diagnose Before Changing Topology

Capture a representative workload and ask:

| Signal | Check first | Possible direction |
|---|---|---|
| One slow read | Query plan, rows examined, sort, returned fields | Query shape, index, bounded result |
| Many reads | Total query rate, repeated data, cache hit rate | Cheaper queries, cache, read model, replica |
| Waiting requests | Connection pool, long transactions, lock waits | Shorter transactions, pool sizing, contention fix |
| Slow writes | Write rate, indexes, locks, storage saturation | Remove avoidable work, scale resources, partition load |
| Growing data | Table size, retention, maintenance, backup time | Archive, partition, larger node, or shard |

Measure request latency and database load together. A fast SQL statement can
still sit behind a saturated connection pool; a large server cannot fix a
missing business invariant or a lock held too long.

## Pick the Smallest Change That Fits

1. **Reduce unnecessary work.** Fix the actual query, repeated queries, large
   results, and missing or redundant indexes. Check the execution plan and
   compare before and after. [Query optimization](./08-query-optimization.md)
   owns the detailed method.
2. **Give the current database room.** More CPU, memory, faster storage, or a
   correctly sized connection pool may relieve a measured resource limit.
   More connections can worsen overload if the database is already saturated.
   Check cost and the next likely limit.
3. **Separate reads when useful.** A cache can remove repeated hot reads;
   a projection can serve a specialized view; a read replica can add read
   capacity. Each introduces a freshness or synchronization decision. Route
   a read that decides a write to the authoritative path when freshness matters.
4. **Partition or shard when data placement is the limit.** Partitioning inside
   one database can improve management and some queries; it does not
   automatically add write capacity across machines. Sharding splits data
   across database nodes and requires a routing key, cross-shard query plan,
   rebalancing, and a way to protect invariants that span shards.

These are options, not a mandatory sequence. A sudden hot key needs a different
response from a large archival table; adding read replicas cannot resolve
primary write contention.

## Example: Order List Under Load

Suppose an order-list endpoint becomes slow. First separate connection wait
from SQL time and count its queries. If one statement scans and sorts far more
orders than the page returns, test a bounded query and supporting index. If
that query is efficient but read traffic still saturates the primary, consider
a replica or a read model with an explicit lag limit. Keep a stock claim or
payment decision on the authoritative write path.

If writes later exceed one node's capacity, estimate traffic per proposed
shard key. A tenant-based split may help when traffic is spread across tenants;
one very busy tenant can still overload a shard. Decide how cross-tenant
reports and live rebalancing will work before committing to that split.

## Verify the Result

Repeat the same workload after the change. Compare latency, throughput,
resource use, lock or connection waits, and error rate. For a cache, replica,
or projection, also test stale reads and recovery. For sharding, test hot keys,
cross-shard operations, and moving data while requests continue.
