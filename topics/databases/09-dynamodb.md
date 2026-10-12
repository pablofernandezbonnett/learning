# DynamoDB: Start From Access Patterns

DynamoDB is a managed key-value and document database. Its useful design
question is not "SQL or NoSQL?" but "Can the important reads and writes be
expressed through known keys, with the required consistency and cost?"
Write down those operations before choosing a table or index.

## Work From Queries to Keys

A table has a **partition key**; a **sort key** is optional. A `Query` names
one partition-key value and can narrow the result by sort key. A filter applied
after that query does not turn an unbounded read into an efficient lookup.

For a notification inbox, suppose the required operations are:

| Operation | Candidate access path |
| --- | --- |
| List one user's recent notifications | `PK = USER#id`, `SK = timestamp#notificationId` |
| Get one notification when user and ID are known | Key lookup if the caller knows its full key, or an explicit lookup mapping |
| Find by external event ID | Another key or a global secondary index if this is a real requirement |
| Find every unread notification across users | A separate index or projection; filtering every user's items is not a key lookup |

The example is a design sketch, not a prescribed schema. Check each operation's
frequency, result size, ordering, and need for fresh data. If most important
queries are still ad hoc or relational after that exercise, a relational
database may be simpler.

## Add Alternate Reads Deliberately

A **global secondary index** (GSI) provides an alternate partition key and
optional sort key. It is maintained from base-table writes and consumes
storage and write capacity. GSI updates are asynchronous, so GSI reads are
eventually consistent. The base table can serve strongly consistent reads
when a workflow needs them; a GSI cannot. A GSI lookup does not enforce
uniqueness for an external event ID; enforce that rule with a key and
conditional write on the owning item.

For a final decision, identify the authoritative item and guard the write
there. A **conditional write** can reject a duplicate key or an invalid state
transition atomically. DynamoDB also supports transactions across a bounded
set of items, but a transaction does not include an external provider or
another service. See [idempotency and local transactions](./01-idempotency-and-transaction-safety.md).

## Design for Uneven Traffic

An access pattern can be efficient yet overload one hot key or index key.
Estimate traffic **per key**, not only total table traffic, and observe
throttling. Choose keys that distribute the expected work. Adaptive capacity
can help uneven access, but it does not remove all per-partition limits.

If one logical stream receives heavy writes, **write sharding** can spread
those items over several keys. Reads then query several shards and combine
results. Do not shard a uniqueness or stock decision without a separate way
to protect that invariant: spreading writes does not make one logical limit
atomic across shards.

Choose on-demand or provisioned capacity from traffic shape and cost. A
managed service still needs key design, capacity monitoring, retry behavior,
permissions, backup, and recovery decisions.

## Choose the Other Tradeoffs Explicitly

- **TTL:** DynamoDB removes expired items later in the background. An expired
  item can remain readable until deletion, so check expiry in application
  logic when expiration is a correctness rule.
- **One table or several:** one table can group related entity types for a
  needed access pattern; several tables can keep unrelated models simpler.
  Choose from the queries and ownership, not a rule that every design must
  use one table.
- **Store choice:** known key-based operations and managed scaling can favor
  DynamoDB. Joins, broad ad hoc queries, and relational constraints can favor
  a relational store. Compare the actual workload and operational cost rather
  than assuming either store is always faster.

## Review Before Building

1. List every required read and write, with expected volume and freshness.
2. Map each to a table key, GSI, or deliberately separate read model.
3. Find hot keys, item collections, and queries that would scan too much.
4. Name the item that owns each invariant and how concurrent writes protect it.
5. Test a retry, a stale GSI result, throttling, and delayed TTL deletion.

## AWS References

- [Core components and keys](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.CoreComponents.html)
- [GSI behavior and consistency](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GSI.html)
- [Read consistency](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.ReadConsistency.html)
- [Partition-key design and hot keys](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-partition-key-design.html)
- [TTL behavior](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/TTL.html)
