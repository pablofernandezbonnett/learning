# Coding Drills

> Primary fit: `Shared core`


Practice problems mapped to the patterns in `01-core-patterns.md`.
Each drill includes both Kotlin and Java versions.
Kotlin is usually shorter under pressure, but it is useful to be able to write the same idea in Java too.

Scope of this note:

- this is still shared core, not a company-specific problem bank
- the drills are based on the most common timed coding shapes used in backend practice
- the business framing is backend-flavored on purpose, but the same underlying prompt may be worded much more generically

Note: this file is the canonical study note for the drills themselves. Keep the
focus on recognizing the pattern, explaining the invariant, and tracing edge cases
cleanly under time pressure.

Runnable companion:

- [labs/algorithms-live-coding/README.md](../../labs/algorithms-live-coding/README.md)

**Strategy for live coding (CoderPad / whiteboard):**
1. Repeat the problem in your own words before writing anything.
2. Write the signature + a few examples. Agree on edge cases.
3. State your approach + complexity before coding.
4. Code the happy path first, handle edges after.
5. Trace through your example manually before saying "done".

For each problem: read the prompt, write the solution yourself, then compare.

## Default Edge-Case Checklist

Before you say "done", check these quickly:

- empty input
- one element
- duplicates or all values the same
- no valid answer / impossible case
- first or last position involved
- sorted-input assumption, if the pattern depends on it
- off-by-one window boundaries

Short rule:

> most interview bugs are not in the main idea, but in the first, last, empty, duplicate, or impossible case

---

## Pattern-First Drill Map

When you are rusty, do not start from topic names like `HashMap` or
`ArrayDeque`.
Start from the shape of the prompt.

| Prompt smell | Core pattern | Usual structure | Start here |
|---|---|---|---|
| "group by customer / ID / symbol / date" | group by | `Map<K, List<V>>` | `H3b` |
| "count occurrences / frequency / duplicates" | frequency map | `Map<K, Integer>` | `H1`, `H1b`, `H3`, `H3c`, `PQ1` |
| "sum per customer / total per product" | aggregate by key | `Map<K, BigDecimal>` or `Map<K, Integer>` | `H3d` |
| "highest price per category / latest per key" | max per key | `Map<K, V>` | `H3e` |
| "first duplicate / have I seen this before?" | seen-set lookup | `Set<T>` | `H1c` |
| "two numbers in sorted input" | two pointers | `left`, `right` | `TP1` |
| "contiguous subarray / substring" | sliding window | `left`, `right`, running state | `SW1`, `SW2`, `SW3` |
| "last opened, first closed" | stack / deque | `ArrayDeque` / `Deque` | `SD1`, `SD2`, `SD3`, `SD4` |

Short rule:

> if the prompt says "per key", first ask whether you need a list, a count, a running total, or just the best-so-far value

---

## Curated Live-Coding Set

Do not try to sample this whole file equally.
Bias toward this smaller working set and repeat it under time pressure.

### Recommended 12

1. `SW3` here -> same core pattern as `longest-substring`
2. `PQ1` here -> same core pattern as `top-k-frequent`
3. `G4` -> `number-of-islands`
4. `SD3` -> `calculator-ii`
5. `D1` -> `lru-cache`
6. `D2` -> `rate-limiter`
7. `MC2` -> `wallet-ledger`
8. `cache-aside` in the companion
9. `event-dedup` in the companion
10. `retry-backoff` in the companion
11. `pagination-merge` in the companion
12. `producer-consumer` in the companion

### Why these 12

- they cover the `4 problems / 70 minutes` shape without turning prep into a puzzle bank
- they force clean explanation of invariants and data-structure choice
- they keep one parser exercise as a stack hedge without letting parser work dominate the loop
- they convert better to backend interviews than adding more medium/hard DSA just for breadth

### Recommended mock rounds

`Mock A` (`OA baseline`):

- `SW3`
- `PQ1`
- `G4`
- `SD3`

`Mock B` (`live coding core`):

- `D1`
- `D2`
- `cache-aside`
- `MC2`

`Mock C` (`backend practical`):

- `event-dedup`
- `retry-backoff`
- `pagination-merge`
- `producer-consumer`

---

## Warm-Up Rendering Problems

### R1 — ASCII Pyramid

**Prompt:** We can render an ASCII art pyramid with `N` levels by printing `N`
rows of asterisks, where the top row has a single asterisk in the center and
each successive row has two additional asterisks.

For `N = 3`:

```text
  *
 ***
*****
```

For `N = 5`:

```text
    *
   ***
  *****
 *******
*********
```

Write a program that generates this pyramid for `N = 10`.

**Why it comes up:** This is a small but useful warm-up for nested loops,
spacing arithmetic, centering, and off-by-one control.

**Pseudo shape:**
```text
for each row from 1 to N:
    spaces = N - row
    stars = (2 * row) - 1
    print spaces
    print stars
    move to next line
```

**Kotlin**
```kotlin
fun printPyramid(levels: Int) {
    for (row in 1..levels) {
        repeat(levels - row) { print(" ") }      // left padding to keep the pyramid centered
        repeat(2 * row - 1) { print("*") }       // odd sequence: 1, 3, 5, 7, ...
        println()                                // next pyramid level
    }
}

fun main() {
    val n = 10
    printPyramid(n)
}
```

<details>
<summary>Java version</summary>

```java
/**
 * Renders a centered ASCII pyramid using nested loops.
 * Key ideas: row control, left padding, and odd-number star growth.
 */
class PyramidSolution {
    public static void main(String[] args) {
        int n = 10;

        for (int row = 1; row <= n; row++) {
            for (int space = 0; space < n - row; space++) {
                System.out.print(" ");
            }

            for (int star = 1; star <= (2 * row - 1); star++) {
                System.out.print("*");
            }

            System.out.println();
        }
    }
}
```

</details>

**Study notes:**

- space logic: if `N = 3`, row `1` needs `2` spaces, row `2` needs `1`, row
  `3` needs `0`, so the rule is `N - row`
- star logic: odd counts come from the standard formula `(2 * row) - 1`
- common bug: using the wrong loop variable in the inner condition can create an
  infinite loop and lead to output-limit failures

## HashMap Problems

### H1 — First Non-Repeating Order ID

**Prompt:** Given a list of order IDs (some may be duplicates from retries),
return the first ID that appears exactly once. Return `null` if none.

**Why it comes up:** Deduplication, idempotency checks.

**Pseudo shape:**
```text
create ordered counter map
for each order ID:
    increment its count
scan entries in original insertion order
return first ID with count 1
```

**Kotlin**
```kotlin
fun firstUniqueOrderId(orderIds: List<String>): String? {
    val count = linkedMapOf<String, Int>()          // LinkedHashMap preserves insertion order
    for (id in orderIds) count[id] = (count[id] ?: 0) + 1 // count how many times each ID appears
    return count.entries.firstOrNull { it.value == 1 }?.key // firstOrNull(...) returns the first matching entry or null
}

// Test:
println(firstUniqueOrderId(listOf("A1", "B2", "A1", "C3", "B2")))  // → C3
println(firstUniqueOrderId(listOf("A1", "A1")))                      // → null
```

<details>
<summary>Java version</summary>

```java
public String firstUniqueOrderId(List<String> orderIds) {
    Map<String, Integer> count = new LinkedHashMap<>(); // LinkedHashMap preserves insertion order
    for (String id : orderIds) {
        count.put(id, count.getOrDefault(id, 0) + 1); // getOrDefault() returns 0 for first occurrence
    }
    for (Map.Entry<String, Integer> entry : count.entrySet()) { // entrySet() lets us read key and count together
        if (entry.getValue() == 1) {
            return entry.getKey();
        }
    }
    return null;
}
```

</details>

**Complexity:** O(n) time, O(n) space. Using `linkedMapOf` avoids a second pass scan.

---

### H1b — Container Queries (`ADD`, `EXISTS`, `REMOVE`)

**Prompt:** Implement a simple container of integer numbers that supports these operations:

- `ADD <value>`: add one occurrence of the value
- `EXISTS <value>`: return `"true"` if the value exists, otherwise `"false"`
- `REMOVE <value>`: remove one occurrence of the value; if several copies exist, remove only one

Given a list of queries, return the result string for each operation.

Example:

```text
queries = [
    ["ADD", "1"],
    ["ADD", "2"],
    ["ADD", "2"],
    ["ADD", "3"],
    ["EXISTS", "1"],
    ["EXISTS", "2"],
    ["EXISTS", "3"],
    ["REMOVE", "2"],
    ["REMOVE", "1"],
    ["EXISTS", "2"],
    ["EXISTS", "1"]
]
```

Output:

```text
["", "", "", "", "true", "true", "true", "true", "true", "true", "false"]
```

**Why it comes up:** This is a compact container-design drill. It tests whether you
notice when a `HashSet` is enough and when duplicates force you to move to a
frequency map.

**Level 1 (`ADD` + `EXISTS` only): `HashSet` is enough**

Your Java version is correct for the simpler level because presence checks do not
care about duplicates yet.

```java
String[] solution(String[][] queries) {
    HashSet<String> storage = new HashSet<>();
    String[] results = new String[queries.length];

    for (int i = 0; i < queries.length; i++) {
        String operation = queries[i][0];
        String value = queries[i][1];

        if (operation.equals("ADD")) {
            storage.add(value); // set is enough when we only care about presence
            results[i] = "";
        } else if (operation.equals("EXISTS")) {
            results[i] = storage.contains(value) ? "true" : "false"; // constant-time membership check
        }
    }
    return results;
}
```

**Kotlin translation of the same level**
```kotlin
fun solution(queries: Array<Array<String>>): Array<String> {
    val storage = hashSetOf<String>()
    val results = Array(queries.size) { "" }

    for (i in queries.indices) {
        val operation = queries[i][0]
        val value = queries[i][1]

        when (operation) {
            "ADD" -> {
                storage.add(value) // set is enough when we only care about presence
                results[i] = ""
            }
            "EXISTS" -> {
                results[i] = if (storage.contains(value)) "true" else "false" // constant-time membership check
            }
        }
    }

    return results
}
```

**Why `HashSet` stops being enough**

Once `REMOVE` appears, duplicates matter:

- after `ADD 2`, `ADD 2`, the container still has two copies of `2`
- one `REMOVE 2` should leave one copy behind
- a `HashSet` only stores presence, not count, so it loses that information

That means the correct structure for this level is:

- `HashMap<String, Int>` in Kotlin
- `HashMap<String, Integer>` in Java

The map stores `value -> occurrence count`.

**Pseudo shape:**
```text
create frequency map
for each query:
    if ADD:
        increment count
        answer ""
    if EXISTS:
        answer "true" when count > 0
    if REMOVE:
        if count > 1: decrement
        if count == 1: remove key
        answer "true" when removal happened, otherwise "false"
```

**Kotlin**
```kotlin
fun solution(queries: Array<Array<String>>): Array<String> {
    val counts = mutableMapOf<String, Int>()
    val results = Array(queries.size) { "" }

    for (i in queries.indices) {
        val operation = queries[i][0]
        val value = queries[i][1]

        when (operation) {
            "ADD" -> {
                counts[value] = (counts[value] ?: 0) + 1 // track how many copies of this value exist
                results[i] = ""
            }

            "EXISTS" -> {
                results[i] = if ((counts[value] ?: 0) > 0) "true" else "false" // present when count is still positive
            }

            "REMOVE" -> {
                val current = counts[value] ?: 0
                results[i] = if (current == 0) {
                    "false"
                } else {
                    if (current == 1) counts.remove(value) else counts[value] = current - 1 // remove key at zero to keep lookups simple
                    "true"
                }
            }
        }
    }

    return results
}
```

<details>
<summary>Java version</summary>

```java
String[] solution(String[][] queries) {
    Map<String, Integer> counts = new HashMap<>();
    String[] results = new String[queries.length];

    for (int i = 0; i < queries.length; i++) {
        String operation = queries[i][0];
        String value = queries[i][1];

        if (operation.equals("ADD")) {
            counts.put(value, counts.getOrDefault(value, 0) + 1); // track multiplicity, not just presence
            results[i] = "";
        } else if (operation.equals("EXISTS")) {
            results[i] = counts.getOrDefault(value, 0) > 0 ? "true" : "false"; // present when count is still positive
        } else if (operation.equals("REMOVE")) {
            int current = counts.getOrDefault(value, 0);
            if (current == 0) {
                results[i] = "false";
            } else {
                if (current == 1) {
                    counts.remove(value); // remove key entirely once the last copy disappears
                } else {
                    counts.put(value, current - 1); // keep one fewer copy
                }
                results[i] = "true";
            }
        }
    }

    return results;
}
```

</details>

**Study notes:**

- `HashSet` is the right answer for the simpler `ADD` / `EXISTS` level
- `HashMap` becomes necessary as soon as duplicate-aware removal appears
- the key invariant is: if `counts[x] = k`, then the container currently holds `k`
  copies of `x`
- removing the key entirely when the count reaches zero keeps `EXISTS` logic clean

**Complexity:** O(q) time for `q` queries, O(u) space for `u` distinct values.

---

### H1c — First Duplicate Event ID

**Prompt:** Given a list of event IDs, return the first ID that appears for the
second time as you scan from left to right. Return `null` if no duplicate
exists.

Example:

```text
["A", "B", "C", "B", "A"] -> "B"
```

**Why it comes up:** This is the cleanest `HashSet` / `seen` drill. It is the
smallest version of deduplication and idempotency detection.

**Pseudo shape:**
```text
create empty set
for each ID:
    if already in set: return it
    add it to set
return null
```

**Kotlin**
```kotlin
fun firstDuplicateEventId(ids: List<String>): String? {
    val seen = mutableSetOf<String>()

    for (id in ids) {
        if (!seen.add(id)) return id // add(...) returns false when the ID already exists
    }

    return null
}
```

<details>
<summary>Java version</summary>

```java
public String firstDuplicateEventId(List<String> ids) {
    Set<String> seen = new HashSet<>();

    for (String id : ids) {
        if (!seen.add(id)) return id; // add(...) returns false when the ID already exists
    }

    return null;
}
```

</details>

**Complexity:** O(n) time, O(n) space.

---

### H2 — Two Products That Sum to a Budget

**Prompt:** Given a list of product prices (Int) and a budget, return the indices
of two products that sum exactly to the budget. Assume exactly one solution exists.

**Why it comes up:** Two Sum — the canonical HashMap interview problem.

**Pseudo shape:**
```text
create empty map: seen value -> index
for each price at index i:
    complement = budget - price
    if complement already seen:
        return previous index and i
    store current price and index
```

**Kotlin**
```kotlin
fun twoProductsBudget(prices: List<Int>, budget: Int): Pair<Int, Int> {
    val seen = mutableMapOf<Int, Int>()             // value → index
    for ((index, price) in prices.withIndex()) {    // withIndex() gives (index, value) pairs while iterating
        val complement = budget - price             // value we would need to hit the target
        // !! is safe here because we just checked the key exists.
        if (complement in seen) return Pair(seen[complement]!!, index)
        seen[price] = index                         // remember current value for later elements
    }
    error("No solution found")
}

// Test:
println(twoProductsBudget(listOf(2990, 4990, 1990, 5990), 7980))  // → (0, 1)
println(twoProductsBudget(listOf(2990, 4990, 1990, 5990), 4980))  // → (0, 2)
```

<details>
<summary>Java version</summary>

```java
public int[] twoProductsBudget(List<Integer> prices, int budget) {
    Map<Integer, Integer> seen = new HashMap<>();
    for (int index = 0; index < prices.size(); index++) {
        int price = prices.get(index);
        int complement = budget - price; // number we need to have seen earlier
        if (seen.containsKey(complement)) { // containsKey(...) checks whether that key is already stored
            return new int[] { seen.get(complement), index };
        }
        seen.put(price, index); // remember current price position for future lookups
    }
    throw new IllegalArgumentException("No solution found");
}
```

</details>

**Complexity:** O(n) — one pass. The complement trick avoids the O(n²) nested loop.

---

### H3 — Group Transactions by Status

**Prompt:** Given a list of `Pair<String, String>` (orderId, status), return a
`Map<String, Int>` of status → count.

**Why it comes up:** Aggregation is a core backend operation. Signals you know
`groupingBy`, not just `for` loops.

**Pseudo shape:**
```text
create empty counter map
for each transaction:
    read status
    increment status count
return final counts
```

**Kotlin**
```kotlin
fun countByStatus(transactions: List<Pair<String, String>>): Map<String, Int> =
    transactions.groupingBy { it.second }.eachCount() // group by status, then count items in each group

// With custom type:
data class Transaction(val orderId: String, val status: String)

fun countByStatusTyped(transactions: List<Transaction>): Map<String, Int> =
    transactions.groupingBy { it.status }.eachCount() // same idea: group by status, then count each group

// Test:
val txs = listOf(
    Transaction("O1", "PAID"), Transaction("O2", "PENDING"),
    Transaction("O3", "PAID"), Transaction("O4", "FAILED"),
    Transaction("O5", "PAID"),
)
println(countByStatusTyped(txs))  // → {PAID=3, PENDING=1, FAILED=1}
```

**Kotlin note:** `groupingBy { }.eachCount()` replaces a full `for` loop with a counter map.

<details>
<summary>Java version</summary>

```java
record Transaction(String orderId, String status) {}
// record = compact immutable data carrier, available in modern Java baseline (17+)

public Map<String, Integer> countByStatusTyped(List<Transaction> transactions) {
    Map<String, Integer> counts = new HashMap<>();
    for (Transaction tx : transactions) {
        counts.put(tx.status(), counts.getOrDefault(tx.status(), 0) + 1); // increment one running counter per status
    }
    return counts;
}
```

</details>

---

### H3b — Summarize CSV Prices by ID

**Prompt:** You receive an array of CSV-like strings such as:

```text
["APPX,150.00", "AMMZ, 145.00", " APPX, 145.00", "AMMZ, 175.00"]
```

Each line contains:

```text
ID, price
```

Clean surrounding spaces, group rows by `ID`, and return one output line per
`ID` using this exact shape:

```text
ID, highest, original-values-in-arrival-order, lowest
```

That means the highest value is always repeated at the front, the lowest value
is always repeated at the end, and the values in the middle stay in their
original arrival order for that `ID`.

Example output for the input above:

```text
[
  "APPX,150.00,150.00,145.00,145.00",
  "AMMZ,175.00,145.00,175.00,145.00"
]
```

**Why it comes up:** This is a practical parsing-and-aggregation drill. It tests
whether you can normalize messy input, preserve insertion order, group with a
map, and avoid floating-point mistakes for money-like values.

**Pseudo shape:**
```text
create LinkedHashMap: ID -> list of BigDecimal values
for each CSV line:
    split into ID and value
    trim both sides
    append parsed value to that ID's list

for each ID in first-seen order:
    highest = max(values)
    lowest = min(values)
    build: ID, highest, values in original order, lowest
```

**Kotlin**
```kotlin
import java.math.BigDecimal

fun solution(csv: Array<String>): Array<String> {
    val grouped = linkedMapOf<String, MutableList<BigDecimal>>()

    for (line in csv) {
        val parts = line.split(",", limit = 2) // split only once: logical shape is always ID,value
        val id = parts[0].trim()
        val value = parts[1].trim().toBigDecimal()
        grouped.computeIfAbsent(id) { mutableListOf() }.add(value) // preserve arrival order inside each ID bucket
    }

    return grouped.map { (id, values) ->
        val highest = values.maxOrNull()!! // highest is repeated at the front
        val lowest = values.minOrNull()!!  // lowest is repeated at the end

        buildString {
            append(id)
            append(",")
            append(highest.toPlainString())

            for (value in values) {
                append(",") // middle section keeps original arrival order
                append(value.toPlainString())
            }

            append(",")
            append(lowest.toPlainString())
        }
    }.toTypedArray()
}
```

<details>
<summary>Java version</summary>

```java
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public String[] solution(String[] csv) {
    Map<String, List<BigDecimal>> grouped = new LinkedHashMap<>();

    for (String line : csv) {
        String[] parts = line.split(",", 2); // split only once: logical shape is always ID,value
        String id = parts[0].trim();
        BigDecimal value = new BigDecimal(parts[1].trim());

        grouped.computeIfAbsent(id, ignored -> new ArrayList<>()).add(value); // preserve arrival order per ID
    }

    List<String> result = new ArrayList<>();

    for (Map.Entry<String, List<BigDecimal>> entry : grouped.entrySet()) {
        String id = entry.getKey();
        List<BigDecimal> values = entry.getValue();

        BigDecimal highest = Collections.max(values); // highest is repeated at the front
        BigDecimal lowest = Collections.min(values);  // lowest is repeated at the end

        StringBuilder sb = new StringBuilder();
        sb.append(id).append(",").append(highest.toPlainString());

        for (BigDecimal value : values) {
            sb.append(",").append(value.toPlainString()); // middle section keeps original arrival order
        }

        sb.append(",").append(lowest.toPlainString());
        result.add(sb.toString());
    }

    return result.toArray(new String[0]);
}
```

</details>

**Study notes:**

- use `LinkedHashMap` / `linkedMapOf` so IDs are emitted in first-seen order
- keep the middle values in arrival order; do not sort them
- compute `highest` and `lowest` separately from the preserved original list
- use `BigDecimal`, not `double`, because the prompt is money-shaped
- if an ID has only one value, that same value appears as `highest`, the single
  original middle value, and `lowest`

**Advanced variants:**

If the interviewer asks for a more declarative style, you can keep the same
logic but express the grouping and rendering with `Streams` in Java or a more
functional `fold` + `map` shape in Kotlin.

**Java stream-oriented variant**
```java
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public String[] solution(String[] csv) {
    Map<String, List<BigDecimal>> grouped = Arrays.stream(csv)
            .map(line -> line.split(",", 2)) // parse into [id, value] pairs first
            .collect(Collectors.groupingBy(
                    parts -> parts[0].trim(),
                    LinkedHashMap::new,
                    Collectors.mapping(
                            parts -> new BigDecimal(parts[1].trim()), // parse as decimal, not double
                            Collectors.toList()
                    )
            ));

    return grouped.entrySet().stream()
            .map(entry -> {
                String id = entry.getKey();
                List<BigDecimal> values = entry.getValue();
                BigDecimal highest = values.stream().max(BigDecimal::compareTo).orElseThrow(); // repeated at the front
                BigDecimal lowest = values.stream().min(BigDecimal::compareTo).orElseThrow();  // repeated at the end

                String middle = values.stream()
                        .map(BigDecimal::toPlainString)
                        .collect(Collectors.joining(",")); // preserve original arrival order in the middle

                return id + "," + highest.toPlainString() + "," + middle + "," + lowest.toPlainString();
            })
            .toArray(String[]::new);
}
```

**Kotlin more-functional variant**
```kotlin
import java.math.BigDecimal

fun solution(csv: Array<String>): Array<String> {
    val grouped = csv.fold(linkedMapOf<String, MutableList<BigDecimal>>()) { acc, line ->
        val parts = line.split(",", limit = 2) // split only once into ID and price
        val id = parts[0].trim()
        val value = parts[1].trim().toBigDecimal()
        acc.computeIfAbsent(id) { mutableListOf() }.add(value) // preserve arrival order per ID
        acc
    }

    return grouped.map { (id, values) ->
        val highest = values.maxOrNull()!! // repeated at the front
        val lowest = values.minOrNull()!!  // repeated at the end
        val middle = values.joinToString(",") { it.toPlainString() } // keep original order in the middle
        "$id,${highest.toPlainString()},$middle,${lowest.toPlainString()}"
    }.toTypedArray()
}
```

**Interview caution:** These variants can look cleaner once the invariant is
already clear. Under time pressure, the simpler loop version is often easier to
debug and explain.

**Edge cases to clarify before coding:**

- empty input -> return an empty array
- one row for an ID -> value appears as `highest`, as the single middle value,
  and again as `lowest`
- malformed CSV line -> decide whether to fail fast or skip invalid rows
- missing ID or missing price -> usually treat as invalid input
- invalid decimal like `APPX,abc` -> fail fast unless the interviewer asks for
  best-effort parsing

**Interview-safe rule for malformed input:**

> If the prompt does not say to recover from bad rows, I would normally fail
> fast with a clear exception. If it is more of a data-cleaning task, I would
> confirm whether invalid rows should be skipped and logged instead.

**Strict parsing helpers**

Kotlin:
```kotlin
private fun parseLine(line: String): Pair<String, BigDecimal> {
    val parts = line.split(",", limit = 2) // strict 2-column parsing
    require(parts.size == 2) { "Malformed CSV row: $line" }

    val id = parts[0].trim()
    val rawValue = parts[1].trim()

    require(id.isNotEmpty()) { "Missing ID: $line" }
    require(rawValue.isNotEmpty()) { "Missing price: $line" }

    return id to rawValue.toBigDecimal()
}
```

Java:
```java
private Map.Entry<String, BigDecimal> parseLine(String line) {
    String[] parts = line.split(",", 2); // strict 2-column parsing
    if (parts.length != 2) {
        throw new IllegalArgumentException("Malformed CSV row: " + line);
    }

    String id = parts[0].trim();
    String rawValue = parts[1].trim();

    if (id.isEmpty()) {
        throw new IllegalArgumentException("Missing ID: " + line);
    }
    if (rawValue.isEmpty()) {
        throw new IllegalArgumentException("Missing price: " + line);
    }

    return Map.entry(id, new BigDecimal(rawValue));
}
```

**Complexity:** O(n) time overall and O(n) space, where `n` is the number of
rows. Each row is parsed once and each grouped value is visited once more while
building the output.

---

### H3c — Count Words in a Log Message

**Prompt:** Given a string such as:

```text
"java kotlin java spring"
```

Return a frequency map:

```text
{java=2, kotlin=1, spring=1}
```

**Why it comes up:** This is the plainest frequency-map drill. It is useful
because many interview problems are just this pattern with different nouns.

**Pseudo shape:**
```text
split text into tokens
create empty counter map
for each token:
    increment token count
return final counts
```

**Kotlin**
```kotlin
fun countWords(text: String): Map<String, Int> {
    if (text.isBlank()) return emptyMap()

    return text.trim()
        .split(Regex("\\s+")) // split on one-or-more spaces
        .groupingBy { it }
        .eachCount() // Kotlin stdlib builds the frequency map for us
}
```

<details>
<summary>Java version</summary>

```java
public Map<String, Integer> countWords(String text) {
    Map<String, Integer> counts = new HashMap<>();
    if (text == null || text.isBlank()) return counts;

    for (String word : text.trim().split("\\s+")) {
        counts.put(word, counts.getOrDefault(word, 0) + 1); // one running counter per distinct word
    }

    return counts;
}
```

</details>

**Complexity:** O(n) time, O(u) space, where `u` is the number of distinct
words.

---

### H3d — Sum Sales by Customer

**Prompt:** Given rows of `(customerId, amount)`, return total sales per
customer.

Example:

```text
[("C1", 10.50), ("C2", 20.00), ("C1", 4.50)] -> {C1=15.00, C2=20.00}
```

**Why it comes up:** This is the `aggregate by key` version of the same map
pattern. In backend work this appears in reconciliation, reporting, and billing
flows.

**Pseudo shape:**
```text
create totals map
for each row:
    add amount into totals[customerId]
return totals
```

**Kotlin**
```kotlin
import java.math.BigDecimal

data class CustomerSale(val customerId: String, val amount: BigDecimal)

fun sumSalesByCustomer(rows: List<CustomerSale>): Map<String, BigDecimal> {
    val totals = linkedMapOf<String, BigDecimal>()

    for (row in rows) {
        totals[row.customerId] = (totals[row.customerId] ?: BigDecimal.ZERO) + row.amount // keep one running total per customer
    }

    return totals
}
```

<details>
<summary>Java version</summary>

```java
import java.math.BigDecimal;

record CustomerSale(String customerId, BigDecimal amount) {}

public Map<String, BigDecimal> sumSalesByCustomer(List<CustomerSale> rows) {
    Map<String, BigDecimal> totals = new LinkedHashMap<>();

    for (CustomerSale row : rows) {
        totals.put(
            row.customerId(),
            totals.getOrDefault(row.customerId(), BigDecimal.ZERO).add(row.amount()) // keep one running total per customer
        );
    }

    return totals;
}
```

</details>

**Complexity:** O(n) time, O(u) space.

---

### H3e — Highest Price Per Category

**Prompt:** Given rows of `(category, price)`, return the highest price seen for
each category.

Example:

```text
[("BOOK", 10), ("GAME", 55), ("BOOK", 12)] -> {BOOK=12, GAME=55}
```

**Why it comes up:** This is the `best-so-far per key` pattern. It appears in
questions like highest score per player, latest timestamp per order, or maximum
price per product family.

**Pseudo shape:**
```text
create best-per-key map
for each row:
    if key unseen: store value
    else store max(currentBest, newValue)
return map
```

**Kotlin**
```kotlin
import java.math.BigDecimal

data class CategoryPrice(val category: String, val price: BigDecimal)

fun highestPricePerCategory(rows: List<CategoryPrice>): Map<String, BigDecimal> {
    val best = linkedMapOf<String, BigDecimal>()

    for (row in rows) {
        val current = best[row.category]
        if (current == null || row.price > current) {
            best[row.category] = row.price // replace only when the new row beats the current best
        }
    }

    return best
}
```

<details>
<summary>Java version</summary>

```java
import java.math.BigDecimal;

record CategoryPrice(String category, BigDecimal price) {}

public Map<String, BigDecimal> highestPricePerCategory(List<CategoryPrice> rows) {
    Map<String, BigDecimal> best = new LinkedHashMap<>();

    for (CategoryPrice row : rows) {
        BigDecimal current = best.get(row.category());
        if (current == null || row.price().compareTo(current) > 0) {
            best.put(row.category(), row.price()); // replace only when the new row beats the current best
        }
    }

    return best;
}
```

</details>

**Complexity:** O(n) time, O(u) space.

---

### H4 — Explain How HashMap Collisions Are Handled

**Prompt:** In an interview, you may be asked: "What happens if two keys land in
the same bucket in a `HashMap`?"

**Why it comes up:** This is a common follow-up when you use `HashMap` in a coding
round. The interviewer is checking whether you understand the data structure, not
just the pattern.

**Short answer:**

- a collision means two **different keys** land in the same internal slot of the map
- that internal slot is usually called a `bucket`
- a collision does **not** mean "same key inserted twice"
- if the exact same key is inserted again, the old value is replaced
- if two different keys land in the same bucket, Java keeps several entries in that bucket and then compares the real keys with `equals()`
- if too many entries pile up in one bucket, lookup degrades from average `O(1)` toward `O(n)`

**What to say for Java specifically:**

- Java `HashMap` first uses `hashCode()` to find the bucket
- then it checks entries in that bucket using `equals()`
- historically that bucket behaved like a short linked structure of entries
- modern JVM implementations can turn a crowded bucket into a tree to avoid very slow lookup
- resize and load factor help keep buckets sparse

**Kotlin note:**

- on `Kotlin/JVM`, `hashMapOf()` uses the same JVM map behavior that matters here
- same key again -> replace value
- different keys in the same bucket -> collision handling still relies on `hashCode()` and `equals()`

**Why `equals` and `hashCode` matter:**

If you use a custom key type, lookup depends on both:

- `hashCode()` to find the bucket
- `equals()` to confirm the exact key

If you forget them, two logically identical keys may not match.

**Interview-safe sentence:**

> A collision means different keys share one internal bucket, not that the same key was inserted twice. If the same key appears again, the value is replaced. If different keys share a bucket, Java compares the real keys with `equals()` to find the right entry.

**Linked-list connection:**

This is the part worth understanding, because it links `HashMap` questions to
linked-list reasoning.

Small mental model:

- a `HashMap` is backed by an internal array
- each array slot is one `bucket`
- one bucket may hold more than one entry if several keys land there
- historically, that bucket behaved like a short linked structure of entries

Conceptual picture:

```text
internal array

bucket[0] -> empty
bucket[1] -> (keyA, tx1) -> (keyB, tx2) -> (keyC, tx3)
bucket[2] -> empty
```

What that means in plain English:

- Java first uses `hashCode()` to jump to the right bucket
- once it is inside that bucket, it may need to walk entry by entry
- on each entry, it checks `equals()` to see whether this is the real key

Pseudo shape for lookup inside one crowded bucket:

```text
find bucket from hashCode
current = first entry in that bucket
while current is not null:
    if current.key equals target key:
        return current.value
    current = current.next
return not found
```

Why linked-list basics still matter:

- you do not need to implement `HashMap` from scratch in interviews
- but interviewers may still ask why collisions can make lookup slower
- the answer is that a crowded bucket stops looking like one direct array lookup and starts looking more like walking a short linked structure

That is the real connection:

- array gives the fast jump to the bucket
- linked structure handles multiple entries inside that bucket

Modern Java note:

- newer JVMs can turn a very crowded bucket into a tree instead of keeping it as a simple linked structure
- but the interview-level idea is still the same: collision means extra structure and extra comparisons inside one bucket

---

## Sliding Window Problems

### SW1 — Peak Daily Transaction Volume (Fixed Window)

**Prompt:** Given an array of hourly transaction counts, find the maximum total
transactions in any 6-hour window.

**Pseudo shape:**
```text
sum the first full window
save it as current best
move window right one step at a time:
    add new item entering the window
    subtract old item leaving the window
    update best
```

**Kotlin**
```kotlin
fun maxTransactionsInWindow(hourly: IntArray, windowSize: Int): Int {
    var window = hourly.take(windowSize).sum() // take(windowSize) = first N items, sum() = add them together
    var maxWindow = window

    for (i in windowSize until hourly.size) {
        window += hourly[i] - hourly[i - windowSize] // add new hour, remove hour that just left the window
        maxWindow = maxOf(maxWindow, window)
    }
    return maxWindow
}

// Test:
println(maxTransactionsInWindow(intArrayOf(10, 20, 30, 5, 15, 40, 25, 10), 3))
// → 80  (window [30, 5, 15, 40] overlapping: actually [5, 40, 25] = ??? — let me trace)
// Trace: windows of 3: [10,20,30]=60, [20,30,5]=55, [30,5,15]=50, [5,15,40]=60, [15,40,25]=80 ✓
```

<details>
<summary>Java version</summary>

```java
public int maxTransactionsInWindow(int[] hourly, int windowSize) {
    int window = 0;
    for (int i = 0; i < windowSize; i++) {
        window += hourly[i]; // sum of the first complete window
    }
    int maxWindow = window;

    for (int i = windowSize; i < hourly.length; i++) {
        window += hourly[i] - hourly[i - windowSize]; // add new hour, remove old hour
        maxWindow = Math.max(maxWindow, window);
    }
    return maxWindow;
}
```

</details>

---

### SW2 — Minimum Consecutive Transactions Covering a Refund (Variable Window)

**Prompt:** Given an array of transaction amounts, find the minimum number of
consecutive transactions whose sum is >= `target`. Return 0 if impossible.

**Pseudo shape:**
```text
left = 0, sum = 0, best = infinity
for each right pointer:
    add right value into sum
    while sum already reaches target:
        record current window length
        remove left value and move left
return 0 if best was never updated
```

**Kotlin**
```kotlin
fun minTransactionsCoveringRefund(amounts: IntArray, target: Int): Int {
    var left = 0
    var sum = 0
    var minLen = Int.MAX_VALUE

    for (right in amounts.indices) { // indices = valid index range, from 0 to lastIndex
        sum += amounts[right] // expand the window to the right
        while (sum >= target) {
            minLen = minOf(minLen, right - left + 1) // current window already covers the target
            sum -= amounts[left++] // shrink from the left to see if a shorter valid window exists
        }
    }
    return if (minLen == Int.MAX_VALUE) 0 else minLen
}

// Test:
println(minTransactionsCoveringRefund(intArrayOf(2, 3, 1, 2, 4, 3), 7))  // → 2 ([4,3])
println(minTransactionsCoveringRefund(intArrayOf(1, 1, 1, 1), 10))       // → 0 (impossible)
```

<details>
<summary>Java version</summary>

```java
public int minTransactionsCoveringRefund(int[] amounts, int target) {
    int left = 0;
    int sum = 0;
    int minLen = Integer.MAX_VALUE;

    for (int right = 0; right < amounts.length; right++) {
        sum += amounts[right]; // expand the window to the right
        while (sum >= target) {
            minLen = Math.min(minLen, right - left + 1); // current window already covers the target
            sum -= amounts[left++]; // remove leftmost value, then advance left pointer
        }
    }
    return minLen == Integer.MAX_VALUE ? 0 : minLen;
}
```

</details>

---

### SW3 — Longest Session Without a Repeated Error Code (Variable Window)

**Prompt:** Given a string of error codes (one char per minute), find the length
of the longest substring without a repeated code.

**Why it comes up:** Longest substring without repeating characters — very common.

**Pseudo shape:**
```text
create map: char -> last seen index
left = 0, best = 0
for each right pointer:
    if char was seen inside current window:
        move left just after old position
    update last seen index for char
    update best length
```

**Kotlin**
```kotlin
fun longestSessionNoRepeat(log: String): Int {
    val lastSeen = mutableMapOf<Char, Int>()        // char → last seen index
    var left = 0
    var maxLen = 0

    for ((right, code) in log.withIndex()) { // withIndex() gives both the position and the character
        // If we've seen this code inside the current window, shrink from left
        val prevIndex = lastSeen[code]
        if (prevIndex != null && prevIndex >= left) {
            left = prevIndex + 1 // jump left past the previous duplicate
        }
        lastSeen[code] = right // latest position for this code
        maxLen = maxOf(maxLen, right - left + 1)
    }
    return maxLen
}

// Test:
println(longestSessionNoRepeat("ABCABCBB"))  // → 3 ("ABC")
println(longestSessionNoRepeat("AAABBB"))   // → 2 ("AB")
println(longestSessionNoRepeat("ABCDE"))    // → 5
```

**Kotlin (same problem, but using a set first):**
```kotlin
fun longestSessionNoRepeatWithSet(log: String): Int {
    val window = mutableSetOf<Char>() // chars currently inside the sliding window
    var left = 0
    var maxLen = 0

    for ((right, code) in log.withIndex()) {
        while (code in window) {
            window.remove(log[left]) // remove one char from the left side
            left++                   // move left step by step until duplicate is gone
        }

        window.add(code)             // now this char is safe to include
        maxLen = maxOf(maxLen, right - left + 1)
    }
    return maxLen
}

// Same tests:
println(longestSessionNoRepeatWithSet("ABCABCBB"))  // → 3
println(longestSessionNoRepeatWithSet("AAABBB"))    // → 2
println(longestSessionNoRepeatWithSet("ABCDE"))     // → 5
```

**Why the index map matters:**

If you only keep a `set`, you know that `"A"` is already inside the window, but you
do not know where the old `"A"` was.

Example with `ABCABCBB`:

- current window is `ABC`
- next char is another `A`
- with a `set`, you would need to keep removing from the left step by step until the old `A` is gone
- with `lastSeen['A'] = 0`, you can move `left` straight to `1`

That is why the map stores the last index, not just presence:

- a `set` tells you **that** a duplicate exists
- `lastSeen[char] = index` tells you **how far left must move**
- the `set` version is still valid, but it shrinks the window step by step
- the index-map version is usually the cleaner interview answer for this exact problem

<details>
<summary>Java version</summary>

```java
public int longestSessionNoRepeat(String log) {
    Map<Character, Integer> lastSeen = new HashMap<>();
    int left = 0;
    int maxLen = 0;

    for (int right = 0; right < log.length(); right++) {
        char code = log.charAt(right); // charAt(i) = character stored at index i
        Integer prevIndex = lastSeen.get(code);
        if (prevIndex != null && prevIndex >= left) {
            left = prevIndex + 1; // jump left past the previous duplicate
        }
        lastSeen.put(code, right); // remember latest index for this code
        maxLen = Math.max(maxLen, right - left + 1);
    }
    return maxLen;
}
```

</details>

---

## Two Pointers Problems

### TP1 — Two Sorted Payment Amounts That Hit an Exact Split

**Prompt:** Given a **sorted** list of payment amounts, find two that sum to exactly
`target`. Return their indices. Assume exactly one pair exists.

**Pseudo shape:**
```text
left = start, right = end
while left < right:
    sum both values
    if sum matches target: return answer
    if sum too small: move left rightward
    if sum too large: move right leftward
```

**Kotlin**
```kotlin
fun twoSumSorted(amounts: List<Int>, target: Int): Pair<Int, Int> {
    var left = 0
    var right = amounts.lastIndex

    while (left < right) {
        val sum = amounts[left] + amounts[right]
        when {
            sum == target -> return Pair(left, right)
            sum < target  -> left++     // need more — move left pointer right
            else          -> right--    // too much — move right pointer left
        }
    }
    error("No solution found")
}

// Test:
println(twoSumSorted(listOf(1000, 2000, 3000, 4000, 5000), 7000))  // → (1, 4)  [2000+5000]
println(twoSumSorted(listOf(1000, 2000, 3000, 4000, 5000), 5000))  // → (0, 3)  [1000+4000]
```

**Note:** Use Two Pointers only when the array is **sorted**. Use HashMap (H2) otherwise.

If the input is not sorted, you can still sort first and use two pointers, but that becomes
`O(n log n)`. If the prompt needs original indices, keep `(value, originalIndex)` pairs before
sorting. `LinkedHashMap` is not the tool for that; it preserves insertion order, but it does not
solve the "sorted values, original indices" problem.

**Kotlin (same problem, unsorted input):**
```kotlin
fun twoSumUnsorted(amounts: List<Int>, target: Int): Pair<Int, Int> {
    val seen = mutableMapOf<Int, Int>() // value -> original index

    for ((index, amount) in amounts.withIndex()) { // withIndex() gives (index, value) pairs
        val complement = target - amount
        val previousIndex = seen[complement]
        if (previousIndex != null) {
            return Pair(previousIndex, index) // found the earlier value that completes the target
        }
        seen[amount] = index // remember this value's original index for later elements
    }

    error("No solution found")
}

// Same idea, but now the input does not need to be sorted:
println(twoSumUnsorted(listOf(4000, 1000, 5000, 2000, 3000), 7000)) // → (0, 4)  [4000+3000]
println(twoSumUnsorted(listOf(4000, 1000, 5000, 2000, 3000), 6000)) // → (1, 2)  [1000+5000]
```

<details>
<summary>Java version</summary>

```java
public int[] twoSumSorted(List<Integer> amounts, int target) {
    int left = 0;
    int right = amounts.size() - 1;

    while (left < right) {
        int sum = amounts.get(left) + amounts.get(right); // current candidate built from the outer pair
        if (sum == target) {
            return new int[] { left, right };
        }
        if (sum < target) {
            left++; // need a larger sum, so move left toward bigger values
        } else {
            right--; // need a smaller sum, so move right toward smaller values
        }
    }
    throw new IllegalArgumentException("No solution found");
}
```

</details>

---

## Binary Search Problems

### BS1 — First Occurrence of a Target in Sorted Logs

**Prompt:** Given a sorted list of event IDs or payment amounts that may contain duplicates,
return the first index where `target` appears. Return `-1` if not found.

**Why it comes up:** This is the boundary-aware version of binary search. If you can solve
this one cleanly, the simpler "exact target search" version is straightforward too.

**Pseudo shape:**
```text
left = 0, right = n - 1, answer = -1
while left <= right:
    mid = middle index
    if mid is target:
        save mid in answer
        keep searching left half
    else move to the half that can still contain target
return answer
```

**Kotlin**
```kotlin
fun firstOccurrence(sorted: IntArray, target: Int): Int {
    var left = 0
    var right = sorted.lastIndex
    var answer = -1

    while (left <= right) {
        val mid = left + (right - left) / 2 // safe middle index without overflow habit
        when {
            sorted[mid] == target -> {
                answer = mid      // remember this match
                right = mid - 1   // keep searching left half for an earlier match
            }
            sorted[mid] < target -> left = mid + 1
            else -> right = mid - 1
        }
    }

    return answer
}

// Test:
println(firstOccurrence(intArrayOf(100, 200, 200, 200, 500), 200)) // → 1
println(firstOccurrence(intArrayOf(100, 200, 200, 200, 500), 300)) // → -1
```

<details>
<summary>Java version</summary>

```java
public int firstOccurrence(int[] sorted, int target) {
    int left = 0;
    int right = sorted.length - 1;
    int answer = -1;

    while (left <= right) {
        int mid = left + (right - left) / 2; // safe middle index without overflow habit
        if (sorted[mid] == target) {
            answer = mid; // remember this match
            right = mid - 1; // keep searching left half for an earlier match
        } else if (sorted[mid] < target) {
            left = mid + 1; // target can only be on the right side now
        } else {
            right = mid - 1; // target can only be on the left side now
        }
    }

    return answer;
}
```

</details>

**Common follow-up:** if the input is guaranteed to have unique values, you can return
immediately on the first match instead of continuing left.

---

### BS2 — Search in Rotated Sorted Array

**Prompt:** Given a sorted array that was rotated once, return the index of
`target`. Return `-1` if it does not exist.

**Why it comes up:** This is the binary-search version that actually shows up in
many online assessments. The real skill is not memorizing the answer, but
keeping the sorted-half invariant straight under pressure.

**Core move:**

- pick `mid`
- detect whether the left half or right half is sorted
- keep only the half that is both sorted and still able to contain the target

**What to say out loud:**

- "At every step, at least one half is still sorted."
- "I do not need the whole array sorted, only one half to be trustworthy."
- "The main bug risk is the boundary check, not the binary-search template itself."

**Complexity:** `O(log n)` time, `O(1)` space.

**Runnable companion:**

- revisit this as a binary-search hedge under time pressure

---

## Linked List Problems

### LL1 — Reverse a Singly Linked List

**Prompt:** Given the head of a singly linked list, reverse it and return the new head.

**Why it comes up:** This is the most common linked-list basic. It tests whether you can update pointers safely without losing the rest of the list.

**Pseudo shape:**
```text
prev = null
current = head
while current exists:
    save current.next
    point current.next to prev
    move prev forward
    move current forward
return prev as new head
```

**Kotlin**
```kotlin
data class ListNode(
    val value: Int,
    var next: ListNode? = null,
)

fun reverseList(head: ListNode?): ListNode? {
    var prev: ListNode? = null
    var current = head

    while (current != null) {
        val next = current.next // save the rest of the list before rewiring
        current.next = prev // reverse the pointer
        prev = current // move prev forward
        current = next // continue with the original next node
    }

    return prev
}
```

<details>
<summary>Java version</summary>

```java
class ListNode {
    int value;
    ListNode next;

    ListNode(int value) {
        this.value = value;
    }
}

public ListNode reverseList(ListNode head) {
    ListNode prev = null;
    ListNode current = head;

    while (current != null) {
        ListNode next = current.next; // save the rest of the list before rewiring
        current.next = prev; // reverse the pointer
        prev = current; // move prev forward
        current = next; // continue with the original next node
    }

    return prev;
}
```

</details>

**Common follow-ups:**
- find the middle node with `slow` and `fast`
- detect a cycle with `slow` and `fast`
- reverse nodes in pairs or in groups of `k`

**Why this still matters outside pure linked-list questions:**
- one classic collision strategy in hash tables is `separate chaining`
- you are usually not asked to implement a full `HashMap`, but interviewers may still ask why linked structures matter inside buckets
- the point is not memorizing internals, but showing that you understand how entries can be traversed safely when many keys share one bucket

---

### LL2 — Remove the K-th Node from the End in One Pass

**Prompt:** Given the head of a singly linked list and an integer `k`, remove the k-th node
from the end and return the new head.

**Why it comes up:** Classic follow-up after reverse list. Tests dummy-node use and whether
you can make two pointers keep a fixed gap.

**Pseudo shape:**
```text
create dummy before head
set fast and slow to dummy
move fast k steps ahead
move both pointers until fast reaches the end
slow is now just before the target node
unlink target node
return dummy.next
```

**Kotlin**
```kotlin
fun removeKthFromEnd(head: ListNode?, k: Int): ListNode? {
    val dummy = ListNode(0, head) // dummy helps when the removed node is the original head
    var fast: ListNode? = dummy
    var slow: ListNode? = dummy

    repeat(k) {
        fast = fast?.next // move fast k steps ahead so the gap stays fixed
    }

    while (fast?.next != null) {
        fast = fast?.next // move both pointers together
        slow = slow?.next // slow stays just before the node we will remove
    }

    slow?.next = slow?.next?.next // unlink the target node
    return dummy.next
}
```

<details>
<summary>Java version</summary>

```java
public ListNode removeKthFromEnd(ListNode head, int k) {
    ListNode dummy = new ListNode(0);
    dummy.next = head; // dummy helps when the removed node is the original head

    ListNode fast = dummy;
    ListNode slow = dummy;

    for (int i = 0; i < k; i++) {
        fast = fast.next; // move fast k steps ahead so the gap stays fixed
    }

    while (fast.next != null) {
        fast = fast.next; // move both pointers together
        slow = slow.next; // slow stays just before the node we will remove
    }

    slow.next = slow.next.next; // unlink the target node
    return dummy.next;
}
```

</details>

---

## Stack / Deque Problems

### SD1 — Validate Properly Nested Brackets

**Prompt:** Given a string containing `()[]{}`, return `true` if brackets are properly
nested and closed in the correct order.

**Why it comes up:** This is the canonical stack problem. It also tests whether you really
know how to use `ArrayDeque` / `Deque`.

**Pseudo shape:**
```text
create empty stack
for each character:
    if opening bracket: push
    if closing bracket:
        if stack empty: fail
        pop latest opening bracket
        if it does not match: fail
return stack is empty
```

**Kotlin**
```kotlin
fun isProperlyNested(text: String): Boolean {
    val stack = ArrayDeque<Char>()
    val matching = mapOf(')' to '(', ']' to '[', '}' to '{')

    for (ch in text) {
        when (ch) {
            '(', '[', '{' -> stack.addLast(ch) // push opening bracket
            ')', ']', '}' -> {
                if (stack.isEmpty()) return false
                if (stack.removeLast() != matching[ch]) return false // pop latest opening bracket and compare
            }
        }
    }

    return stack.isEmpty() // every opening bracket must have been matched
}
```

<details>
<summary>Java version</summary>

```java
public boolean isProperlyNested(String text) {
    Deque<Character> stack = new ArrayDeque<>();
    Map<Character, Character> matching = Map.of(
        ')', '(',
        ']', '[',
        '}', '{'
    ); // Map.of(...) is immutable, which is fine for a fixed lookup table

    // toCharArray() lets us loop through the string one character at a time.
    for (char ch : text.toCharArray()) {
        if (ch == '(' || ch == '[' || ch == '{') {
            stack.push(ch); // push(...) adds to the top of the stack
        } else if (ch == ')' || ch == ']' || ch == '}') {
            if (stack.isEmpty()) return false;
            // pop() removes top item; matching.get(ch) returns the opening bracket that should match.
            if (stack.pop() != matching.get(ch)) return false;
        }
    }

    return stack.isEmpty(); // every opening bracket must have been matched
}
```

</details>

---

### SD2 — Min-Tracking Stack

**Prompt:** Implement a stack that supports `push`, `pop`, and `getMin` in `O(1)` time.

**Why it comes up:** Common interview variant once the interviewer knows you can use a basic stack.
Also useful for explaining why one structure can track another in parallel.

**Pseudo shape:**
```text
keep one normal stack for values
keep one stack for current minima
push:
    push into values
    also push into mins when value is a new minimum
pop:
    pop from values
    if popped value equals current minimum, pop mins too
getMin:
    read top of mins
```

**Kotlin**
```kotlin
class MinStack {
    private val values = ArrayDeque<Int>()
    private val mins = ArrayDeque<Int>()

    fun push(value: Int) {
        values.addLast(value) // main stack
        if (mins.isEmpty() || value <= mins.last()) {
            mins.addLast(value) // push duplicate minima too, so pop() keeps min correct
        }
    }

    fun pop(): Int {
        val removed = values.removeLast()
        if (removed == mins.last()) {
            mins.removeLast() // remove matching min when the current minimum leaves
        }
        return removed
    }

    fun getMin(): Int = mins.last() // last() = current minimum on the min stack top
}
```

<details>
<summary>Java version</summary>

```java
public final class MinStack {
    private final Deque<Integer> values = new ArrayDeque<>();
    private final Deque<Integer> mins = new ArrayDeque<>();

    public void push(int value) {
        values.push(value); // main stack
        if (mins.isEmpty() || value <= mins.peek()) {
            mins.push(value); // push duplicate minima too, so pop() keeps min correct
        }
    }

    public int pop() {
        int removed = values.pop();
        if (removed == mins.peek()) {
            mins.pop(); // remove matching min when the current minimum leaves
        }
        return removed;
    }

    public int getMin() {
        return mins.peek(); // peek() reads current minimum without removing it
    }
}
```

</details>

---

### SD3 — Basic Calculator II

**Prompt:** Evaluate a string expression containing non-negative integers,
spaces, and the operators `+`, `-`, `*`, `/`.

**Why it comes up:** This is one of the best live-coding stack exercises because
it mixes parsing, operator precedence, and off-by-one mistakes at the end of the
scan.

**Core move:**

- build the current number digit by digit
- when you hit an operator, commit the previous number
- push `+` and `-` terms directly
- fold `*` and `/` immediately against the previous stack value

**What to say out loud:**

- "I only need one pass."
- "Multiplication and division are resolved immediately so the stack only keeps additive terms."
- "The usual bug is forgetting to flush the last number."

**Complexity:** `O(n)` time, `O(n)` space in the worst case.

**Runnable companion:**

- parser and operator-precedence drills of this shape

---

### SD4 — Basic Calculator

**Prompt:** Evaluate an expression with `+`, `-`, spaces, and parentheses.

**Why it comes up:** This is the stronger parser follow-up after `Calculator II`.
The arithmetic is simple, but the state management is easier to break during a
live interview.

**Core move:**

- keep `result`, `number`, and current `sign`
- when you see `(`, push the outer `result` and `sign`
- when you see `)`, finish the inner expression and merge it back into the outer state

**What to say out loud:**

- "The stack stores deferred outer state, not all numbers."
- "This is mostly a state-machine exercise."
- "I want to test nested parentheses and leading negatives immediately."

**Complexity:** `O(n)` time, `O(n)` space in the worst case for nested parentheses.

**Runnable companion:**

- same family as parentheses and state-stack expression parsing

---

## Priority Queue / Heap Problems

### PQ1 — Top K Frequent Event Types

**Prompt:** Given a list of event types, return the `k` most frequent ones.

**Why it comes up:** This is one of the most common heap-style interview questions.

**Pseudo shape:**
```text
count frequency of each event
create min-heap ordered by frequency
for each frequency entry:
    push entry into heap
    if heap grows beyond k:
        remove smallest frequency
read remaining heap entries into result
reverse if needed
```

**Kotlin**
```kotlin
fun topKFrequentEvents(events: List<String>, k: Int): List<String> {
    val count = events.groupingBy { it }.eachCount()
    val heap = PriorityQueue(compareBy<Map.Entry<String, Int>> { it.value })
    // Min-heap: smallest frequency stays on top.

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

// Test:
println(topKFrequentEvents(listOf("PAYMENT", "ORDER", "PAYMENT", "REFUND", "PAYMENT", "ORDER"), 2))
// → [PAYMENT, ORDER]
```

<details>
<summary>Java version</summary>

```java
public List<String> topKFrequentEvents(List<String> events, int k) {
    Map<String, Integer> count = new HashMap<>();
    for (String event : events) {
        count.put(event, count.getOrDefault(event, 0) + 1);
    }

    PriorityQueue<Map.Entry<String, Integer>> heap =
        new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
    // Min-heap: smallest frequency stays on top.

    for (Map.Entry<String, Integer> entry : count.entrySet()) {
        heap.offer(entry); // offer() pushes one entry into the heap
        if (heap.size() > k) {
            heap.poll();   // poll() returns and removes the smallest frequency
        }
    }

    List<String> result = new ArrayList<>();
    while (!heap.isEmpty()) {
        result.add(heap.poll().getKey()); // get key from the removed heap-top entry
    }
    Collections.reverse(result); // min-heap gives smallest-first, so reverse to return largest-first
    return result;
}
```

</details>

**Complexity:** `O(n log k)`, which is the whole point of the heap here. You do not sort everything.

**Heap method reminder:**

- `offer()` in Java adds one element into the heap
- `peek()` reads the heap-top element without removing it
- `poll()` returns and removes the heap-top element
- in a min-heap, `poll()` removes the smallest item currently stored
- iterating a `PriorityQueue` does not give sorted order; repeated `poll()` does

---

### PQ2 — Find Median From Data Stream

**Prompt:** Design a structure that supports:

- `addNum(int num)`
- `findMedian()`

**Why it comes up:** This is the heap question most worth practicing for live
coding. It forces you to defend the invariant, not just the API.

**Core move:**

- keep the lower half in a max-heap
- keep the upper half in a min-heap
- rebalance so sizes differ by at most `1`
- expose the top or the average of the tops as the median

**What to say out loud:**

- "Every number belongs either in the lower half or the upper half."
- "The balance invariant matters as much as the insertion rule."
- "I am trading `O(log n)` updates for `O(1)` median reads."

**Complexity:** `O(log n)` per insert, `O(1)` per median read.

**Runnable companion:**

- same family as running median and two-heap balancing

---

## Data-Structure Design Problems

### D1 — LRU Cache

**Prompt:** Implement an `LRUCache` with:

- `get(key)` in `O(1)`
- `put(key, value)` in `O(1)`

**Why it comes up:** This is the most valuable design-style coding problem in the
set because it tests both data-structure choice and implementation discipline.

**Core move:**

- `HashMap` from key to node
- doubly linked list for recency order
- move touched nodes to the front
- evict from the tail when capacity is exceeded

**What to say out loud:**

- "The map gives direct access; the list gives ordering."
- "LinkedHashMap is fine to mention, but I should still be able to explain the real structure."
- "The bug risk is pointer manipulation during detach and attach."

**Complexity:** `O(1)` average time for `get` and `put`, `O(capacity)` space.

**Runnable companion:**

- same family as LRU cache and map-plus-linked-structure design

---

### D2 — Sliding Window Rate Limiter

**Prompt:** Design a simple limiter that allows at most `N` requests in the last
`W` seconds.

**Why it comes up:** This is not a classic LeetCode puzzle, but it is a much
better backend-flavored live-coding discussion than another exotic tree problem.

**Core move:**

- keep accepted timestamps in a queue
- evict timestamps that are outside the active window
- reject when the remaining queue size already hits capacity

**What to say out loud:**

- "This in-memory version assumes timestamps arrive in order."
- "For one process this is enough; for distributed enforcement I would need shared state or sharding."
- "The queue is only storing the active window, not all history."

**Complexity:** `O(1)` amortized per request, `O(active window size)` space.

**Runnable companion:**

- same family as queue/window-based throttling design

---

## Interval Problems

### I1 — Merge Overlapping Time Windows

**Prompt:** Given a list of time windows `[start, end]`, merge all overlaps and return the
minimal non-overlapping set.

**Why it comes up:** Intervals are common in scheduling, inventory holds, booking slots,
maintenance windows, and campaign time ranges.

**Pseudo shape:**
```text
if input empty: return empty result
sort windows by start
start merged result with first window
for each remaining window:
    if it overlaps last merged window:
        extend last merged end
    else:
        append new merged window
```

**Kotlin**
```kotlin
data class TimeWindow(var start: Int, var end: Int)

fun mergeTimeWindows(windows: List<TimeWindow>): List<TimeWindow> {
    if (windows.isEmpty()) return emptyList() // emptyList() = immutable empty list

    val sorted = windows.sortedBy { it.start } // sortedBy { ... } returns a new list sorted by that field
    val merged = mutableListOf(TimeWindow(sorted[0].start, sorted[0].end))

    for (window in sorted.drop(1)) { // drop(1) = all items except the first one
        val last = merged.last() // last merged window so far
        if (window.start <= last.end) {
            last.end = maxOf(last.end, window.end) // overlap: extend current merged window
        } else {
            merged.add(TimeWindow(window.start, window.end)) // no overlap: start a new merged window
        }
    }

    return merged
}

// Test:
println(mergeTimeWindows(listOf(TimeWindow(1, 3), TimeWindow(2, 6), TimeWindow(8, 10))))
// → [(1,6), (8,10)]
```

<details>
<summary>Java version</summary>

```java
class TimeWindow {
    int start;
    int end;

    TimeWindow(int start, int end) {
        this.start = start;
        this.end = end;
    }
}

public List<TimeWindow> mergeTimeWindows(List<TimeWindow> windows) {
    if (windows.isEmpty()) return List.of(); // List.of() = immutable empty list

    List<TimeWindow> sorted = new ArrayList<>(windows);
    sorted.sort(Comparator.comparingInt(window -> window.start)); // sort by start time first

    List<TimeWindow> merged = new ArrayList<>();
    merged.add(new TimeWindow(sorted.get(0).start, sorted.get(0).end));

    for (int i = 1; i < sorted.size(); i++) {
        TimeWindow current = sorted.get(i);
        TimeWindow last = merged.get(merged.size() - 1); // last merged window so far

        if (current.start <= last.end) {
            last.end = Math.max(last.end, current.end); // overlap: extend current merged window
        } else {
            merged.add(new TimeWindow(current.start, current.end)); // no overlap: start a new merged window
        }
    }

    return merged;
}
```

</details>

---

## BFS / DFS Problems

### G1 — Find All Services Affected by a Failure (DFS)

**Prompt:** Given a service dependency graph (Map<String, List<String>>), a failed
service, and the graph, return all services that will be impacted (directly or
transitively depend on the failed service). Use DFS.

**Pseudo shape:**
```text
reverse the graph so dependency -> dependents
create visited set
dfs from failed service in reversed graph
each newly visited dependent is affected
return visited affected services
```

**Kotlin**
```kotlin
fun affectedServices(
    dependsOn: Map<String, List<String>>,  // service → list of services it calls
    failed: String,
): Set<String> {
    // Reverse the graph: for each service, who depends on it?
    val dependedOnBy = mutableMapOf<String, MutableList<String>>()
    for ((service, deps) in dependsOn) {
        for (dep in deps) {
            // getOrPut() returns existing list or creates one if missing.
            dependedOnBy.getOrPut(dep) { mutableListOf() }.add(service)
        }
    }

    // DFS from the failed node upward
    val affected = mutableSetOf<String>()   // Kotlin mutable set for visited dependents
    fun dfs(node: String) {
        // emptyList() = use no dependents when the key is missing.
        for (dependent in dependedOnBy[node] ?: emptyList()) {
            if (affected.add(dependent)) dfs(dependent) // recurse only on first visit
        }
    }
    dfs(failed)
    return affected
}

// Test:
val deps = mapOf(
    "checkout"   to listOf("inventory", "payment"),
    "payment"    to listOf("fraud-check"),
    "fraud-check" to listOf("risk-db"),
    "inventory"  to listOf("warehouse-db"),
)
println(affectedServices(deps, "risk-db"))   // → {fraud-check, payment, checkout}
println(affectedServices(deps, "warehouse-db")) // → {inventory, checkout}
```

<details>
<summary>Java version</summary>

```java
public Set<String> affectedServices(
    Map<String, List<String>> dependsOn,
    String failed
) {
    Map<String, List<String>> dependedOnBy = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : dependsOn.entrySet()) {
        String service = entry.getKey();
        for (String dep : entry.getValue()) {
            // computeIfAbsent() creates the list only when the key is missing.
            dependedOnBy.computeIfAbsent(dep, ignored -> new ArrayList<>()).add(service);
        }
    }

    Set<String> affected = new HashSet<>(); // visited set doubles as the final answer
    dfsAffected(failed, dependedOnBy, affected); // walk outward from the failed dependency
    return affected;
}

private void dfsAffected(
    String node,
    Map<String, List<String>> dependedOnBy,
    Set<String> affected
) {
    // getOrDefault(...) uses empty list when nobody depends on this node.
    for (String dependent : dependedOnBy.getOrDefault(node, List.of())) {
        if (affected.add(dependent)) {
            dfsAffected(dependent, dependedOnBy, affected); // recurse only on first visit
        }
    }
}
```

</details>

---

### G2 — Shortest Service Call Chain (BFS)

**Prompt:** Given a directed service graph, find the minimum number of hops to
get from service A to service B. Return -1 if unreachable.

**Pseudo shape:**
```text
if source equals target: return 0
queue = [(source, 0)]
visited = {source}
while queue not empty:
    pop oldest node and distance
    for each neighbor:
        if neighbor is target: return distance + 1
        if unseen: mark visited and enqueue
return -1
```

**Kotlin**
```kotlin
fun shortestCallChain(
    graph: Map<String, List<String>>,
    from: String,
    to: String,
): Int {
    if (from == to) return 0
    val queue = ArrayDeque<Pair<String, Int>>()  // Kotlin queue of (node, distance)
    val visited = mutableSetOf(from)             // Kotlin mutable set for visited nodes

    queue.add(Pair(from, 0))                     // start at source with distance 0

    while (queue.isNotEmpty()) {
        val (node, dist) = queue.removeFirst()   // remove oldest node from the queue
        // emptyList() = use no neighbors when the key is missing.
        for (neighbor in graph[node] ?: emptyList()) {
            if (neighbor == to) return dist + 1
            if (visited.add(neighbor)) queue.add(Pair(neighbor, dist + 1)) // visit once, then enqueue
        }
    }
    return -1
}

// Test:
val graph = mapOf(
    "checkout" to listOf("inventory", "payment"),
    "payment"  to listOf("fraud-check", "bank"),
    "inventory" to listOf("warehouse"),
    "fraud-check" to listOf("bank"),
)
println(shortestCallChain(graph, "checkout", "bank"))      // → 2  (checkout→payment→bank)
println(shortestCallChain(graph, "checkout", "warehouse")) // → 2
println(shortestCallChain(graph, "bank", "checkout"))      // → -1  (no reverse path)
```

<details>
<summary>Java version</summary>

```java
public int shortestCallChain(
    Map<String, List<String>> graph,
    String from,
    String to
) {
    if (from.equals(to)) return 0;

    Deque<Map.Entry<String, Integer>> queue = new ArrayDeque<>();
    Set<String> visited = new HashSet<>();
    visited.add(from);
    queue.add(Map.entry(from, 0)); // Map.entry(...) is a compact immutable pair-like holder in Java

    while (!queue.isEmpty()) {
        Map.Entry<String, Integer> current = queue.removeFirst();
        String node = current.getKey();
        int dist = current.getValue(); // number of hops taken to reach this node

        // getOrDefault(...) uses empty list when node has no outgoing edges.
        for (String neighbor : graph.getOrDefault(node, List.of())) {
            if (neighbor.equals(to)) return dist + 1;
            if (visited.add(neighbor)) {
                queue.add(Map.entry(neighbor, dist + 1)); // enqueue unseen neighbor with one more hop
            }
        }
    }
    return -1;
}
```

</details>

---

### G3 — Detect Circular Service Dependency (DFS + cycle detection)

**Prompt:** Given a service dependency graph, return `true` if there is a circular
dependency (A → B → C → A). This would cause infinite recursion or startup failure.

**Pseudo shape:**
```text
for each unvisited node:
    run dfs
dfs(node):
    mark node as visited and in current path
    for each neighbor:
        if neighbor already in current path: cycle
        if neighbor unseen and dfs(neighbor): cycle
    remove node from current path
return whether any dfs found a cycle
```

**Kotlin**
```kotlin
fun hasCircularDependency(graph: Map<String, List<String>>): Boolean {
    val visited = mutableSetOf<String>()     // nodes seen in any DFS branch
    val inStack = mutableSetOf<String>()  // current DFS path

    fun dfs(node: String): Boolean {
        visited.add(node)   // node has now been explored at least once
        inStack.add(node)   // node is in the current DFS path
        for (neighbor in graph[node] ?: emptyList()) {
            if (neighbor in inStack) return true          // back-edge = cycle
            if (neighbor !in visited && dfs(neighbor)) return true
        }
        inStack.remove(node) // backtrack: node leaves the current DFS path
        return false
    }

    return graph.keys.any { it !in visited && dfs(it) }  // check all disconnected components too
}

// Test:
val noCycle = mapOf("A" to listOf("B"), "B" to listOf("C"), "C" to emptyList<String>())
val cycle   = mapOf("A" to listOf("B"), "B" to listOf("C"), "C" to listOf("A"))
println(hasCircularDependency(noCycle))  // → false
println(hasCircularDependency(cycle))    // → true
```

<details>
<summary>Java version</summary>

```java
public boolean hasCircularDependency(Map<String, List<String>> graph) {
    Set<String> visited = new HashSet<>();
    Set<String> inStack = new HashSet<>();

    for (String node : graph.keySet()) {
        if (!visited.contains(node) && dfsCycle(node, graph, visited, inStack)) { // cover disconnected components too
            return true;
        }
    }
    return false;
}

private boolean dfsCycle(
    String node,
    Map<String, List<String>> graph,
    Set<String> visited,
    Set<String> inStack
) {
    visited.add(node);
    inStack.add(node); // mark node as part of the current DFS path

    for (String neighbor : graph.getOrDefault(node, List.of())) {
        if (inStack.contains(neighbor)) return true; // back-edge to current path = cycle
        if (!visited.contains(neighbor) && dfsCycle(neighbor, graph, visited, inStack)) {
            return true;
        }
    }

    inStack.remove(node); // backtrack: this node is no longer in the current DFS path
    return false;
}
```

</details>

---

### G4 — Number of Islands

**Prompt:** Given a `0/1` grid, return how many disconnected land masses
(`1`) it contains.

**Why it comes up:** This is the grid-traversal problem most worth keeping warm
for online assessments. It is simple enough to finish under pressure, but rich
enough to expose traversal bugs immediately.

**Core move:**

- scan every cell
- when you hit unseen land, count one island
- DFS or BFS from that cell to mark the full component as visited

**What to say out loud:**

- "This is connected-components counting on a grid."
- "The actual trick is safe boundary handling, not advanced graph theory."
- "I can mark visited either in a separate matrix or by mutating the grid."

**Complexity:** `O(rows * cols)` time, `O(rows * cols)` worst-case stack or queue usage.

**Runnable companion:**

- same family as BFS/DFS island counting and flood fill

---

## Dynamic Programming Problems

### DP1 — Coin Change

**Prompt:** Given coin denominations and a target amount, return the minimum
number of coins needed, or `-1` if impossible.

**Why it comes up:** This is the highest-return DP drill for the current loop.
It is still small enough for an OA, and it lets you explain the jump from brute
force to bottom-up reuse clearly.

**Core move:**

- let `dp[a]` mean the minimum coins needed for amount `a`
- initialize impossible values to a sentinel
- for each amount, try every coin and reuse `dp[a - coin]`

**What to say out loud:**

- "The repeated subproblem is the best answer for smaller amounts."
- "I want a sentinel that makes impossible states obvious."
- "If I am short on time, I would still mention the brute-force recursion first, then code bottom-up."

**Complexity:** `O(amount * number of coins)` time, `O(amount)` space.

**Runnable companion:**

- same family as bottom-up DP change-making

---

## Machine-Coding Problems

### MC1 — Payment Source Prioritizer

**Prompt:** Implement allocation for a payment amount across prioritized funding
sources, for example:

- gift balance first
- wallet balance second
- card last

Return the allocation steps or fail if total funds are insufficient.

**Why it comes up:** This is closer to real backend coding than another pure
puzzle. It tests clean modeling, order of application, and failure handling.

**Core move:**

- keep sources in explicit priority order
- consume from each source only up to the remaining amount
- stop once fully allocated
- fail cleanly if you still have remainder after the last source

**What to say out loud:**

- "The priority order is business logic, so I want it explicit."
- "I am not over-generalizing this into a framework."
- "If the interviewer pushes, I can discuss partial rollback, persistence, and idempotency next."

**Complexity:** `O(number of sources)` time, `O(number of allocation steps)` output space.

**Runnable companion:**

- same family as greedy allocation under priority rules

---

### MC2 — Idempotent Wallet Ledger

**Prompt:** Implement a wallet update function that:

- applies a request only once per `requestId`
- returns the same result on retries
- rejects debits that would make balance negative

**Why it comes up:** This is a very interview-useful "payments correctness"
exercise. The coding part is simple; the follow-up discussion is where the value
is.

**Core move:**

- store the first result per `requestId`
- return that stored result on duplicates
- update balance only for the first successful application
- store rejected outcomes too, so retries stay deterministic

**What to say out loud:**

- "Idempotency means the same request does not create a second state change."
- "A rejected request should also be replay-safe."
- "In production, this state would need a durable transaction boundary, not just memory."

**Complexity:** `O(1)` average lookup and apply time, `O(number of processed requests)` space.

**Runnable companion:**

- same family as idempotent ledger and request-key state updates

---

## SQL Drills

Write these queries from memory. Time yourself — target < 3 minutes each.

### SQL1 — Top 3 Products per Category (Last 30 Days)

**Query shape:**
```text
join products with recent order_items
group by product inside each category
rank products within each category by total sold
keep only ranks 1 to 3
```

```sql
-- Products table: id, name, category
-- Order_items table: product_id, quantity, ordered_at

SELECT category, name, total_sold
FROM (
    SELECT
        p.category,
        p.name,
        SUM(oi.quantity)                                      AS total_sold,
        RANK() OVER (PARTITION BY p.category ORDER BY SUM(oi.quantity) DESC) AS rk
    FROM products p
    JOIN order_items oi ON oi.product_id = p.id
    WHERE oi.ordered_at >= NOW() - INTERVAL '30 days'
    GROUP BY p.category, p.id, p.name
) ranked
WHERE rk <= 3
ORDER BY category, rk;
```

**Pattern:** Window function with `RANK() OVER (PARTITION BY ...)`. Needed for
"top N per group" — cannot do this with plain `ORDER BY + LIMIT`.

---

### SQL2 — Orders Where ALL Items Are Shipped

**Query shape:**
```text
start from each order
reject any order that still has a non-shipped item
return orders that survive that check
```

```sql
-- orders: id
-- order_items: order_id, status  ('PENDING' | 'SHIPPED' | 'CANCELLED')

SELECT o.id
FROM orders o
WHERE NOT EXISTS (
    SELECT 1
    FROM order_items oi
    WHERE oi.order_id = o.id
      AND oi.status != 'SHIPPED'
);
-- Alternative with HAVING:
SELECT order_id
FROM order_items
GROUP BY order_id
HAVING COUNT(*) = COUNT(CASE WHEN status = 'SHIPPED' THEN 1 END);
```

**Pattern:** `NOT EXISTS` subquery or `HAVING COUNT(*) = COUNT(condition)`.
Know both — interviewers sometimes ask "can you do it another way?"

---

### SQL3 — Customers Inactive for 90+ Days

**Query shape:**
```text
left join customers to orders
group by customer
compute latest order date
keep customers whose latest order is older than 90 days
also keep customers with no orders at all
```

```sql
-- customers: id, email
-- orders: id, customer_id, created_at

SELECT c.id, c.email, MAX(o.created_at) AS last_order
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id
GROUP BY c.id, c.email
HAVING MAX(o.created_at) < NOW() - INTERVAL '90 days'
    OR MAX(o.created_at) IS NULL;     -- include customers who never ordered
```

**Pattern:** `LEFT JOIN + GROUP BY + HAVING`. The `IS NULL` case (never ordered)
is the edge case interviewers look for.

---

### SQL4 — N+1 Fix in JPQL (Kotlin and Java / Spring Data)

**Query shape:**
```text
broken version:
    load parent rows
    lazily read children one parent at a time
fixed version:
    fetch parent and child data together
or tell JPA up front which child graph to load
```

**Kotlin**
```kotlin
// BROKEN — N+1: loads orders then issues 1 query per order for items
val orders = orderRepository.findAll()
orders.forEach { order ->
    println(order.items.size)   // LazyInitializationException or N queries
}

// FIXED — JOIN FETCH: 1 query loads everything
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.status = :status")
fun findByStatusWithItems(@Param("status") status: String): List<Order>

// FIXED — @EntityGraph alternative (no JPQL needed)
@EntityGraph(attributePaths = ["items"])
fun findByStatus(status: String): List<Order>

// FIXED — batch size (N queries → N/batchSize queries)
// In application.yml:
// spring.jpa.properties.hibernate.default_batch_fetch_size: 100
```

<details>
<summary>Java version</summary>

```java
// BROKEN — N+1: loads orders then issues 1 query per order for items
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    System.out.println(order.getItems().size());
}

// FIXED — JOIN FETCH: 1 query loads everything
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.status = :status")
List<Order> findByStatusWithItems(@Param("status") String status);

// FIXED — @EntityGraph alternative (no JPQL needed)
@EntityGraph(attributePaths = {"items"})
List<Order> findByStatus(String status);
```

</details>

---

### SQL5 — SELECT FOR UPDATE (Inventory Deduct Without Overselling)

**Query shape:**
```text
start transaction
read product row with write lock
check stock
deduct stock
commit
```

**Kotlin**
```kotlin
// The pattern you reach for when pessimistic locking is needed
@Transactional
fun deductStock(productId: Long, quantity: Int) {
    // Locks the row — no other transaction can read/update until this commits
    val product = em.createQuery(
        "SELECT p FROM Product p WHERE p.id = :id",
        Product::class.java
    )
    .setLockMode(LockModeType.PESSIMISTIC_WRITE)   // SELECT ... FOR UPDATE
    .setParameter("id", productId)
    .singleResult

    check(product.stock >= quantity) { "Insufficient stock" }
    product.stock -= quantity
}

// Spring Data equivalent:
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
fun findByIdForUpdate(@Param("id") id: Long): Product?
```

<details>
<summary>Java version</summary>

```java
@Transactional
public void deductStock(Long productId, int quantity) {
    Product product = em.createQuery(
            "SELECT p FROM Product p WHERE p.id = :id",
            Product.class
        )
        .setLockMode(LockModeType.PESSIMISTIC_WRITE) // lock the row so concurrent writers cannot oversell
        .setParameter("id", productId)
        .getSingleResult();

    if (product.getStock() < quantity) {
        throw new IllegalStateException("Insufficient stock");
    }
    product.setStock(product.getStock() - quantity); // update happens while the row is still locked
}

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Product findByIdForUpdate(@Param("id") Long id);
```

</details>

**When to use:** Low-contention writes where you need to guarantee no lost updates.
For high-contention (flash sale), prefer Redis `DECR` instead — `SELECT FOR UPDATE`
under high load creates a queue that can timeout.

---

## Complexity Quick-Reference

| Algorithm | Time | Space | When |
|---|---|---|---|
| HashMap lookup | O(1) avg | O(n) | Dedup, frequency count, complement trick |
| Fixed sliding window | O(n) | O(1) | Max/min/sum over K elements |
| Variable sliding window | O(n) | O(k) | Shortest/longest satisfying condition |
| Two pointers (sorted) | O(n) | O(1) | Two-sum on sorted array |
| BFS | O(V+E) | O(V) | Shortest path, level-order |
| DFS | O(V+E) | O(V) | Reachability, cycle detection, all paths |

---

## Interview Tips — Language-Specific

**Kotlin: use stdlib, don't reinvent:**

```kotlin
// Don't write a counter loop
val freq = list.groupingBy { it }.eachCount()

// Don't write a filter+map chain manually
val result = items.filter { it.active }.map { it.sku }

// Don't write your own min/max tracking
val peak = windows.maxOf { it.sum() }

// Use destructuring in for loops
for ((index, value) in array.withIndex()) { ... }
for ((key, value) in map) { ... }
```

**Kotlin container defaults:**

```kotlin
val queue = ArrayDeque<String>()
queue.addLast("A")      // enqueue (to back)
queue.removeFirst()     // dequeue (from front)   — BFS

val stack = ArrayDeque<String>()
stack.addLast("A")      // push
stack.removeLast()      // pop                    — DFS iterative
```

- `ArrayDeque` is the normal Kotlin default for queue and stack work.
- Kotlin `ArrayDeque` is not thread-safe.
- Kotlin does not have a dedicated stdlib linked list for interview problems.
- If the problem is a linked-list problem, define `ListNode` directly instead of trying to force a collection type into it.
- `groupingBy { }.eachCount()` is Kotlin's short way to build a frequency map.
- `withIndex()` gives you `(index, value)` pairs while iterating.

<details>
<summary>Java container defaults</summary>

```java
Deque<String> queue = new ArrayDeque<>();
queue.addLast("A");     // enqueue (to back)
queue.removeFirst();    // dequeue (from front)   — BFS

Deque<String> stack = new ArrayDeque<>();
stack.push("A");        // push onto stack top
stack.pop();            // pop from stack top     — DFS iterative

LinkedList<String> linked = new LinkedList<>();
linked.add("A");
linked.add("B");
// Java LinkedList = doubly linked list, but ArrayDeque is the usual stack/queue default
```

- `ArrayDeque` is the normal Java default for stack and queue operations in interviews.
- `ArrayDeque` does not allow `null`, and it is not thread-safe.
- `LinkedList` is useful to recognize, but it is usually not the best first choice for stack/queue problems.
- `Stack` is legacy. Prefer `Deque` / `ArrayDeque`.

</details>

<details>
<summary>Java priority queue default</summary>

```java
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
minHeap.offer(5);
minHeap.offer(1);
minHeap.offer(3);
minHeap.peek(); // 1 = read smallest item without removing it
minHeap.poll(); // 1 = smallest item first, not first item inserted
```

- a normal queue is FIFO
- a priority queue removes by priority, usually smallest-first in Java unless you reverse the comparator
- `PriorityQueue` is the right default for heap-style interview problems like `top k`, `merge k sorted lists`, or "always process smallest/earliest next"

</details>

**Concurrency note for containers:**

- `ArrayDeque`, `LinkedList`, and `PriorityQueue` are normal single-threaded defaults; do not call them thread-safe in an interview
- for multi-threaded FIFO work on the JVM, know `ConcurrentLinkedQueue` (`offer`/`poll` are non-blocking)
- for multi-threaded stack/deque work, know `ConcurrentLinkedDeque`
- for producer-consumer handoff where threads should wait, know `BlockingQueue` implementations like `LinkedBlockingQueue` or `ArrayBlockingQueue` (`put`/`take` can block)
- for multi-threaded priority ordering, know `PriorityBlockingQueue`
- in Kotlin/JVM, you still usually use these `java.util.concurrent` collections for the concurrent versions

**Kotlin solutions are shorter than Java — use it when allowed:**
- No `new ArrayList<>()` → `mutableListOf()`
- No `map.getOrDefault(k, 0) + 1` → `(map[k] ?: 0) + 1`
- No `Collections.min/max` → `.min()` / `.max()`
- Destructuring, `when`, extension functions — all fair game in interviews
