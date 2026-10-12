# System Design Drills

Use these prompts after the [general design method](./00-general-system-design-method.md).
Try each one before reading its decision check. The point is to practise a
reasoned choice under a constraint, not to memorize a diagram.

For each drill, state the promise, the owner of the final decision, what the
caller sees, what can be repeated, and how unfinished work is found.

## 1. A Payment Request Times Out

**Prompt:** A payment authorization request times out. The provider may have
processed it, and the customer retries checkout. What should the API return?
Can it send another authorization? How will it learn the original result?

**Decision check:** Keep the local attempt and order pending. Use a stable
operation ID to query the provider or retry idempotently if supported.
Reconcile attempts that never receive a conclusive response. A timeout means
*unknown*, not *declined*. See the [checkout case](./checkout-to-fulfillment-end-to-end.md)
for the full flow.

## 2. An Order Commits but Publication Fails

**Prompt:** A confirmed order should start fulfillment through an event. The
process crashes between committing the order and publishing the event. What
belongs in the local transaction? What if the publisher crashes after the
broker accepts the event?

**Decision check:** Commit an outbox record with the order. Relay it later;
publication may repeat after a crash, so use a stable event ID and a consumer
that records its local effect once. External follow-up needs its own retry
safety. Monitor unpublished-event age. The outbox closes the local
database-to-broker gap, not every later workflow gap.
See [cross-service recovery](../architecture/03-distributed-transactions-and-events.md).

## 3. Callbacks Arrive Late or Out of Order

**Prompt:** A payment provider sends a success callback twice, then an older
pending callback. Fulfillment has already begun. Which message may change
local state, and when must you query the provider?

**Decision check:** Authenticate and durably accept the callback; deduplicate
its ID and apply only a valid business transition. An older message must not
silently move a confirmed order backward. If arrival order cannot establish
the actual outcome, reconcile against the provider's record. See
[webhook intake](../api/03-webhooks-basics.md).

## 4. The Last Units of One SKU Are Contended

**Prompt:** A flash sale sends many reservation requests for a few remaining
units. More application replicas do not improve the final write. Must the
buyer get an immediate result, or may the request remain pending?

**Decision check:** Keep the guarded reservation at the inventory owner.
Control admission and measure contention. For immediate confirmation, bound
concurrent writes and return a clear unavailable result. If pending is
acceptable, a queue can smooth intake, but workers still need a safe final
write. A stale browse projection never promises the last unit. See
[inventory and fulfillment](../architecture/15-retail-inventory-and-fulfillment-systems.md).

## 5. Search Reads Threaten Catalogue Writes

**Prompt:** Catalogue writes are reliable, but search queries are slow and
compete with them. How stale may search results be? Would an index, cache,
projection, or dedicated search store solve the measured problem?

**Decision check:** Keep catalogue writes with their owner. Measure and tune
the current queries first. If the read shape or load remains different,
build a projection and state its lag and rebuild path. Search results may be
stale; the search copy does not become the final product record. See
[cross-domain read choices](../architecture/19-database-per-service-and-read-model-migration.md).

## Self-Review

After each answer, check that you can explain:

- the invariant and source of truth;
- the immediate response and later work;
- a retry, duplicate, timeout, and partial failure;
- the main security or access boundary;
- the first likely capacity limit, an observable signal, and an operator recovery path;
- why the chosen mechanism earns its complexity.

The [worked diagrams](./worked-diagrams.md) show three of these decisions
visually. Redraw one only if the drawing exposes a boundary your explanation
missed.
