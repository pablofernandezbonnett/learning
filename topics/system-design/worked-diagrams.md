# System Design Diagrams

> Primary fit: `Retail / Ecommerce`

Status:

- companion file for the system-design notes in this folder
- useful for deeper retail and workflow study after the main guide
- best used as visual support, not as the first entry point

Mermaid diagrams for worked system design exercises.
Paste any block into a Mermaid renderer (GitHub markdown, Obsidian, draw.io, mermaid.live).

Quick term guide for this companion file:

- `BFF` = `Backend For Frontend`: an edge backend that shapes one response for one client type
- `TTL` = `time to live`: how long a cache entry or temporary reservation lives before expiry
- `CDC` = `Change Data Capture`: copying committed database changes into another system
- `Debezium` = a common tool that reads database change logs and publishes those changes as events
- `WAL` = `Write-Ahead Log`: the database change log Debezium reads in Postgres
- `SSE` = `Server-Sent Events`: one-way server-to-browser streaming over HTTP
- `WMS` = `Warehouse Management System`

---

## Exercise 1: Global Checkout System

### 1a. Component Architecture

```mermaid
graph TB
    subgraph Client
        WEB[Web Browser]
        APP[Mobile App]
    end

    subgraph Edge
        CDN[CDN / WAF]
        APIGW[API Gateway<br/>Rate Limit + Auth]
    end

    subgraph Services
        CART[Cart Service<br/>Redis]
        INV[Inventory Service<br/>PostgreSQL]
        PRICE[Pricing Service<br/>Lua Rules Engine]
        PAY[Payment Service<br/>Stripe / PayPal]
        ORDER[Order Service<br/>PostgreSQL]
        NOTIF[Notification Service<br/>Email / Push]
    end

    subgraph Messaging
        MQ[Message Broker<br/>Kafka]
    end

    subgraph Storage
        REDIS[(Redis<br/>Sessions + Cache)]
        PG[(PostgreSQL<br/>Orders + Inventory)]
        S3[(S3<br/>Receipts + Exports)]
    end

    WEB --> CDN --> APIGW
    APP --> CDN

    APIGW --> CART
    APIGW --> INV
    APIGW --> PRICE
    APIGW --> ORDER
    APIGW --> PAY

    CART --> REDIS
    INV --> PG
    ORDER --> PG
    ORDER --> MQ

    MQ --> NOTIF
    MQ --> S3

    PRICE -.->|Lua scripts| S3
```

### 1a-bis. Gateway With Internal gRPC Aggregation

Use this variant when the public edge still speaks normal HTTP, but the gateway or `BFF`
(`Backend For Frontend`) has to call several internal services owned by the same organisation.

```mermaid
graph TB
    subgraph Client
        WEB[Web Browser]
        APP[Mobile App]
    end

    subgraph Edge
        CDN[CDN / WAF]
        BFF[API Gateway / BFF<br/>Backend For Frontend<br/>REST or GraphQL outward]
    end

    subgraph Internal Services
        ORDER[Order Service]
        INV[Inventory Service]
        PRICE[Pricing Service]
        AUTH[Auth / Policy Service]
    end

    WEB --> CDN --> BFF
    APP --> CDN --> BFF

    BFF -->|gRPC unary| ORDER
    BFF -->|gRPC unary| INV
    BFF -->|gRPC unary| PRICE
    BFF -->|gRPC unary| AUTH
```

Protocol note:

- browser or public app traffic usually stays `REST/JSON` or `GraphQL` at the edge
- internal gateway-to-service calls can be `gRPC` if you want typed contracts and low-overhead one-to-many backend calls from the gateway to several services
- do not force `gRPC` if the backend is still one deployable service or if the main consumers are browsers

### 1b. Checkout Flow (Happy Path)

```mermaid
sequenceDiagram
    actor Customer
    participant API as API Gateway
    participant Cart as Cart Service
    participant Inv as Inventory Service
    participant Price as Pricing Service
    participant Pay as Payment Service
    participant Order as Order Service
    participant MQ as Kafka

    Customer->>API: POST /checkout
    API->>Cart: GET cart contents
    Cart-->>API: items[]

    API->>Price: calculateTotal(items, customerTier)
    Note over Price: Lua rules engine<br/>applies discounts
    Price-->>API: finalAmount

    API->>Inv: reserveStock(items)
    Note over Inv: Soft hold (TTL: 15 min)
    Inv-->>API: reservationId

    API->>Pay: authorize(amount, paymentMethod)
    Pay-->>API: authorizationToken

    API->>Order: createOrder(cart, reservation, auth)
    Order-->>API: orderId

    API->>Pay: capture(authorizationToken)
    Pay-->>API: transactionId

    API->>Inv: confirmReservation(reservationId)
    API->>Order: updateStatus(orderId, PAID)

    Order->>MQ: OrderConfirmed event

    API-->>Customer: 201 Created {orderId}
```

### 1c. Failure Scenarios

```mermaid
flowchart TD
    START([Checkout Request]) --> RESERVE[Reserve Stock]

    RESERVE -->|Success| AUTH[Authorize Payment]
    RESERVE -->|Fail: out of stock| ERR1[400 Insufficient Stock]

    AUTH -->|Success| CREATE[Create Order]
    AUTH -->|Fail: declined| REL1[Release Stock Reservation]
    REL1 --> ERR2[402 Payment Declined]

    CREATE -->|Success| CAPTURE[Capture Payment]
    CREATE -->|Fail| REL2[Release Reservation + Void Auth]
    REL2 --> ERR3[500 Order Creation Failed]

    CAPTURE -->|Success| CONFIRM[Confirm Reservation]
    CAPTURE -->|Fail| COMP[Compensate:<br/>Release Reservation<br/>+ Update Order → FAILED]
    COMP --> ERR4[500 Payment Capture Failed]

    CONFIRM --> DONE([Order Complete])
```

---

## Exercise 2: Omnichannel Inventory

### 2a. Inventory Architecture

```mermaid
graph TB
    subgraph Channels
        ONLINE[Online Store<br/>next.js]
        POS[POS Terminal<br/>store associate]
        APP[Mobile App]
    end

    subgraph Core
        INVAPI[Inventory API<br/>Spring Boot]
        INVDB[(PostgreSQL<br/>Source of Truth)]
        CACHE[(Redis Cache<br/>Read Replica TTL: 30s)]
    end

    subgraph Events
        KAFKA[Kafka<br/>StockUpdated events]
        SEARCH[Search Index<br/>Elasticsearch]
        ANAL[Analytics<br/>BigQuery]
    end

    ONLINE -->|read stock| CACHE
    APP -->|read stock| CACHE
    POS -->|sell item| INVAPI

    INVAPI -->|write| INVDB
    INVDB -->|DB change stream via CDC and Debezium| KAFKA
    KAFKA -->|update| CACHE
    KAFKA -->|index| SEARCH
    KAFKA -->|stream| ANAL

    CACHE -.->|miss fallback| INVDB
```

### 2b. Stock Reservation State Machine

```mermaid
stateDiagram-v2
    [*] --> Available: product created

    Available --> SoftHeld: reserve(qty, TTL=15min)
    SoftHeld --> Available: TTL expired / released
    SoftHeld --> Committed: payment captured
    Committed --> Available: order cancelled (return qty)
    Committed --> Shipped: fulfillment confirmed
    Shipped --> Delivered: delivery confirmed
    Delivered --> Available: return processed (restock)
    Delivered --> [*]: no return

    note right of SoftHeld
        Prevents overselling:
        online stock = physical - soft holds
    end note

    note right of Committed
        Hard reservation:
        stock decremented in DB
    end note
```

### 2c. Cache Invalidation Strategy

```mermaid
sequenceDiagram
    participant POS as POS Terminal
    participant API as Inventory API
    participant DB as PostgreSQL
    participant CDC as Debezium<br/>DB change stream
    participant MQ as Kafka
    participant Cache as Redis Cache
    participant Web as Web Store

    POS->>API: POST /inventory/{sku}/sell {qty: 1}
    API->>DB: UPDATE stock SET qty = qty - 1 WHERE sku = ?
    DB-->>API: OK
    API-->>POS: 200 OK

    DB->>CDC: committed database change from WAL
    CDC->>MQ: StockUpdated {sku, newQty, storeId}
    MQ->>Cache: INVALIDATE stock:{sku}
    Note over Cache: Next read will miss → fallback to DB

    Web->>Cache: GET stock:J001
    Cache-->>Web: MISS
    Web->>DB: SELECT qty FROM inventory WHERE sku = 'J001'
    DB-->>Web: {qty: 249}
    Web->>Cache: SET stock:J001 249 EX 30
    Cache-->>Web: OK
```

### 2d. Interface Fit Note

Use this pattern when:

- public and gateway-facing traffic should stay `HTTP/REST`
- the current backend is still one main deployable unit
- `gRPC` would only become interesting if the backend later splits into several
  internal services and the gateway or `BFF` has to call several of them for
  one frontend request

Practical implementation direction if that split ever happens:

- keep `REST` at the public edge
- add `gRPC` only for internal service-to-service contracts
- let the gateway or BFF aggregate `gRPC` calls and return one HTTP response to
  the frontend

---

## Exercise 3: Order Lifecycle

### 3a. Order State Machine

```mermaid
stateDiagram-v2
    [*] --> Created: checkout submitted

    Created --> Confirmed: payment authorized
    Created --> Cancelled: user cancels / timeout

    Confirmed --> PaymentCaptured: payment captured
    Confirmed --> Cancelled: payment capture fails

    PaymentCaptured --> PickingStarted: warehouse system (`WMS`) picks up order
    PaymentCaptured --> Cancelled: fraud review fails

    PickingStarted --> Packed: items packed
    Packed --> Shipped: carrier picks up

    Shipped --> Delivered: delivery confirmed
    Shipped --> Lost: carrier reports lost

    Delivered --> ReturnRequested: customer requests return
    Delivered --> [*]: 30-day return window closed

    ReturnRequested --> Returned: return received
    Returned --> Refunded: payment refunded
    Refunded --> [*]

    Lost --> Refunded: investigation complete
    Cancelled --> [*]
```

### 3b. Order Service Architecture

```mermaid
graph LR
    subgraph Commands
        API[Order API<br/>POST /orders]
    end

    subgraph Domain
        ORDER_SVC[Order Service<br/>state machine]
        SAGA[Saga Orchestrator<br/>compensating txns]
    end

    subgraph Storage
        PG[(PostgreSQL<br/>orders + events)]
        OUTBOX[(Outbox Table<br/>transactional publish)]
    end

    subgraph Events
        KAFKA[Kafka]
        INV_SVC[Inventory Service]
        PAY_SVC[Payment Service]
        SHIP_SVC[Fulfillment Service]
        NOTIF[Notification Service]
    end

    API --> ORDER_SVC
    ORDER_SVC --> PG
    ORDER_SVC --> OUTBOX

    OUTBOX -->|Outbox Pattern| KAFKA

    KAFKA --> INV_SVC
    KAFKA --> PAY_SVC
    KAFKA --> SHIP_SVC
    KAFKA --> NOTIF

    INV_SVC -->|StockReleased| KAFKA
    PAY_SVC -->|PaymentRefunded| KAFKA
```

### 3c. Idempotency Pattern (Prevent Double Processing)

```mermaid
sequenceDiagram
    participant Client
    participant API as Order API
    participant DB as PostgreSQL
    participant PAY as Payment Service

    Note over Client,API: Client generates idempotency key before request

    Client->>API: POST /checkout<br/>X-Idempotency-Key: uuid-abc123
    API->>DB: SELECT * FROM idempotency_keys WHERE key = 'uuid-abc123'
    DB-->>API: NOT FOUND

    API->>DB: INSERT idempotency_keys (key, status=PROCESSING)
    API->>PAY: charge(amount)
    PAY-->>API: txnId-xyz

    API->>DB: UPDATE idempotency_keys SET status=DONE, response={orderId}
    API-->>Client: 201 Created {orderId: O-001}

    Note over Client,API: Network drops — client retries with SAME key

    Client->>API: POST /checkout<br/>X-Idempotency-Key: uuid-abc123
    API->>DB: SELECT * FROM idempotency_keys WHERE key = 'uuid-abc123'
    DB-->>API: {status: DONE, response: {orderId: O-001}}
    API-->>Client: 201 Created {orderId: O-001}
    Note over API: Returns cached response — NO duplicate charge
```

---

## Quick Reference: The Four Questions

Every system design answer must address:

```mermaid
mindmap
  root((System Design))
    What fails first?
      Single points of failure
      DB connections exhausted
      External API timeouts
    What scales first?
      Read traffic → cache + read replicas
      Write traffic → sharding or CQRS
      Message processing → Kafka partitions
    What must never be cached?
      Current stock during checkout
      Payment status
      Security tokens
    Where is the source of truth?
      Orders → PostgreSQL
      Stock → PostgreSQL + Redis read cache
      Prices → Config / Lua scripts in S3
      Sessions → Redis
```
