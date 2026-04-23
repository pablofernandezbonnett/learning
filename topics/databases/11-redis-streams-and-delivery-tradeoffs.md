# Redis Streams, Pub/Sub, and Delivery Tradeoffs

> Primary fit: `Shared core`


If you already know Redis as cache, lock store, and rate limiter, the next useful
step is understanding its messaging models.

The practical question is not:

> Can Redis move messages?

The real question is:

> What delivery guarantees do I need, and what tradeoffs am I accepting?

---

## 1. Redis Pub/Sub

Pub/Sub is the simplest Redis messaging model.

How it works:

- a publisher sends a message to a channel
- current subscribers receive it
- disconnected subscribers miss it

This makes Pub/Sub:

- simple
- low-latency
- good for fanout
- bad when delivery or replay matters

Good use cases:

- cache invalidation
- live dashboards
- transient notifications
- SSE/WebSocket fanout helpers

Bad use cases:

- order processing
- payment events
- workflows that require retries
- anything where a consumer must not silently miss a message

Practical rule:

> Use Pub/Sub for ephemeral notifications, not for durable workflows.

---

## 2. Redis Streams

Streams are Redis's append-only message log structure.

The important step up from Pub/Sub is:

- messages stay in the stream until trimmed or deleted
- consumers can read from a position
- consumer groups let multiple workers share a stream
- acknowledgments let you track processing progress

That means Streams can support:

- replay
- consumer groups
- pending message inspection
- retry and claim flows

Core ideas:

- `XADD`: append a message
- `XREAD`: read from a position
- `XGROUP`: create/manage consumer groups
- `XREADGROUP`: read as part of a consumer group
- `XACK`: acknowledge successful processing

Practical rule:

> Redis Streams are much closer to a lightweight event log than Pub/Sub is.

---

## 3. Pub/Sub vs Streams

Pub/Sub:

- fire-and-forget
- no persistence
- no replay
- no consumer groups
- low coordination overhead

Streams:

- persisted in Redis memory/disk according to Redis durability settings
- replayable
- consumer-group aware
- supports pending and retry handling
- more operationally complex

If the consumer being offline must not lose the event, Streams is the first Redis
feature to look at, not Pub/Sub.

---

## 4. Retries and Pending Messages

Streams help with retries, but they do not magically solve correctness.

With consumer groups:

- a consumer reads a message
- the message becomes pending for that consumer
- on success, the consumer sends `XACK`
- if the consumer crashes, another consumer can later claim the pending message

This is useful, but it implies:

- duplicates are still possible
- consumers must be idempotent
- retry strategy still belongs to your application design

Good rule:

> Treat Streams as at-least-once delivery unless you have a very specific reason
> to claim otherwise.

That means:

- process idempotently
- keep side effects safe to retry
- design for duplicate delivery

---

## 5. Durability Tradeoffs

Streams are more durable than Pub/Sub, but not equal to "guaranteed forever."

The durability you get depends on Redis itself:

- memory-backed primary storage
- RDB/AOF persistence settings
- replication configuration
- trimming policy

Important implications:

- if Redis is configured loosely, you can still lose recent messages
- if you trim too aggressively, replay windows disappear
- if a stream is treated like the source of truth, you may be building on the wrong foundation

Practical rule:

> Redis Streams can be durable enough for many internal backend flows, but they
> are still a tradeoff between simplicity and stronger event-platform guarantees.

---

## 6. Redis Streams vs Kafka vs NATS

Redis Streams:

- simpler to adopt if Redis already exists
- good for small-to-medium internal async workflows
- lower operational weight than Kafka
- weaker as a long-retention event platform

Kafka:

- stronger for durable event logs
- stronger for long retention and replay
- stronger ecosystem for large-scale event-driven systems
- heavier to operate and explain

NATS:

- great for lightweight messaging
- strong fit when you want simple request-reply or event distribution
- operationally lighter than Kafka
- different tradeoffs depending on whether you use core NATS or JetStream

For your current profile, a good learning order is:

1. understand Pub/Sub
2. understand Streams and consumer groups
3. compare that with NATS or Kafka later

---

## 7. When Redis Streams Make Sense

Good candidates:

- async catalog import processing
- webhook follow-up work
- internal notifications with retry needs
- moderate event-driven background jobs

Less good candidates:

- high-value financial source-of-truth event systems
- long-term replay-heavy analytics pipelines
- architectures that already clearly need Kafka-class guarantees

---

## 8. Interview Lines

- "Redis Pub/Sub is fine for ephemeral notifications, but not for workflows where missed delivery matters."
- "Redis Streams add consumer groups, acknowledgments, and replay, so they are much better for lightweight event-driven processing."
- "Even with Streams, I still assume at-least-once delivery and design consumers to be idempotent."
- "The main tradeoff is simplicity versus durability and replay strength compared with Kafka-class systems."

---

## 9. Further Reading

- Redis Pub/Sub:
  https://redis.io/docs/latest/develop/pubsub/
- Redis Streams overview:
  https://redis.io/docs/latest/develop/data-types/streams/
- `XREADGROUP` command:
  https://redis.io/docs/latest/commands/xreadgroup/
