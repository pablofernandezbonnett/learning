# Coding Drills

These are a small set of problems that exercise different [algorithm patterns](./01-core-patterns.md). Read the prompt, solve it yourself, then use the guide and [compact Java snippets](#compact-java-snippets) to check your invariant and edge cases. The [backend extensions](../../labs/algorithms-live-coding/README.md) add retry, cache, and queue boundaries.

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

## Compact Java Snippets

Attempt the prompts first. These snippets show the decisive step for each
invariant, not a runnable lab or every input-validation rule. Standard
`java.util` imports are omitted.

### 1. Two prices

Look for a prior item *before* storing the current one. `putIfAbsent` keeps
the first index for a repeated price.

```java
static int[] twoPrices(int[] prices, int target) {
    Map<Long, Integer> seen = new HashMap<>();
    for (int i = 0; i < prices.length; i++) {
        long needed = (long) target - prices[i];
        if (seen.containsKey(needed)) return new int[] {seen.get(needed), i};
        seen.putIfAbsent((long) prices[i], i);
    }
    return new int[0];
}
```

### 2. Longest span without repeats

`left` only moves forward. A repeated value outside the active span does not
shrink it.

```java
static int longestDistinctSpan(int[] values) {
    Map<Integer, Integer> last = new HashMap<>();
    int left = 0, best = 0;
    for (int right = 0; right < values.length; right++) {
        Integer previous = last.put(values[right], right);
        if (previous != null && previous >= left) left = previous + 1;
        best = Math.max(best, right - left + 1);
    }
    return best;
}
```

### 3. First match in sorted data

Search for the first value **at least** `target`, then check whether it is an
actual match. The half-open interval `[low, high)` keeps the boundary clear.

```java
static int firstMatch(int[] sorted, int target) {
    int low = 0, high = sorted.length;
    while (low < high) {
        int mid = low + (high - low) / 2;
        if (sorted[mid] < target) low = mid + 1;
        else high = mid;
    }
    return low < sorted.length && sorted[low] == target ? low : -1;
}
```

### 4. Nested brackets

The top of the stack is the only opening bracket that may close next. This
version treats any character outside the three bracket pairs as invalid.

```java
static boolean validBrackets(String input) {
    Deque<Character> open = new ArrayDeque<>();
    for (char c : input.toCharArray()) {
        if (c == '(' || c == '[' || c == '{') {
            open.push(c);
            continue;
        }
        char expected = switch (c) {
            case ')' -> '(';
            case ']' -> '[';
            case '}' -> '{';
            default -> '\0';
        };
        if (expected == '\0' || open.isEmpty() || open.pop() != expected)
            return false;
    }
    return open.isEmpty();
}
```

### 5. Top K frequent event types

Count first. The heap keeps the **worst retained** candidate at its root:
lower count loses, then alphabetically later name loses. The final sort gives
descending count and alphabetical ties.

```java
static List<String> topK(List<String> events, int k) {
    if (k <= 0) return List.of();
    Map<String, Integer> count = new HashMap<>();
    for (String event : events) count.merge(event, 1, Integer::sum);
    Comparator<String> worstFirst = Comparator
        .comparingInt((String s) -> count.get(s))
        .thenComparing(Comparator.reverseOrder());
    PriorityQueue<String> heap = new PriorityQueue<>(worstFirst);
    for (String event : count.keySet()) {
        heap.add(event);
        if (heap.size() > k) heap.remove();
    }
    List<String> result = new ArrayList<>(heap);
    result.sort(Comparator.<String>comparingInt(count::get)
        .reversed().thenComparing(Comparator.naturalOrder()));
    return result;
}
```

### 6. Connected areas in a grid

Mark a land cell when it enters the stack so another path cannot enqueue it
again. This assumes a rectangular grid of `'1'` land and `'0'` water.

```java
static int countAreas(char[][] grid) {
    if (grid.length == 0) return 0;
    int rows = grid.length, cols = grid[0].length, areas = 0;
    boolean[][] seen = new boolean[rows][cols];
    int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    for (int row = 0; row < rows; row++) for (int col = 0; col < cols; col++) {
        if (grid[row][col] != '1' || seen[row][col]) continue;
        areas++;
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[] {row, col});
        seen[row][col] = true;
        while (!stack.isEmpty()) {
            int[] cell = stack.pop();
            for (int[] step : steps) {
                int r = cell[0] + step[0], c = cell[1] + step[1];
                if (r < 0 || r >= rows || c < 0 || c >= cols) continue;
                if (grid[r][c] != '1' || seen[r][c]) continue;
                seen[r][c] = true;
                stack.push(new int[] {r, c});
            }
        }
    }
    return areas;
}
```

### 7. LRU cache

Java's access-order `LinkedHashMap` packages the map plus linked-list behavior:
`get` moves an entry to the recent end, and `removeEldestEntry` evicts the
oldest after insertion. Explain those two structures if asked to implement
LRU without this library class.

```java
static final class LruCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    LruCache(int capacity) {
        super(16, 0.75f, true);
        if (capacity < 0) throw new IllegalArgumentException();
        this.capacity = capacity;
    }

    @Override protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
```

### 8. Sliding window rate limiter

The window is `(now - windowNanos, now]`: an event exactly at the old boundary
is removed. The caller supplies nondecreasing monotonic timestamps;
`synchronized` makes this one instance's check-and-append atomic. A
distributed limit needs shared state.

```java
static final class WindowLimiter {
    private final int limit;
    private final long windowNanos;
    private final Map<String, Deque<Long>> times = new HashMap<>();

    WindowLimiter(int limit, long windowNanos) {
        if (limit < 0 || windowNanos <= 0) throw new IllegalArgumentException();
        this.limit = limit;
        this.windowNanos = windowNanos;
    }

    synchronized boolean allow(String client, long now) {
        Deque<Long> recent = times.computeIfAbsent(client, key -> new ArrayDeque<>());
        while (!recent.isEmpty() && recent.peekFirst() <= now - windowNanos)
            recent.removeFirst();
        if (recent.size() >= limit) return false;
        recent.addLast(now);
        return true;
    }
}
```

In a long-lived process, also remove idle clients so the map does not grow
forever. Monotonic timestamps are meaningful only within this process.

### 9. Idempotent wallet update

The same request ID returns its first result; a different amount under that
ID is an error. The critical section protects only this in-memory example.
For crash safety, store the request identity, balance change, and result in
one database transaction.

```java
static final class Wallet {
    record Result(boolean applied, long balance) {}
    record Attempt(long delta, Result result) {}

    private long balance;
    private final Map<String, Attempt> attempts = new HashMap<>();

    synchronized Result apply(String requestId, long delta) {
        Attempt previous = attempts.get(requestId);
        if (previous != null) {
            if (previous.delta() != delta) throw new IllegalArgumentException();
            return previous.result();
        }
        long next = Math.addExact(balance, delta);
        Result result = new Result(next >= 0, next >= 0 ? next : balance);
        if (result.applied()) balance = next;
        attempts.put(requestId, new Attempt(delta, result));
        return result;
    }
}
```

## Where to Go Next

Use the [lab](../../labs/algorithms-live-coding/README.md) for cache aside, event deduplication, retry, page merge, and producer–consumer practice. For SQL query choice, use the [database block](../databases/README.md).
