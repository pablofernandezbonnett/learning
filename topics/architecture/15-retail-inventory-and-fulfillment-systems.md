# Retail Inventory, Fulfillment, and Logistics Systems

Use this note when you want the retail version of backend and system-design
judgment, not only generic ecommerce vocabulary.

---

## Why This Matters

Retail systems are where many backend assumptions become more concrete:

- stock is physical
- fulfillment is slow and expensive
- checkout correctness has a real warehouse consequence
- returns and pickup flows create awkward cross-system state changes

This matters because a retail backend is not only "website plus orders."
It is a coordination system across inventory truth, payment state, routing, and
physical execution.

---

## Smallest Useful Mental Model

Treat retail architecture as three connected problems:

- what inventory truth you trust
- how the order moves from checkout to fulfillment
- how much delay or inconsistency each customer-facing flow can tolerate

Practical translation:

- browse can often tolerate stale stock for a short time
- final stock commit cannot
- warehouse and store operations usually force asynchronous processing later

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- retail is just normal ecommerce plus a warehouse integration
- if the product page says "in stock," the hard problem is solved
- fulfillment is only an after-checkout implementation detail

Better mental model:

- retail is a correctness and coordination problem across physical and digital state
- inventory visibility and final stock commitment are different problems
- fulfillment location, batching, and returns shape the backend design from the start

Small concrete example:

- weak approach: treat Redis stock count as final truth for the whole system
- better approach: use fast cache or projections for browse, but keep final stock truth in the protected transactional write path

---

## 1. Omnichannel Means Shared Flow, Not Shared UI

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

Short rule:

> omnichannel usually means the same business truth must survive several operational paths, not just several frontends

---

## 2. Inventory Is A Truth Problem

The most important question is not only:

- how many units exist physically

It is:

- how many units are truly available for sale right now

Useful split:

- `physical`: what exists on shelf or in warehouse
- `allocated`: what is reserved for carts, paid orders, or in-flight operational work
- `available for sale`: `physical - allocated`

### Strong vs Eventual Consistency

- stronger consistency belongs on the final checkout and stock-commit path
- eventual consistency is often acceptable for browse, search, or product-listing views

Real tradeoff:

- if you force the final stock rule onto every browse request, you usually hurt scale and latency badly

---

## 3. OMS and WMS Have Different Jobs

`OMS` means `Order Management System`.
It decides how the order moves through business state.

`WMS` means `Warehouse Management System`.
It coordinates physical warehouse execution such as pick, pack, and dispatch.

Smallest practical flow:

1. order is placed
2. payment is authorized or confirmed
3. stock is reserved or committed
4. the `OMS` decides where fulfillment should happen
5. the `WMS` or store operations receive the work

Why this matters:

- the `OMS` owns business orchestration
- the `WMS` owns physical execution
- mixing those responsibilities carelessly makes change harder and recovery messier

---

## 4. Fulfillment Logic Is A Routing Problem

Typical choices:

- ship from warehouse
- ship from store
- customer pickup

The real design question is:

- where should this order be fulfilled from, given stock, distance, cost, and operational constraints

That routing decision affects:

- inventory reservation shape
- failure handling
- later customer updates

Short rule:

> fulfillment routing is not a small afterthought; it is one of the business decisions the backend must protect clearly

---

## 5. Warehouse Work Is Usually Async

Warehouse and logistics work is slow compared with API response time.

That means:

- pick and pack commands should usually be asynchronous
- batching may be the right operational choice
- retries and replay safety matter because physical work is expensive

Examples:

- waiting for several picks in one aisle to improve robot path efficiency
- retrying a pick request safely without duplicating warehouse work

Good default:

- use async messaging or commands for warehouse execution
- keep user-facing checkout separate from long-running physical operations

---

## 6. Flash Sales Expose The Real Limits

During large sales events, the system is really tested on:

- critical write-path protection
- admission control
- inventory hotspot handling
- graceful degradation

Common strategies:

- queueing or waiting-room control to smooth checkout load
- hot-counter coordination to reduce pressure on the relational write path
- read scaling for browse and stock visibility

Bad example:

- letting all traffic hit the final stock write path at once and hoping horizontal app replicas solve it

Better example:

- controlling admission, protecting the critical write path, and scaling browse separately from final stock commitment

---

## Reusable Takeaway

> Retail backends work best when browse, checkout, fulfillment, and returns are treated as one coordination system, with explicit inventory truth, asynchronous warehouse execution, and protected final stock and order state.
