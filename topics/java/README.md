# Java Refresh for Backend Engineers

Use this folder as a practical Java refresh when your mental model is strongest
in Java `8` or `11` and needs updating for modern backend work.

Focus:

- JVM memory and garbage collection behavior
- concurrency, visibility, and the Java Memory Model (`JMM`)
- language features that matter in day-to-day backend code
- production concurrency issues such as pool saturation and request overload

Working style:

- explain runtime behavior before language trivia
- keep concurrency tied to real backend failure modes instead of academic examples
- prefer the Java `17-21` baseline that is most useful in current backend work
- treat records, sealed types, pattern matching, and virtual threads as the modern stable baseline to reopen first

## Recommended Order

1. [01-jvm-memory-and-gc.md](./01-jvm-memory-and-gc.md): heap, stack, metaspace, allocation, and GC behavior
2. [02-java-concurrency-and-jmm.md](./02-java-concurrency-and-jmm.md): race conditions, visibility, `volatile`, locks, atomics, executors, and the `JMM`, meaning the rules that decide when one thread can see another thread's writes
3. [03-modern-java-for-backend-engineers.md](./03-modern-java-for-backend-engineers.md): records, sealed types, pattern matching, and modern backend-friendly language features
4. [04-modern-java-21-plus-notes.md](./04-modern-java-21-plus-notes.md): structured concurrency, scoped values, and newer post-21 awareness
5. [05-concurrency-in-production.md](./05-concurrency-in-production.md): pool saturation, admission control, request budgets, and when local locks stop being enough

## Working Loop

1. refresh one document at a time
2. map each topic to real systems you already know
3. keep one or two short explanations per topic
4. stop when the mental model feels current again

If time is limited:

1. read `03` first
2. read `02` second
3. read `05` third
4. read `01` fourth if JVM/runtime behavior feels rusty
5. treat `04` as awareness material after the stable baseline is warm

## Companion Lab

Use [../../labs/java-modern-features/README.md](../../labs/java-modern-features/README.md)
for a small Java `21` example focused on records, sealed types, pattern
matching, and virtual threads.

Use `VirtualThreadsRequestBudgetLab` inside that lab when you want the
backend-specific virtual-thread example rather than only the language-feature
walkthrough.

For concurrency drills with runnable output, also use
[../../labs/kotlin-backend-examples/README.md](../../labs/kotlin-backend-examples/README.md)
topics `jvm/concurrency` and `jvm/concurrency-production`.

For the Kotlin coroutine model, use
[../kotlin/03-kotlin-coroutines-for-backend.md](../kotlin/03-kotlin-coroutines-for-backend.md).

## Stable Modern Baseline

The modern Java backend baseline worth treating as normal and usable today is:

- records
- sealed types
- pattern matching for `switch`
- virtual threads

That is the part to internalize first.

Treat these as secondary awareness until the baseline above feels natural:

- structured concurrency
- scoped values

Why:

- the first group already changes day-to-day modeling and concurrency style
- the second group is useful, but you do not need it before you can write or review modern backend code well

## If You Are A Spring Boot Engineer

After this folder, reopen these next:

1. [../spring-boot/01-spring-boot-fast-review.md](../spring-boot/01-spring-boot-fast-review.md)
2. [../spring-boot/03-transactions-and-isolation.md](../spring-boot/03-transactions-and-isolation.md)
3. [../spring-boot/04-jpa-hibernate-performance-traps.md](../spring-boot/04-jpa-hibernate-performance-traps.md)

That is where the Java refresh reconnects to:

- request handling
- transactions
- persistence behavior
- runtime tradeoffs in real services

## Core Rule

- prefer depth on a few high-value topics over shallow coverage of everything
- memory, concurrency, and modern language features give the highest return
- keep the baseline practical: Java `17-21`
