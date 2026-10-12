# Choose a Data Store From the Workload

The useful question is which store can protect the business rules and serve
the required reads and writes at an acceptable cost. "SQL vs NoSQL" is only a
starting label: NoSQL includes different models with different guarantees.

## Describe the Work Before Choosing

Write down:

1. **Business truth:** which facts must survive failure, and who may change them?
2. **Write rules:** which records must change together? What must a concurrent
   or repeated request never do?
3. **Reads:** which lookups, filters, joins, sorts, and reports are required?
   How fresh must each result be?
4. **Load and growth:** expected reads and writes, record size, retention, hot
   keys, and likely changes to query shape.
5. **Operations:** backup, restore, migration, access control, and the team's
   ability to run the store.

An attractive read model is insufficient if the store cannot protect the
critical write. A fast key lookup is insufficient if the product needs
unplanned filters across related data.

## Match the Model to Those Operations

| Model | Good starting point when | Main question to test |
|---|---|---|
| Relational SQL | Related facts need constraints, transactions, and flexible queries | Can one database own the write and meet the measured load? |
| Document | A bounded document is usually read or changed together; its shape varies | What happens when embedded data grows or related documents change together? |
| Key-value | Important operations are known by key and can be modeled directly | Can every required query and invariant use an efficient, safe key path? |
| In-memory shared state | Short-lived lookup, counter, or coordination needs low latency | What happens when a key expires, is evicted, or is lost? |

These are starting fits, not guarantees. A relational database can store JSON;
document and key-value stores can offer transactional operations. Compare the
specific engine and workload before choosing. See [PostgreSQL](./10-postgres-in-depth.md),
[MongoDB](./07-mongodb-in-depth.md), [DynamoDB](./09-dynamodb.md), and
[Redis](./06-redis-in-depth.md) for their mechanisms and limits.

## Apply It to Common Cases

**Booking or checkout.** A reservation must not claim the same capacity twice,
and an order transition must remain valid under retries. Start with a store
that can enforce the relevant invariant in one owned write or transaction.
A relational database is a practical default for several related facts. A
key-based store can also work when the invariant maps cleanly to its atomic
operations. A cached availability read never decides the final claim.

**Product catalog.** Products may have different attributes and are often read
as complete records. A document model may simplify that read. A relational
model with JSON fields may also be enough. Check the real filters, updates,
document size, and indexing before adding a new store.

**Notification inbox.** Reads by user and time can suit a key-based model.
Before choosing one, account for lookups by external event ID, unread counts,
and whether those views may lag. Each extra read path has a cost.

**Hot product page.** Keep an owner for product facts and add a cache only if
measured latency or read load calls for it. Define acceptable staleness and
invalidation; see [Caching strategies](../architecture/07-caching-strategies.md).

## Avoid Accidental Extra Owners

One system may use several stores, but each business fact still needs an
authoritative owner. A search index, cache, or reporting copy can serve reads
without becoming another place to approve a payment or reserve stock. Define
how copies update, how far they may lag, and how they are rebuilt. See
[cross-domain read models](../architecture/19-database-per-service-and-read-model-migration.md).

Choose the simplest model that meets the current write and read requirements.
Add another store when a concrete access pattern, scale limit, or isolation
need justifies the synchronization and operational work.
