# Choosing a Concurrency Model

Concurrency lets work overlap. The useful choice is about what the work does
while it waits, how tasks are cancelled, and which state they share. A lighter
task model can support more waiting operations; it does not make a database,
CPU, or remote provider faster.

## Ask Three Questions First

1. Is the workload mostly waiting for I/O or using CPU?
2. Which mutable state can several tasks touch, and where is its real owner?
3. How many tasks may be in flight, and what happens when the request is
   cancelled or its deadline expires?

| Model | Useful fit | Main caution |
| --- | --- | --- |
| Platform threads and bounded pools | Modest concurrency, blocking libraries, or CPU work | Blocked threads and queued work consume finite capacity |
| Java virtual threads | Many concurrent blocking-style I/O tasks | They do not speed up one task or increase downstream capacity |
| Kotlin coroutines | Coroutine-aware I/O and structured asynchronous composition | A blocking call still occupies a thread unless handled appropriately |
| Go goroutines | Concurrent workers or pipelines in a Go service | Channels do not remove shared-state races or the need for cancellation |

These models are options within a language and runtime, not architectural
labels for whole systems. Go code can use shared memory and mutexes; Java or
Kotlin code can pass messages between workers. Choose the model that fits the
libraries and operational limits already present.

## Bound Fan-Out Regardless of Model

Suppose one request asks 100 stores for stock. Launching 100 lightweight
tasks may reduce thread pressure, but it can still overload the HTTP client,
stores, or database. Set an overall deadline, limit concurrent calls, and
decide whether a partial result is acceptable. Cancel or stop unnecessary
work when the caller leaves, where the libraries support it.

For production pool limits, request budgets, and admission control, see
[concurrency in production](../java/05-concurrency-in-production.md).
For Java memory visibility and locks, see [Java concurrency and the
JMM](../java/02-java-concurrency-and-jmm.md). For Kotlin's suspending calls
and structured cancellation, see [coroutines for
backend](../kotlin/03-kotlin-coroutines-for-backend.md).

## Separate Local Coordination From Shared Truth

A mutex protects state inside one process. It cannot stop another replica
from updating the same stock row. Put the final inventory or payment rule at
the shared write boundary, often using a conditional database update or
transaction. See [database locks and
concurrency](../databases/02-database-locks-and-concurrency.md).

If the question is which *instance* should perform a background task, use
[leader election and leases](./12-consensus-and-leader-election.md). Choosing
one worker is a different problem from choosing a thread or coroutine model.

## Decision Rule

Choose the simplest task model supported by the stack, then bound in-flight
work and protect shared state at its owner. Higher concurrency is useful only
while downstream capacity and correctness hold.

## Further Reading

- [Java virtual threads](https://docs.oracle.com/en/java/javase/26/core/virtual-threads.html).
- [Kotlin coroutines basics](https://kotlinlang.org/docs/coroutines-basics.html).
