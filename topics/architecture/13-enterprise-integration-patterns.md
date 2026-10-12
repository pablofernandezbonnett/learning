# Integrating With External and Legacy Systems

An external platform may use different identifiers, states, timing, and
availability from your application. The integration design must say how data
is translated, which system owns each fact, when updates move, and how missed
updates are repaired.

## Keep the Models Separate

An **anti-corruption layer** is an adapter that translates between your domain
model and an external model. It prevents provider field names and workflow
rules from spreading through your business code.

Example: your order has `storeId` and `CONFIRMED`; a warehouse system expects
`siteCode` and a different status vocabulary. The adapter maps the two and
handles unsupported values explicitly. The order domain still speaks its own
language. The cost is mapping code that must evolve when either contract
changes.

Do not assume both systems agree just because an API call returned success.
Record the external identifier and the last confirmed integration state so a
later retry or reconciliation can identify the same business item.

## Decide Who Owns Each Fact

Before moving data, assign an owner for orders, stock, prices, or any other
shared concept. A receiving system may hold a copy, but it should be clear
which system wins when copies disagree and who can correct the difference.

For example, if a warehouse owns fulfillment status, the order application
can display a copy but should not invent a shipment confirmation locally.
If your service owns the order, the warehouse response should not silently
rewrite unrelated order rules.

## Choose the Update Cadence

| Need | Common approach | Main tradeoff |
| --- | --- | --- |
| The user needs an answer before continuing | Synchronous call with a timeout | The request depends on external availability |
| Another system needs the change soon | Durable asynchronous handoff | Delay, retries, and duplicates need handling |
| Large volume can arrive later | Restartable batch or file transfer | Data is stale until the next run |

Near-real-time does not mean every update must be instantaneous. Define an
acceptable delay from the business decision: a stock display may need a much
shorter window than a nightly finance report. Use the [sync/async decision
guide](../api/07-sync-vs-async-integration-choice.md) for the request boundary.

## Make Transfers Recoverable

- Give each transfer a stable identifier, version, and owner. A retry should
  update or recognize the same item instead of creating another one.
- Record success, failure, and last attempt. Set a retry limit for temporary
  errors and send permanent mapping or validation failures for repair.
- Reconcile periodically: compare important counts or records with the owner
  and repair missing or conflicting updates. A delivered message is not proof
  that both systems now agree.
- For batch, process in bounded chunks and checkpoint progress. Make reruns
  safe for a file, time window, or item range. See [production batch
  jobs](../spring-boot/25-spring-batch-production-jobs.md) for implementation.

If a local business write must reliably trigger a later transfer, use the
[outbox mechanics](./03-distributed-transactions-and-events.md) rather than
assuming a database commit and external call succeed together.

## Keep Reporting Off the Live Path

Heavy cross-domain reporting belongs in a reporting store or warehouse fed
from owned data. Define its freshness and reconciliation rules; do not use a
delayed analytical copy as the source for a live business decision. See
[cross-domain read choices](./19-database-per-service-and-read-model-migration.md).

## Decision Rule

Protect the domain model with translation, name the owner of each fact, choose
timing from business urgency, and make retries and reconciliation explicit.
