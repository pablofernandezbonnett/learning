# MongoDB: Model Documents From the Workload

MongoDB stores records as documents. Its main design choice is what belongs in
one document: data read and changed together may fit there, while independent
or unbounded data usually needs a separate collection. Start with the required
queries and write rules, then check how the model behaves as records grow.

## Decide What One Document Owns

Suppose a product page reads a product's name, status, category, and a small
set of variable attributes together. Those fields can live in one product
document. Reviews can grow without a fixed bound and have a separate update
rate, so store them separately and query them by product ID when needed.

```json
{
  "_id": "sku-123",
  "categoryId": "jackets",
  "status": "ACTIVE",
  "name": "Running jacket",
  "updatedAt": { "$date": "2026-10-01T12:00:00Z" },
  "attributes": { "color": "navy", "sizes": ["S", "M", "L"] }
}
```

Embedding avoids another read and lets one document update atomically. It
also makes the document grow and can repeat the same facts in several places.
Referencing keeps independently growing data separate but adds reads or a
`$lookup` join. MongoDB has a 16 MiB document size limit; avoid arrays that
can grow without a bound well before reaching it.

An operation on one document is atomic. MongoDB also supports transactions
across documents when a rule needs them; account for their cost and scope
instead of assuming a document model removes every cross-record invariant.
See [store choice](./04-sql-vs-nosql.md) for the earlier decision about which
business fact the store should own.

## Design Queries and Indexes Together

For a catalog screen that filters active products by category and sorts by
update time, write the actual query before proposing an index:

```javascript
db.products.find(
  { categoryId: "jackets", status: "ACTIVE" },
  { name: 1, status: 1, _id: 0 }
).sort({ updatedAt: -1 }).limit(30)
```

A candidate compound index is
`{ categoryId: 1, status: 1, updatedAt: -1 }`: equality filters precede the
sort field. MongoDB's **ESR guideline** (equality, sort, range) is a useful
starting point for field order, not a rule that overrides measured query
behavior. Check selectivity, result size, and the `explain()` plan on
representative data. Every added index also costs storage and write work.

A **projection** selects the fields returned to the caller, as in the query
above. It can reduce transferred data, but does not by itself make a poor
filter efficient. Arrays may use multikey indexes; check the actual plan when
filtering or sorting through array fields.

## Use Aggregation for a Defined Read

An aggregation pipeline processes documents in stages: `$match` filters,
`$group` aggregates, `$project` shapes output, and `$lookup` joins data from
another collection. Put a selective `$match` early when it can use an index
and still preserve the intended result. Some stages depend on earlier
transformations, and the optimizer may reorder eligible stages, so "always
put `$match` first" is not a safe rule.

Use `$lookup` when the required read needs it and the measured plan is
acceptable. If a page repeatedly joins large, changing collections, revisit
the document boundary or build a read model with an explicit freshness rule.

## Plan Replication and Sharding Separately

A replica set has a writable primary and secondary members. Reads directed to
a secondary can lag behind a write to the primary. Choose read preference,
read concern, and write concern from the freshness and durability needed by
each operation. These settings control where reads go, what data they may see,
and how a write is acknowledged. Test failover and client retry behavior.

Sharding spreads a collection across nodes by a shard key. Check that common
queries can route efficiently and that writes distribute across key values.
A hot key or a poor query can remain a bottleneck after sharding. Plan how
cross-shard work and later key changes will be handled before using sharding
as a response to load.

## Review the Model

1. List the main reads, updates, and business rules for each document type.
2. Bound embedded arrays and identify facts duplicated across documents.
3. Test the real queries and candidate indexes with `explain()`.
4. Decide which reads may be stale and how failures affect writes.
5. Add sharding only after measuring a limit that distribution can address.

## MongoDB References

- [Embedding data](https://www.mongodb.com/docs/manual/data-modeling/embedding/)
- [Transactions](https://www.mongodb.com/docs/manual/core/transactions/)
- [ESR index guideline](https://www.mongodb.com/docs/manual/tutorial/equality-sort-range-guideline/)
- [Query optimization](https://www.mongodb.com/docs/manual/core/query-optimization/)
- [Read preference](https://www.mongodb.com/docs/manual/core/read-preference/)
- [Shard keys](https://www.mongodb.com/docs/manual/core/sharding-choose-a-shard-key/)
