# Coding Drills

These are a small set of problems that exercise different [algorithm patterns](./01-core-patterns.md). Read the prompt, solve it yourself, then use the guide to check your invariant and edge cases. The [backend extensions](../../labs/algorithms-live-coding/README.md) add retry, cache, and queue boundaries.

## Practice Loop

1. State the input, output, and assumptions.
2. Trace one normal case and one awkward case before coding.
3. Name the invariant and choose a data structure.
4. Implement the solution, then compare it with the guide.
5. Check time and space cost; test empty, repeated, first, last, and impossible cases where relevant.

## 1. Two Prices That Match a Budget

**Prompt:** Given prices and a target budget, return the indices of two different items whose prices sum to the target. Return no result if none exists.

**Guide:** Scan once. For each price, look for `target - price` in a map of previously seen prices to indices. Check before storing the current item so one item cannot pair with itself. Keep the first index if duplicate prices are allowed and the task expects the earliest pair.

**Check:** Empty input, one item, two equal prices at different indices, and no pair. Expected cost: `O(n)` time and space on average.

## 2. Longest Span Without Repeated Values

**Prompt:** Return the length of the longest contiguous span with no repeated value.

**Guide:** Keep a left boundary and the last index of each value. When a value repeats inside the current span, move `left` just past its previous index. Never move `left` backward. After each step, the active span contains unique values.

**Check:** Empty input, all equal, no repeats, and a repeat outside the current span. Expected cost: `O(n)` time and up to `O(k)` space for distinct values.

## 3. First Match in Sorted Data

**Prompt:** Return the first index of a target in a sorted list, or no result.

**Guide:** Binary search while preserving the possibility of an earlier match. When the middle value equals the target, record it and continue left. When it is smaller, discard the left half; when larger, discard the right half.

**Check:** Empty list, first or last item, duplicates, and missing target. Expected cost: `O(log n)` time and `O(1)` extra space.

## 4. Validate Nested Brackets

**Prompt:** Decide whether `()`, `[]`, and `{}` are correctly nested.

**Guide:** Push opening brackets. For each closing bracket, require a matching item at the top and pop it. A valid result ends with an empty stack.

**Check:** Empty input, wrong closing order, an unmatched closing bracket, and leftover openings. Expected cost: `O(n)` time and up to `O(n)` space.

## 5. Top K Frequent Event Types

**Prompt:** Return the `k` most frequent event types; define a stable tie rule before implementing.

**Guide:** Count with a map, then maintain a min-heap of at most `k` candidates. Evict the least preferred candidate when a better one arrives. The heap root must always be the next candidate to discard. Sort the final `k` results if output order matters.

**Check:** `k = 0`, `k` larger than the number of distinct types, and equal counts. Expected cost: `O(n + u log k)` time and `O(u + k)` space for `u` distinct types.

## 6. Count Connected Areas in a Grid

**Prompt:** In a grid of land and water cells, count groups of land connected vertically or horizontally.

**Guide:** Scan cells. At each unvisited land cell, increment the count and use DFS or BFS to mark every reachable land cell. The invariant is that each land cell is marked once.

**Check:** Empty grid, all water, all land, a single cell, and diagonal contact. Expected cost: `O(rows × columns)` time and up to that much visited or traversal space.

## 7. LRU Cache

**Prompt:** Design a fixed-capacity cache that reads and writes a key in constant time and evicts the least recently used key.

**Guide:** Use a map from key to node and a doubly linked list ordered by recent use. A successful read or write moves its node to the recent end. At capacity, remove the oldest node from both structures. The map and list must describe exactly the same keys.

**Check:** Capacity zero, repeated write, read of a missing key, and eviction after a read. Expected cost: `O(1)` per operation, `O(capacity)` space.

## 8. Sliding Window Rate Limiter

**Prompt:** Allow at most `limit` requests per client in the previous `window` duration.

**Guide:** For each client, keep timestamps in order. Remove timestamps outside the active window, then accept and append only if fewer than `limit` remain. Define whether the boundary timestamp counts and how clock movement is handled.

**Check:** Zero limit, exact boundary time, idle client, and many clients. This is an in-memory exercise; a multi-instance limit needs shared state and an atomic update.

## 9. Idempotent Wallet Update

**Prompt:** Apply a credit or debit once per `requestId`. Repeated requests return the original result; a debit must not make the balance negative.

**Guide:** Look up the request ID first. If already applied, return the stored result. Otherwise validate the new balance, apply the change, and record the result as one atomic operation. The invariant is that each request ID changes the balance at most once.

**Check:** Duplicate credit and debit, insufficient funds, simultaneous requests, and a crash between the balance change and result recording. An in-memory map illustrates the rule; durable correctness needs a transaction and uniqueness constraint. See [idempotency and transaction safety](../databases/01-idempotency-and-transaction-safety.md).

## Where to Go Next

Use the [lab](../../labs/algorithms-live-coding/README.md) for cache aside, event deduplication, retry, page merge, and producer–consumer practice. For SQL query choice, use the [database block](../databases/README.md).
