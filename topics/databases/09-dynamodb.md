# DynamoDB — When and Why

> Primary fit: `Platform / Growth lane`


Quick review version:

- [12-dynamodb-cheatsheet.md](./12-dynamodb-cheatsheet.md)

DynamoDB is Amazon's fully managed, serverless NoSQL database. It is not a replacement for
Postgres — it is a different tool for a different problem. Knowing when to reach for it
(and when not to) is what separates a senior engineer's answer from a junior one.

---

## 1. The Core Model — Partition Key + Sort Key

Unlike SQL (rows + columns) or MongoDB (documents), DynamoDB organises data around **keys**.

Every item in a table has:
- **Partition Key (PK):** Determines which physical server partition holds the item.
  All items with the same PK are stored together.
- **Sort Key (SK) — optional:** Within a partition, items are sorted by this key.
  Enables range queries within a partition.

```
Table: payments

PK (userId)     SK (timestamp)          amount    status
─────────────────────────────────────────────────────────
user#123        2024-01-15T10:23:00Z    ¥2,990    PAID
user#123        2024-01-16T08:45:00Z    ¥8,500    PAID
user#123        2024-01-17T14:12:00Z    ¥1,200    FAILED
user#456        2024-01-15T09:00:00Z    ¥5,000    PAID
```

**What you can query efficiently:**
- `PK = "user#123"` → all payments for user 123
- `PK = "user#123" AND SK BETWEEN "2024-01-01" AND "2024-01-31"` → payments in January
- `PK = "user#123" AND SK begins_with "2024-01"` → same result, using key prefix

**What you cannot query without a scan (expensive):**
- "All FAILED payments across all users" → requires a full table scan or a GSI

---

## 2. The Fundamental Rule

> **Design your table around your access patterns, not around your data model.**

In SQL, you normalise data first and add indexes later as queries emerge. In DynamoDB, you
must know your queries *before* you design the table. The PK/SK structure is fixed at
creation time. This is the biggest conceptual shift from relational databases.

**Practical rule:** "Before choosing DynamoDB for a new service, I write down every query the
service needs to answer. If all of them can be answered with PK lookups and PK+SK range
scans, DynamoDB is a good fit. If I need ad-hoc filtering on many different attributes, I
would use Postgres or put a search index in front."

---

## 3. Global Secondary Index (GSI)

A GSI lets you query by a different attribute — essentially a second table maintained
automatically by DynamoDB with a different PK and SK.

**Problem:** You need to look up payments by `orderId`, but your table PK is `userId`.

```
GSI: by-order-id

PK (orderId)       SK (timestamp)       userId    amount
─────────────────────────────────────────────────────────
order#ABC-001      2024-01-15T10:23:00Z user#123  ¥2,990
order#DEF-002      2024-01-16T08:45:00Z user#456  ¥8,500
```

The application can now query `GSI: PK = "order#ABC-001"` to find the payment for a given
order. DynamoDB maintains this index automatically on every write.

**Cost:** Every GSI doubles write cost (DynamoDB writes to the base table AND the index).
Only create GSIs you will actually use.

---

## 4. The Hot Partition Problem

This is one of the most important operational concepts in DynamoDB at scale.

Each partition has a **throughput limit**: 3,000 RCU (read) and 1,000 WCU (write) per
second. If too many requests hit the same partition key, you get throttling errors even
if the total table capacity is fine.

**Bad partition key design:**
```
PK = "payment_type"  →  values: "QR_CODE", "BANK_TRANSFER", "CARD"
```
A very high-volume workload can push most traffic to the same
partition → hot partition → throttling.

**Good partition key design — high cardinality keys:**
```
PK = "user#<userId>"      →  millions of distinct users, load is distributed
PK = "merchant#<merchantId>"  →  thousands of merchants
PK = "txn#<transactionId>"    →  each transaction is unique
```

**When a naturally hot key is unavoidable (e.g., a viral product launch):**
Add a random suffix to spread writes across N partitions, then query all N and merge:
```
PK = "product#SKU-001#shard-3"   (shard = random 0–9)
// Writer: append random(0..9) to PK
// Reader: issue 10 parallel queries for shards 0-9, aggregate results
```
This is the **write sharding** pattern — used when a single item receives massive write
throughput.

---

## 5. Consistency Modes

| Mode | Behaviour | Cost | Use when |
|---|---|---|---|
| **Eventually consistent read** (default) | May return data up to ~1s stale | 0.5 RCU per 4KB | Most reads — dashboards, history, catalogue |
| **Strongly consistent read** | Always returns the latest committed write | 1 RCU per 4KB | Post-payment balance check, idempotency key lookup |
| **Transactions** (`TransactWriteItems`) | ACID across up to 25 items | 2x WCU + 2x RCU | Multi-item atomic updates (e.g., deduct balance + create payment record) |

**Payments example:** When a payment completes, the balance update and payment record
creation use `TransactWriteItems` — both succeed or both fail. Reading the payment history
for a dashboard uses eventually consistent reads (stale by 1 second is fine there).

---

## 6. DynamoDB vs the Other Databases

| | DynamoDB | PostgreSQL | MongoDB | Redis |
|---|---|---|---|---|
| **Model** | Key-value + document | Relational | Document | Key-value / structures |
| **Scaling** | Infinite, automatic | Vertical + read replicas | Horizontal sharding | In-memory, cluster |
| **Query flexibility** | Low (key-based + GSI) | High (any SQL) | Medium (indexes on any field) | Low (by key) |
| **Consistency** | Eventual (default) / Strong | ACID | Configurable | No persistence by default |
| **Latency** | Single-digit ms at any scale | Low at small scale, degrades | Low, degrades with data growth | Sub-millisecond |
| **Operations** | Zero (serverless) | Connection pool, vacuuming | Replica management | Memory management |
| **Best for** | High-throughput, known access patterns | Transactions, complex queries | Flexible schema, moderate scale | Cache, sessions, leaderboards |
| **Avoid when** | Ad-hoc queries, unknown access patterns | Infinite write throughput needed | ACID transactions required | Data must survive restarts |

---

## 7. When to Use DynamoDB (and When Not To)

### Use DynamoDB when:
- You need **single-digit millisecond latency at millions of requests per second**
  (payment confirmations, session lookups, real-time leaderboards)
- Your access patterns are **well-defined and key-based** — no ad-hoc queries
- You want **zero operational overhead** — no connection pool sizing, no vacuum, no
  manual scaling
- The workload is **serverless/Lambda-based** — DynamoDB has no persistent connections to
  exhaust (unlike Postgres which needs HikariCP tuning)
- You need **time-to-live (TTL)** on records — DynamoDB auto-expires items, ideal for
  sessions, idempotency keys, rate-limit counters

### Do NOT use DynamoDB when:
- You need **complex queries, joins, or aggregations** — use Postgres
- Your access patterns are **unknown or evolving** — the fixed PK/SK structure will fight you
- You need **strong relational integrity** (foreign keys, check constraints) — use Postgres
- Your team is **not yet familiar** with the access-pattern-first design discipline — the
  learning curve is real and costly if you get the schema wrong

---

## 8. Single-Table Design (Advanced)

In DynamoDB, multiple entity types can coexist in a single table — often called
**single-table design**. Instead of one table per entity, you overload the PK/SK with
entity type prefixes.

```
PK              SK                   type        data
────────────────────────────────────────────────────────────
user#123        profile              USER        { name, email }
user#123        payment#2024-01-15   PAYMENT     { amount, status }
user#123        order#ORD-001        ORDER       { items, total }
merchant#ABC    profile              MERCHANT    { name, category }
merchant#ABC    payout#2024-01       PAYOUT      { amount }
```

**Pros:** All data for user#123 is co-located on the same partition — a single query
fetches the user profile + their payments + their orders in one round trip.

**Cons:** Complex to maintain, hard to understand without documentation, and GSI design
becomes tricky. Most teams only adopt this pattern after hitting the limits of
multi-table design at scale.

**Practical framing:** "I would start with one table per entity type — it is easier to
reason about. Single-table design is an optimisation I would reach for if profiling showed
we are making multiple DynamoDB round trips for data that is always fetched together."

---

## 9. Practical Summary

*"DynamoDB is the right choice when I need guaranteed single-digit millisecond latency
at very high throughput — payment confirmations, session management, or real-time counters.
Its serverless model means no connection pool, no vacuuming, and automatic scaling.*

*The key discipline is designing around access patterns first. I define every query the
service needs before touching the table schema — the PK/SK structure is load-bearing and
hard to change later.*

*The main operational risk at scale is hot partitions. I choose high-cardinality
partition keys (userId, transactionId) and use write sharding for any data that concentrates
writes on a single key.*

*For anything requiring complex joins, ad-hoc analytics, or unknown query patterns, I keep
Postgres. DynamoDB and Postgres solve different problems — I would not use one to replace
the other."*
