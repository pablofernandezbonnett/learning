# Live Coding Companion (Java and Kotlin)

> Role: `Companion support for the algorithm refresh notes`

This folder is not a new study path.
It is the runnable companion for the highest-return live-coding exercises that
support:

- [01-core-patterns.md](../../topics/algorithms/01-core-patterns.md)
- [02-coding-round-drills.md](../../topics/algorithms/02-coding-round-drills.md)

Use it after reading the notes, not instead of them.

---

## Why This Exists

The docs are good for fast refresh.
This folder exists so you can also run the exact problem shapes that matter most
for practical coding practice:

- time-boxed problem solving
- `heap + stream` questions where you need to keep one clear rule true while data keeps arriving
- `calculator / parser` exercises where edge cases matter more than speed
- `data-structure design` questions that need tradeoff discussion
- `mini machine-coding` exercises that feel closer to backend work than puzzles

The same curated set is implemented in:

- plain `Kotlin`
- plain `Java`

No framework is required.

---

## How To Run

List the available exercises:

```bash
./run-kotlin.sh list
./run-java.sh list
```

Run one exercise:

```bash
./run-kotlin.sh median-stream
./run-java.sh lru-cache
```

Run the full curated set:

```bash
./run-kotlin.sh all
./run-java.sh all
```

---

## Exercise Set

### Focused working set (`highest return now`)

1. `longest-substring` -> sliding window on strings without turning practice into puzzle hunting
2. `top-k-frequent` -> hash map plus heap for counting and ordering
3. `number-of-islands` -> grid traversal with DFS/BFS shape
4. `calculator-ii` -> stack discipline and operator-precedence follow-up
5. `lru-cache` -> map plus doubly linked list
6. `rate-limiter` -> sliding-window queue and system tradeoffs
7. `wallet-ledger` -> idempotent state changes by request ID
8. `cache-aside` -> TTL, invalidation, and hot-read behavior
9. `event-dedup` -> hash-set dedup for at-least-once delivery
10. `retry-backoff` -> transient vs permanent failure handling
11. `pagination-merge` -> two pointers on sorted pages plus duplicate filtering
12. `producer-consumer` -> blocking queue, FIFO processing, and natural slowdown when consumers cannot keep up

### Supporting hedge (`keep available, not primary`)

13. `ascii-pyramid` -> nested loops, spacing arithmetic, and centered rendering
14. `container-queries` -> `HashSet` vs frequency-map judgment for `ADD` / `EXISTS` / `REMOVE`
15. `rotated-search` -> keeping the "one half is still sorted" rule straight under pressure
16. `median-stream` -> two heaps and the balancing rule that keeps median lookup cheap
17. `basic-calculator` -> parentheses, sign handling, state stack
18. `coin-change` -> bottom-up dynamic programming
19. `payment-priority` -> prioritized payment-source allocation

### Concept follow-ups kept in the docs

These are worth rehearsing aloud, but they do not need more runnable examples:

- `linked list vs array`
- `how HashMap works`
- time and space complexity talk tracks

---

## Recommended Mock Rounds

### Mock A (`time-boxed baseline`)

- `longest-substring`
- `top-k-frequent`
- `number-of-islands`
- `calculator-ii`

### Mock B (`live-coding core`)

- `lru-cache`
- `rate-limiter`
- `wallet-ledger`
- `cache-aside`

### Mock C (`backend practical`)

- `event-dedup`
- `retry-backoff`
- `pagination-merge`
- `producer-consumer`

---

## Rule

For each exercise:

1. solve it yourself first
2. say the invariant or state model aloud, meaning the rule your current data structure must keep true
3. explain the time and space tradeoff
4. only then compare with the runnable version
