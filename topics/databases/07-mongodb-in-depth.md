# MongoDB In-Depth: Architecture, Indexing, and Best Practices

> Primary fit: `Shared core`


MongoDB is the most popular NoSQL document database. It is flexible and scales horizontally well, but terrible data modeling or missing indexes can cause severe performance bottlenecks.

---

## 1. High Availability and Scaling Architecture

### Replica Sets (High Availability)
A Replica Set is a group of MongoDB instances that maintain the same data set. This provides redundancy and high availability.
*   **Primary node:** Receives all write operations.
*   **Secondary nodes:** Replicate the operations from the primary.
*   **Elections:** If the primary crashes, secondaries vote to elect a new primary automatically.
*   *Interview context:* You can configure reads to go to secondary nodes to reduce the load on the primary (Eventual Consistency).

### Sharding (Horizontal Scaling)
When a single machine can no longer hold the data or handle the read/write load, you partition the data across multiple machines.
*   **Shard Key:** A field chosen to distribute documents. (e.g., `user_id` or `country`).
*   Choosing the wrong shard key leads to **jumbo chunks** (too much data on one shard) or **hot shards** (all writes hitting only one server).

---

## 2. Projections: Returning Only What You Need

A common mistake in MongoDB (especially with ORMs like Spring Data MongoDB) is fetching massive documents when the client only needs a few fields.

**What is a Projection?**
It instructs the database engine to only transmit specific fields over the network.
*   *Reduces Network Latency:* Less data to send.
*   *Reduces Memory Usage:* Your application server allocates less RAM deserializing JSON.

**Example (Spring Data MongoDB):**
Instead of fetching the whole `Product` (with 50 fields, arrays, embedded reviews):
```kotlin
@Query(value="{ 'categoryId' : ?0 }", fields="{ 'name' : 1, 'price' : 1, '_id': 0 }")
fun findNamesAndPricesByCategory(categoryId: String): List<ProductSummary>
```
This tells MongoDB to *only* return `name` and `price`. `_id` is excluded explicitly.

---

## 3. Indexing & The ESR Rule

Indexes make database queries fast by avoiding a **Collection Scan** (reading every single document).

### Types of Indexes
1.  **Single Field Index:** On a single key (e.g., `email`).
2.  **Compound Index:** On multiple fields (e.g., `{ lastName: 1, firstName: 1 }`). Order matters entirely!
3.  **Multikey Index:** Used to index elements of an array.

### The ESR Rule (Equality, Sort, Range)
When creating a Compound Index to optimize a query, the fields in the index MUST follow this order:
1.  **E (Equality):** Fields matched exactly (e.g., `status = "ACTIVE"`).
2.  **S (Sort):** Fields used to order the results (e.g., `ORDER BY createdAt DESC`).
3.  **R (Range):** Fields filtered by range (e.g., `price > 100`, `$gt`, `$in`).

**Example Question:** Optimize the query: "Find all active users, sorted by registration date, older than 25 years."
*   *Query:* `{ status: "ACTIVE", age: { $gt: 25 } } sort: { registeredAt: -1 }`
*   *Correct Index (ESR):* `{ status: 1, registeredAt: -1, age: 1 }`

---

## 4. The Aggregation Pipeline

The Aggregation Pipeline is MongoDB's framework for data transformation (similar to `GROUP BY` and complex `JOIN`s in SQL). It passes documents through a multi-stage pipeline.

*   `$match`: Filters documents (always do this FIRST to reduce the pipeline size, ideally using an index).
*   `$group`: Groups documents by a specified key and applies accumulators (e.g., `$sum`, `$avg`).
*   `$project`: Reshapes each document (adding/removing fields).
*   `$lookup`: Performs a "Left Outer Join" with another collection. (Use sparingly, as NoSQL databases are not optimized for joins).

---

## 5. Document Design: Embedding vs. Referencing

"We are building a blog. Should comments be embedded in the Post document, or stored in a separate collection?"

*   **Embedding (Storing an Array of Comments in the Post):**
    *   *Pros:* Fastest read time (1 query fetches post + comments). Great if data is accessed together.
    *   *Cons:* MongoDB documents have a 16MB limit. If a post goes viral and gets 100,000 comments, the document exceeds the limit and breaks.
*   **Referencing (Separating Collections):**
    *   *Pros:* Infinite scaling. Standard relational model.
    *   *Cons:* Requires multiple queries or an expensive `$lookup`.

**Rule of Thumb:** Understand the relationship bounding.
*   *1-to-Few:* Embed (e.g., User addresses).
*   *1-to-Many:* Reference (e.g., Product reviews).
*   *1-to-Squillions:* Reference, possibly keeping an array of references in the parent if needed.
