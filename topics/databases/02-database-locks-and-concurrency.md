# Database Locks and Concurrency

> Primary fit: `Shared core / Payments / Fintech`


Concurrency bugs are not theoretical.
They are what turns checkout, inventory, and payment systems into inconsistent systems.

In plain language, `concurrency` just means multiple requests are trying to do work at the same time.
The bug appears when those requests make decisions from stale state and then overwrite each other.

This note follows the same reusable progression:

- what goes wrong
- the smallest example
- the main fixes
- how to explain it clearly

---

## 1. What Problem Concurrency Actually Creates

The core problem is simple:

> two requests read the same state, both make a decision from stale data, and the final
> write violates the business rule.

The most common example is the **lost update** problem.

Broken sequence:

1. Request A reads stock = 10
2. Request B reads stock = 10
3. Request A writes stock = 9
4. Request B writes stock = 9

Result:

- two items were sold
- stock only dropped by one

Other anomalies worth knowing:

- **dirty read**: you read data another transaction has not committed yet
- **non-repeatable read**: you read the same row twice and get different committed values
- **phantom read**: you rerun a query and extra matching rows appear or disappear

The lost update problem is the one to explain first.
The other terms matter, but they only help if you can already explain the basic race in plain English.

---

## 2. The Smallest Broken Example

Without any concurrency control, a read-modify-write flow is unsafe.

```kotlin
fun sellOne(productId: Long) {
    val currentStock = repository.findStock(productId)
    repository.updateStock(productId, currentStock - 1)
}
```

<details>
<summary>Java version</summary>

```java
public void sellOne(long productId) {
    int currentStock = repository.findStock(productId);
    repository.updateStock(productId, currentStock - 1);
}
```

</details>

Why it is broken:

- two threads can read the same `currentStock`
- both compute the same next value
- the later write silently overwrites the earlier one

That is the smallest useful mental model for most lock discussions.

---

## 3. The Two Main Fixes

### 3.1 Optimistic locking

Optimistic locking means:

- do not block first
- try the write
- fail if someone else changed the row before you finished

It assumes conflicts happen sometimes, not constantly.

Smallest SQL shape:

```sql
UPDATE products
SET stock = 9, version = version + 1
WHERE id = 42 AND version = 7;
```

Interpretation:

- if one row is updated, you won the race
- if zero rows are updated, someone else changed the row first

JPA shape:

```kotlin
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Version

@Entity
class Product(
    @Id val id: Long,
    var stock: Int,
    @Version var version: Long,
)
```

Important nuance:

> optimistic locking does not prevent conflicts, it detects them.

That means the application must decide what happens next:

- retry
- return conflict
- re-read and re-evaluate the business rule

When it fits:

- collisions are relatively rare
- throughput matters
- retries are acceptable

### 3.2 Pessimistic locking

Pessimistic locking means:

- lock first
- let other writers wait
- finish the critical section

It assumes contention is common or the business boundary is sensitive enough that you do
not want two writers racing and then resolving the conflict later.

Smallest SQL shape:

```sql
SELECT stock
FROM products
WHERE id = 42
FOR UPDATE;
```

Interpretation:

- the row is locked for the current transaction
- another writer must wait
- you serialize access by blocking

JPA shape:

```kotlin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import jakarta.persistence.LockModeType

interface ProductRepository : JpaRepository<Product, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    fun findByIdForUpdate(id: Long): Product?
}
```

When it fits:

- contention is frequent
- you cannot tolerate optimistic retries at that boundary
- the transaction can stay short

Tradeoff:

- stronger serialization
- lower throughput
- deadlock risk if you lock multiple rows in inconsistent order

---

## 4. How To Choose Between Optimistic And Pessimistic

The clean rule is:

- prefer **optimistic** when collisions are uncommon and scale matters
- choose **pessimistic** when contention is high and the cost of a conflict is high

Good examples:

- profile updates: optimistic is usually fine
- inventory decrement for hot SKUs: pessimistic may be justified
- payment or ledger-like flows: often combine strict transaction rules with very small
  transaction scopes

Bad answer pattern:

> I always use pessimistic locking because it is safer.

That sounds safe, but it usually means you are willing to pay in throughput, latency,
and deadlocks without asking whether the business case needs it.

---

## 5. Isolation Levels In Practical Terms

Isolation levels define what concurrent transactions are allowed to observe.
This is the database rulebook for overlapping work.

- `READ_COMMITTED`: prevents dirty reads; common default
- `REPEATABLE_READ`: stable row reads inside the transaction
- `SERIALIZABLE`: strongest guarantee, highest contention cost

Practical rule:

- isolation level is not a substitute for good write design
- stronger isolation helps, but it is not free
- you still need to understand lock scope, retries, and transaction length

Typical failure cases to connect back to business behavior:

- overselling stock
- duplicate reservation
- long-running transactions causing blocking
- deadlocks during bursts

---

## 6. MVCC In One Minute

Practical question:

> If one transaction is writing a row in Postgres, can another transaction still read it?

Short answer:

> Usually yes, because Postgres uses MVCC.

What that means in practice:

- Postgres keeps row versions
- readers can often see the previous committed version
- reads do not always block writes, and writes do not always block reads

Why it matters:

- better read concurrency
- less blocking than old lock-heavy mental models
- but old row versions still need cleanup, which is why autovacuum matters

`MVCC` means `Multi-Version Concurrency Control`.

That name sounds intimidating, but the basic idea is simple:

- instead of one row having only one visible version, the database can keep older committed versions around briefly
- that lets many reads continue without waiting behind every write

MVCC is not a magic shield:

- writers can still block writers
- poor transaction design still causes contention

---

## 7. The Smallest Spring Decision Pattern

If you want the Spring version, keep the answer simple:

1. identify the shared state
2. identify the invariant you must protect
3. choose optimistic or pessimistic control
4. keep the transaction short
5. define the retry or conflict behavior explicitly

Clean example:

> For inventory I would first ask whether hot contention is common. If not, I would
> start with optimistic locking using a version field and explicit retry or conflict
> handling. If contention is frequent and overselling risk is high, I would move to a
> tighter transaction with `SELECT ... FOR UPDATE` or a pessimistic lock, but I would
> keep that transaction very short to avoid killing throughput.

---

## 8. 20-Second Answer

> The main concurrency bug I think about first is the lost update problem: two requests
> read the same value, both write back a derived value, and one write silently overwrites
> the other. My default choice is optimistic locking when conflicts are rare, because it
> scales better. If contention is high or the business boundary is especially strict, I
> use pessimistic locking or `SELECT FOR UPDATE`, but only inside a short transaction.

---

## 9. 1-Minute Answer

> I frame database concurrency around the business invariant, not around locks first. A
> classic problem is lost update: two requests read the same stock value and overwrite each
> other. Optimistic locking detects that race with a version field and is usually my default
> when collisions are uncommon because it avoids blocking. Pessimistic locking is more
> appropriate when contention is frequent and the cost of a conflict is high, but it reduces
> throughput and increases deadlock risk, so I keep the transaction short. I also separate
> locking from isolation level: `READ_COMMITTED`, `REPEATABLE_READ`, and `SERIALIZABLE`
> change what transactions can observe, but they do not remove the need for good write-path
> design. In Postgres I also rely on MVCC knowledge to explain why readers often do not block
> writers.

---

## 10. What To Internalize

- the lost update problem is the first concurrency bug to explain
- optimistic locking detects conflicts; it does not prevent them up front
- pessimistic locking serializes access by blocking
- stronger locking and stronger isolation both cost throughput
- short transactions matter as much as lock choice
- MVCC improves read concurrency but does not remove write contention
