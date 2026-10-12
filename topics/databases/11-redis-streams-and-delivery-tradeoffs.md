# Redis Pub/Sub and Streams

Redis offers two ways to send messages. Choose from the consumer's need to
recover missed work, then check how long the message must remain available and
what failure the system can tolerate.

| Question | Pub/Sub | Streams |
|---|---|---|
| Is a message kept for an offline consumer? | No | Yes, until trimmed or deleted, subject to Redis persistence and failover |
| Can a consumer replay? | No | Yes, from an available stream entry |
| Can workers share processing in a group? | No | Yes |
| Is processing acknowledged? | No | Yes, within a consumer group |

## Pub/Sub: Ephemeral Fan-out

A publisher sends to a channel, and connected subscribers receive the message.
Disconnected subscribers miss it. Use Pub/Sub for live UI updates or hints to
invalidate a cache when missing one message is acceptable or another mechanism
restores the correct state.

Do not make a business workflow depend on every subscriber seeing a Pub/Sub
message. There is no replay or acknowledgment.

## Streams: Retained Entries and Consumer Groups

A stream keeps entries until they are trimmed or deleted. A consumer can read
from a position. In a consumer group, Redis tracks delivered entries until they
are acknowledged.

| Command | Role |
|---|---|
| `XADD` | Append an entry |
| `XREAD` | Read entries from a position |
| `XGROUP` | Create or manage a consumer group |
| `XREADGROUP` | Deliver entries to group consumers |
| `XACK` | Mark a processed entry as acknowledged for the group |
| `XPENDING`, `XAUTOCLAIM` | Inspect or reclaim pending work after a worker stops |

A typical worker reads, applies its effect, and acknowledges only after that
effect succeeds. If it crashes before `XACK`, another worker may later reclaim
the entry. The effect can run twice, so use a stable event ID and an idempotent
write or deduplication at the business-data owner. Acknowledgment does not make
an external database update and the stream update one transaction.

## Size the Recovery Window

- Set a retention or trimming rule from the longest realistic consumer outage
  and replay need. Trimming removes the ability to recover older entries.
- Configure Redis persistence and failover for the loss window you can accept.
  A retained entry is still subject to the durability of the Redis deployment.
- Monitor stream length, pending entries, consumer lag, and reclaim attempts.
  Decide what happens to messages that repeatedly fail.
- If the event originates from a database update, close the publication gap
  with an outbox or another reliable handoff. See
  [Message brokers and delivery semantics](../api/02-message-brokers-and-delivery-semantics.md).

For example, an internal catalog import can use a stream if workers need to
resume after restarting and its Redis recovery window fits the job. For a
long-lived event history or stronger delivery requirements, compare a dedicated
broker using the same retention, replay, throughput, and failure questions in
the [broker guide](../api/02-message-brokers-and-delivery-semantics.md).

## Further Reading

- [Redis Pub/Sub](https://redis.io/docs/latest/develop/use-cases/pub-sub/)
- [Redis Streams](https://redis.io/docs/latest/develop/data-types/streams/)
- [Consumer group reads](https://redis.io/docs/latest/commands/xreadgroup/)
- [Reclaiming pending entries](https://redis.io/docs/latest/commands/xautoclaim/)
