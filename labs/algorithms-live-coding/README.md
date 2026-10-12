# Algorithm and Backend Coding Drills

The [coding drills](../../topics/algorithms/02-coding-round-drills.md) own the problem statements, examples, and general pattern hints. Solve them in any language. This page adds a small backend practice set; it does not repeat the full algorithm catalog.

## Practice loop

1. Choose a problem and restate its input, output, and edge cases.
2. Name the rule your data structure must keep true.
3. Implement it yourself, then test empty input, repeats, boundaries, and invalid input where relevant.
4. Explain time and space cost and how the design changes under concurrency or persistence.

## Selected coding problems

Use the [nine coding drills](../../topics/algorithms/02-coding-round-drills.md) for maps, windows, search, stacks, heaps, graphs, caches, limits, and idempotency. Read the [core patterns](../../topics/algorithms/01-core-patterns.md) when you need the general rule behind a solution.

## Backend extensions

| Task | What to implement | Guide to the invariant | Check |
| --- | --- | --- | --- |
| Cache aside | Serve reads through a TTL cache and update the source of truth. | Read cache → on miss read store → cache only a valid value. After a write, invalidate the affected key. | Expiry, stale reads, simultaneous misses. |
| Event deduplication | Consume events that may be delivered again. | Atomically record event ID with its local effect; repeated IDs do no new work. | Crash before commit, crash after commit, reordered events. |
| Retry with backoff | Retry a transient remote failure inside one deadline. | Classify error → if retryable and budget remains, wait with capped backoff and jitter → retry with the same idempotency key. | Permanent error, exhausted budget, duplicate remote effect. |
| Page merge | Merge two sorted pages into one ordered page. | Compare current heads, emit the next item, and track stable identity to avoid duplicates. | Empty page, equal sort keys, repeated item across pages. |
| Producer–consumer | Bound work accepted faster than it is processed. | Producer waits or rejects when queue is full; consumer acknowledges only after processing. | Slow consumer, shutdown, failed work. |

These guides describe the rule to preserve, not a ready-made implementation.
