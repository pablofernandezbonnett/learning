# Inventory and Fulfillment: Separate Promise From Physical Work

Retail is a useful system-design example because a digital promise must match
physical stock and work. The central rule is: do not promise the same limited
unit to two orders. Apply the [general design method](../system-design/00-general-system-design-method.md)
first; this note focuses on the decisions that physical inventory adds.

## Identify Each Source of Truth

Ask who owns these facts before designing APIs or queues:

| Fact | Typical owner | Why it matters |
| --- | --- | --- |
| Physical count or movement | Warehouse or store operation | Receipts, picks, damage, and recounts change reality. |
| Sellable availability | Inventory capability | It combines stock, reservations, buffers, and policy. |
| Customer order state | Order capability | It records what was promised and what remains pending. |
| Pick, pack, and dispatch state | Fulfillment operation | Physical work may finish long after checkout. |

The exact owners vary. If two systems can change the same sellable quantity
without coordination, a cache or extra application replicas will not make the
promise reliable.

Availability is a policy-derived number, not simply the count on a shelf. A
system might subtract active reservations and a safety buffer from eligible
stock. Returns, damaged units, store pickup, and stock in transit may each
have different eligibility rules. Make those rules explicit.

## Separate Visibility From Commitment

A product page can show a recent availability projection. Its job is to help
customers browse; it may be stale. The final reservation or stock commitment
must check the current sellable state with a guarded write in the inventory
owner. Define what the buyer sees when that check fails.

For example, two customers may both see “one left.” Only one reservation can
succeed. The other receives a clear unavailable result instead of an order
that silently cannot be fulfilled. A reservation needs an ID, a lifetime or
release rule, and a safe retry path. Expired or cancelled reservations must be
released without releasing stock already committed to another order.

## Route and Track Physical Work

After checkout accepts an order, choose a fulfillment location from eligible
stock, delivery promise, cost, and operating capacity. Send the work to a
warehouse or store and track acceptance, picking, dispatch, and exceptions.
The order owner records the customer-facing promise; the fulfillment owner
records the physical progress.

A timeout does not prove that a pick request failed. Use a stable work ID,
query or reconcile an unknown result, and make repeated commands safe. The
[checkout case study](../system-design/checkout-to-fulfillment-end-to-end.md)
covers the wider payment and order flow; the [cross-service workflow guide](./03-distributed-transactions-and-events.md)
covers recovery mechanics.

## Design for Physical Exceptions

A warehouse may report a missing unit after an order was confirmed. Decide
whether to reroute, backorder, substitute, cancel, or ask the customer. The
order must show that exception; retries should not create a second pick.
Returns add another state transition: a received item is not necessarily
sellable until inspection or another policy check.

During a flash sale, browse traffic and reservation writes have different
limits. Scale read projections independently, control admission to the scarce
write path, and measure reservation contention, stale availability, pending
orders, and fulfillment backlog. The goal is to protect the stock promise
under load, not merely return fast product pages.

**Reusable lesson:** Whenever a system promises a scarce or slow-to-deliver
resource, separate discovery from commitment, name the final decision owner,
and model the work that can fail after the promise is made.
