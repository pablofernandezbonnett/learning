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

Quick terms used here:

- `stale state` = the request is making a decision from data that was true a moment ago but is no longer current
- `contention` = several requests want the same row or business key at the same time
- `isolation level` = the database rulebook for what concurrent transactions may observe

---

## Why This Matters

Concurrency mistakes are one of the fastest ways to break checkout, stock,
ledger, and payment flows even when the code looks correct in single-threaded
thinking.

This matters because many strong backend systems fail not from bad schemas but
from unsafe read-modify-write logic under real contention.

## Smallest Mental Model

Treat concurrency as a business-rule protection problem, not just a threading
or SQL topic.

The useful first question is:

> what must remain true if two or twenty requests try to update the same thing
> at the same time?

## Bad Mental Model vs Better Mental Model

Bad mental model:

- one transaction annotation makes the flow safe
- if the code reads cleanly, concurrent requests will behave cleanly too
- locking is mostly a database tuning topic

Better mental model:

- concurrency safety is about protecting business rules when several writers act
  at the same time
- transactions still need the right read/write pattern, lock strategy, and
  conflict handling
- locking choice is a correctness decision before it is a performance decision

Small concrete example:

- weak approach: read stock, subtract one in memory, write the new value back
- better approach: detect the write race with optimistic locking or serialize
  access with a short pessimistic lock when contention is high

Strong default:

- start by naming the business invariant, then choose an atomic conditional
  write, optimistic locking, or pessimistic control based on the data shape,
  contention, and conflict cost

Reusable takeaway:

> I frame database concurrency around protecting one business invariant under
> competing writers. For a simple inventory counter, I use an atomic
> conditional update; for richer state, I choose optimistic locking for rarer
> collisions or pessimistic locking when contention is high and retries are too
> expensive.

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

These terms matter, but the first useful mental model is still the lost update:
two writers start from the same old fact and one silently overwrites the other.

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

<details>
<summary>Java version</summary>

```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Product {

    @Id
    private Long id;

    private int stock;

    @Version
    private Long version;
}
```

</details>

Important nuance:

> optimistic locking does not prevent conflicts, it detects them.

That is why optimistic locking is usually paired with explicit retry logic or a
clear "please try again" response to the caller.

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

<details>
<summary>Java version</summary>

```java
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Product findByIdForUpdate(Long id);
}
```

</details>

When it fits:

- contention is frequent
- you cannot tolerate optimistic retries at that boundary
- the transaction can stay short

Tradeoff:

- stronger serialization
- lower throughput
- deadlock risk if you lock multiple rows in inconsistent order

`Serialization` here means you are deliberately forcing one writer to finish
before another writer can continue on the same protected row or range.

### 3.3 Atomic conditional update for limited inventory

For a numeric availability counter, the simplest strong default is often not
"read, then lock, then write." It is one database statement that claims stock
only if enough remains.

The portable idea is simple. Model remaining capacity separately from the
reservation record: one capacity entry represents one resource and one time
slot, such as a hotel room type on one night. A reservation may only be created
after it claims every required capacity entry.

For one room on one night, the conditional claim looks like this in SQL-like
pseudocode:

```text
decrease available rooms by 1
only where this hotel, room type, and night still have at least 1 room
```

Every relational database has an equivalent of this operation. The application
must learn whether the claim succeeded from the statement result:

- success: this request claimed the room
- no matching change: availability is gone, so return a conflict

Two callers can issue this at the same time. The database coordinates the
conflicting write: one claim succeeds, and the other finds that the condition
is no longer true. The important property is not the syntax. It is that the
database evaluates "is there capacity?" and decrements it as one protected
change, rather than the application making those two steps separately.

There is no unsafe application-side `SELECT available_rooms` followed later by
an unconditional update.

For a stay across several nights, make the whole claim and reservation one short
local transaction:

```text
begin transaction
  claim every requested night, only if each still has enough capacity
  if every night was claimed:
    create reservation or temporary hold
    commit
  otherwise:
    roll back
```

If one night is unavailable, rollback means any earlier decrement in this
attempt disappears too. The client receives a conflict such as `409` with an
actionable "availability changed" response; it should choose another option
rather than blindly retrying the same request.

The invariant is:

> confirmed reservations must never make `available_rooms` negative, and every
> confirmed reservation must have claimed all of its nights atomically.

Important boundaries:

- this protects two different customers competing for the same inventory
- an `Idempotency-Key` separately protects one customer retrying the same
  reservation request after a timeout; see
  [`01-idempotency-and-transaction-safety.md`](./01-idempotency-and-transaction-safety.md)
- do not keep row locks open while calling a payment provider; create a
  durable, expiring hold in the local transaction when payment confirmation is
  later, then confirm or release that hold through explicit state transitions
- handle a database deadlock or serialization failure with a small bounded
  retry of the whole transaction, not a retry loop that runs forever

Do not use a cache or a distributed lock as the final authority here. They can
reduce load or coordinate best effort, but the transaction that changes durable
inventory must enforce the no-oversell rule.

### Engine-Specific Mappings

The strategy above is database-agnostic. These are implementation differences,
not different correctness rules:

- PostgreSQL can return claimed rows directly with `UPDATE ... RETURNING`
- MySQL uses the conditional `UPDATE` and the affected-row count to tell the
  application whether capacity was claimed
- all engines have their own lock, deadlock, and isolation behavior; use their
  documentation when writing the production query

MySQL/InnoDB companion notes:

- use `InnoDB`; a transaction annotation cannot give transactional semantics to
  a non-transactional table engine
- keep the transaction short: do not call payment, email, or another HTTP API
  before `COMMIT` or `ROLLBACK`
- MySQL/InnoDB defaults to `REPEATABLE READ`, but the safe result above comes
  from the conditional write and transaction, not from changing isolation level
  and hoping

---

## 4. How To Choose A Concurrency Strategy

The clean rule is:

- prefer an **atomic conditional update** when one counter or state transition
  can express the business rule directly
- prefer **optimistic** when collisions are uncommon and scale matters
- choose **pessimistic** when contention is high and the cost of a conflict is high

Good examples:

- profile updates: optimistic is usually fine
- inventory decrement represented by a counter: atomic conditional update is
  often the simplest fit
- inventory with a complex decision or consistently hot rows: pessimistic may
  be justified
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

- `READ_COMMITTED`: prevents dirty reads; common default in some relational
  databases
- `REPEATABLE_READ`: stable row reads inside the transaction; the default for
  MySQL/InnoDB unless the server or session changes it
- `SERIALIZABLE`: strongest guarantee, highest contention cost

Practical rule:

- isolation level is not a substitute for good write design
- stronger isolation helps, but it is not free
- you still need to understand lock scope, retries, and transaction length

`Lock scope` means exactly which rows, keys, or ranges are being protected and
for how long the transaction keeps them.

Typical failure cases to connect back to business behavior:

- overselling stock
- duplicate reservation
- long-running transactions causing blocking
- deadlocks during bursts

---

## 6. MVCC In One Minute: Postgres And InnoDB

Practical question:

> If one transaction is writing a row, can another transaction still read it?

Short answer:

> Often yes. Both Postgres and MySQL/InnoDB use MVCC, but the exact result also
> depends on the isolation level and whether the read asks for a lock.

What that means in practice:

- the database retains enough information about older row versions to give a
  consistent read
- readers can often see the previous committed version
- reads do not always block writes, and writes do not always block reads

Why it matters:

`MVCC` means `Multi-Version Concurrency Control`: the database keeps enough
row-version information for many readers to continue without waiting for every
writer to finish.

- better read concurrency
- less blocking than old lock-heavy mental models
- but old versions still need cleanup; Postgres and InnoDB manage that work
  differently

MVCC is not a magic shield:

- writers can still block writers
- poor transaction design still causes contention

---

## 7. The Smallest Spring Decision Pattern

If you want the Spring version, keep the answer simple:

1. identify the shared state
2. identify the invariant you must protect
3. choose an atomic conditional write, optimistic, or pessimistic control
4. keep the transaction short
5. define the retry or conflict behavior explicitly

Clean example:

> For a simple inventory counter, I prefer an atomic conditional decrement because
> the database either claims the stock or returns zero rows. If the decision spans
> richer state, I use optimistic locking for uncommon collisions, or a short
> `SELECT ... FOR UPDATE` transaction when hot contention makes conflicts too costly.

---

## 8. 20-Second Answer

> The main concurrency bug I think about first is the lost update problem: two requests
> read the same value, both write back a derived value, and one write silently overwrites
> the other. For a simple stock or room counter, I prefer an atomic conditional update:
> it decrements only when availability remains, so one caller wins and the other gets a
> conflict. For richer state, I choose optimistic locking for rare collisions or a short
> pessimistic transaction when contention is high.

---

## 9. 1-Minute Answer

> I frame database concurrency around the business invariant, not around locks first. For
> a hotel room or simple stock counter, the invariant is that confirmed reservations cannot
> make availability negative. I enforce it with a short transaction containing an atomic
> conditional update and the reservation insert: one request claims the inventory, while a
> competing one updates zero rows and receives a conflict. An idempotency key handles a retry
> of that same request; it is different from competition between two customers. For richer
> state I use optimistic locking for rare collisions, or a short pessimistic transaction when
> contention is high. I keep transactions short, especially around external payment calls,
> because locks reduce throughput and can deadlock.

---

## 10. What To Internalize

- the lost update problem is the first concurrency bug to explain
- an atomic conditional update is often the simplest safe claim for a limited
  numeric inventory counter
- optimistic locking detects conflicts; it does not prevent them up front
- pessimistic locking serializes access by blocking
- stronger locking and stronger isolation both cost throughput
- short transactions matter as much as lock choice
- MVCC improves read concurrency but does not remove write contention

## Further Reading

- [MySQL `UPDATE` statement](https://dev.mysql.com/doc/refman/8.4/en/update.html): syntax and affected-row behaviour used by the conditional claim
- [MySQL/InnoDB autocommit, commit, and rollback](https://dev.mysql.com/doc/refman/8.4/en/innodb-autocommit-commit-rollback.html): why a multi-statement reservation needs an explicit transaction
- [MySQL/InnoDB transaction isolation levels](https://dev.mysql.com/doc/refman/8.4/en/innodb-transaction-isolation-levels.html): the `REPEATABLE READ` default and locking implications
