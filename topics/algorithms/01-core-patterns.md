# Core DSA Patterns for Practical Engineering

> Primary fit: `Shared core`


You do not need to become puzzle-first to get value from these patterns.
What you do need is a small set of DSA patterns that help you reason clearly about
lookups, windows, merging, and simple traversal under pressure.

Here are the six most practical patterns that apply to real-world backend scenarios, rather than pure LeetCode puzzles.

Scope of this note:

- this is a compact core, not a company-specific OA guide
- the patterns here are the ones that recur most often across generic timed coding rounds
- the examples are written with backend-flavored data like orders, logs, traffic, and services, not because those exact business cases will be asked verbatim

---

## What To Rehearse Now

If the current goal is timed coding or a refresh under time pressure, do not try
to "cover DSA" broadly.
Use this pattern guide to recognize the shape, then rehearse a small curated set
of exercises repeatedly.

Use the runnable companion here:

- [live-coding-companion/README.md](./live-coding-companion/README.md)

Note: this path prioritizes conceptual clarity over implementing low-level structures. For `HashMap` internals (buckets, collisions, load factor, resize behaviour), prefer a concise spoken explanation and the H4 exposition in `02-coding-round-drills.md` rather than coding a full reimplementation under time pressure.

### Highest-return exercise map

- `Hash map / strings / arrays`: `H1/H2/H3` in [02-coding-round-drills.md](./02-coding-round-drills.md) plus `longest-substring`, `top-k-frequent`, and `event-dedup` in the companion
- `Grid BFS / DFS`: `G4` in [02-coding-round-drills.md](./02-coding-round-drills.md) plus `number-of-islands` in the companion
- `Stack / queue basics`: `calculator-ii` and `producer-consumer` in the companion
- `Data-structure design`: `lru-cache`, `rate-limiter`, and `cache-aside` in the companion
- `Backend correctness and retry flow`: `wallet-ledger`, `retry-backoff`, and `pagination-merge` in the companion
- `Concept follow-ups`: linked-list vs array and `HashMap` collision handling stay in the notes because they are better as spoken follow-up answers than as more runnable code

### Smallest useful set for the current loop

If time is tight, bias toward these `12` and stop there:

1. `longest-substring`
2. `top-k-frequent`
3. `number-of-islands`
4. `calculator-ii`
5. `lru-cache`
6. `rate-limiter`
7. `wallet-ledger`
8. `cache-aside`
9. `event-dedup`
10. `retry-backoff`
11. `pagination-merge`
12. `producer-consumer`

### Why this set is enough

- it covers the most common OA shapes without turning prep into a puzzle collection
- it forces you to explain invariants, edge cases, and tradeoffs out loud
- it keeps the runnable set biased toward backend-flavored code instead of broad DSA coverage
- it is small enough to repeat under a timer, which matters more than adding exotic topics

---

## 1. Hash Maps (The O(1) Lookup)

**The Pattern:** Storing key-value pairs for instantaneous retrieval, rather than iterating through lists (O(n)).

**Pseudo shape:**
```text
create empty set or map
for each item:
    if item already seen:
        return answer
    store item
return not found
```

**Practical Application:**
*   Caching (Redis).
*   Finding duplicates in a large dataset (e.g., checking if an incoming order ID has already been seen).
*   Joining offline datasets quickly.

<details>
<summary>Java version</summary>

**Java Example (Finding the first duplicate ID):**
```java
// Instead of a nested loop O(N^2)
public String findFirstDuplicate(List<String> orderIds) {
    Set<String> seenIds = new HashSet<>(); // HashSet = hash-based set for O(1) average lookup
    for (String id : orderIds) {
        if (!seenIds.add(id)) { // add() returns false if item already exists: O(1) operation
            return id; 
        }
    }
    return null;
}
```

</details>

**Kotlin Example (Finding the first duplicate ID):**
```kotlin
fun findFirstDuplicate(orderIds: List<String>): String? {
    val seenIds = mutableSetOf<String>() // Kotlin mutable set for seen IDs
    for (id in orderIds) {
        if (!seenIds.add(id)) {          // add() returns false if the ID already exists
            return id
        }
    }
    return null
}
```

---

## 2. Sliding Window

**The Pattern:** Maintaining a "window" of elements over a sequential dataset (like an array or stream) and moving that window forward.

**Pseudo shape:**
```text
build first window
record current answer
move right edge one step at a time:
    add new element
    remove old element
    update answer
```

**Practical Application:**
*   **Rate Limiting:** Counting requests within a rolling 60-second window.
*   Log analysis: Finding the peak error rate over any 5-minute interval.

**Kotlin Example (Max sum of a contiguous block for rate-limiting simulation):**
```kotlin
fun findMaxTrafficInWindow(requestsPerMinute: IntArray, windowSize: Int): Int {
    var maxTraffic = 0
    var currentTraffic = 0

    // Initialize the first window
    for (i in 0 until windowSize) {
        currentTraffic += requestsPerMinute[i] // build the first full window sum
    }
    maxTraffic = currentTraffic

    // Slide the window
    for (i in windowSize until requestsPerMinute.size) {
        // Add the new element, subtract the element that fell out of the window
        currentTraffic += requestsPerMinute[i] - requestsPerMinute[i - windowSize]
        maxTraffic = maxOf(maxTraffic, currentTraffic)
    }

    return maxTraffic
}
```

<details>
<summary>Java version</summary>

**Java Example (Max sum of a contiguous block for rate-limiting simulation):**
```java
public int findMaxTrafficInWindow(int[] requestsPerMinute, int windowSize) {
    int maxTraffic = 0;
    int currentTraffic = 0;

    for (int i = 0; i < windowSize; i++) {
        currentTraffic += requestsPerMinute[i]; // build the first full window sum
    }
    maxTraffic = currentTraffic;

    for (int i = windowSize; i < requestsPerMinute.length; i++) {
        // Add new minute, remove minute that left the window.
        currentTraffic += requestsPerMinute[i] - requestsPerMinute[i - windowSize];
        maxTraffic = Math.max(maxTraffic, currentTraffic);
    }

    return maxTraffic;
}
```

</details>

---

## 3. Two Pointers

**The Pattern:** Iterating through a collection with two markers (pointers) simultaneously, usually moving towards each other or moving at different speeds.

**Pseudo shape:**
```text
left = start
right = end
while left < right:
    inspect left and right
    move one or both pointers
```

Two common shapes:

*   **Inward pointers:** one pointer starts at the left, one at the right, and they move toward each other.
*   **Parallel pointers:** one pointer walks each sorted input, and you keep taking the smaller current value.

**Practical Application:**
*   Validating data structures (e.g., checking if a log file sequence is a palindrome).
*   Merging two sorted streams of data (e.g., merging timestamped logs from two different servers).
*   "Slow-Fast Pointer" pattern: Detecting cycle/loops in graph traversals or finding the middle of an unknown-length stream without buffering it all in memory.

<details>
<summary>Java version</summary>

**Java Example (Palindrome check, strict):**
```java
public boolean isStrictPalindrome(String text) {
    int left = 0;                   // start pointer at the first character
    int right = text.length() - 1;  // end pointer at the last character

    while (left < right) {
        if (text.charAt(left) != text.charAt(right)) { // charAt(i) = character stored at index i
            return false;
        }
        left++;   // move left pointer inward
        right--;  // move right pointer inward
    }

    return true; // all mirrored character pairs matched
}
```

</details>

<details>
<summary>Java version</summary>

**Java Example (Palindrome check, letters only):**
```java
public boolean isLetterPalindrome(String text) {
    int left = 0;                   // start pointer at the first character
    int right = text.length() - 1;  // end pointer at the last character

    while (left < right) {
        // Character.isLetter(...) checks whether this char is a letter.
        while (left < right && !Character.isLetter(text.charAt(left))) left++;
        while (left < right && !Character.isLetter(text.charAt(right))) right--;

        // toLowerCase(...) ignores upper/lower-case differences.
        if (Character.toLowerCase(text.charAt(left)) != Character.toLowerCase(text.charAt(right))) {
            return false;
        }
        left++;   // move left pointer inward
        right--;  // move right pointer inward
    }

    return true; // all mirrored letter pairs matched
}
```

</details>

**Kotlin Example (Palindrome check, strict):**
```kotlin
fun isStrictPalindrome(text: String): Boolean {
    var left = 0                     // start pointer at the first character
    var right = text.lastIndex       // lastIndex = index of the final character

    while (left < right) {
        if (text[left] != text[right]) { // strict version compares every character as-is
            return false
        }
        left++                       // move left pointer inward
        right--                      // move right pointer inward
    }

    return true                      // all mirrored character pairs matched
}
```

**Kotlin Example (Palindrome check, letters only):**
```kotlin
fun isLetterPalindrome(text: String): Boolean {
    var left = 0                     // start pointer at the first character
    var right = text.lastIndex       // lastIndex = index of the final character

    while (left < right) {
        while (left < right && !text[left].isLetter()) left++   // skip digits, spaces, or symbols on the left
        // Skip digits, spaces, or symbols on the right.
        while (left < right && !text[right].isLetter()) right--

        // lowercaseChar() compares letters without caring about upper/lower case.
        if (text[left].lowercaseChar() != text[right].lowercaseChar()) {
            return false
        }
        left++                       // move left pointer inward
        right--                      // move right pointer inward
    }

    return true                      // all mirrored letter pairs matched
}
```

**Which version to use:**

- use the strict version when the prompt says every character counts
- use the letters-only version when the prompt says to ignore digits, spaces, punctuation, or case

**Additional Kotlin Example (Merging sorted logs):**
```kotlin
fun mergeSortedLogs(list1: List<Int>, list2: List<Int>): List<Int> {
    val merged = mutableListOf<Int>()
    var i = 0
    var j = 0

    while (i < list1.size && j < list2.size) {
        if (list1[i] <= list2[j]) {
            merged.add(list1[i]) // take smaller current value from list1
            i++
        } else {
            merged.add(list2[j]) // take smaller current value from list2
            j++
        }
    }

    while (i < list1.size) {
        merged.add(list1[i]) // append remaining items from list1
        i++
    }

    while (j < list2.size) {
        merged.add(list2[j]) // append remaining items from list2
        j++
    }

    return merged
}
```

<details>
<summary>Java version</summary>

**Additional Java Example (Merging sorted logs):**
```java
public List<Integer> mergeSortedLogs(List<Integer> list1, List<Integer> list2) {
    List<Integer> merged = new ArrayList<>();
    int i = 0;
    int j = 0;

    while (i < list1.size() && j < list2.size()) {
        if (list1.get(i) <= list2.get(j)) {
            merged.add(list1.get(i)); // take smaller current value from list1
            i++;
        } else {
            merged.add(list2.get(j)); // take smaller current value from list2
            j++;
        }
    }

    while (i < list1.size()) {
        merged.add(list1.get(i)); // append remaining items from list1
        i++;
    }

    while (j < list2.size()) {
        merged.add(list2.get(j)); // append remaining items from list2
        j++;
    }

    return merged;
}
```

</details>

Short memory hook:

*   palindrome check -> pointers move inward
*   merge sorted inputs -> pointers move forward in parallel

---

## 4. Linked List Basics

**The Pattern:** Work with node-by-node structures where pointer updates matter more than random access.

**Why it still matters:** Even if most product code uses arrays, lists, and framework collections, linked-list exercises still appear in coding rounds because they test whether you can reason clearly about references, mutation order, and edge cases. They also still show up indirectly when people ask how hash-table collisions can be handled with separate chaining.

**The common basics worth knowing:**
*   reverse a singly linked list
*   find the middle node with slow/fast pointers
*   detect a cycle with slow/fast pointers

**Pseudo shape for reverse:**
```text
prev = null
current = head
while current != null:
    next = current.next
    current.next = prev
    prev = current
    current = next
return prev
```

<details>
<summary>Java version</summary>

**Java Example (Reverse a singly linked list):**
```java
class ListNode {
    int value;
    ListNode next;

    ListNode(int value) {
        this.value = value;
    }
}

public ListNode reverse(ListNode head) {
    ListNode prev = null;
    ListNode current = head;

    while (current != null) {
        ListNode next = current.next; // save the rest of the list before rewiring
        current.next = prev;          // reverse the pointer
        prev = current;               // move prev forward
        current = next;               // move current forward
    }

    return prev;                      // new head after reversal
}
```

</details>

**Kotlin Example (Find the middle node):**
```kotlin
data class ListNode(
    val value: Int,
    var next: ListNode? = null,
)

fun middleNode(head: ListNode?): ListNode? {
    var slow = head
    var fast = head

    while (fast?.next != null) {
        slow = slow?.next         // move one step
        fast = fast.next?.next    // move two steps
    }

    return slow                   // when fast ends, slow is in the middle
}
```

Short memory hook:

*   if the task says `reverse`, think `prev / current / next`
*   if the task says `middle` or `cycle`, think `slow / fast`

**What Java and Kotlin actually give you today:**

- `Java` has `LinkedList<E>` in `java.util`, and it is a **doubly linked list** that also implements `Deque<E>`.
- `Java` usually prefers `ArrayDeque<E>` for **stack** and **queue** work. It is array-backed and is the normal default in coding rounds.
- `Java Stack<E>` still exists, but it is a legacy class. Default to `Deque<E>` / `ArrayDeque<E>` unless the prompt explicitly asks for `Stack`.
- `Kotlin` has `ArrayDeque<T>` in the standard library, and that is the clean default for **stack** and **queue** work there too.
- `Kotlin` does **not** have a dedicated stdlib linked-list type that you normally use in coding drills. For linked-list problems, define a small `ListNode` yourself. If product code really needs a linked list, Kotlin can interoperate with `java.util.LinkedList<E>`.
- `LinkedList<E>`, `ArrayDeque<E>`, and Kotlin `ArrayDeque<T>` are normal single-threaded structures. Do not call them thread-safe unless the discussion is explicitly about concurrency.
- if the conversation turns concurrent, the JVM collections to recognize are usually `ConcurrentLinkedQueue`, `ConcurrentLinkedDeque`, or `BlockingQueue`, depending on whether you need FIFO, deque-style access, or waiting producer-consumer handoff.

**Minimal library reality check:**

<details>
<summary>Java version</summary>

```java
Deque<String> queue = new ArrayDeque<>();
queue.addLast("checkout");   // enqueue at the back
queue.removeFirst();         // dequeue from the front

Deque<String> stack = new ArrayDeque<>();
stack.push("checkout");      // push onto stack top (same as addFirst)
stack.pop();                 // pop from stack top (same as removeFirst)

LinkedList<String> linked = new LinkedList<>();
linked.add("A");
linked.add("B");
// LinkedList = doubly linked list in Java, but not the usual default for stack/queue drills
// LinkedList is also not thread-safe
```

</details>

```kotlin
val queue = ArrayDeque<String>()
queue.addLast("checkout")    // enqueue at the back
queue.removeFirst()          // dequeue from the front

val stack = ArrayDeque<String>()
stack.addLast("checkout")    // push onto stack top
stack.removeLast()           // pop from stack top

// Kotlin has no dedicated stdlib linked list for coding-drill problems,
// so a small custom ListNode is still the normal shape there.
// Kotlin ArrayDeque is not thread-safe either.
```

---

## 5. Priority Queue / Heap

**The Pattern:** Keep the current smallest or largest element available without sorting the whole input every time.

**Pseudo shape for top `k`:**
```text
count frequencies
create heap
for each entry:
    push entry into heap
    if heap is bigger than k:
        remove the smallest
read remaining heap items into result
```

**Practical Application:**
*   Top `k` frequent items.
*   Always process the next earliest timestamp or smallest cost first.
*   Merge multiple sorted sources while only keeping the next candidate from each source in memory.

**What makes a priority queue different from a normal queue:**

- a normal queue is `FIFO`: first in, first out
- a priority queue removes the element with the highest priority first
- in most coding examples here, "highest priority" really means "smallest value at the top" because we use a min-heap
- if two items have the same priority, you should not assume a stable arrival order unless you add your own tie-breaker

Smallest mental model:

```text
normal queue:
    add A, add B, add C
    remove -> A, then B, then C

priority queue:
    add 5, add 1, add 3
    remove -> 1, then 3, then 5
```

**What Java and Kotlin actually give you today:**

- `Java` has `PriorityQueue<E>` in `java.util`, which is the normal in-memory heap implementation for coding drills
- `Java PriorityQueue<E>` is a min-heap by default when you use natural ordering or a comparator like `Comparator.comparingInt(...)`
- `Java PriorityQueue<E>` is **not** thread-safe
- `Kotlin` on the JVM usually uses `java.util.PriorityQueue<E>` too
- for concurrent priority ordering on the JVM, the class to recognize is `PriorityBlockingQueue<E>`

<details>
<summary>Java version</summary>

**Java Example (Top 2 frequent statuses):**
```java
public List<String> topKFrequent(List<String> statuses, int k) {
    Map<String, Integer> count = new HashMap<>();
    for (String status : statuses) {
        // getOrDefault(...) gives 0 when the key is not in the map yet.
        count.put(status, count.getOrDefault(status, 0) + 1);
    }

    PriorityQueue<Map.Entry<String, Integer>> heap =
        new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
    // Min-heap: the smallest frequency stays at the top.

    for (Map.Entry<String, Integer> entry : count.entrySet()) {
        heap.offer(entry); // push one entry into the heap
        if (heap.size() > k) {
            heap.poll();   // remove the smallest frequency so only top k remain
        }
    }

    List<String> result = new ArrayList<>();
    while (!heap.isEmpty()) {
        result.add(heap.poll().getKey()); // poll() returns and removes the heap top
    }
    Collections.reverse(result);
    return result;
}
```

</details>

**Kotlin Example (Top 2 frequent statuses):**
```kotlin
fun topKFrequent(statuses: List<String>, k: Int): List<String> {
    val count = statuses.groupingBy { it }.eachCount() // groupingBy { it }.eachCount() = frequency map
    val heap = PriorityQueue(compareBy<Map.Entry<String, Int>> { it.value })
    // Min-heap: the smallest frequency stays at the top.

    for (entry in count.entries) {
        heap.add(entry)          // push one entry into the heap
        if (heap.size > k) {
            heap.poll()          // remove the smallest frequency
        }
    }

    val result = mutableListOf<String>()
    while (heap.isNotEmpty()) {
        result.add(heap.poll().key) // poll() returns and removes the heap top
    }
    return result.asReversed() // asReversed() returns the same items in reverse order
}
```

Short memory hook:

*   "top `k`" -> think heap
*   "always pick next smallest or earliest" -> think heap
*   normal queue -> arrival order matters
*   priority queue -> comparator / priority matters more than arrival order

---

## 6. BFS / DFS (Breadth-First & Depth-First Search)

**The Pattern:** Traversing graphs or trees methodically.

**Practical Application:**

- **BFS (Breadth-First):** Shortest path in an unweighted graph, level-by-level traversal.
  Uses a **Queue** (FIFO). Explores all neighbors before going deeper.
- **DFS (Depth-First):** Explore a full path before backtracking. Cycle detection,
  connected components, all-paths. Uses a **Stack** (or recursion).

**When to use which:**

- "Shortest path / minimum hops" → BFS
- "Does a path exist / all reachable nodes" → DFS
- "Detect a cycle" → DFS with a visited set
- "Level-order / by-distance" → BFS

---

### BFS — Kotlin (Shortest Path in a Service Dependency Graph)

```kotlin
// Graph: adjacency list (each service → list of services it calls)
// Find shortest dependency path from `start` to `end` service
fun bfsShortestPath(
    graph: Map<String, List<String>>,
    start: String,
    end: String,
): List<String> {
    val queue = ArrayDeque<List<String>>()   // Kotlin queue; stores full paths, not just nodes
    val visited = mutableSetOf(start)        // Kotlin mutable set for visited nodes

    queue.add(listOf(start))                 // start with one path: [start]

    while (queue.isNotEmpty()) {
        val path = queue.removeFirst()       // remove oldest path from the queue
        val node = path.last()          // last() = last element of the current path list

        if (node == end) return path         // found — this is guaranteed shortest

        // emptyList() = use no neighbors when the key is missing.
        for (neighbor in graph[node] ?: emptyList()) {
            if (neighbor !in visited) {
                visited.add(neighbor)
                queue.add(path + neighbor)   // path + neighbor = new list with one more node
            }
        }
    }
    return emptyList()                       // no path exists
}

// Usage:
val serviceGraph = mapOf(
    "checkout"  to listOf("inventory", "payment"),
    "inventory" to listOf("warehouse"),
    "payment"   to listOf("fraud-check", "bank"),
    "warehouse" to listOf("shipping"),
    "fraud-check" to listOf("bank"),
    "bank"      to emptyList(),
    "shipping"  to emptyList(),
)

println(bfsShortestPath(serviceGraph, "checkout", "bank"))
// → [checkout, payment, bank]  (2 hops — shorter than checkout→payment→fraud-check→bank)
```

<details>
<summary>Java version</summary>

### BFS — Java (Shortest Path in a Service Dependency Graph)

```java
public List<String> bfsShortestPath(
    Map<String, List<String>> graph,
    String start,
    String end
) {
    Deque<List<String>> queue = new ArrayDeque<>();
    Set<String> visited = new HashSet<>();
    visited.add(start);
    queue.add(List.of(start)); // List.of(start) = immutable one-item list: [start]

    while (!queue.isEmpty()) {
        List<String> path = queue.removeFirst();      // pop oldest path from the queue
        String node = path.get(path.size() - 1);      // last element = current node

        if (node.equals(end)) return path;

        // getOrDefault(...) uses empty list when node has no outgoing edges.
        for (String neighbor : graph.getOrDefault(node, List.of())) {
            if (visited.add(neighbor)) {
                List<String> nextPath = new ArrayList<>(path);
                nextPath.add(neighbor);               // extend path by one hop
                queue.add(nextPath);                  // push extended path back into queue
            }
        }
    }

    return List.of(); // empty immutable list = "no path found"
}
```

</details>

---

### DFS — Kotlin (Find All Affected Services + Cycle Detection)

```kotlin
// DFS: find all services reachable from a failing service
fun dfsReachable(
    graph: Map<String, List<String>>,
    start: String,
    visited: MutableSet<String> = mutableSetOf(), // Kotlin mutable set for visited nodes
): Set<String> {
    visited.add(start)
    for (neighbor in graph[start] ?: emptyList()) { // emptyList() = use no neighbors when the key is missing
        if (neighbor !in visited) {
            dfsReachable(graph, neighbor, visited) // recursive DFS instead of an explicit stack
        }
    }
    return visited
}

// DFS: detect a cycle (e.g., circular service dependency)
// Returns true if a cycle exists
fun hasCycle(
    graph: Map<String, List<String>>,
    node: String,
    visited: MutableSet<String> = mutableSetOf(),  // nodes already seen in any DFS branch
    inStack: MutableSet<String> = mutableSetOf(),   // nodes in current DFS path
): Boolean {
    visited.add(node)
    inStack.add(node)

    for (neighbor in graph[node] ?: emptyList()) { // emptyList() = use no neighbors when the key is missing
        if (neighbor !in visited && hasCycle(graph, neighbor, visited, inStack)) return true
        if (neighbor in inStack) return true         // back-edge = cycle
    }

    inStack.remove(node)
    return false
}

// Usage:
println(dfsReachable(serviceGraph, "checkout"))
// → {checkout, inventory, payment, warehouse, fraud-check, bank, shipping}

val cyclicGraph = mapOf("A" to listOf("B"), "B" to listOf("C"), "C" to listOf("A"))
println(hasCycle(cyclicGraph, "A"))  // → true
```

<details>
<summary>Java version</summary>

### DFS — Java (Find All Affected Services + Cycle Detection)

```java
public Set<String> dfsReachable(
    Map<String, List<String>> graph,
    String start,
    Set<String> visited
) {
    visited.add(start);
    // Use empty list when start has no neighbors.
    for (String neighbor : graph.getOrDefault(start, List.of())) {
        if (!visited.contains(neighbor)) {
            dfsReachable(graph, neighbor, visited);
        }
    }
    return visited;
}

public boolean hasCycle(
    Map<String, List<String>> graph,
    String node,
    Set<String> visited,
    Set<String> inStack
) {
    visited.add(node);
    inStack.add(node);

    for (String neighbor : graph.getOrDefault(node, List.of())) { // use empty list when node has no neighbors
        if (!visited.contains(neighbor) && hasCycle(graph, neighbor, visited, inStack)) {
            return true;
        }
        if (inStack.contains(neighbor)) {
            return true;
        }
    }

    inStack.remove(node);
    return false;
}
```

</details>

---

## 6b. Sliding Window — Variable Size

Fixed-size sliding window (already covered above) answers: "max/sum over any window of size K."

Variable-size answers: "find the **smallest/longest** subarray that satisfies a condition."

**Pattern:**
```
expand right pointer while condition not met
once met → record result, shrink from left
repeat
```

**Practical use cases:** minimum transaction window to cover a refund amount, longest
session without a repeated error code, smallest log segment containing all required
event types.

```kotlin
// Variable-size: minimum length contiguous subarray with sum >= target
// Use case: find the minimum run of transactions that covers a refund of `target` yen
fun minWindowSum(amounts: IntArray, target: Int): Int {
    var left = 0
    var currentSum = 0
    var minLength = Int.MAX_VALUE

    for (right in amounts.indices) { // indices = valid index range, from 0 to lastIndex
        currentSum += amounts[right]            // expand right

        while (currentSum >= target) {          // condition met → try to shrink
            minLength = minOf(minLength, right - left + 1)
            currentSum -= amounts[left]
            left++
        }
    }
    return if (minLength == Int.MAX_VALUE) 0 else minLength
}

// Usage:
val transactions = intArrayOf(2, 3, 1, 2, 4, 3)
println(minWindowSum(transactions, 7))  // → 2  (subarray [4,3])
```

**Fixed vs Variable — decision:**

| Question type | Window type |
|---|---|
| "Max/min/avg over exactly K elements" | Fixed — subtract left, add right |
| "Shortest/longest subarray satisfying X" | Variable — shrink when condition met |

---

## 5. Dynamic Programming (Memoization & Optimal Substructure)

**The Pattern:** Break a problem into overlapping subproblems. Solve each subproblem once
and cache the result. Reuse it instead of recomputing.

This is not about memorizing LeetCode puzzles. The two practical signals that tell you DP
applies:
1. **Overlapping subproblems:** The same sub-calculation is needed multiple times.
2. **Optimal substructure:** The optimal solution to the full problem is built from optimal
   solutions to its subproblems.

If only (2) is true without (1), a simple greedy algorithm (always pick the locally best
option) is usually enough. DP is needed when greedy fails.

### Memoization — Caching Recursive Results

**Real use case:** Wallet or payment discount rules with stacked campaigns. A checkout may
have merchant coupons, wallet cashback, and campaign rules that interact, so the same
partial pricing state can be recomputed many times if you do not cache it.

**Pseudo shape:**
```text
solve(state):
    if state is base case:
        return base answer
    if state already in memo:
        return cached answer

    compute answer from smaller states
    store answer in memo
    return answer
```

```kotlin
// Without memoization: calculates the same sub-discounts repeatedly — O(2^n)
fun applyDiscount(price: Int, rules: List<DiscountRule>): Int {
    if (rules.isEmpty()) return price
    // drop(1) = list without the first rule.
    val withRule = rules[0].apply(applyDiscount(price, rules.drop(1)))
    val withoutRule = applyDiscount(price, rules.drop(1)) // same idea: solve the remaining rules only
    return minOf(withRule, withoutRule)
}

// With memoization: each unique (price, rulesIndex) pair computed once — O(n * price_range)
fun applyDiscount(price: Int, index: Int, rules: List<DiscountRule>,
                  memo: HashMap<Pair<Int,Int>, Int> = HashMap()): Int {
    if (index == rules.size) return price // base case: no rules left, so final price is current price
    val key = price to index // state = current price plus which rule index we are considering
    memo[key]?.let { return it } // if this state was solved before, reuse cached answer

    // Apply current rule, then solve the smaller subproblem.
    val withRule = rules[index].apply(applyDiscount(price, index + 1, rules, memo))
    // Skip current rule, then solve the smaller subproblem.
    val withoutRule = applyDiscount(price, index + 1, rules, memo)
    // Save best answer for this state before returning.
    return minOf(withRule, withoutRule).also { memo[key] = it }
}
```

<details>
<summary>Java version</summary>

```java
// Assume DiscountRule has a method like: int apply(int price)

// Without memoization: calculates the same sub-discounts repeatedly — O(2^n)
public int applyDiscount(int price, int index, List<DiscountRule> rules) {
    if (index == rules.size()) return price; // base case: no rules left, so final price is current price

    // Apply current rule, then solve remaining rules.
    int withRule = rules.get(index).apply(applyDiscount(price, index + 1, rules));
    int withoutRule = applyDiscount(price, index + 1, rules); // skip current rule, then solve remaining rules
    return Math.min(withRule, withoutRule);
}

record DiscountState(int price, int index) {}
// record = compact immutable key for memoization state (Java 17+)

// With memoization: each unique (price, index) state computed once
public int applyDiscount(
    int price,
    int index,
    List<DiscountRule> rules,
    Map<DiscountState, Integer> memo
) {
    if (index == rules.size()) return price; // base case: no rules left

    // state = current price + which rule we are considering
    DiscountState state = new DiscountState(price, index);
    Integer cached = memo.get(state);
    if (cached != null) return cached; // reuse previous answer instead of recomputing

    int withRule = rules.get(index).apply(applyDiscount(price, index + 1, rules, memo)); // take current rule
    int withoutRule = applyDiscount(price, index + 1, rules, memo); // skip current rule
    int best = Math.min(withRule, withoutRule);

    memo.put(state, best); // save answer for this state before returning
    return best;
}
```

</details>

**Wallet discounts example:**

```kotlin
fun interface WalletDiscountRule {
    fun apply(amountYen: Int): Int
}

fun bestWalletCheckoutAmount(
    amountYen: Int,
    index: Int,
    rules: List<WalletDiscountRule>,
    // hashMapOf() creates a mutable HashMap for cached states.
    memo: MutableMap<Pair<Int, Int>, Int> = hashMapOf(),
): Int {
    if (index == rules.size) return amountYen // base case: no campaign rules left to evaluate

    val state = amountYen to index // state = current payable amount + which rule we are evaluating now
    memo[state]?.let { return it } // if this wallet/campaign state was solved before, reuse it

    // Apply current rule after solving the remaining rules.
    val applyRule = rules[index].apply(bestWalletCheckoutAmount(amountYen, index + 1, rules, memo))
    // Ignore current rule and move on.
    val skipRule = bestWalletCheckoutAmount(amountYen, index + 1, rules, memo)

    // Cache the best payable amount for this state.
    return minOf(applyRule, skipRule).also { memo[state] = it }
}

val walletRules = listOf(
    WalletDiscountRule { amount -> amount - 500 }, // flat 500 JPY merchant coupon
    // Wallet campaign above threshold.
    WalletDiscountRule { amount -> if (amount >= 5_000) amount - 700 else amount },
    WalletDiscountRule { amount -> amount - minOf(amount / 10, 1_000) }, // 10% cashback with 1,000 JPY cap
)

// Best payable amount after evaluating all rule combinations.
println(bestWalletCheckoutAmount(10_000, 0, walletRules))
```

<details>
<summary>Java version</summary>

```java
@FunctionalInterface
interface WalletDiscountRule {
    int apply(int amountYen);
}

record WalletCheckoutState(int amountYen, int index) {}
// record = compact immutable key for memoization state (Java 17+)

public int bestWalletCheckoutAmount(
    int amountYen,
    int index,
    List<WalletDiscountRule> rules,
    Map<WalletCheckoutState, Integer> memo
) {
    if (index == rules.size()) return amountYen; // base case: no rules left

    // state = payable amount + current rule index
    WalletCheckoutState state = new WalletCheckoutState(amountYen, index);
    Integer cached = memo.get(state);
    if (cached != null) return cached; // reuse previous answer for the same checkout state

    // Apply current rule, then solve the remaining rules.
    int applyRule = rules.get(index).apply(bestWalletCheckoutAmount(amountYen, index + 1, rules, memo));
    int skipRule = bestWalletCheckoutAmount(amountYen, index + 1, rules, memo); // skip current rule
    int best = Math.min(applyRule, skipRule);

    memo.put(state, best); // save best payable amount for this state
    return best;
}

List<WalletDiscountRule> walletRules = List.of(
    amount -> amount - 500, // flat 500 JPY merchant coupon
    amount -> amount >= 5_000 ? amount - 700 : amount, // wallet campaign above threshold
    amount -> amount - Math.min(amount / 10, 1_000) // 10% cashback with 1,000 JPY cap
);

System.out.println(bestWalletCheckoutAmount(10_000, 0, walletRules, new HashMap<>()));
```

</details>

### The Knapsack Pattern — Capacity / Budget Allocation

**The question type:** "Given a set of items with weights and values, what is the maximum
value I can fit within a capacity constraint?"

**Real use cases:**
- Flash sale campaign: which product promotions to run given a fixed marketing budget to
  maximize expected revenue.
- Fulfilment centre packing: which orders to batch into a single delivery run given a
  truck weight limit to maximize shipment value.
- Feature prioritisation: given N engineer-sprints, which features deliver the most user value.
- Wallet campaign planning: given a fixed cashback budget, which cashback campaigns
  should run this month to generate the most extra payment volume.

**Pseudo shape:**
```text
create dp table
for each item:
    for each capacity:
        skip = best answer without this item
        take = best answer if item fits
        dp[current] = best of skip and take
return answer from final cell
```

```kotlin
// Standard 0/1 knapsack: each item either included or not
// items: list of Pair(weight, value). capacity: max weight allowed.
// Returns maximum achievable value.
fun knapsack(items: List<Pair<Int, Int>>, capacity: Int): Int {
    val n = items.size
    // dp[i][w] = max value using first i items with capacity w
    // Extra row/column handle "0 items" and "0 capacity" base cases.
    val dp = Array(n + 1) { IntArray(capacity + 1) }

    for (i in 1..n) {
        val (weight, value) = items[i - 1] // current item because dp row i means "using first i items"
        for (w in 0..capacity) {
            // Option A: skip this item
            dp[i][w] = dp[i - 1][w] // carry forward best value from previous row
            // Option B: take this item (if it fits)
            if (weight <= w) {
                dp[i][w] = maxOf(dp[i][w], dp[i - 1][w - weight] + value) // compare skip vs take
            }
        }
    }
    return dp[n][capacity] // final row + final capacity = best overall answer
}

// Example: 3 wallet cashback campaigns, budget = 5 units
// (campaign_cost_units, expected_extra_payment_volume_yen)
val campaigns = listOf(Pair(2, 300_000), Pair(3, 500_000), Pair(1, 150_000))
println(knapsack(campaigns, 5)) // 800_000 (take campaigns with cost 2 + 3)
```

<details>
<summary>Java version</summary>

```java
// Standard 0/1 knapsack: each item either included or not
// items[i][0] = weight, items[i][1] = value
public int knapsack(int[][] items, int capacity) {
    int n = items.length;
    // Extra row/column handle "0 items" and "0 capacity" base cases.
    int[][] dp = new int[n + 1][capacity + 1];

    for (int i = 1; i <= n; i++) {
        int weight = items[i - 1][0]; // current item because dp row i means "using first i items"
        int value = items[i - 1][1];

        for (int w = 0; w <= capacity; w++) {
            dp[i][w] = dp[i - 1][w]; // Option A: skip this item and keep previous best

            if (weight <= w) {
                // Option B: take this item if it fits.
                dp[i][w] = Math.max(dp[i][w], dp[i - 1][w - weight] + value);
            }
        }
    }

    return dp[n][capacity]; // final row + final capacity = best overall answer
}

// Example: 3 wallet cashback campaigns, budget = 5 units
int[][] promos = {
    {2, 300_000},
    {3, 500_000},
    {1, 150_000}
};
System.out.println(knapsack(promos, 5)); // 800_000
```

</details>

**Wallet campaigns example:**

```kotlin
data class WalletCampaign(
    val budgetCostUnits: Int,
    val expectedExtraPaymentVolumeYen: Int,
)

fun chooseBestWalletCampaignMix(
    campaigns: List<WalletCampaign>,
    monthlyBudgetUnits: Int,
): Int {
    val dp = Array(campaigns.size + 1) { IntArray(monthlyBudgetUnits + 1) }

    for (i in 1..campaigns.size) {
        val campaign = campaigns[i - 1]
        for (budget in 0..monthlyBudgetUnits) {
            dp[i][budget] = dp[i - 1][budget] // skip this campaign
            if (campaign.budgetCostUnits <= budget) {
                dp[i][budget] = maxOf(
                    dp[i][budget],
                    // Take this campaign if budget allows it.
                    dp[i - 1][budget - campaign.budgetCostUnits] + campaign.expectedExtraPaymentVolumeYen,
                )
            }
        }
    }

    // Best total extra payment volume inside the monthly budget.
    return dp[campaigns.size][monthlyBudgetUnits]
}

val walletCampaigns = listOf(
    WalletCampaign(2, 300_000), // merchant coupon campaign
    WalletCampaign(3, 500_000), // weekend cashback campaign
    WalletCampaign(1, 150_000), // wallet activation incentive
)

println(chooseBestWalletCampaignMix(walletCampaigns, 5)) // 800_000
```

<details>
<summary>Java version</summary>

```java
record WalletCampaign(int budgetCostUnits, int expectedExtraPaymentVolumeYen) {}

public int chooseBestWalletCampaignMix(List<WalletCampaign> campaigns, int monthlyBudgetUnits) {
    int[][] dp = new int[campaigns.size() + 1][monthlyBudgetUnits + 1];

    for (int i = 1; i <= campaigns.size(); i++) {
        WalletCampaign campaign = campaigns.get(i - 1);
        for (int budget = 0; budget <= monthlyBudgetUnits; budget++) {
            dp[i][budget] = dp[i - 1][budget]; // skip this campaign

            if (campaign.budgetCostUnits() <= budget) {
                dp[i][budget] = Math.max(
                    dp[i][budget],
                    // Take this campaign if budget allows it.
                    dp[i - 1][budget - campaign.budgetCostUnits()] + campaign.expectedExtraPaymentVolumeYen()
                );
            }
        }
    }

    // Best total extra payment volume inside the monthly budget.
    return dp[campaigns.size()][monthlyBudgetUnits];
}

List<WalletCampaign> walletCampaigns = List.of(
    new WalletCampaign(2, 300_000),
    new WalletCampaign(3, 500_000),
    new WalletCampaign(1, 150_000)
);

System.out.println(chooseBestWalletCampaignMix(walletCampaigns, 5)); // 800_000
```

</details>

**Time complexity:** O(n × capacity). **Space:** O(n × capacity), reducible to O(capacity)
with a 1D rolling array.

### Greedy vs DP — When to Use Which

| Signal | Use Greedy | Use DP |
|---|---|---|
| Local best = global best | Yes | — |
| Choices have side-effects on future options | — | Yes |
| Overlapping subproblems | — | Yes |
| Example | Activity scheduling (always pick shortest remaining) | 0/1 knapsack, longest path |

**Practical tip:** If asked an optimisation question, first ask yourself "does the greedy
choice always work here?" If yes, simpler greedy. If picking the locally best option now
can prevent reaching the global best later, switch to DP.
