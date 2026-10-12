# Separate Deployments With a Shared Database

Deployment and data ownership are different boundaries. An API, worker, and
scheduler may deploy separately while belonging to one business capability.
That can be a practical runtime arrangement. Problems arise when independent
business domains quietly read and write each other's tables.

## When Sharing Is Reasonable

An order API and its background worker can use the same database if one team
owns the schema and the worker performs order-domain work. A worker may consume
an outbox record or job table and update its delivery status. The processes
still need compatible schema changes and safe retry behavior, but a separate
database would not by itself create a useful business boundary.

The important questions are:

- Who owns each business rule and table?
- Which processes may write it?
- Can a schema change roll out while old and new process versions coexist?
- Does one process need hidden access to another domain's data to do its job?

## When Sharing Blocks Independence

Suppose `Orders` and `Inventory` are presented as independent services, but
both update `orders` and `stock` tables. A change to either table can break the
other service; both teams must coordinate schema changes and releases. The
runtime cost of separate services remains, while independent change does not.

A shared physical database is not automatically the problem. Cross-domain
business writes and unowned schema changes are. Separate schemas, permissions,
and conventions can reduce coupling during migration, but they do not replace
clear ownership.

## Rules for the Transitional Stage

1. Assign one owning domain for each business fact. Its API and workers may
   write it; other domains request changes through its contract.
2. Inventory direct reads and writes across domains. Remove hidden joins and
   database triggers that couple releases.
3. Change schemas compatibly: add the new shape, deploy readers and writers
   that tolerate both versions, migrate data, then remove the old shape.
4. Keep shared operational tables narrow. An outbox or job table can serve one
   owning capability; it should not become a back door to unrelated business
   tables.
5. Watch for coordinated releases. If every routine change still requires
   several services to deploy together, the boundary has not delivered
   independence.

## Moving Toward Independent Data

Start with logical ownership while the database is shared. Stop cross-domain
writes, then replace direct reads with the approach that fits the use case.
Only then move tables or storage behind the owner if separate storage brings
operational value. Preserve compatibility while old and new paths coexist, and
verify that both read and write traffic have moved before removing access.

See [cross-domain read options](./19-database-per-service-and-read-model-migration.md)
for replacing joins. See [distributed transactions and
events](./03-distributed-transactions-and-events.md) when one workflow must
update several owners.

## Decision Rule

Separate deployment is useful for runtime reasons even with one database.
Claim independent service ownership only when routine changes can respect
exclusive business writes and evolve without hidden schema coordination.
