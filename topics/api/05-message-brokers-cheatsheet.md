# Message Brokers Cheatsheet

> Primary fit: `Shared core`


Use this after reading [02-message-brokers-and-delivery-semantics.md](./02-message-brokers-and-delivery-semantics.md).

This is the short version to retain and reopen quickly.

Quick terms used here:

- `DLQ` = `dead-letter queue`
- `DLT` = `dead-letter topic`

---

## The Core Idea

Do not ask:

- "Which broker is better?"

Ask:

- "Is this a task queue problem or an event stream problem?"

---

## Queue vs Log

### Queue

Examples:

- SQS
- RabbitMQ

Use when:

- one consumer or one worker group handles the task
- the message is processed, acknowledged, and effectively gone
- the problem is background work, not event replay or durable event history

Good fits:

- email jobs
- webhook follow-up work
- image processing

Short explanation:

> a queue is mainly about handing one unit of work to one processing path later

### Distributed Log

Concrete example:

- Kafka

Use when:

- multiple consumers need the same event
- replay matters
- high-throughput event streams matter
- partitioned ordering matters

Good fits:

- order events
- analytics streams
- event-driven microservice integration

Short explanation:

> a distributed log keeps the event around for a retention period, so different
> consumers can read it independently and even replay it later

---

## The 5 Things To Remember

1. **SQS/RabbitMQ for tasks. Kafka for event streams.**

2. **At-least-once is the default practical model.**
   Duplicates are normal, which is why the consumer must tolerate seeing the same event again.

3. **Idempotent consumers matter more than heroic broker claims.**

4. **Partition key decides ordering and parallelism in Kafka.**
   The key is what decides which partition receives the event, so it also decides the local ordering boundary.

5. **`DLQ` / `DLT` (`dead-letter queue` / `dead-letter topic`) is part of the design, not an afterthought.**
   This is where messages go when normal retry should stop and a human or later replay flow must take over.

---

## Fast Choice Rule

- one worker does the job -> queue
- many systems need the event -> Kafka
- replay matters -> Kafka
- simple async task only -> queue

---

## Common Traps

- using Kafka for a simple worker queue when no replay or multi-consumer history is needed
- assuming "exactly once" removes all duplicate concerns at the business layer
- bad partition-key choice, so ordering lands on the wrong boundary
- no `DLQ`, so poison messages just retry forever
- no consumer idempotency, so crash-and-retry turns into duplicate side effects

---

## Interview Answer Shape

Good short answer:

> If the only problem is background work, I prefer a simple queue like SQS or
> RabbitMQ. If the event is a domain fact that multiple systems consume and may
> need to replay later, Kafka is the better fit. In both cases I assume
> at-least-once delivery and make consumers idempotent.
