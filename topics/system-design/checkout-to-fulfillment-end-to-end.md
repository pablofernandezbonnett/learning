# Checkout to Fulfillment: One Complete System Design

This case applies the [general design method](./00-general-system-design-method.md)
to a purchase. The reusable task is to find the promise, its owner, and the
failure paths before choosing components. Checkout makes those questions
concrete because money, limited stock, and physical delivery can disagree.

## 1. Define the Promise

A customer submits a cart and eventually learns whether an order was accepted.
The first response may be `PENDING` if a payment or stock result is unknown.
For this example, protect these rules:

- One purchase intent cannot create two accepted orders or duplicate payment effects.
- A confirmed order has the required payment outcome and a valid stock commitment.
- Limited stock is not promised to two orders at once.
- The customer and support can inspect the same durable order state.

Set product-specific targets too: acceptable checkout latency, how long an
order may remain pending, recovery time for stuck work, expected peak writes,
and the cost of rejecting a sale versus overselling. Those targets decide
whether confirmation can wait in the request or must finish later.

## 2. Name the Owners and Boundaries

| Fact or decision | Owner in this example | Boundary question |
| --- | --- | --- |
| Buyer and cart access | Checkout API | May this actor submit this cart? |
| Final price and terms | Pricing | What offer is valid for this buyer now? |
| Order state | Orders | What was accepted, and what remains pending? |
| Sellable stock | Inventory | Can this quantity be reserved without overselling? |
| Payment outcome | Payment integration | What did the provider actually do? |
| Pick and dispatch progress | Fulfillment | What physical work has completed? |

These are logical responsibilities, not a requirement for six services. A
modular application can own several of them in one database; an external
payment provider still sits outside its transaction. If owners are split,
each keeps its own final write decision. See [service boundaries](../architecture/01-monolith-vs-microservices.md).

## 3. Define the API and Trust Boundary

An example write contract is `POST /checkout/confirm` with a cart reference
and a stable idempotency key. The server verifies the actor's access to the
cart, recalculates the authoritative price, and checks that the requested
terms are still valid. A client-supplied total or order status is not a source
of truth.

Claim the idempotency key **atomically** with a unique constraint, scoped to
the actor or purchase context. Store a request fingerprint and the resulting
order or operation ID. A retry with the same key and payload returns the same
operation; the same key with different payload is a conflict. This prevents
two concurrent requests from both passing a read-before-write check. Use a
stable operation ID for provider calls as well, where the provider supports
idempotent requests. See [API write semantics](../api/01-api-design.md) and
[transaction-safe idempotency](../databases/01-idempotency-and-transaction-safety.md).

The response describes the durable state: confirmed, rejected, or pending.
Provide a way to read the order later. Authenticate provider callbacks,
deduplicate their deliveries, authorize order reads, limit abusive requests,
and avoid storing raw card details or logging secrets. See [webhooks](../api/03-webhooks-basics.md).

## 4. Trace One Purchase

The sequence of stock reservation and payment authorization depends on the
product's cost of holding stock or authorizing funds. This example does not
assume that both can commit in one transaction:

1. In a local transaction, claim the request key and create a `PENDING` order
   with the accepted price and a payment-attempt record.
2. Ask Inventory to reserve stock with a stable reservation ID. Inventory
   performs a guarded write against its sellable quantity. A browse cache may
   show recent availability, but it cannot make this final decision.
3. Ask the payment provider to authorize with a stable attempt ID. Do not
   hold the local database transaction open across this network call.
4. Record each known outcome. Confirm the order only when the required
   payment and stock outcomes are known. If either result is unknown, keep a
   pending state and reconcile it; a timeout is not proof of failure.
5. If a required step clearly fails, release the stock hold or void the
   authorization as appropriate. Track compensation until it succeeds or
   someone resolves it.
6. Commit the confirmed order state with an outbox record in the same local
   transaction. A worker publishes the fulfillment request after commit.

Here, **idempotency** makes repeated commands safe; an **outbox** closes the
gap between a local commit and publishing follow-up work; **compensation**
reverses a completed business step when a later step cannot finish. None
turns the provider and local database into one transaction. See
[cross-service workflow recovery](../architecture/03-distributed-transactions-and-events.md).

## 5. Follow the States, Including Failure

| Observation | Order behavior | Recovery |
| --- | --- | --- |
| Stock unavailable | Reject the purchase | Do not authorize payment, or void an authorization already made. |
| Provider clearly declines | Do not confirm the order | Release the reservation. |
| Provider times out | Stay pending | Query or reconcile using the stable attempt ID before retrying. |
| Duplicate request or callback | Return or preserve the recorded outcome | Do not repeat order, stock, or money effects. |
| Order confirmed; event not published | Keep the confirmed order | Retry the outbox relay; consumers tolerate duplicate events. |
| Warehouse cannot pick | Show a fulfillment exception | Reroute, backorder, substitute, or cancel according to policy. |

The precise names of order, payment, and reservation states may differ. Keep
their transitions explicit and test forbidden moves: for example, a late
`payment declined` callback must not silently undo a confirmed order. If an
external result conflicts with local state, investigate and reconcile it.

Fulfillment acknowledges work and reports pick, pack, dispatch, and exception
progress asynchronously. A repeated work command must not cause a second
physical pick. See [inventory and fulfillment](../architecture/15-retail-inventory-and-fulfillment-systems.md)
for the stock and warehouse decisions.

## 6. Add Reads, Capacity, and Operation

Order history, support screens, and fulfillment queues can start as ordinary
database queries. Add projections when their query shape or load justifies
them; they may lag behind the order write path. Keep the final order decision
with its owner rather than letting a dashboard copy decide it.

Under a sale spike, measure checkout admission, inventory write contention,
payment-provider limits, database latency, and worker backlog. Scale the
browse path separately from the guarded reservation path. Bound retries and
queue growth so a slow dependency does not turn into unbounded work.

Observe pending orders by age, payment outcomes unknown to the local system,
reservation failures, failed compensations, outbox age, and fulfillment
exceptions. Give operators a way to inspect a purchase by stable IDs and
repair a stuck case without issuing a second charge.

## What Transfers to Other Systems

The same method applies to booking a seat, allocating a scarce resource, or
starting a long-running job: name the promise, assign the final decision
owner, make retries safe, represent unknown outcomes honestly, and track work
that continues after the first response.
