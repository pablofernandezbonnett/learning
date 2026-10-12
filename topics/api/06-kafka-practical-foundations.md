# Kafka Practical Foundations

Read [brokers and delivery semantics](./02-message-brokers-and-delivery-semantics.md)
for the queue-versus-stream decision, consumer idempotency, and outbox. This
note adds the Kafka-specific decisions needed to implement a stream safely.

## Topic, Partition, and Key

A topic is a named stream. Its partitions are separate ordered sequences of
records. A record key normally keeps records for the same entity in one
partition, so choose the key from the business ordering requirement.

For `OrderCreated` and `OrderCancelled`, using `orderId` as the key keeps
one order's records together. A random key would scatter them. Kafka does not
provide a global order across partitions, and processing code can still break
the order even when records arrive in order.

```text
topic: orders
key: order-42
value: OrderCreated(eventId=evt-1, orderId=order-42)
```

More partitions can raise parallelism, but they also change assignment and
ordering assumptions. Do not add them just because throughput sounds useful.

## Consumer Groups and Offsets

Separate consumer groups can read the same topic independently. Inside one
group, a partition is assigned to one consumer at a time; more consumers than
partitions will not increase parallelism for that topic.

An offset is a record's position within one partition. The committed consumer
position tells the group where to resume after a restart:

- Commit before durable business work: the consumer may skip work after a crash.
- Commit after durable business work: the record may be read again after a
  crash, so processing must tolerate duplicates.

Rebalances can move a partition to another consumer. Do not keep the only
copy of progress in a consumer's memory.

## Processing Safely

Give each event a stable ID. Claim it durably and apply a local business change
in the same database transaction when possible. For external effects, pass a
stable idempotency key downstream or reconcile uncertain results. Commit the
offset only after the effect is durable enough for the workflow.

If order matters, avoid parallel processing that lets a later record for the
same key finish first. A dead-letter topic (DLT) may let the stream progress,
but it also means a later record can arrive before the failed one is repaired.
Define that business rule explicitly.

Kafka producer idempotence reduces duplicates from producer retries, and
`acks=all` waits for the configured in-sync replicas to acknowledge a write.
Neither protects a separate database write or an external side effect. For a
database change that must lead to an event, design a durable handoff such as
the [outbox pattern](./02-message-brokers-and-delivery-semantics.md#reliable-publication).

## Retry and Replay

Decide which errors are temporary, how many attempts to make, and when to move
the record to a DLT. Record why it failed and who owns replay. Replaying old
events may reach code and data that have since changed, so retain compatible
event schemas and test the recovery path.

Monitor consumer lag, failed records, DLT volume, and age of unprocessed
business work. Lag shows delay; it does not by itself say whether the system
has applied the right business state.

## Small Decision Check

- Which business entity must stay ordered? Use that as the key.
- How many independent processing purposes exist? Give each its own group.
- When is processing durable enough to advance the offset?
- What happens if the same record arrives again?
- What happens when a record fails permanently or is replayed months later?

See the Kafka delivery exercise in the
[Kotlin backend lab](../../labs/kotlin-backend-examples/README.md).

## Further Reading

- [Apache Kafka documentation](https://kafka.apache.org/documentation/)
