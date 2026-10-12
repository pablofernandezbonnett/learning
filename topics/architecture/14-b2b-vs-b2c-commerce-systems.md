# B2B and B2C Commerce: Model the Buyer First

Use this example to see how a change in **who buys** changes a system design.
`B2C` sells to an individual; `B2B` sells to an organization. Neither label
determines a fixed architecture. Discover the actual buyer, contract, and
approval rules before choosing a platform or splitting services. Start with
the [general system design method](../system-design/00-general-system-design-method.md)
for the full set of questions.

## Ask What Changes With the Buyer

| Decision | Consumer purchase may need | Business purchase may need |
| --- | --- | --- |
| Identity | Guest or personal account | Organization, location, buyer, and approver |
| Authorization | Buyer acts for themselves | Buyer acts for a specific organization and role |
| Catalogue and price | Public or promotional offer | Account-specific eligibility, contract price, or volume tier |
| Commitment | Immediate checkout | Quote, budget check, purchase order, or approval |
| Payment | Payment during checkout | Invoice, credit terms, or payment during checkout |
| Integration | Payment and delivery providers | Procurement or accounting systems as well |

These are questions, not universal B2B/B2C rules. A consumer sale may use
invoicing; a business sale may be a simple card purchase. Model only the
rules the product actually promises.

## Example: One SKU, Two Buying Contexts

A public customer sees a listed price. A company buyer may see a contract
price for the same SKU and need manager approval above a spending limit.
The design has three immediate consequences:

1. **Price is contextual.** The pricing decision needs the buyer's verified
   organization, applicable agreement, quantity, currency, and time. Store
   the accepted price and its basis on the order so later contract changes
   do not rewrite what was agreed.
2. **Permission is contextual.** Every catalogue, quote, cart, and order read
   must check which organization the actor represents. Hiding a price in the
   UI does not protect an API that returns it to the wrong buyer.
3. **Approval changes state.** `PENDING_APPROVAL` is not a completed order.
   Define who may approve, what changes require reapproval, and when payment
   or stock commitment happens.

This is a useful [bounded-context](./09-domain-driven-design.md) exercise:
customer identity, pricing agreements, orders, and inventory can own distinct
facts even if the first implementation is one application.

## Design the Flow Before the Platform

Trace one business purchase from login to fulfillment:

1. Resolve the actor and organization; authorize the action for that pair.
2. Calculate an offer from the applicable catalogue and pricing rules.
3. Record the order or quote with a stable ID and accepted terms.
4. Apply approval and payment rules; make retries safe and show pending states.
5. Hand confirmed work to fulfillment and reconcile failures across owners.

If a procurement system sends a purchase order, decide which side owns its
status and identifiers. Translate external messages at the boundary; do not
make their schema the internal order model. See [integration boundaries](./13-enterprise-integration-patterns.md)
and [cross-service recovery](./03-distributed-transactions-and-events.md).

## Choose Channel Boundaries Deliberately

One product may serve public and business channels. Shared catalogue data or
inventory does not require shared prices, permissions, or checkout rules.
Before choosing one storefront, several storefronts, or separate platforms,
ask:

- Can every read and write enforce the correct buyer and organization scope?
- Which facts are shared, and who alone may change each one?
- Do the channels need different release pace, contracts, or operational limits?
- What will happen if one channel's traffic spikes or an integration is down?

Keep a shared backend when it can enforce those boundaries clearly and meet
the operational needs. Separate components when independent change or failure
is worth the added synchronization and support cost. Validate any vendor's
current capabilities against the actual approval, pricing, and integration
requirements rather than assuming a platform label settles the choice.

**Reusable lesson:** Change the actor and agreement in a familiar flow, then
revisit authorization, source of truth, state transitions, failure handling,
and capacity. Those questions apply beyond commerce.
