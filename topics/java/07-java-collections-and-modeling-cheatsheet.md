# Java Collections and Modeling Cheat Sheet

Use this note when the question is not "what is the API?" but "what is the
right default in normal backend code?"

This is the level that matters in reviews, interviews, and day-to-day design:

- which collection shape matches the access pattern
- when mutability is a bug magnet
- when wrapper types are required
- why `BigDecimal` exists in money code

Shortest rule:

- choose by access pattern and correctness need, not by habit
- return interfaces, not concrete mutable implementation types
- keep mutability narrow

---

## 1. Strong Defaults Worth Remembering

- prefer `List`, `Set`, and `Map` in method signatures, not `ArrayList`, `HashSet`, or `HashMap`
- prefer `ArrayList` over `LinkedList` for normal list work
- prefer `HashMap` when the real need is lookup by key
- prefer `HashSet` when the real need is uniqueness or membership checks
- prefer `ArrayDeque` for stack or queue behavior
- prefer `int` over `Integer` unless `null`, generics, or frameworks require the wrapper
- prefer immutable snapshots at API boundaries with `List.copyOf`, `Set.copyOf`, and `Map.copyOf`
- prefer `BigDecimal` for money amounts and other exact decimal business values

If you remember only one sentence:

> Pick the structure by the operation you need most, and make mutation someone
> else's problem only when it truly must be shared.

---

## 2. Bad Mental Model vs Better Mental Model

Bad mental model:

> `ArrayList` is the normal container, so I start there unless performance forces something else.

Better mental model:

> First ask whether I need lookup by key, uniqueness, order, queueing, stack behavior, sorted access, or only iteration.

Bad mental model:

> Returning `ArrayList` is fine because callers also want a list.

Better mental model:

> Returning the `List` interface hides implementation details and makes it easier to keep the result immutable.

Bad mental model:

> `Integer` is just `int` with extra syntax.

Better mental model:

> `Integer` is an object wrapper. Use it when object semantics are required, not as a default for ordinary arithmetic state.

---

## 3. `HashMap` vs `ArrayList`

Use `HashMap<K, V>` when the primary question is:

- "give me the value for this key"
- "update the value for this key"
- "check whether this key already exists"

Use `ArrayList<E>` when the primary question is:

- "keep these items in sequence"
- "append items and iterate over them"
- "access by index"

Do not replace a keyed lookup problem with a list scan unless the collection is
tiny and that tradeoff is deliberate.

Example:

- product by `productId` -> `HashMap<String, Product>`
- ordered line items in a checkout request -> `ArrayList<LineItem>` or just `List<LineItem>`

Short rule:

> If you keep searching a list to find one item by ID, the data probably wants a
> map.

Useful related defaults:

- use `LinkedHashMap` when stable insertion order matters
- use `TreeMap` when sorted keys or range-style navigation matter

---

## 4. `Set` vs `List`

Use `Set<E>` when duplicates are not meaningful and membership matters.

Good uses:

- processed webhook IDs
- allowed roles
- visited nodes in a graph traversal

Use `List<E>` when duplicates and order both matter.

Good uses:

- ordered response items
- cart lines
- event timeline entries

Practical question:

> If I add the same item twice, should the meaning change?

If the answer is no, a `Set` is often the better fit.

Common defaults:

- `HashSet` for membership and uniqueness
- `LinkedHashSet` when insertion order matters too
- `TreeSet` when sorted uniqueness matters

---

## 5. `Queue`, `Deque`, and "Stack" In Modern Java

Use the abstraction that matches the behavior:

- `Queue`: first-in-first-out processing
- `Deque`: add and remove at both ends
- stack behavior: usually model with `Deque`, not with legacy `Stack`

Good backend fits:

- job intake buffer -> `Queue`
- producer-consumer handoff -> `BlockingQueue`
- breadth-first traversal -> `Queue`
- depth-first traversal or expression parsing -> `Deque` used as a stack

Strong default:

```java
Deque<String> stack = new ArrayDeque<>();
Queue<String> queue = new ArrayDeque<>();
```

Why `ArrayDeque` is the usual default:

- simple
- fast for queue and stack operations
- avoids the old `Stack` class shape

Short rule:

> In modern Java, "stack" usually means `Deque` with `push`, `pop`, and `peek`,
> not `java.util.Stack`.

---

## 6. Why `LinkedList` Is Rarely The Right Default

`LinkedList` sounds attractive because inserts and deletes at known node
positions are cheap in theory.

But in ordinary application code, what usually dominates is not theoretical
pointer surgery.
It is:

- traversal cost
- allocation overhead
- cache-unfriendly access
- the fact that most code does not already hold the target node reference

That is why the normal default is still:

- `ArrayList` for list behavior
- `ArrayDeque` for queue or stack behavior

Use `LinkedList` only when you truly need that specific linked structure and
can explain why.

Short rule:

> If you are reaching for `LinkedList` by reflex, stop and ask whether you
> really mean `ArrayList` or `ArrayDeque`.

---

## 7. Trees, Heaps, Queues, and Stacks In Plain English

You do not need deep algorithms theory to use these well.
You need the reuse rule.

Use a tree-shaped structure when:

- sorted order matters
- range queries matter
- hierarchy is the real domain shape

Common backend examples:

- `TreeMap` for sorted keys
- category hierarchy as an explicit tree in domain code

Use a heap / `PriorityQueue` when:

- you repeatedly need the next highest or lowest priority item
- full sorting all items every time would be wasteful

Common backend examples:

- top `k` most frequent items
- next retry due soonest
- highest-priority job next

Use a queue when:

- fairness or arrival order matters
- work should be processed in FIFO order

Use stack behavior when:

- the newest nested context should be processed first
- parsing or backtracking needs last-in-first-out behavior

The interview-level version and the practical version are the same here:

> choose by retrieval rule, not by class name familiarity.

---

## 8. `int` vs `Integer`

Prefer `int` when:

- the value is required
- there is no valid `null` meaning
- you are doing ordinary arithmetic or counters

Use `Integer` when:

- a framework or generic type requires an object
- `null` is a meaningful state
- the value must live inside a collection like `List<Integer>`

Weak default:

```java
Integer retryCount = 0;
```

Stronger default:

```java
int retryCount = 0;
```

Use `Integer` deliberately, not decoratively.

One practical caution:

- if `null` really means "unknown" or "not provided yet", model that intentionally
- if `null` only means "we did not choose a sensible default", fix the model

---

## 9. Immutable Return Types At API Boundaries

Bad habit:

```java
public ArrayList<OrderLine> lines() { ... }
```

Why this is weak:

- callers now know the concrete implementation
- callers may assume mutation is allowed and meaningful
- you tie your API to one storage choice

Stronger boundary:

```java
public List<OrderLine> lines() {
    return List.copyOf(lines);
}
```

Why this is better:

- the signature exposes the abstraction, not the storage detail
- callers receive a snapshot they should not mutate
- internal representation can change later without breaking the API contract

Good rule:

> Return the narrowest useful abstraction and copy when you need a stable
> immutable boundary.

Useful factory methods worth keeping ready:

- `List.of(...)`
- `Set.of(...)`
- `Map.of(...)`
- `List.copyOf(existing)`
- `Set.copyOf(existing)`
- `Map.copyOf(existing)`

---

## 10. Why `BigDecimal` For Money

Money logic usually wants exact decimal behavior, explicit rounding, and stable
business rules.

That is why `BigDecimal` exists in backend code for:

- prices
- taxes
- discounts
- settlements
- ledger-style calculations

Why `double` is a weak default for money:

- binary floating-point is good for many numeric tasks
- it is a poor fit when the business meaning is exact decimal value plus explicit rounding policy

Good rule:

> For money, the important question is not only "number". It is "exact decimal
> value with explicit rounding semantics".

Small example:

```java
record PriceLine(String sku, BigDecimal amount) {}
```

Practical cautions:

- define rounding mode where business rules require it
- keep currency explicit in the model when multiple currencies exist
- avoid constructing business-critical decimal values from imprecise floating-point input

---

## 11. Small Modern Java Shapes Worth Using

For data carriers:

```java
public record ProductView(String id, String name, BigDecimal price) {}
```

For immutable small collections:

```java
List<String> roles = List.of("ADMIN", "OPS");
Map<String, Integer> limits = Map.of("search", 20, "checkout", 5);
```

For stack or queue behavior:

```java
Deque<Long> stack = new ArrayDeque<>();
Queue<Long> queue = new ArrayDeque<>();
```

For a safe return boundary:

```java
return List.copyOf(results);
```

These are the kinds of modern Java defaults that improve both readability and
review quality.

---

## 12. Optional Kotlin Comparison

If Kotlin helps you reopen the idea faster:

- Java `record` maps roughly to a Kotlin `data class` for simple immutable data carriers
- Java `List.of` is close in spirit to Kotlin `listOf`
- Java `int` maps roughly to Kotlin non-null `Int`
- Java `Integer` is closer to Kotlin nullable `Int?` at the modeling level, though the runtime details are not identical

Do not let the comparison become the main topic.
The important part is still the data-shape decision.

---

## 13. Short Takeaways

- `ArrayList` for ordinary ordered lists
- `HashMap` for lookup by key
- `HashSet` for uniqueness and membership
- `ArrayDeque` for stack and queue behavior
- `LinkedList` rarely as the first choice
- `int` when nullability is not part of the model
- `Integer` when object or nullable semantics are required
- `BigDecimal` for exact decimal business values such as money
- return `List`, `Set`, `Map`, not concrete mutable implementation types
- copy at boundaries when mutation should stop there

## 14. Further Reading

- Java `java.util` package overview: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/package-summary.html
- Java `List` API: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html
- Java `Map` API: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html
- Java `Queue` API: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Queue.html
- Java `Stack` API: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Stack.html
- Java `BigDecimal` API: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html
