# Cross-Domain Reads After Data Ownership Splits

Once each domain owns its business writes, an old SQL join across domain tables
needs a new home. The choice depends on whether the read needs current data,
fast repeated access, or analytical history. A separate physical database is
not the prerequisite; an explicit data owner is.

## Choose the Read Path

| Need | Approach | Main cost |
| --- | --- | --- |
| A detail page needs a few current values | Compose responses from the owning APIs | Latency and availability depend on live calls |
| A busy screen needs combined data quickly | Store a projection built from owners' changes | Data can lag; updates and rebuilds need care |
| Reporting scans large histories across domains | Copy data to a warehouse or reporting store | Data is delayed and must be reconciled |

**API composition** means one backend calls the owners and combines their
responses for the client. Start here for a small number of calls and a need
for current status. Set timeouts and decide what the screen shows when one
owner is unavailable; a long chain of live calls can make the page fragile.

A **projection** is a read-only copy shaped for a query, such as an order list
containing payment and shipment status. It is built from changes published by
the owning domains. It speeds up repeated reads but may show older information.
It must not become another place to edit the business truth.

A reporting store serves analytical queries and broad historical scans. It is
usually a poor source for an operational decision that needs the latest state.

## Small Example: An Admin Order Screen

`Orders` owns the order, `Payments` owns payment attempts, and `Fulfillment`
owns shipment state. A detail page can call the three owners through an admin
backend and show a partial result if shipment status is temporarily unavailable.

An order-search screen read thousands of times per minute may instead use a
projection with the fields needed for search and display. Each owner publishes
relevant changes; the projection updates its copy. The screen shows when the
copy was last refreshed if delay matters to the user. A payment action still
goes to `Payments`, not to the projection.

## Keep Projections Correct Enough

- Publish changes reliably. If a business write commits but its event is lost,
  the projection drifts; an outbox can close that gap.
- Handle duplicate and out-of-order delivery. Apply updates idempotently and
  use a version or ordering key where one record's sequence matters.
- Plan rebuild and reconciliation. Keep a way to replay or reload from owners
  and compare important counts or states.
- Define tolerated lag per use case. Search results may accept delay; a
  payment confirmation may require a fresh owner read.

The [message broker notes](../api/02-message-brokers-and-delivery-semantics.md)
cover delivery behavior. The [distributed workflow
note](./03-distributed-transactions-and-events.md) covers outbox and
cross-owner consistency.

## Replace an Existing Cross-Domain Join

1. Identify the screen or process using the join and its freshness, traffic,
   and failure requirements.
2. Define a read contract with the data owners. Use API composition first when
   a few live calls meet those requirements.
3. Add a projection only when measured read volume, latency, or availability
   makes composition a poor fit.
4. Run old and new reads together long enough to compare results. Account for
   expected projection lag, then remove the direct table read.

The [shared-database migration note](./18-shared-db-and-independent-deployments.md)
owns the broader write-ownership and storage transition.

## Decision Rule

Protect write ownership first. Use live composition for small fresh reads,
projections for repeated combined operational reads, and reporting stores for
analytics. Make freshness and failure behavior explicit for each use case.
