# Message Brokers and Delivery Semantics

> Primary fit: `Shared core / Payments / Fintech`

Message brokers matter when synchronous HTTP is no longer enough.
They let you decouple services, smooth traffic spikes, and process work asynchronously.

This note keeps the topic practical:

- what problem brokers solve
- the smallest mental model
- when to choose queue vs log
- what to say about delivery, partitions, and reliability

Quick review version:

- [05-message-brokers-cheatsheet.md](./05-message-brokers-cheatsheet.md)

Kafka-specific follow-up:

- [06-kafka-practical-foundations.md](./06-kafka-practical-foundations.md)

Boundary-choice follow-up:

- [07-sync-vs-async-integration-choice.md](./07-sync-vs-async-integration-choice.md)

---

## 1. What Problem A Broker Actually Solves

The core problems are usually one or more of these:

- the caller should not wait for slow follow-up work in other systems
- multiple systems care about the same event
- traffic spikes need buffering
- retries and failures need clearer boundaries

Small concrete example:

- user places an order
- you want to send email, update analytics, notify warehouse, and maybe trigger fraud checks

If you do all of that synchronously inside one HTTP request, the user request becomes too dependent on many later steps.
A broker lets you separate the critical write from the follow-up work.

Smallest code example:

```kotlin
fun placeOrder(order: Order) {
    orderRepository.save(order)
    emailService.sendConfirmation(order)
    analyticsService.trackOrder(order)
}
```

Better production direction:

```kotlin
fun placeOrder(order: Order) {
    orderRepository.save(order)
    broker.publish(OrderPlaced(order.id))
}
```

The first shape makes the user request wait on follow-up work.
The second shape keeps the critical write small and lets later consumers run
independently.

---

## 2. The Smallest Useful Mental Model

There are two main shapes to understand.

### 2.1 Queue

A queue is best when a message is mainly a work item for one processing path.

Mental model:

- producer sends job
- one worker consumes it
- message is acknowledged and removed

Typical examples:

- send email
- generate PDF
- run background import

Pros:

- simple mental model
- good for one processing path
- easy way to move slow work out of the request path

Tradeoffs / Cons:

- weaker fit when many consumers need the same event
- replay and retained history are usually not the point

### 2.2 Distributed log or event stream

A log is best when the message is a domain event that multiple consumers care about.

Mental model:

- producer appends event to a topic
- multiple consumers read it independently
- messages stay for a retention period
- new consumers can replay old events

Typical examples:

- order placed
- payment confirmed
- stock updated

Pros:

- many consumers can react independently
- replay is possible
- strong fit for durable domain events

Tradeoffs / Cons:

- more infrastructure and harder day-to-day operation
- ordering is only within a partition, not across the whole topic

Short decision rule:

- one background workflow -> queue
- shared domain event, replay, many consumers -> log

---

## 3. Queue vs Kafka-Style Log

### 3.1 RabbitMQ and SQS

These are strong default answers when the need is a task queue.

Why they fit:

- simple mental model
- easy worker decoupling
- good for background jobs
- no need to think in terms of retained event history

Tradeoffs / Cons:

- not the strongest fit when several teams need independent replayable event history
- you usually think in jobs, not durable domain streams

Short line:

> If the only requirement is asynchronous job processing, SQS or RabbitMQ is usually the
> simpler fit. That is a queueing problem, not necessarily an event-log problem.

### 3.2 Kafka

Kafka is the right answer when the event itself has durable value across systems.

Why it fits:

- many consumers can process the same event
- consumers can move at different speeds
- historical replay is possible
- high throughput is a core strength

Tradeoffs / Cons:

- higher conceptual and operational overhead
- partitioning and consumer-group behaviour need to be understood, not guessed

Short line:

> Kafka makes sense when the event itself has durable value and multiple consumers need
> independent processing or replay.

---

## 4. Kafka Basics You Must Explain Clearly

### Topics and partitions

A topic is a named stream of events.
Each topic is split into partitions.
Partitions are the unit of parallelism and ordering.

Important rule:

- ordering is guaranteed only within a partition

That is why the partition key matters.

Examples of good keys:

- `orderId`
- `customerId`
- `sku`

Use the key that matches the ordering boundary you care about.

### Consumer groups

A consumer group shares the work of reading a topic.

Rules worth remembering:

- each partition is assigned to one consumer inside a group
- more consumers than partitions means idle consumers
- if a consumer crashes, partitions rebalance to the remaining consumers

### Offsets

The offset is the consumer's position in a partition.

Short explanation:

- commit before processing -> risk data loss
- commit after processing -> risk duplicates

That leads directly to delivery semantics.

---

## 5. Delivery Semantics

| Guarantee | Commit timing | Main risk |
|---|---|---|
| At-most-once | before processing | data loss |
| At-least-once | after processing | duplicates |
| Exactly-once | coordinated transactions and idempotence | high complexity |

The most practical default is:

> at-least-once delivery with idempotent consumers

Why:

- it is simpler than end-to-end exactly-once
- it is good enough for most backend systems

Smallest code example:

```kotlin
class ShipmentConsumer {
    private val processedEventIds = mutableSetOf<String>()

    fun handle(eventId: String, orderId: String) {
        val firstTime = processedEventIds.add(eventId)
        if (!firstTime) return

        shipmentService.createShipment(orderId)
    }
}
```

That snippet is deliberately small, but it shows the core idea clearly:

- at-least-once means the same event may arrive again
- the consumer needs a stable event identity
- duplicate delivery must not create a second business effect

Production translation:

- keep the deduplication state durable, not in memory
- combine it with valid state-transition checks
- assume retries are normal, not exceptional
- it matches how real systems behave under retry and crash conditions

If the consumer crashes after doing the work but before committing the offset, the same
message can be processed again after restart. That is normal. Your consumer should tolerate it.

Pros:

- practical and reliable default
- simpler than chasing end-to-end exactly-once

Tradeoffs / Cons:

- duplicates are expected
- consumers must be designed to tolerate retry and reprocessing

---

## 6. Idempotent Consumers: The Other Half

If you choose at-least-once delivery, the consumer must tolerate duplicates.

Smallest durable shape:

```sql
CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

```kotlin
@Transactional
fun handleOrderPlaced(event: OrderPlacedEvent) {
    if (processedEventRepository.existsById(event.id)) { // duplicate delivery: business work already done
        return
    }

    inventoryService.reserve(event.orderId, event.items) // business side effect
    processedEventRepository.save(ProcessedEvent(event.id)) // remember that this event ID was processed
}
```

Why this matters:

- the same message may arrive twice
- the business side effect should still happen once

Short rule:

> broker-level reliability does not remove consumer-side duplicate risk

---

## 7. Retries And Dead Letter Queues

Retries are part of normal broker operation.
What matters is what you do when retries keep failing.

Practical rule:

- retry transient failures a limited number of times
- move poison messages to a dead letter queue or topic
- alert on the DLQ instead of blocking the main processing path forever

Why this matters:

- one permanently bad payload should not stall a hot partition forever
- operations need a place to inspect and replay failed messages safely

Pros:

- keeps the main flow moving
- gives operations a safe inspection and replay boundary

Tradeoffs / Cons:

- adds failure-handling workflow and backlog management
- bad DLQ hygiene can hide real production issues

Short line:

> I would use bounded retries for transient failures and move permanently failing messages
> to a `DLQ` or `DLT` (`dead-letter queue` or `dead-letter topic`) so the normal consumer path does not stay blocked.

---

## 8. Producer Reliability

Producer-side reliability matters just as much as consumer logic.

If the producer gets an acknowledgement from only the leader replica and that leader crashes
before replication, you can lose the event.

For important event streams, the minimum clean answer is:

- `acks=all`
- idempotent producer enabled

```kotlin
import org.apache.kafka.clients.producer.ProducerConfig

val config = mapOf(
    ProducerConfig.ACKS_CONFIG to "all", // leader waits for all in-sync replicas to acknowledge
    ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true, // broker can deduplicate producer retries more safely
    ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to 5, // keep retry ordering compatible with idempotent producer mode
)
```

Short line:

> For critical order or payment events, `acks=all` and producer idempotence are the safe
> default because they reduce leader-failure loss and unnecessary duplicate sends.

Important nuance:

- producer idempotence helps on the broker boundary
- it does not replace consumer-side idempotency

---

## 9. Outbox: The Real Write-Boundary Answer

The most common integration bug is:

1. write business state to the database
2. publish event directly
3. one succeeds, the other fails

That is the dual-write problem.

Safer pattern:

1. save business state
2. save outbox record in the same local transaction
3. separate relay publishes from the outbox

```kotlin
@Transactional
fun placeOrder(order: Order) {
    orderRepository.save(order) // durable business state change first
    outboxRepository.save(
        OutboxEvent(
            aggregateId = order.id,
            eventType = "OrderPlaced",
            payload = serialize(order), // save publish intent, not just the order row
        )
    )
}
```

Why this is such a strong answer:

- local transaction stays local
- event publication becomes retryable
- service restart does not lose the intent to publish

---

## 10. Queue, Log, And Failure: Quick Decision Rules

- background job with one consumer path -> SQS or RabbitMQ
- shared domain event with multiple consumers -> Kafka
- retry by default -> design for at-least-once
- critical event publication -> outbox
- duplicate processing risk -> idempotent consumers

---

## 11. Choice By Use Case

### One background job, one main consumer

- queue: yes
- Kafka-style log: usually no
- outbox: maybe, only if the job must stay consistent with a DB write
  Why: this is mainly a work-distribution problem.

### Order or payment event that several systems care about

- queue: maybe
- Kafka-style log: yes
- idempotent consumers: yes
  Why: many consumers need the same event and replay may matter later.

### Critical local write followed by later async work

- broker: maybe yes
- outbox: yes
- direct publish after DB write: no
  Why: the main problem is avoiding the dual-write failure.

### One application only, no real async need

- broker: usually no
- local event or direct method call: maybe enough
  Why: do not add broker infrastructure just to sound modern.

---

## 12. The Big Traps

1. **Using Kafka for a simple one-consumer background job**
   Example: one email worker with no replay need gets turned into a whole event-stream platform.

2. **Assuming "exactly-once" removes business duplicates automatically**
   Example: payment event is still applied twice because the consumer side effect is not idempotent.

3. **No partition-key reasoning**
   Example: events for the same `orderId` land in different partitions and processing order becomes confusing.

4. **No DLQ or failure-handling plan**
   Example: one poison message blocks normal processing for too long.

5. **Publishing directly after the DB write for a critical event**
   Example: order row commits, publish fails, later systems never hear about the order.

---

## 13. 20-Second Answer

> I would introduce a broker when I need asynchronous boundaries, buffering, or independent
> consumers. If the problem is just background work, a queue like SQS or RabbitMQ is often
> enough. If the event has durable value across multiple systems and may need replay, Kafka
> becomes a stronger fit. The practical delivery model is usually at-least-once with
> idempotent consumers, and for critical database-plus-event flows the outbox pattern is the
> clean answer.

---

## 14. 1-Minute Answer

> I first separate queueing from event streaming. If I only need to offload one background
> workflow, SQS or RabbitMQ is usually the simplest correct choice. If the event is a domain
> fact that multiple consumers care about and replay matters, Kafka is often the stronger
> fit. Then I explain partitions, consumer groups, and offsets clearly: ordering is only per
> partition, the key defines the ordering boundary, and consumer groups control parallelism.
> For reliability, at-least-once delivery plus idempotent consumers is usually the practical
> answer, because duplicates are easier to tolerate than silent data loss. For critical
> publication, the clean way to avoid dual writes is to combine the business write and an
> outbox record in one local transaction, then publish asynchronously from the outbox.

---

## 15. What To Internalize

- a queue solves work distribution; a log solves event distribution and replay
- Kafka ordering is per partition, not global
- consumer groups define parallelism
- at-least-once plus idempotent consumers is the practical default
- DLQ keeps poison messages from blocking the main flow forever
- producer idempotence helps, but it is not the whole reliability story
- outbox is the clean answer when DB write and event publication must stay consistent
