# Databases: SQL vs NoSQL

> Primary fit: `Shared core`

Saying only "I would use a database" is not enough.
You need to explain which store fits the problem and what tradeoff you are accepting.

This note keeps the question practical:

- what each option is good at
- the smallest concrete examples
- where correctness matters more than scale
- the practical decision rule

---

## 1. Default Rule

Before comparing them, keep one definition straight:

- `SQL` usually means relational databases such as Postgres or MySQL
- `NoSQL` is a broad family of non-relational stores, not one single model
- document stores and Redis are both often called `NoSQL`, but they solve very different problems

Start with this:

- `SQL` when correctness, transactions, and relational queries matter
- document `NoSQL` when one record is read mostly as one document and the shape varies
- `Redis` when the real need is cache, counter, session, or short-lived shared state

Short rule:

> Postgres is usually the source of truth, document stores fit access-pattern-heavy reads, and Redis is usually a support layer, not the final truth

---

## 2. The Smallest Concrete Examples

### 2.1 SQL: order plus payment

```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    status TEXT NOT NULL
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    amount NUMERIC(12,2) NOT NULL,
    status TEXT NOT NULL
);

SELECT o.id, o.status, p.amount, p.status
FROM orders o
JOIN payments p ON p.order_id = o.id
WHERE o.customer_id = 42;
```

Why SQL fits:

- order and payment are related
- the relationship matters
- correctness matters when both change

### 2.2 Document store: product catalog

```json
{
  "productId": "sku-123",
  "name": "Running Jacket",
  "brand": "GenericBrand",
  "attributes": {
    "size": ["S", "M", "L"],
    "color": ["black", "navy"],
    "material": "polyester"
  }
}
```

```javascript
db.products.findOne({ productId: "sku-123" })
```

Why a document store fits:

- the product shape can vary a lot
- the client often wants the full record in one read
- joins are less important than one predictable access pattern

### 2.3 Redis: hot read or counter

```redis
GET product:sku-123
SETEX product:sku-123 300 "{...cached json...}"
INCR rate:user:42
EXPIRE rate:user:42 60
```

Why Redis fits:

- extremely fast simple operations
- ideal for cache and counters
- not the place for the final order or payment truth

---

## 3. When SQL Is The Right Answer

Use SQL when you need:

- transactions across related data
- clear constraints and relationships
- joins and flexible queries
- correctness under concurrent writes

Good examples:

- payments
- orders
- inventory truth
- ledger-like state

Pros:

- strong consistency tools
- mature query power
- easy to defend for money-sensitive workflows

Tradeoffs:

- horizontal scaling is harder
- schema changes need more care
- very high write scale may need partitioning or extra architecture around it

Practical line:

> I default to SQL for the commit path when the business rule is more important than raw write scale.

---

## 4. When Document NoSQL Fits Better

Use a document store when:

- the record shape varies a lot
- one object is usually read as one object
- access patterns are known up front
- denormalized reads matter more than joins

Good examples:

- product catalog
- profile or preference documents
- read-optimized content records

Pros:

- flexible schema
- easy one-record reads
- natural fit for embedded data

Tradeoffs:

- duplicated data can drift
- cross-document transactions are weaker or more expensive
- ad hoc relational questions are a worse fit

Practical line:

> I choose a document store when the main win is reading one denormalized record cleanly, not when I need cross-entity correctness.

---

## 5. Where Redis Fits

Redis is usually not the answer to `SQL vs NoSQL`.
It usually sits beside them.

Use Redis for:

- cache
- sessions
- rate limits
- simple distributed coordination
- short-lived shared state

Do not use Redis as the main answer for:

- final payment state
- final order state
- complex relational queries

Practical line:

> Redis is a speed layer or coordination layer, not my default source of truth for critical business state.

---

## 6. Consistency In Plain English

You do not need a long CAP speech in most practical discussions.
You usually need one simple distinction:

- some domains must reject ambiguity
- some domains can tolerate slightly stale reads

Examples:

- checkout, payment, stock commit: prefer strong correctness
- catalog, recommendations, read-heavy dashboards: some staleness is acceptable

That is the practical meaning behind `ACID` (`atomicity, consistency, isolation, durability`), `eventual consistency`, and related terms.

Short rule:

> use strong correctness where duplicate or inconsistent writes hurt the business; accept staleness where the read path matters more than the latest millisecond of truth

---

## 7. Quick Comparison

| Question | SQL | Document NoSQL | Redis |
|---|---|---|---|
| Data shape | Structured and relational | Flexible, document-shaped | Key-value or simple shared state |
| Best for | Orders, payments, inventory truth | Catalogs, profiles, content | Cache, sessions, counters, rate limits |
| Relationships | Strong joins and constraints | Usually denormalized reads | Not a join-oriented store |
| Consistency | Strong default for critical writes | Varies by model and workflow | Usually not the final truth |
| Scaling pattern | Start simple, then scale carefully | Often chosen for access-pattern-heavy reads | Added mainly for latency and hot shared state |
| Main risk | Overusing it for clearly denormalized read models | Using it for cross-entity correctness without enough care | Letting it become accidental source of truth |

Short reading of the table:

- SQL is usually the safest default for business-critical state
- document NoSQL fits when one whole record is the main unit of read and write
- Redis usually supports the system rather than owning the main business data

---

## 8. Choice By Use Case

### Checkout, orders, or payments

- SQL: yes
- document store: usually no
- Redis: maybe, as support only
  Why: this is mainly a correctness problem.

### Product catalog with many variable attributes

- SQL: maybe
- document store: yes
- Redis: maybe, for hot read cache
  Why: the main win is flexible shape and one-record reads.

### User-facing hot read path

- SQL: source of truth, maybe yes
- document store: maybe
- Redis: yes, often
  Why: this is a latency and read-load problem.

### Analytics or event ingestion

- SQL: maybe
- document store: maybe
- Redis: maybe
  Why: the right answer depends on write volume, retention, and query shape.

---

## 9. The Big Traps

1. **Choosing NoSQL just because scale sounds impressive**
   Example: a normal order system loses useful constraints and joins for no real gain.

2. **Using SQL everywhere even when the read shape is clearly denormalized**
   Example: a product page needs five joins for one object the client always reads together.

3. **Treating Redis as a primary database by accident**
   Example: final payment or order truth lives only in cache-like storage.

4. **Talking about CAP as a slogan**
   Example: saying "this is AP" without connecting it to the business effect of stale reads or rejected writes.

---

## 10. 20-Second Answer

> I choose storage by the business rule first. For payments, orders, and inventory truth I usually start with SQL because transactions, constraints, and concurrency safety matter most. For highly variable records that are usually read as one object, a document store can fit better. Redis is usually a support layer for cache, sessions, counters, or short-lived coordination, not my main source of truth.

---

## 11. What To Internalize

- SQL is usually the default for correctness-sensitive write paths
- document NoSQL fits access-pattern-heavy, denormalized records
- Redis usually supports the system rather than owning the final truth
- the real question is not "which database is better?" but "which tradeoff is correct for this domain?"

In practice, many modern systems are hybrid:

- SQL for the transactional core
- document storage where flexible read shape is the main win
- Redis for cache, sessions, counters, or other fast shared state

That is often a stronger design than trying to force one store to do every job.
