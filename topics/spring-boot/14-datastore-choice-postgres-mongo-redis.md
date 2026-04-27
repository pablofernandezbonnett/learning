# Postgres vs MongoDB vs Redis for Spring Boot

> Primary fit: `Supporting reference for data-store choice`


This is not a deep NoSQL document.
It is a quick decision guide for a common storage-choice question:

> Why would you choose Postgres here, MongoDB there, and Redis somewhere else?

The wrong answer is "they are all just database choices" or "both are NoSQL".
The useful answer is about data shape, access pattern, latency, and correctness.

---

## 1. The Smallest Mental Model

First, what `NoSQL` actually means:

- `NoSQL` does not mean "faster database"
- it usually means "not a traditional relational SQL-first database model"
- MongoDB and Redis are both commonly grouped under `NoSQL`, but they are not similar enough to treat as one choice

Smallest useful comparison:

- **Postgres**: relational database; strongest default for transactional business truth, joins, and constraints
- **MongoDB**: document database; useful when the application naturally reads and writes one document-shaped record at a time
- **Redis**: an in-memory key-value and data-structure store for fast shared state, cache, counters, sessions, and coordination

Short rule:

> Postgres usually owns transactional truth, MongoDB usually owns document-shaped application data, and Redis usually accelerates or coordinates.

That distinction matters because backend work often depends on matching the storage tool to the real workload.

---

## 2. When MongoDB Fits

MongoDB is useful when the application data is naturally document-shaped and you want flexible schema evolution without treating every relationship like a normalized relational model.

Good fits:

- product catalogs with nested attributes
- content management systems
- user-generated documents or profiles with uneven shape
- telemetry or event-like records where document reads matter more than relational joins

Why teams choose it:

- one document can hold a natural read shape
- schema can evolve more flexibly than a tightly normalized relational model
- aggregations are useful when you need grouped analytics or summary views

Tradeoffs:

- cross-document joins are not the strength of the model
- relational invariants can be harder than in Postgres
- flexible schema helps velocity, but weak discipline can create messy data over time

Spring shape:

- `MongoRepository` for straightforward document CRUD
- `MongoTemplate` when you need aggregations, explicit updates, or more control over query shape

Smallest practical line:

> I would choose MongoDB when the data is naturally document-shaped and the read model benefits from storing that shape directly, but I would still be careful about relational-style invariants and cross-document consistency.

---

## 3. When Postgres Fits

Postgres fits when the most important thing is reliable business state, not schema flexibility or cache speed.

Good fits:

- orders
- payments
- inventory truth
- account balances
- any flow where constraints, transactions, and joins matter

Why teams choose it:

- strong transaction support
- constraints such as primary keys, foreign keys, and uniqueness
- mature SQL for joins, filters, and reporting-style queries

Tradeoffs:

- more schema discipline
- denormalized document-style reads may need extra shaping
- scaling write-heavy hotspots still requires careful design

Smallest practical line:

> I default to Postgres when the system needs strong transactional correctness, relational integrity, and a clear source of truth.

---

## 4. When Redis Fits

Redis is useful when the main value is speed or shared ephemeral state, not rich primary data modeling.

Good fits:

- cache for hot read paths
- sessions
- rate limiting
- counters
- distributed locks or simple coordination
- short-lived state such as reservation windows or idempotency records

Why teams choose it:

- in-memory access is very fast
- useful data structures exist for counters, sets, expirations, and atomic operations
- multiple app instances can share the same fast state

Tradeoffs:

- memory is expensive, so you should not treat Redis like a casual dump for everything
- cached or short-lived state needs TTL and invalidation thinking
- it is usually the wrong default source of truth for core payment or order correctness

Spring shape:

- Spring Cache with `@Cacheable` for simple read caching
- `RedisTemplate` when you need explicit key design, counters, locks, sessions, or rate limits

Smallest practical line:

> I choose Redis when the main problem is latency or shared fast state, not when I need it to behave like the primary business database.

---

## 5. The Common Mistake

The most common mistake is treating MongoDB and Redis as interchangeable just because both are "NoSQL".

They solve different problems:

- Postgres is a relational source-of-truth database
- MongoDB is closer to a primary application database
- Redis is closer to a fast access and shared-state layer

Bad decision pattern:

- "We need performance, so let's move the main data to Redis"

Better decision pattern:

- ask where the source of truth should live
- ask what read/write pattern is hot
- ask how much staleness is acceptable
- ask whether the data is document-shaped, relational, or just temporary shared state

---

## 6. Quick Comparison

| Question | Postgres | MongoDB | Redis |
|---|---|---|---|
| Main role | Transactional source of truth | Primary document database | Fast shared state / cache / coordination |
| Data shape | Relational rows and joins | Documents with nested structure | Key-value and data-structure oriented |
| Best for | Orders, payments, inventory, relational business data | Product/content/document-style data | Cache, sessions, counters, rate limits, locks |
| Latency goal | Good operational DB latency with strong correctness | Good operational database latency | Very low latency |
| Source of truth | Usually yes | Often yes | Usually no |
| Main Spring tool | JPA repositories, `JdbcTemplate`, native SQL | `MongoRepository`, `MongoTemplate` | `RedisTemplate`, Spring Cache |
| Main risk | Hot write paths and schema evolution require care | Flexible schema can become messy | Stale data, weak invalidation, memory misuse |

---

## 7. Practical Choice Examples

- **Order and payment flow** -> Postgres is the strongest default because the main problem is correctness, not document flexibility.
- **Product catalog with changing attributes** -> MongoDB can fit well if the document shape maps cleanly to the product read model.
- **Homepage product cache** -> Redis is a strong fit because the problem is repeated hot reads.
- **Login session storage across instances** -> Redis is a common fit because multiple app instances need shared session state.
- **Rate limiting per user or API key** -> Redis is usually a better fit than MongoDB because atomic counters and TTL are the real need.
- **Primary order ledger or strongly relational payment data** -> usually not Redis, and often not MongoDB either; this is where a relational database may still be the better answer.

That last point matters.
The real senior move is not "pick NoSQL".
The real senior move is "pick the storage model that matches the invariant and access pattern".

In practice, many real systems are hybrid:

- Postgres for transactional truth
- MongoDB for document-shaped application data where that shape really helps
- Redis for cache, sessions, counters, or fast coordination

That is often more realistic than trying to make one store fit every access pattern.

---

## 8. Interview Conversion

Good short answer:

> I do not group Postgres, MongoDB, and Redis into one vague "database choice". Postgres is my default for transactional truth and relational consistency. MongoDB fits document-shaped application data when one object is usually read as one object. Redis is usually a speed and shared-state tool for cache, sessions, counters, and coordination. I choose between them based on source of truth, access pattern, and latency, not on the NoSQL label.
