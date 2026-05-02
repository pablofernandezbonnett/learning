# Java Refresh for Backend Engineers

Use this folder as a practical Java refresh when your mental model is strongest
in Java `8` or `11` and needs updating for modern backend work.

## Recommended Order

1. [01-jvm-memory-and-gc.md](./01-jvm-memory-and-gc.md): heap, stack, metaspace, allocation, and GC behavior
2. [02-java-concurrency-and-jmm.md](./02-java-concurrency-and-jmm.md): race conditions, visibility, `volatile`, locks, atomics, executors, and virtual threads
3. [03-modern-java-for-backend-engineers.md](./03-modern-java-for-backend-engineers.md): records, sealed types, pattern matching, and modern backend-friendly language features
4. [04-modern-java-21-plus-notes.md](./04-modern-java-21-plus-notes.md): structured concurrency, scoped values, and newer post-21 awareness
5. [05-concurrency-in-production.md](./05-concurrency-in-production.md): pool saturation, admission control, request budgets, and when local locks stop being enough

## Working Loop

1. refresh one document at a time
2. map each topic to real systems you already know
3. keep one or two short explanations per topic
4. stop when the mental model feels current again

If time is limited:

1. read `02` first
2. read `01` second
3. skim `03` last

## Companion Lab

Use [../../labs/java-modern-features/README.md](../../labs/java-modern-features/README.md)
for a small Java `21` example focused on records, sealed types, pattern
matching, and virtual threads.

For concurrency drills with runnable output, also use
[../../labs/kotlin-backend-examples/README.md](../../labs/kotlin-backend-examples/README.md)
topics `jvm/concurrency` and `jvm/concurrency-production`.

## Core Rule

- prefer depth on a few high-value topics over shallow coverage of everything
- memory, concurrency, and modern language features give the highest return
- keep the baseline practical: Java `17-21`
