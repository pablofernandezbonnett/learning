# 17. Transactions and Isolation in Spring

> Primary fit: `Shared core / Payments / Fintech`

Transactions are one of the easiest backend topics to sound confident about while still
getting the design wrong.

This note keeps the topic practical:

- what a transaction really gives you
- the smallest correct example
- how `@Transactional` behaves in Spring
- how to explain it clearly without hand-waving

---

## 1. What A Transaction Actually Gives You

A transaction gives you a **local consistency boundary** for one database.

Short version:

- all local changes commit together
- or all local changes roll back together
- concurrent operations see the database under a defined isolation model

Small concrete example:

- deduct balance
- create order row

If both writes are in the same database transaction:

- either both happen
- or neither happens

What a transaction does **not** automatically give you:

- consistency with Kafka
- consistency with Stripe
- consistency with another service over HTTP

That distinction is one of the main senior-level signals.

---

## 2. The Smallest Useful Example

```kotlin
@Service
class OrderService(
    private val accountRepository: AccountRepository,
    private val orderRepository: OrderRepository,
) {
    @Transactional // both writes should commit together or roll back together
    fun createOrder(accountId: Long, amount: BigDecimal) {
        val account = accountRepository.findById(accountId).orElseThrow() // stop if the account does not exist
        account.debit(amount)
        accountRepository.save(account) // first local write

        orderRepository.save(Order(accountId = accountId, amount = amount)) // second local write
    }
}
```

Why this example matters:

- it shows one local unit of work
- if the second write fails, the first write rolls back too

That is the core mental model.

---

## 3. What `@Transactional` Actually Does

`@Transactional` is not magic inside the method body.

Spring normally applies transaction behavior through a proxy around the bean method call.

`proxy` here simply means:

- Spring wraps the bean with an extra layer
- that extra layer starts and ends the transaction around the method call
- if you bypass that layer, the transaction behavior may not run

That is why this common mistake happens:

```kotlin
class CheckoutService {
    fun runCheckout() {
        persistOrder() // direct self-call: may bypass Spring proxy, so @Transactional may not run
    }

    @Transactional
    fun persistOrder() { }
}
```

Why this is dangerous:

- self-invocation can bypass the proxy
- the transaction you think exists may not exist at all

Practical rule:

> put `@Transactional` on public service methods called through a bean boundary

---

## 4. Isolation Levels In Practical Terms

Isolation level answers:

> what can concurrent transactions observe while they overlap?

Smallest mental model:

- transaction = "all-or-nothing" for one unit of local work
- isolation = "what happens when two transactions run at the same time"

Think about two checkout requests hitting the same product stock at almost the same moment.
Isolation is about whether those two requests can read stale values, overwrite each other,
or force one another to retry.

The main levels worth remembering:

- `READ_COMMITTED`: common default, prevents dirty reads
- `REPEATABLE_READ`: row reads stay stable inside the transaction
- `SERIALIZABLE`: strongest guarantee, highest contention cost

What that means in practice:

### `READ_COMMITTED`

You only see data that other transactions have already committed.
That prevents obvious nonsense like reading uncommitted balance deductions.

But it does **not** mean your whole read-modify-write flow is safe by itself.
Two transactions can still both read `stock = 1`, both decide "I can sell this",
and then race unless you add locking or an atomic update pattern.

Good default when:

- contention is moderate
- you want sane baseline behavior
- you will enforce business correctness with constraints, locking, or update conditions where needed

### `REPEATABLE_READ`

If you read the same row twice inside one transaction, you keep seeing the same committed value.
That makes your in-transaction view more stable.

This is useful when your logic depends on reading the same row more than once and expecting consistency.
But it is not a magic "no concurrency bugs ever" mode.
Exact behavior still depends on the database engine and your query pattern.

### `SERIALIZABLE`

This is the strongest mental model:
the database tries to make concurrent execution behave as if transactions had run one by one.

That gives the strongest protection, but the cost is usually:

- more blocking
- more contention
- more transaction retries or failures under load

Use it when the invariant is extremely important and the throughput cost is acceptable.

What matters here:

- you know stronger isolation is not free
- you connect isolation to business invariants
- you still think about locking and transaction scope
- you do not pretend isolation alone solves every race condition

Typical failure cases:

- lost updates
- overselling
- deadlocks under contention

Practical line:

> I treat isolation as a concurrency tradeoff, not a checkbox. `READ_COMMITTED` is often a good baseline, but if I need stronger protection for a business invariant, I combine the right isolation level with locking, constraints, or atomic update patterns.

What this means operationally:

- `READ_COMMITTED` is often enough **if the write itself is safe**
- `REPEATABLE_READ` helps when you need a stable view of the same row inside one transaction
- `SERIALIZABLE` is for the cases where the invariant is strong enough that you accept more blocking or retries

So the normal fix is not "increase isolation and hope."
The normal fix is "design the concurrent operation so the invariant is protected."

Smallest practical example:

- business rule: stock must never go below `0`
- weak shape: read stock, decide in application code, then write the new value
- stronger shape: use one atomic update such as `update ... where stock >= 1`

That matters because an atomic write often solves the race more cleanly than just moving from
`READ_COMMITTED` to a stronger isolation level.

### How to design a concurrent operation well

Start from the invariant, not from the annotation.

Ask:

1. what business rule must stay true under concurrent requests
2. can I express the write as one atomic database operation
3. do I need optimistic locking, pessimistic locking, or a uniqueness/check constraint
4. how short can I keep the transaction
5. if the database detects a conflict, do I retry, return `409`, or re-read and re-evaluate

Practical toolbox:

- atomic updates for simple counters, stock decrements, and state transitions
- unique constraints for "this must only happen once"
- optimistic locking when collisions are possible but not constant
- pessimistic locking when contention is high and the boundary is very sensitive
- retries only when the operation is safe to retry

Deeper concurrency design is covered in:

- [../../topics/databases/02-database-locks-and-concurrency.md](../../topics/databases/02-database-locks-and-concurrency.md)

---

## 5. Propagation

Propagation answers:

> what happens if this method runs while another transaction already exists?

Smallest mental model:

- isolation is about **concurrent transactions**
- propagation is about **nested method calls**

Plain English version:

- isolation = what overlapping requests can see
- propagation = what an inner service method does when an outer transaction already exists

Example shape:

```kotlin
@Service
class CheckoutService(
    private val orderRepository: OrderRepository,
    private val auditService: AuditService,
) {
    @Transactional
    fun checkout() {
        orderRepository.save(Order(...))
        auditService.record("checkout started")
    }
}
```

The propagation setting on `auditService.record(...)` decides whether it joins the same
transaction as `checkout()` or runs with different transaction behavior.

The main ones to remember:

- `REQUIRED`: join the current transaction or create one
- `REQUIRES_NEW`: suspend the outer one and start a new one
- `MANDATORY`: fail if no transaction exists
- `NOT_SUPPORTED`: run without a transaction

What that means in practice:

### `REQUIRED`

This is the normal default.
If `checkout()` already opened a transaction, the inner method joins it.

Implication:

- if the outer transaction rolls back, the inner work rolls back too
- this is usually what you want for one business operation

### `REQUIRES_NEW`

The outer transaction pauses and the inner method gets its own separate transaction.

Implication:

- the inner work can commit even if the outer transaction later fails
- useful for audit logs, isolated failure recording, or side work you explicitly want to keep
- dangerous if you accidentally persist business state that should have rolled back with the main flow

### `MANDATORY`

This is a guardrail.
It means: "this method only makes sense inside an existing transaction."

### `NOT_SUPPORTED`

This says: "if there is a transaction, suspend it; I want this code to run outside one."
Useful for non-transactional reads or side work that should not participate in the current transaction.

Practical rule:

- `REQUIRED` is the normal default
- use `REQUIRES_NEW` carefully

Why `REQUIRES_NEW` is tricky:

- useful for audit or isolated side work
- easy to misuse
- the outer rollback does not undo the inner committed work

Practical line:

> Propagation is about how nested service calls relate to an existing transaction. I use `REQUIRED` by default, and I only use `REQUIRES_NEW` when I truly want the inner work to commit independently of the outer result.

---

## 6. Transactions Do Not Solve Distributed Coordination

This is the main correction most people need.

Broken mental model:

- reserve stock
- call payment provider
- publish event
- update order
- assume one `@Transactional` annotation made the whole workflow atomic

That is false.

A local Spring transaction stops at the local database boundary.

It can commit or roll back:

- your own database writes
- within the same local database transaction

It cannot automatically commit or roll back:

- a payment provider call over HTTP
- a Kafka publish
- a webhook already delivered
- another service's database update

Smallest failure example:

1. your service inserts `payment_attempt(status=PENDING)`
2. your service calls the payment provider
3. the payment provider charges successfully
4. before your local transaction finishes, your app crashes

Now the provider may have taken money, but your local database may still say the payment
is pending or even missing the final update.

That is the core distributed-coordination problem:
the external system and your local database do not share one simple ACID (atomicity, consistency, isolation, durability) transaction.

So the real job is not "make everything atomic with one annotation".
The real job is "make the workflow safe even when steps succeed at different times".

For external effects, you usually need:

- idempotency
- outbox
- retries
- compensating actions

Why each one exists:

- idempotency: safe retries without double-charging or double-creating work
- outbox: commit local state and "message to publish" together, then publish reliably afterward
- retries: external calls fail transiently, so one attempt is not enough
- compensating actions: if one step succeeded and a later step failed, you may need a business undo flow such as refund or cancellation

Practical rule:

- use transactions for local database correctness
- use workflow patterns for cross-system correctness

This is why payments, inventory, and event publishing discussions almost always mention:

- idempotency keys
- callbacks/webhooks
- reconciliation jobs
- outbox/inbox patterns

Short rule:

> keep the transaction boundary local and treat external effects as a second problem

---

## 7. The Smallest E-commerce Framing

Bad shape:

- open transaction
- do database write
- call payment provider
- call another service
- publish event
- hold the transaction open through all of it

Better shape:

- use one local transaction for local state
- persist the intent to publish or continue the workflow
- execute external side effects with retry-safe handling

That is why outbox and idempotency keep appearing next to transaction discussions.

### Payment flow framing

Smallest payment flow:

- create a local payment row with status `PENDING`
- store an idempotency key or merchant request key
- commit that local transaction
- call the external payment provider or wallet API outside the DB transaction
- persist the provider result in a new local transaction
- continue with retry-safe event or callback handling

Bad shape:

- open one DB transaction
- insert payment row
- call the payment provider while the transaction is still open
- wait on network latency and provider response
- commit local transaction only at the end

Why this is bad:

- your local transaction does not make the payment provider call atomic
- you hold locks longer while waiting on the network
- timeouts and retries become harder to reason about
- if the provider succeeded but your commit failed, you now need reconciliation

Better shape:

- commit your local intent first
- make the external payment call with idempotency
- update local state from the provider response or callback
- design for retries, duplicate callbacks, and reconciliation

What matters here:

- you know `@Transactional` only protects local database work
- you separate local consistency from external payment coordination
- you mention idempotency, webhook/callback handling, and reconciliation as first-class concerns

---

## 8. Common Mistakes

- self-invocation bypassing transaction advice
- making the transaction too wide
- doing slow network calls inside the transaction
- assuming stronger isolation is always better
- using `REQUIRES_NEW` casually
- thinking transaction correctness replaces idempotency

---

## 9. 20-Second Answer

> I treat a transaction as a local consistency boundary for one database. In Spring,
> `@Transactional` gives me all-or-nothing behavior for local writes, but it does not
> make Kafka publishes or payment provider calls atomic. I keep transactions short, use the default
> propagation unless I have a strong reason not to, and choose isolation based on the
> business invariant and contention profile.

---

## 10. 1-Minute Answer

> A transaction groups local database work into one unit: either all the local changes
> commit or they all roll back. In Spring I remember that `@Transactional` is usually
> proxy-based, so self-invocation can silently bypass it. I keep the transaction scope
> tight and use isolation as a correctness-versus-contention tradeoff, not as a checkbox.
> `READ_COMMITTED` is often the normal baseline, while stronger levels cost throughput.
> The most important senior distinction is that a transaction is local. It does not make
> HTTP calls, Kafka publishes, or payment provider operations atomic. For those boundaries
> I combine transactions with idempotency, outbox, retries, and compensating logic rather
> than pretending one annotation solves distributed coordination.

---

## 11. What To Internalize

- a transaction is a local database consistency boundary
- `@Transactional` behavior depends on Spring proxies
- self-invocation is a common trap
- isolation is a business and contention tradeoff
- short transactions are usually safer than wide transactions
- transactions and idempotency solve different problems
