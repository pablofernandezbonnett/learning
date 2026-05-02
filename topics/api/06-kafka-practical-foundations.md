# Kafka Practical Foundations

> Primary fit: `Shared core / Payments / Commerce`

Use this after [02-message-brokers-and-delivery-semantics.md](./02-message-brokers-and-delivery-semantics.md).

This note is for the moment when "Kafka is a durable log" is no longer enough
and you want the practical model that affects design and implementation.

Keep this line warm:

> Kafka gives you partitioned, replayable event streams; your job is to choose
> a good key, tolerate duplicates, and keep consumer progress explicit

---

## 1. When Kafka Is The Right Tool

Kafka is usually the right answer when:

- the event itself has durable value
- several consumers care about the same event
- replay matters
- high write throughput matters
- you need partitioned ordering by business key

Kafka is usually the wrong answer when:

- one worker group just needs background jobs
- nobody needs replay
- a simple queue would solve the problem more clearly

Short rule:

> Kafka is strongest when the event stream is part of the architecture, not
> just when you want "something async"

---

## 2. Topic, Partition, And Key

The key decides which partition receives the event.
That means the key also decides the ordering boundary you get.

Kotlin sketch:

```kotlin
data class OrderCreatedEvent(
    val eventId: String,
    val orderId: String,
    val customerId: String,
)

fun publish(event: OrderCreatedEvent) {
    kafkaTemplate.send(
        "orders.created",
        event.orderId, // same orderId -> same partition -> preserved local order
        event,
    )
}
```

<details>
<summary>Java version</summary>

```java
public record OrderCreatedEvent(
    String eventId,
    String orderId,
    String customerId
) {}

public void publish(OrderCreatedEvent event) {
    kafkaTemplate.send(
        "orders.created",
        event.orderId(), // same orderId -> same partition -> preserved local order
        event
    );
}
```

</details>

Good keys:

- `orderId` when one order's events must stay ordered
- `paymentId` when payment state transitions must stay ordered
- `sku` when inventory events for the same product must stay ordered

Bad keys:

- random UUID when you actually need business ordering
- no key when one entity's events must stay together

Important rule:

> Kafka ordering is within one partition, not across the whole topic

---

## 3. Consumer Groups And Offsets

Inside one consumer group:

- one partition is assigned to one consumer at a time
- more consumers than partitions means idle consumers
- crash or scale changes trigger rebalance

The offset is just the consumer's read position in a partition.
That is why commit timing matters.

Kotlin sketch:

```kotlin
@KafkaListener(topics = ["orders.created"], groupId = "inventory-service")
fun handle(record: ConsumerRecord<String, OrderCreatedEvent>) {
    val event = record.value()

    inventoryService.reserve(event.orderId)
    // offset commit should happen only after the business step is durable enough
}
```

<details>
<summary>Java version</summary>

```java
@KafkaListener(topics = "orders.created", groupId = "inventory-service")
public void handle(ConsumerRecord<String, OrderCreatedEvent> record) {
    OrderCreatedEvent event = record.value();

    inventoryService.reserve(event.orderId());
    // offset commit should happen only after the business step is durable enough
}
```

</details>

Short rule:

> commit before durable business work risks loss; commit after durable work risks duplicates

That tradeoff is normal.
It is why at-least-once plus idempotent consumer is the practical default.

---

## 4. Idempotent Consumer Is The Real Safety Net

If a consumer crashes after doing the business work but before the offset is
committed, the same record can be delivered again.

The consumer must tolerate that.

Kotlin sketch:

```kotlin
@Transactional
fun handle(event: OrderCreatedEvent) {
    val claimed = processedEventRepository.insertIfAbsent(event.eventId)
    if (!claimed) {
        return // duplicate replay or redelivery
    }

    inventoryService.reserve(event.orderId)
}
```

<details>
<summary>Java version</summary>

```java
@Transactional
public void handle(OrderCreatedEvent event) {
    boolean claimed = processedEventRepository.insertIfAbsent(event.eventId());
    if (!claimed) {
        return; // duplicate replay or redelivery
    }

    inventoryService.reserve(event.orderId());
}
```

</details>

The important part is not the exact repository method name.
The important part is:

- stable event ID
- unique durable claim
- business work only once

---

## 5. Retry And DLT Are Part Of The Design

Some failures are transient.
Some are poison messages.
Do not mix them.

Kotlin sketch:

```kotlin
@KafkaListener(topics = ["orders.created"], groupId = "inventory-service")
fun handle(record: ConsumerRecord<String, OrderCreatedEvent>) {
    try {
        inventoryService.reserve(record.value().orderId)
    } catch (error: TemporaryWarehouseTimeout) {
        throw error // let retry policy handle it
    } catch (error: InvalidInventoryState) {
        deadLetterPublisher.publish(record) // deterministic failure -> DLT
    }
}
```

<details>
<summary>Java version</summary>

```java
@KafkaListener(topics = "orders.created", groupId = "inventory-service")
public void handle(ConsumerRecord<String, OrderCreatedEvent> record) {
    try {
        inventoryService.reserve(record.value().orderId());
    } catch (TemporaryWarehouseTimeout error) {
        throw error; // let retry policy handle it
    } catch (InvalidInventoryState error) {
        deadLetterPublisher.publish(record); // deterministic failure -> DLT
    }
}
```

</details>

What to decide explicitly:

- how many retries
- backoff shape
- which failures are retryable
- when to send to DLT
- who owns replay from DLT

---

## 6. Outbox Still Matters

Kafka does not remove the dual-write problem.

Broken shape:

```kotlin
orderRepository.save(order) // DB write succeeds
kafkaTemplate.send("orders.created", order.id, event) // broker call may fail
```

Safe shape:

- save order and outbox row in one local transaction
- relay the outbox later to Kafka
- keep consumers idempotent anyway

Short rule:

> Kafka is reliable transport, not a magic replacement for local transaction boundaries

Related reading:

- [../architecture/03-distributed-transactions-and-events.md](../architecture/03-distributed-transactions-and-events.md)
- [../databases/01-idempotency-and-transaction-safety.md](../databases/01-idempotency-and-transaction-safety.md)

---

## 7. Common Traps

- using a random key when one entity needs ordered processing
- adding more consumers than partitions and expecting more throughput
- treating "exactly once" as if downstream business effects can never duplicate
- no DLT plan
- no replay plan
- no event schema ownership
- using Kafka for a plain worker queue with no replay need

---

## 8. Companion

Read first:

- [02-message-brokers-and-delivery-semantics.md](./02-message-brokers-and-delivery-semantics.md)
- [05-message-brokers-cheatsheet.md](./05-message-brokers-cheatsheet.md)

Then run:

- [../../labs/kotlin-backend-examples/README.md](../../labs/kotlin-backend-examples/README.md)
  topic `integration/kafka-patterns`

The companion simulates partitions, consumer-group assignment, duplicate
delivery, and DLT flow without forcing local Kafka setup before the mental
model is ready.
