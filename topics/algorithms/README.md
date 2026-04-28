# Algorithms for Practical Problem Solving

Use this folder to reopen the algorithm patterns that are most useful in timed
coding, debugging, and data-structure reasoning.

This is not meant to become a full puzzle bank.
The goal is a small repeatable set with Java and Kotlin awareness.

One term that appears often in this folder is `invariant`.
Here it simply means the rule your current state must keep true after every
step, such as "the sliding window never contains duplicates" or "the left heap
never ends up smaller than the right heap by more than one item."

## Recommended Order

1. [01-core-patterns.md](./01-core-patterns.md): the highest-value DSA patterns with backend-flavored examples
2. [02-coding-round-drills.md](./02-coding-round-drills.md): worked drills, edge cases, and complexity discussion

## Working Loop

1. read one pattern family
2. solve 2-3 drills without looking
3. explain the invariant, meaning the rule your pointers, window, stack, or heap keeps true, and then explain the complexity
4. repeat the same set instead of sampling too widely

## Core Rule

- pattern recognition matters more than collecting many problems
- edge cases and explanation quality matter as much as the main idea
- one small repeatable set beats a wide but shallow problem bank
