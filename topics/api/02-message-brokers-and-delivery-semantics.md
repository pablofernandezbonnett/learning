# Message Brokers and Delivery Semantics

Read [sync vs async](./07-sync-vs-async-integration-choice.md) first to decide
whether work belongs outside the request. This note covers what changes when
a queue or event stream carries that work. For Kafka implementation details,
see [Kafka foundations](./06-kafka-practical-foundations.md).

## Why Use a Broker?

A broker can buffer work, let a user request finish without waiting for later
tasks, and let independent consumers react to an event. It also introduces
delayed results, duplicate delivery, backlog, and recovery work. Use it when
that tradeoff solves a real need.

Example: placing an order may require a durable order write now, while email
and analytics can run later. The user should not wait for those follow-ups.

## Queue or Event Stream?

| Need | Usual fit | Main tradeoff |
| --- | --- | --- |
| One worker path handles a job, such as sending email | Queue | Retained history and independent replay are not its main purpose. |
| Several systems need the same business fact, such as `OrderPlaced` | Event stream/log | Retention and replay add operational and schema work. |

A queue distributes work. An event stream keeps events for a retention period
so separate consumer groups can read or replay them. SQS and RabbitMQ are queue
examples; Kafka is an event stream example. These are useful starting points,
not a rule that every product has only one pattern.

Before choosing, ask who owns the work, who else needs the event, whether
replay matters, and what happens if processing stops for an hour.

## Delivery and Duplicate Effects

If a consumer completes a database write but fails before acknowledging a
message, the broker may deliver it again. Prefer a design that tolerates this
retry: a stable event ID, a durable uniqueness claim, and a business transition
that is safe to repeat.

For a local database effect, claim the event ID and apply the business change
in one transaction:

```sql
BEGIN;
INSERT INTO processed_events (event_id)
VALUES (:event_id)
ON CONFLICT DO NOTHING;
-- Only if the insert claimed a new event:
UPDATE orders SET status = 'READY' WHERE id = :order_id AND status = 'NEW';
COMMIT;
```

The sketch requires checking whether the insert actually claimed the ID. A
read-then-write check is unsafe when two consumers run concurrently. If the
effect happens in another service, this local transaction cannot cover it;
give the downstream call its own stable idempotency key or reconciliation
path. See [idempotency and transaction safety](../databases/01-idempotency-and-transaction-safety.md).

Broker features advertised as "exactly once" have a scope. They do not by
themselves make an external payment, email, or database effect happen once.
For many integrations, at-least-once delivery plus idempotent processing is
the practical model.

## Retries and Failed Messages

Retry temporary failures with a limit and backoff. Put messages that cannot be
processed normally into a dead-letter queue or topic (DLQ/DLT), with an owner
to inspect, repair, and replay them. An invalid payload is different from a
temporary outage: retrying it forever blocks progress.

Decide whether later messages for the same entity may pass a failed message.
That choice affects ordering and recovery. Watch backlog age, failure counts,
and dead-letter volume so asynchronous work cannot fail silently.

## Reliable Publication

If a committed database change must produce a broker event, the publisher
needs an [outbox](../architecture/03-distributed-transactions-and-events.md).
Broker delivery rules start after publication; they cannot make a separate
database write and publish atomic.

## Review Questions

1. Does the caller need a final answer now, or can the work finish later?
2. Is this one owned task or a fact several consumers need?
3. What duplicate effect could a retry cause?
4. Can the business write commit while publication fails?
5. Who owns dead-letter recovery and backlog monitoring?

For concrete Kafka partition, offset, and consumer-group decisions, continue
with [Kafka foundations](./06-kafka-practical-foundations.md).
