# Core Algorithm Patterns

Use this note to recognize the shape of a problem before choosing a data structure. The [drills](./02-coding-round-drills.md) provide prompts and solution guides; this note owns the reusable reasoning.

## Start With the Invariant

An invariant is the rule that must stay true after each step. For a window without repeated values, it might be: every value between `left` and `right` is unique. State the rule, then choose the smallest structure that maintains it.

Before coding, identify the input, output, constraints, edge cases, and whether mutation or approximate results are allowed. After coding, check that each step preserves the invariant and state the time and space cost.

## Choose the Pattern

| Problem shape | Useful pattern | Rule to preserve | Common failure |
| --- | --- | --- | --- |
| Find, count, group, or aggregate by key | Set or map | Each key represents the intended identity | Wrong equality rule or overwriting duplicates |
| Inspect a contiguous range | Sliding window | Current state describes exactly the active range | Moving a boundary without updating its state |
| Scan sorted input from both ends | Two pointers | The discarded region cannot contain the answer | Applying it to unsorted input |
| Find a boundary in sorted data | Binary search | The possible answer remains inside the search interval | Off-by-one at the first or last match |
| Match nested or last-opened items | Stack or deque | Top item is the next one to close or process | Losing order when popping |
| Repeatedly choose the smallest or largest item | Heap or priority queue | Root is the next eligible item | Assuming insertion order is retained |
| Explore reachable nodes or a shortest unweighted path | DFS or BFS | Visited state prevents repeated work | Marking a node too late and revisiting it |
| Reuse solutions to smaller states | Dynamic programming | Each state has a precise meaning and is solved once | Caching by an incomplete state key |

## A Few Decision Details

### Maps and sets

A set answers “have I seen this key?” A map can also hold a count, list, running total, or best value per key. Hash-based lookup is usually constant time on average; it can degrade with collisions, and key equality must match the problem's identity rule. If order matters, choose an ordered structure or retain the original order separately.

### Windows and pointers

A fixed window has a known width: add the new item and remove the old one. A variable window grows until a condition fails or succeeds, then moves its left boundary. A simple variable window relies on a property that changes predictably as the window grows or shrinks; negative values can invalidate a sum-based shortcut.

Two pointers often need sorted input or another monotonic property. If neither exists, a map or another method may be more appropriate.

### Search and traversal

Binary search needs a sorted sequence or a monotonic true/false condition. Decide whether the answer is any match, the first match, or the first valid boundary before choosing how to move the bounds.

BFS explores one distance layer at a time, so it finds a shortest path when every edge has equal cost. DFS is useful for reachability and cycle detection. Both need a clear visited rule; a directed-cycle check also needs to distinguish nodes still on the active path from nodes already completed.

### Heaps and dynamic programming

A heap helps when the next smallest or largest item matters repeatedly. It does not sort the whole collection for free. For `top k`, compare the cost of sorting all items with maintaining a heap of size `k`.

Dynamic programming helps when the same well-defined state is reached through different paths. Define the state, base case, transition, and computation order first. A greedy choice is valid only if you can justify why that local choice cannot block a better final result.

## Practical Loop

1. Explain the problem in plain words and name the invariant.
2. Try a small normal case and one awkward case: empty, repeated, first, last, or impossible.
3. Choose the structure that makes each step easy to keep correct.
4. Write or review the [matching drill](./02-coding-round-drills.md).
5. Check the invariant, complexity, and what changes if input grows or concurrent writers appear.

For production flows, distinguish an in-memory exercise from durable correctness. A local set can detect duplicates in one process; it cannot by itself protect a payment or event handler across retries and restarts. Use the [idempotency note](../databases/01-idempotency-and-transaction-safety.md) for that boundary.
