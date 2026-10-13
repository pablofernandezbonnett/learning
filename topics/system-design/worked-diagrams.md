# Worked System Design Diagrams

Each diagram answers one question that is easy to miss in a happy-path
architecture sketch. Read the [checkout case](./checkout-to-fulfillment-end-to-end.md)
for the full flow and the [design drills](./system-design-drills.md)
to practice choosing a mechanism. These diagrams focus on where the final
decision is made and what happens after a retry or partial failure.

## Where do the checkout decisions live?

This is a map of responsibilities, not a requirement for separate deployable
services. The order store owns request identity and order state; Inventory
owns the final stock reservation; the payment provider owns its own outcome.
The outbox starts later work only after the local order change commits.

```mermaid
flowchart LR
    Buyer[Buyer] --> API[Checkout API]
    API --> Orders[Orders and request identity]
    Orders --> OrderDB[(Order store and outbox)]
    API --> Stock[Inventory owner]
    Stock --> StockDB[(Sellable stock)]
    API --> Pay[Payment provider]
    OrderDB --> Relay[Outbox relay]
    Relay --> Fulfill[Fulfillment worker]
```

The arrows do not imply one distributed transaction. A provider timeout leaves
an attempt pending until lookup or reconciliation establishes the result.

## How can browse stock be fast without promising the last unit?

A browse copy may lag behind the stock owner. Both online checkout and the
store terminal send the final reservation or sale to the same guarded write;
neither trusts the browse number to decide who gets the last unit.

```mermaid
flowchart LR
    Online[Online shopper] --> Browse[Browse API]
    Browse --> Copy[(Stock read copy)]
    Online --> Reserve[Reserve command]
    Store[Store terminal] --> Reserve
    Reserve --> Owner[Inventory owner]
    Owner --> DB[(Authoritative stock)]
    DB -. committed updates .-> Refresh[Read-copy updater]
    Refresh --> Copy
```

This copy earns its place only when measured read load or query shape needs it.
Its lag may change what is displayed, but the guarded write prevents oversell.

## Which order states require follow-up?

The state model distinguishes an unknown outcome from a confirmed failure.
It also keeps a warehouse exception visible after checkout has succeeded.
Names and transitions are product choices; the important rule is that an old
callback cannot move a confirmed order back to pending.

```mermaid
stateDiagram-v2
    [*] --> Pending: purchase intent accepted
    Pending --> Pending: provider result unknown; reconcile
    Pending --> Rejected: conclusive payment or stock failure
    Pending --> Confirmed: payment and stock confirmed
    Confirmed --> InFulfillment: worker accepts order
    InFulfillment --> Shipped: dispatch confirmed
    InFulfillment --> FulfillmentException: pick cannot finish
    FulfillmentException --> InFulfillment: resolved and retried
```

## How do two checkout requests avoid creating two orders?

The application first claims a request identity with a database uniqueness
constraint. The claim and pending order are committed together. A second
request with the same key and payload finds the same operation; a different
payload with that key is rejected.

```mermaid
sequenceDiagram
    actor Buyer
    participant API as Checkout API
    participant DB as Order database
    participant Pay as Payment provider

    Buyer->>API: confirm cart, key K
    API->>DB: transaction: unique claim K + create PENDING order O1
    DB-->>API: committed O1
    API->>Pay: authorize with stable attempt ID A1
    Note over API,Pay: Response is lost; provider outcome is unknown
    API->>DB: keep attempt A1 pending for reconciliation
    API-->>Buyer: O1 is PENDING
    Buyer->>API: retry same cart, key K
    API->>DB: claim K
    DB-->>API: existing K maps to O1
    API-->>Buyer: O1 is PENDING
```

The key prevents a duplicate *local* order. The provider effect also needs
a stable attempt ID and a safe retry or lookup path. See
[transaction-safe idempotency](../databases/01-idempotency-and-transaction-safety.md).

## What if event publication succeeds just before a crash?

An **outbox** stores the event in the same transaction as the order change.
The relay may publish twice when it cannot record that the first publish
succeeded. The consumer must recognize the event ID and avoid repeating its
business effect.

```mermaid
sequenceDiagram
    participant App as Order use case
    participant DB as Order database
    participant Relay as Outbox relay
    participant Broker as Message broker
    participant Worker as Fulfillment worker

    App->>DB: transaction: confirm O1 + outbox event E1
    DB-->>App: committed
    Relay->>DB: read unpublished E1
    Relay->>Broker: publish E1
    Broker-->>Relay: acknowledged
    Broker->>Worker: deliver E1
    Worker->>Worker: claim E1 atomically with local work record
    Note over Relay: Crashes before marking E1 published
    Relay->>DB: restart; read unpublished E1
    Relay->>Broker: publish E1 again
    Broker->>Worker: deliver E1
    Worker->>Worker: E1 already applied; no second work record
```

The outbox protects local commit to publication; the worker still needs its
own duplicate handling. Calling an external warehouse adds another retry and
reconciliation boundary. See [workflow recovery](../architecture/03-distributed-transactions-and-events.md).

## Why can “one left” lead to an unavailable result?

A browse projection may be stale. Only a guarded write at the inventory owner
can reserve the final unit; the product page cannot make that promise.

```mermaid
sequenceDiagram
    actor BuyerA as Buyer A
    actor BuyerB as Buyer B
    participant Read as Browse projection
    participant Stock as Inventory owner

    BuyerA->>Read: view SKU
    Read-->>BuyerA: one unit appears available
    BuyerB->>Read: view SKU
    Read-->>BuyerB: one unit appears available
    BuyerA->>Stock: reserve SKU, operation A
    Stock->>Stock: guarded write; one unit remains
    Stock-->>BuyerA: reservation A accepted
    BuyerB->>Stock: reserve SKU, operation B
    Stock->>Stock: guarded write; no sellable unit remains
    Stock-->>BuyerB: unavailable
```

The design must tell Buyer B the truth and later refresh the browse view.
See [inventory and fulfillment](../architecture/15-retail-inventory-and-fulfillment-systems.md).
