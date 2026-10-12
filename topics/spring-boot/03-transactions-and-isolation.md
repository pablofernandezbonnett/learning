# Transactions in Spring

`@Transactional` tells Spring where to begin and end database work for a
method call. It is useful only when the call passes through Spring's
transaction advice and the chosen transaction manager covers the database
operations involved. The annotation does not choose the business invariant
or make a remote call part of the database transaction.

For those decisions, use [idempotency and local transactions](../databases/01-idempotency-and-transaction-safety.md)
and [concurrent writes](../databases/02-database-locks-and-concurrency.md).

## Put the Boundary on a Service Call

```kotlin
@Service
class OrderService(
    private val orders: OrderRepository,
    private val attempts: AttemptRepository,
) {
    @Transactional
    fun createOrder(command: CreateOrder): OrderId {
        val order = orders.save(command.toOrder())
        attempts.save(command.toCompletedAttempt(order.id))
        return order.id
    }
}
```

When a caller invokes this method through the Spring bean, both repository
writes can join one local transaction. If the method fails under a rollback
rule, neither write commits. The request key, uniqueness constraints, and
concurrent-write guards still have to be designed at the data boundary.

By default, Spring's declarative transactions use a **proxy**: an object
around the bean that applies the transaction before and after an external
method call. A method calling another `@Transactional` method on `this`
bypasses that proxy, so the second method's annotation does not take effect.
Put an independently configured transaction on another Spring bean, or call
through a deliberate programmatic transaction boundary. See
[Proxies and AOP](./07-proxies-and-aop.md) for the proxy mechanism.

## Know the Defaults That Change Behavior

| Setting | Default | Practical consequence |
|---|---|---|
| Propagation | `REQUIRED` | Join an existing transaction or create one |
| Isolation | `DEFAULT` | Use the database or transaction manager's default |
| Rollback | `RuntimeException` and `Error` | Checked exceptions do not trigger rollback by default |
| Read-only | `false` | `readOnly = true` is a hint, not a general write prohibition |

An exception that is caught and not rethrown may let the transaction commit.
For a checked exception that should roll back, configure `rollbackFor` or
choose a failure type covered by the policy. The same Spring rollback rule
applies to exception types thrown by Kotlin code.

`isolation = DEFAULT` does not mean the same isolation level on every
database. With `REQUIRED`, an inner method that joins an existing transaction
normally inherits its physical transaction's settings; an annotation on the
inner method does not create a new isolation boundary. See
[concurrent writes](../databases/02-database-locks-and-concurrency.md) for
choosing the database operation that protects the rule.

## Choose Propagation for the Intended Commit

Propagation decides what happens when an annotated method is called while a
transaction already exists:

| Mode | Behavior | Use with care when |
|---|---|---|
| `REQUIRED` | Participate in the outer transaction | An inner failure marks the shared transaction rollback-only |
| `REQUIRES_NEW` | Suspend the outer transaction and start an independent one | The inner result must survive an outer rollback |
| `MANDATORY` | Fail if no transaction exists | A method must never be called outside a larger unit of work |
| `NOT_SUPPORTED` | Suspend the current transaction and run without one | The work deliberately must not hold a transaction open |

For example, an audit record written with `REQUIRES_NEW` can commit even if
the order transaction later rolls back. That separation must be intentional:
the audit cannot assume the uncommitted order will exist. The outer transaction
keeps its resources while the inner one obtains another connection, so heavy
use of `REQUIRES_NEW` can exhaust a small connection pool.

If an inner `REQUIRED` scope marks the shared transaction rollback-only, an
outer method cannot restore it by catching the exception. Spring can throw
`UnexpectedRollbackException` when the outer scope tries to commit. Handle
the failure at the correct transaction boundary.

## Keep the Local Scope Short

Do not hold a database transaction open while waiting for a payment provider,
another service, or a broker. A timeout may leave the remote effect uncertain
even if the local transaction rolls back. Persist local intent, commit, then
continue through a retry-safe workflow. See
[idempotency and local transactions](../databases/01-idempotency-and-transaction-safety.md)
and [distributed workflows](../architecture/03-distributed-transactions-and-events.md).

Before relying on an annotation, verify that the call crosses the proxy, the
expected transaction manager is selected, an exception triggers the intended
rollback, and a nested call has the propagation behavior you meant.

## Spring References

- [`@Transactional` settings and proxy behavior](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)
- [Propagation and independent transactions](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html)
- [Rollback rules](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html)
