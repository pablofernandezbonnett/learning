# Database Per Service and Read Model Migration

> Primary fit: `Shared core`

Use this topic when the real question is not only:

- should each service own its own database

But also:

- how do we stop sharing tables without breaking useful reads
- what replaces the old cross-schema joins
- when do projections, replicated views, or read models become worth it

This is a transition topic, not a purity topic.

---

## Why This Matters

Teams often understand the rule:

- each service should own its own data

But then hit the practical problem:

- old reads still join several domains together
- dashboards need data from many places
- admin screens still expect one relational view

If you ignore that read-side problem, one of two weak things happens:

- services keep sharing tables forever
- or teams split writes cleanly but leave reporting and read flows broken

---

## Smallest Useful Mental Model

When services stop sharing a database, you usually need to separate:

- **write ownership**: who can change business truth
- **read composition**: how another screen or service sees combined information

Short rule:

> Database per service solves write ownership. It does not magically solve cross-domain reads.

That second part needs an explicit read strategy.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- once each service owns its database, the architecture problem is solved
- if a dashboard needs more data, let it query several service databases directly
- `CQRS` means every service now needs a second database

Better mental model:

- service-owned databases protect writes and boundaries
- cross-domain reads should move behind APIs, projections, or dedicated read models
- you only add heavier read-model machinery when normal API composition becomes too painful

Small concrete example:

- weak approach: `Admin Service` directly joins `orders`, `payments`, and
  `inventory` tables across several service databases
- better approach: each service owns writes, and admin reads use an aggregated
  view built through APIs or event-fed projections

---

## 1. The Problem After The Split

In the monolith or shared-schema phase, one SQL query can do this:

- order status
- payment status
- shipment status
- customer email

After database per service:

- `Order Service` owns orders
- `Payment Service` owns payment attempts
- `Fulfillment Service` owns shipment state

Now one cross-domain SQL join is no longer the right default.

That is not a bug.
It is the consequence of stronger ownership boundaries.

---

## 2. The Main Read Strategies

### Strategy A: API composition

One backend layer calls several services and combines the result at request
time.

Good fit:

- low or moderate traffic
- small number of services
- data must be fresh right now
- aggregation logic is still simple

Examples:

- mobile BFF fetches order summary plus loyalty points
- admin detail page composes a few current statuses

Tradeoff:

- simple to start
- latency and failure now depend on several live calls

### Strategy B: Projection or replicated read view

A separate read model stores the combined data shape needed by the screen or
dashboard.

Good fit:

- read-heavy dashboards
- search
- denormalized history views
- reads that would otherwise hammer several services repeatedly

Examples:

- merchant dashboard
- order history read model
- support search projection

Tradeoff:

- faster and simpler reads
- eventual consistency and projection maintenance

### Strategy C: Direct reporting database or warehouse path

Data is copied into a reporting-oriented store for analytics and heavier
queries.

Good fit:

- BI queries
- reconciliation
- finance reporting
- product analytics

Tradeoff:

- best for heavy reporting
- usually not the right answer for user-facing operational reads

---

## 3. Strong Default

Use this default progression:

1. database per service for write ownership
2. API composition for simple fresh reads
3. projections or replicated read models when reads become too slow, too chatty, or too expensive
4. warehouse or analytics path for heavyweight reporting

That is usually stronger than:

1. split databases
2. let everyone read each other's tables anyway

---

## 4. Concrete Example

Imagine a commerce platform with:

- `Order Service`
- `Payment Service`
- `Fulfillment Service`
- `Admin UI`

### Step 1. Shared schema world

One admin query does:

- join order table
- join payment table
- join shipment table

Simple, but tightly coupled.

### Step 2. Service-owned writes

Each service now owns:

- one database
- one write model

Admin reads now break if they still depend on direct joins.

### Step 3. Transitional fix

Use API composition first:

- admin detail page calls `Admin Backend`
- admin backend calls order, payment, and fulfillment services

This is acceptable for moderate traffic and current-status pages.

### Step 4. Read-heavy fix

For dashboards or search:

- services publish events or outbox-fed updates
- a projection builder maintains `admin_order_view`

Now the dashboard reads one fast denormalized model without violating write
ownership.

---

## 5. What A Projection Actually Is

A `projection` is a read-optimized copy shaped for one use case.

It is not the primary truth.
It is the read-friendly view built from the real truth elsewhere.

Example:

- order service is truth for order status
- payment service is truth for payment state
- projection table stores `order_id`, `order_status`, `payment_status`,
  `shipment_status`, `customer_email`

Why this helps:

- read path becomes fast and simple
- write ownership stays clean

Main caution:

- projection can lag behind for a while

---

## 6. API Composition vs Projection

### Use API composition when

- the read needs fresh data now
- the page is not extremely high traffic
- the number of live calls stays small
- partial failure is still manageable

### Use projection when

- the read is hot
- the shape is stable and denormalized
- many screens ask for the same combined view
- live composition would create too much latency or coupling

Short rule:

> Compose live when freshness matters more. Project ahead when read cost matters more.

---

## 7. Operational Tables vs Read Models

Do not confuse:

- `outbox_events`
- `scheduled_jobs`
- `email_delivery`

With:

- customer-facing or admin-facing read models

Operational tables exist to coordinate background work.
Read models exist to answer queries efficiently.

They solve different problems.

---

## 8. Migration Path From Shared DB To Service-Owned DB

Use this order:

1. make write ownership explicit while still shared
2. stop new direct cross-domain writes
3. move cross-domain reads behind one API layer where possible
4. publish domain events or outbox records
5. build projections only for the reads that really need them
6. split physical databases once the ownership boundary is already behaving like a real boundary

This matters because:

- physical split is easier after logical ownership is already real

Not before.

---

## 9. Big Traps

1. **Database per service, but everyone still reads each other's tables**
   Example: ownership is claimed, coupling remains.

2. **Jumping to heavy CQRS too early**
   Example: extra read stores before normal API composition was even tried.

3. **Using API composition for everything forever**
   Example: dashboards become slow and brittle because every read waits on several services live.

4. **Treating a projection as final truth**
   Example: support edits the projection row instead of the source system.

5. **Splitting the database before the ownership model is clear**
   Example: physical separation happens while domain boundaries are still blurry.

---

## 10. Practical Summary

Practical summary:

> Database per service is mainly about clean write ownership. After that, cross-domain reads need an explicit strategy: live API composition for smaller fresh reads, and projections or read models for hot denormalized queries. The mistake is thinking the old joins will somehow disappear without replacing them with a real read design.
