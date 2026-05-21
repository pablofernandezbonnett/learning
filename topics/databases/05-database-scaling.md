# Database Scaling: Indexing, Replication, and Sharding

> Primary fit: `Shared core`


When the database is getting slow, the useful answer is usually a progression from the easiest fix to the hardest one.

Here is the hierarchy of how to scale a database.

---

## Why This Matters

Database scaling questions are dangerous because teams often jump too quickly to
"we need sharding" when the real problem is still query shape, indexes, or one
bad hot path.

This topic matters because database bottlenecks directly affect:

- request latency
- write correctness under load
- operational cost
- migration complexity when the easy fixes stop working

The most useful answer is usually not the fanciest one.
It is the cheapest safe fix that matches the real bottleneck.

## Smallest Mental Model

Database scaling is usually a progression:

1. make the existing queries cheaper
2. give the database more room
3. split reads from writes when reads dominate
4. shard only when one node truly cannot carry the data or write load

Strong default:

- first fix query and index problems
- then scale up or add replicas
- treat sharding as the expensive last step

## Bad Mental Model vs Better Mental Model

Bad mental model:

- slow database means we need sharding
- replicas solve all database scale problems
- scaling is mostly an infrastructure purchase

Better mental model:

- many "database scaling" problems are still query-design problems
- read scale and write scale are different problems
- every scaling step adds operational and consistency tradeoffs

Small concrete example:

- weak approach: add replicas while one expensive query is still scanning a large table badly
- stronger approach: fix the query and index first, then add replicas only if read pressure is still the real bottleneck

Interview-ready takeaway:

> I treat database scaling as a progression from cheapest to most dangerous:
> query and index fixes first, then vertical scaling or replicas, and only then
> sharding when one node truly cannot hold the write or data load anymore.

---

## Step 1: Indexing (The Easiest Fix)

**What is it?**
A B-Tree data structure created alongside your table that keeps specific columns sorted. It allows the database to find rows in `O(log N)` time instead of `O(N)` (Full Table Scan).

**When to use it:**
If queries filtering by a specific column (e.g., `WHERE email = '...'`) are slow.

**The Trade-off:**
Indexes dramatically speed up **Reads**, but they slow down **Writes**. Every time you `INSERT`, `UPDATE`, or `DELETE` a row, the database must also update the Index. 
*Do not index every column. Only index columns frequently used in `WHERE`, `JOIN`, or `ORDER BY` clauses.*

---

## Step 2: Vertical Scaling (Scaling Up)

**What is it?**
Throwing money at the problem. Moving your database to a larger AWS/GCP instance with more CPU, Memory, and faster SSDs (NVMe).

**When to use it:**
When indexing isn't enough, but your dataset isn't unimaginably huge.

**The Trade-off:**
It is the easiest operational choice (no code changes), but it has a hard physical ceiling. You can only buy a server so big.

---

## Step 3: Horizontal Scaling (Read Replicas)

**What is it?**
Creating copies (Replicas) of your main database (Primary/Master). 

*   **Primary Node:** Handles all `INSERT`, `UPDATE`, `DELETE` operations.
*   **Replica Nodes:** Handle `SELECT` operations. They asynchronously sync data from the Primary.

**When to use it:**
When your system is heavily **Read-Biased** (like a social media feed or product catalog). 

**The Trade-off (Eventual Consistency):**
Because replication takes a few milliseconds, a user might update their profile on the Primary, instantly refresh the page (hitting a Replica), and see old data. You must design your application to tolerate "replication lag" (Eventual Consistency).

---

## Step 4: Horizontal Scaling (Sharding / Partitioning)

**What is it?**
Splitting a single massive table across multiple different database servers. 

*Example:* `Users A-M` live on Database Server 1. `Users N-Z` live on Database Server 2.

**When to use it:**
When your dataset is so large it cannot fit on a single physical disk, or the **Write** volume is so high that a single Primary node cannot handle the load.

**The Trade-off (The Nightmare Scenario):**
Sharding is incredibly complex. 
*   **Joins across shards:** You cannot easily SQL `JOIN` a row from Server 1 and Server 2. You have to do it in application code.
*   **Resharding:** What happens when Server 1 gets full? Re-balancing the data across a new Server 3 requires migrating live data with zero downtime.
*   **Celebrity Problem (Hotspotting):** If you shard by UserID, but one User is Justin Bieber and generates 90% of the traffic, Server 1 melts while Server 2 is idle. Shard keys must be chosen perfectly to distribute load evenly.

---

## Practical Summary

"If our database was a bottleneck, I would start by analyzing our slowest queries and ensuring we have appropriate **Indexes**. If reads are overwhelming the database, I would introduce **Read Replicas** and route `SELECT` queries to them, accepting eventual consistency. If the issue is **Write Throughput**, we can vertically scale the Primary node. I would only consider **Sharding** as an absolute last resort due to the massive application complexity it introduces around distributed joins and re-balancing."

---

## What To Internalize

- most database scale conversations should start with query cost, not with sharding
- vertical scaling is often the simplest next step after query fixes
- replicas mainly help read-heavy systems and introduce replication-lag tradeoffs
- sharding is a write-scale and data-volume answer with serious application complexity
- scaling the database is still a correctness topic, not only a speed topic
