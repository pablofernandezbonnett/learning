# Choosing Application and Service Boundaries

A service split is useful when a business capability needs its own ownership,
release cycle, or runtime behavior. It also turns local calls into network calls
and makes cross-boundary changes harder. Decide which boundary earns that cost.

## The Smallest Useful Model

- A **modular monolith** is one deployable application with explicit internal
  modules. Modules can have clear interfaces and data ownership without being
  separate services.
- **Microservices** are separate deployables with explicit contracts and
  ownership of their business data. The value is independent change and
  operation where that independence is needed.
- Both architectures can depend on databases, queues, and external providers.
  Those dependencies still have network failures even with one application.

One deployable does not mean one undifferentiated codebase. Several deployables
do not automatically mean independent services.

## When a Split Helps

Look for a specific constraint that an internal module cannot solve well:

| Signal | Check before extracting |
| --- | --- |
| Teams repeatedly block each other's releases | Can clearer module ownership or a safer release process remove the friction? |
| One capability needs a different scaling or availability policy | Is that load measured, and can the current runtime handle it more simply? |
| A stable business capability changes independently | Can it own its writes and expose a small contract? |
| A different security or compliance boundary is required | Which data and operations must actually be isolated? |

If the domain and boundaries still change together, a modular monolith often
makes those changes easier. If a boundary is stable and independent operation
has measurable value, extraction may be worth the cost.

## What the Split Costs

Inside one process, a call is local and several writes may fit in one database
transaction. Across services, calls can time out or succeed without a response;
each service commits its own writes. You need explicit timeout and retry rules,
observable requests, compatibility between deployed versions, and a recovery
plan for partial work. More services also mean more deployments and operational
ownership.

Do not split solely because a codebase is large. First identify whether the
problem is poor internal boundaries, slow delivery, uneven load, or a genuine
need for independent ownership.

## Small Example: Orders and Inventory

Initially, `orders` and `inventory` can be modules in one application. The
order module requests a reservation through an inventory interface; only the
inventory module changes stock. This keeps the boundary visible while the
workflow is still easy to change.

Suppose inventory later has a separate team, load pattern, and release cycle.
It may become a service that owns reservations. The order side calls its
contract and records an order state that reflects success, failure, or pending
work. A timeout is not treated as proof that the reservation failed: the
caller needs a safe way to check or retry the outcome.

The new deployment boundary helps only if inventory changes no longer require
direct writes to order tables or coordinated releases for routine changes.

## Extract Without Losing the Boundary

1. Define the capability and its owner inside the current application. Keep
   callers behind an interface and remove direct cross-module writes.
2. Confirm the reason to extract with release, load, reliability, or security
   evidence. Name the operational owner.
3. Define the external contract, write ownership, failure behavior, and version
   compatibility before moving traffic.
4. Move one flow at a time. Keep old and new versions compatible during the
   rollout; observe errors and have a rollback path.
5. Split storage only when ownership and migrations are clear. A shared
   database can be transitional; exclusive write ownership matters first.

For the shared-database stage, see [separate deployments with a shared
database](./18-shared-db-and-independent-deployments.md). For cross-domain
reads after ownership splits, see [read model
migration](./19-database-per-service-and-read-model-migration.md). For a
workflow that crosses transaction boundaries, see [distributed transactions
and events](./03-distributed-transactions-and-events.md).

## Decision Rule

Keep the smallest deployment boundary that lets teams change and operate the
system safely. Extract a service when its independent ownership solves a real
problem and the team can carry the communication, consistency, and operations
cost.
