# Concurrency Models: Java, Go, and Kotlin

Use this when you already know the basic concurrency primitives and now want the
practical comparison:

- shared-state threads
- goroutines and channels
- coroutines and suspension
- virtual threads on the JVM

The goal is not runtime trivia for its own sake.
The goal is to know which model fits which backend workload and what tradeoff
you inherit with it.

If you keep one line warm, keep this:

> different concurrency models are mainly different ways to handle waiting,
> coordination, and shared state under load

---

## 1. The First Big Split

The cleanest first distinction is:

- Java and Kotlin on the JVM often start from **shared memory**
- Go often pushes you toward **message passing**

`Shared memory` means several threads can touch the same in-process data
structure, so you need coordination such as `synchronized`, locks, or atomics.

`Message passing` means one worker sends data to another over an explicit
channel, so ownership is handed off more clearly instead of many threads
editing the same object directly.

Neither model is magic.
They just make different mistakes easier or harder.

---

## 2. Java And Kotlin: Shared Memory First

On the JVM, the default mental model is usually:

- several threads exist
- they can all access heap objects
- you protect critical sections explicitly

Why this is powerful:

- very flexible
- direct fit for many server applications
- mature libraries and tooling

Why this is risky:

- race conditions are easy to introduce
- deadlocks become possible if locks are taken in inconsistent order
- visibility bugs appear when one thread writes and another does not see the latest value

Short rule:

> shared memory is flexible, but correctness depends heavily on disciplined coordination

---

## 3. Go: Message Passing First

Go pushes a different default style:

- start many lightweight goroutines
- coordinate them with channels
- pass work and data explicitly

`Goroutine` means a lightweight concurrent task managed by the Go runtime,
not by the operating system directly.

`Channel` means a typed handoff point where one goroutine sends a value and
another receives it.

Why this is attractive:

- clearer ownership of work
- fewer shared-state bugs if the design follows the model well
- very strong fit for pipelines, fan-out workers, and network services

Why this is not free:

- channel-heavy code can still become hard to reason about
- not every problem becomes cleaner just because a channel exists
- shared state does not disappear completely; you still sometimes need mutexes

Short rule:

> Go nudges you toward explicit handoff and coordination, which often helps when many workers process independent tasks

---

## 4. What The "Lightweight Unit" Actually Is

These models differ not only in syntax, but also in what unit of work gets
scheduled.

| Feature | Java Platform Threads | Go Goroutines | Kotlin Coroutines | Java Virtual Threads |
| :--- | :--- | :--- | :--- | :--- |
| Managed by | OS kernel | Go runtime | Kotlin coroutine library | JVM |
| Main idea | one OS thread per task | many goroutines share fewer threads | function can suspend without blocking its thread | many lightweight threads share carrier threads |
| Best mental model | shared-state threads | message-passing workers | async composition with suspension | blocking-style code at much higher concurrency |
| Practical scale | thousands | very high | very high | very high |

The key practical difference is:

- platform threads are relatively expensive to keep blocked
- goroutines, coroutines, and virtual threads make waiting cheaper in different ways

---

## 5. Kotlin Coroutines

Kotlin coroutines are mainly an **async programming model**.

`Suspend` means a function can pause at a suspension point, such as `delay()` or
a non-blocking I/O boundary, without occupying the underlying thread the whole time.

When people mention a `state machine` here, they mean the compiler rewrites the
function so execution can pause and later continue with its local state restored.

Why this is useful:

- clear async flows
- structured concurrency, meaning child tasks belong to one parent scope
- good fit when one request fans out to several I/O-heavy calls

What it does not mean:

- not every coroutine is automatically fast
- blocking code inside coroutines is still blocking unless you move it to an appropriate dispatcher

Good fit:

- Android
- WebFlux or other non-blocking services
- orchestration-heavy backend flows

---

## 6. Go Goroutines

Go goroutines are mainly a **runtime-level concurrency model**.

People often describe this as an `M:N scheduler`.
That just means:

- many goroutines are multiplexed onto fewer OS threads
- the Go runtime decides which goroutine runs where

Why this matters:

- you can keep many concurrent tasks in flight cheaply
- blocked network work does not require one heavyweight OS thread per task
- worker-pool and pipeline style code often stays very direct

Good fit:

- network services
- infrastructure tooling
- worker pipelines
- high-concurrency I/O services

---

## 7. Java Virtual Threads

Java virtual threads are mainly a **runtime improvement for blocking-style JVM code**.

They keep the familiar thread-per-task style, but make that model viable at much
higher concurrency because the JVM schedules many virtual threads onto a smaller
set of carrier threads.

`Carrier thread` means the underlying platform thread that temporarily runs the
virtual thread.

Why this is attractive:

- existing Java code and APIs often need fewer changes
- simple blocking code scales much better than with only platform threads
- easier migration path for many Spring-style services

What still matters:

- locks can still contend
- database connections are still finite
- pinned threads and slow downstream systems can still hurt throughput

Short rule:

> virtual threads make blocking cheaper, but they do not remove coordination or capacity limits

---

## 8. A Practical Comparison

Scenario:

> one request needs stock from 500 stores

Old fixed-thread-pool Java:

- maybe only 50 threads are available
- the rest of the work waits in batches
- slow stores tie up worker threads

Kotlin coroutines:

- launch many coroutines
- suspend while waiting on non-blocking calls
- compose results cleanly if the stack is coroutine-friendly

Go goroutines:

- launch many goroutines
- collect results with channels
- strong fit when the workload already looks like a worker or pipeline problem

Java virtual threads:

- launch many lightweight threads
- keep blocking-style code
- strong fit when the system is mostly traditional JVM code and you want higher concurrency without a full async rewrite

---

## 9. Which One Should You Prefer

The useful answer is not ideological.
It depends on the workload and the surrounding stack.

Prefer classic JVM threads plus locks when:

- the concurrency level is modest
- the code is already simple and stable
- there is no real benefit from adopting a new model

Prefer Kotlin coroutines when:

- the service already uses coroutine-friendly libraries
- one request fans out into many I/O-heavy calls
- structured async composition matters more than thread-style familiarity

Prefer Go goroutines when:

- the system naturally looks like a concurrent worker or pipeline service
- message passing and explicit handoff make the design clearer
- you are already in a Go codebase or platform-heavy environment

Prefer Java virtual threads when:

- the service is already JVM-based
- the code is blocking-style and reasonably clean
- you want much higher concurrency without forcing everything into reactive style

---

## 10. Short Answer Shape

Good short answer:

> I choose the concurrency model based on the workload and the stack around it.
> Coroutines are strong for structured async orchestration. Goroutines are strong
> for worker-style and pipeline-style systems with explicit handoff. Virtual
> threads are strong when I want much higher concurrency on the JVM without
> rewriting everything into reactive code.
