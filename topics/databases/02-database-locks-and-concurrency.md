# Protect Data Under Concurrent Writes

Concurrency becomes a correctness problem when two requests read the same old
state and both act on it. For example, two customers can see one remaining
room. The displayed availability is a past observation; a durable write must
decide which customer gets it.

Start by naming the **invariant**: a business rule that must still hold after
all competing requests finish. For room inventory, confirmed reservations
must never exceed available capacity.

## Make the Final Claim Atomic

Assume one availability row per hotel, room type, and night, protected by a
unique constraint; also constrain `remaining >= 0`. Combine the check and
change in one statement:

```sql
UPDATE room_availability
SET remaining = remaining - 1
WHERE hotel_id = :hotel_id
  AND room_type_id = :room_type_id
  AND stay_date = :stay_date
  AND remaining >= 1;
```

Check the affected-row count. One changed row means the request claimed a
unit; zero means no claim was made, so inspect whether the row is absent or
capacity is gone. Do not read availability in the application and later send
an unconditional update: two callers can both act on the same old value.

For a booking across several nights, claim each night and insert the
reservation in one local transaction. Process nights in a consistent order
to reduce deadlock risk. If any claim fails, roll back every earlier claim in
that attempt. A competing customer receives a conflict and can choose another
option. The [idempotency guide](./01-idempotency-and-transaction-safety.md)
covers a retry of the **same** booking request after an uncertain response.

A unique constraint is another atomic claim when the invariant is one record
per key, such as one booking of a specific seat for a specific event. Let the
database reject a second insert for that key.

## Choose a Control for Richer Changes

| Control | How it works | Good fit | Main cost |
|---|---|---|---|
| Conditional write | Changes a row only while a condition holds | Counter, capacity, or simple state transition | Must check the affected-row count |
| Unique constraint | Rejects a second record with the same business identity | Seat, checkout, or external reference | Conflict must become a domain result |
| Optimistic version | Updates only if the version still matches | Conflicts are uncommon and work can be retried | Caller must re-read and decide after a conflict |
| Pessimistic row lock | Holds a lock until the local transaction ends | Hot row or decision spanning several checks | Waiting, deadlocks, and lower throughput under contention |

An optimistic update makes the conflict explicit:

```sql
UPDATE orders
SET status = 'CONFIRMED', version = version + 1
WHERE id = :order_id
  AND version = :expected_version
  AND status = 'PENDING';
```

One affected row means this transition won. Zero may mean another writer
changed the version or the transition is no longer valid; re-read before
deciding whether to retry or return a conflict. Repeating the same SQL
without checking the new state does not resolve that decision.

For a decision that must inspect a row before changing it,
`SELECT ... FOR UPDATE` can lock that row inside a short transaction. Other
writers may wait. If several rows are locked, take them in a consistent order
where possible.
Do not hold database locks while waiting for payment, email, or another remote
service. Save a short-lived hold and complete the external workflow afterward
when the domain requires that separation.

## Isolation and Failure Handling

Transaction isolation controls which concurrent changes a transaction can
observe. It does not replace a guarded write, a constraint, or a clear
conflict policy. Stronger isolation can abort transactions to preserve its
guarantee; retry the **whole** failed transaction from fresh state, with a
bound on attempts. A deadlock victim also needs a whole-transaction retry if
the business action is still valid.

Multiversion concurrency control (MVCC) lets ordinary reads often proceed
while writes occur. It does not remove writer-to-writer contention. Exact
isolation and lock behavior depend on the database engine; see
[PostgreSQL operating choices](./10-postgres-in-depth.md) and its official
documentation before relying on one engine's behavior.

## Verify the Invariant

Run competing requests against the same capacity or state transition. Check
that one succeeds when one unit remains, the loser receives a useful conflict,
and no confirmed reservation exists without a successful claim. Also test a
rollback after one of several nightly claims and a retry after a deadlock.

For Spring transaction mechanics and repository locking, see
[Transactions and isolation in Spring](../spring-boot/03-transactions-and-isolation.md).

## PostgreSQL References

- [Transaction isolation](https://www.postgresql.org/docs/current/transaction-iso.html)
- [`UPDATE`](https://www.postgresql.org/docs/current/sql-update.html)
- [Explicit locking](https://www.postgresql.org/docs/current/explicit-locking.html)
- [Constraints](https://www.postgresql.org/docs/current/ddl-constraints.html)
