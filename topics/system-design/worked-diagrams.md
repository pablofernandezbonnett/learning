# Worked System Design Diagrams

Each diagram answers one question that is easy to miss in a happy-path
architecture sketch. Read the [checkout case](./checkout-to-fulfillment-end-to-end.md)
for the full flow and the [design drills](./system-design-drills.md)
to practice choosing a mechanism. These diagrams focus on where the final
decision is made and what happens after a retry or partial failure.

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
