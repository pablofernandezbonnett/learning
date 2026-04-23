# Retail Inventory, Fulfillment, and Logistics Systems

This is a retail-specific reference note for backend and system design study.

Use it to connect backend design ideas to physical inventory, logistics, and
fulfillment constraints.

In a global retailer, the backend is not only about the storefront. It is also
about orchestrating physical goods across stores, warehouses, and long-running
operational flows.

### What omnichannel means

`Omnichannel` means the customer journey is treated as one connected system across:

- ecommerce site
- mobile app
- physical stores
- warehouses
- returns, pickup, and fulfillment flows

In practice, it means the backend should support flows like:

- buy online, pick up in store
- ship from store
- return store-bought and online-bought items through a consistent process
- keep stock, order, and customer state coherent across channels

---

## 1. Inventory Management: The "Truth" Problem

The most critical challenge in retail is knowing exactly how many units of "Item X" are available *right now*.

### Strong vs. Eventual Consistency
*   **Strong Consistency (Postgres/SQL):** Used during the final checkout commit path. Database transactions and row-level locking help reduce oversell risk and protect the final stock write.
*   **Eventual Consistency (Redis/NoSQL):** Used for the "Product Listing Page" (PLP). It is okay if the search page says "In Stock" but it takes 1 second to update when someone else buys the last one. 
    *   *Real-world trade-off:* If we forced strong consistency on every search page view, the database would crash under high traffic.

### Virtual Inventory vs. Physical Inventory
*   **Physical:** What is physically on the shelf in a warehouse.
*   **Virtual (Allocated):** Stock that is "reserved" because someone has it in their cart or has paid, but it hasn't left the building yet.
*   **Formula:** `Available for Sale = Physical - Allocated`.

---

## 2. Order Management System (OMS) Flow

An OMS is the "brain" that orchestrates an order's lifecycle.

1.  **Placement:** Order is saved in the DB (Postgres) and an "Inventory Reservation" event is fired.
2.  **Payment:** The system waits for a `PaymentSucceeded` event from the payment service provider.
3.  **Fulfillment Logic:** The OMS decides *where* to ship from.
    *   *Ship-from-Store (SFS):* Shipping from the local store nearest to the customer.
    *   *Ship-from-Warehouse:* Shipping from a massive automated DC (Distribution Center).
4.  **WMS Integration:** The OMS sends a "Pick/Pack" command to the **Warehouse Management System (WMS)**.

---

## 3. Warehouse Management Systems (WMS) & Automation

*   **Connectivity:** The backend must integrate with IoT systems and PLC (Programmable Logic Controllers) that control robots.
*   **Batching vs. Real-time:** Orders are often "batched" for efficiency. Instead of a robot moving for 1 item, the system waits for 50 items in the same aisle to optimize the robot's movement path.
*   **Asynchronous Processing:** Warehouse operations are slow (physical travel). The backend must handle long-running processes using **Message Brokers (Kafka)** rather than waiting for an API response.

---

## 4. Handling Retail Events (Global Flash Sales)

Events like "Black Friday" or a major seasonal sale create massive spikes (10x-50x normal traffic).

### Architectural Strategies:
1.  **Queueing / Admission Control:** If 1 million users hit checkout at 00:00, we do not let them all hammer the critical write path at once. A waiting-room product and internal queues smooth the load to a rate the backend can survive.
2.  **Inventory Pre-caching:** Move hot inventory counters to **Redis** using atomic operations (`DECRBY`) to reduce pressure on the relational database. The final authoritative stock commit still belongs in the source-of-truth store.
3.  **Read Replicas:** Scale the "In Stock" status checks horizontally by using read-only database replicas.

---

## Practical Summary

*"In a global retail environment, I would use a hybrid consistency model: eventual consistency for browse and discovery paths, and stronger consistency on the final checkout and inventory commit path. I would treat OMS/WMS integration as a long-running asynchronous workflow, and I would use Redis carefully as a fast coordination layer rather than as the final source of truth."*
