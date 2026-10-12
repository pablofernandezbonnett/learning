# Idempotency and Local Transactions

A client can retry after a timeout without knowing whether the first request
committed. Idempotency makes the **same logical request** safe to repeat. A
local transaction makes related database writes commit or roll back together.
Use both when a retryable action changes several local records.

Two different customers competing for the last unit are a separate problem;
[Database locks and concurrency](./02-database-locks-and-concurrency.md)
owns that decision.

## Give the Action a Stable Identity

Setting a value to a known final state can be naturally idempotent: repeating
`set active = true` leaves the same state. Repeating `increment balance by 10`
or `create payment` can create another effect. Give such an action a stable
request key or business ID that stays the same across retries.

For an API payment request, store at least:

| Field | Why it exists |
|---|---|
| Scope and request key | Identifies one action without colliding with another tenant or operation |
| Request fingerprint | Rejects reuse of the key with a different amount or target |
| Processing state | Distinguishes unfinished work from a completed result |
| Result reference | Lets a retry find the original payment or order |

A database uniqueness constraint can make the claim durable across processes:

```sql
CREATE TABLE request_attempts (
    tenant_id bigint NOT NULL,
    operation text NOT NULL,
    request_key text NOT NULL,
    payload_hash text NOT NULL,
    state text NOT NULL,
    result_ref text,
    PRIMARY KEY (tenant_id, operation, request_key)
);
```

The key and fingerprint are different: the key says *which attempt* this is;
the fingerprint checks that a caller did not attach a different request to it.
Define how long records remain and how that period relates to client retry and
downstream deduplication windows.

## Commit Local Work Together

For an action entirely inside one database, claim the key, make the business
change, and save its outcome in one short transaction. The unique constraint
lets only one transaction claim that key. A competing retry waits or sees the
existing attempt; it must not run the business change again.

```text
begin transaction
  claim (tenant, operation, request key) uniquely
  create the order and its related local records
  save the order ID as the attempt result
commit
```

If any local write fails, roll back the claim and the business change together.
On a retry, compare the fingerprint first. Return the stored result when the
attempt completed; for unfinished work, return an explicit in-progress outcome
or wait according to the API contract. A retry need not receive identical HTTP
bytes, but it must not create a second business effect.

Also enforce a unique **business key** when the domain requires it, such as
one order per checkout ID. A request key protects a retry of one attempt;
the business key protects the domain even if a caller sends a new request key.

## Handle External Effects Separately

A database transaction cannot include a payment provider, HTTP service, or
broker publish merely because those calls happen inside one method. If an
external effect is needed:

1. Persist a durable pending attempt or work intent before making the call.
2. Send the same downstream idempotency key on every retry, if the provider
   supports it.
3. Record the confirmed outcome. After a timeout, treat it as unknown until
   the provider response or reconciliation establishes what happened.
4. Resume the same attempt; do not create a new charge simply because the
   first response was lost.

If the provider has no idempotency support, do not automatically repeat an
uncertain call. Query or reconcile its outcome before deciding the next step.

For a database write followed by message publication, save an outbox record
with the business write and publish it later. The
[distributed workflow guide](../architecture/03-distributed-transactions-and-events.md)
owns that handoff and reconciliation detail.

Webhooks and broker messages can be delivered again. Their consumer needs its
own durable event or business identity and must commit deduplication with the
effect it protects. See [Webhooks](../api/03-webhooks-basics.md) and
[message delivery](../api/02-message-brokers-and-delivery-semantics.md).

## Check the Failure Paths

- Same key, same payload: return or resume the original attempt.
- Same key, different payload: reject it without changing the original.
- Crash before local commit: retry may claim and run the transaction.
- Crash after local commit but before response: retry finds the saved result.
- External call times out: reconcile its outcome before any new effect.
- A different request key targets the same business object: the business
  uniqueness rule still prevents an invalid duplicate.

For HTTP method semantics, see [API design](../api/01-api-design.md). For
Spring transaction boundaries, see
[Transactions and isolation in Spring](../spring-boot/03-transactions-and-isolation.md).
