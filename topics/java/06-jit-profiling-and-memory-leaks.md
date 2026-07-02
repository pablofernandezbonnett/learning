# JIT, Profiling, and Memory Leaks for Backend Engineers

Use this note when the JVM is no longer just "the thing that runs Java" and you
need enough runtime judgment to diagnose real backend problems.

Why this matters:

- many production issues come from retention, allocation pressure, or hot code paths rather than from syntax mistakes
- "the heap is high" is not yet a diagnosis
- a senior backend engineer does not need to become a VM internals specialist, but does need a usable troubleshooting model

## Smallest Useful Mental Model

Three ideas matter most:

- `JIT` (`Just-In-Time`) compilation makes hot code run faster over time
- profiling measures where time, allocation, lock contention, or blocking actually go
- a memory leak in Java usually means objects are retained too long, not that native memory is "forgotten" in the C sense

That is enough to reopen most production investigations.

## Bad Mental Model vs Better Mental Model

Bad mental model:

- CPU high means Java is slow
- heap high means memory leak
- one benchmark result explains production

Better mental model:

- hot code changes shape as the `JIT` optimizes it
- memory pressure can come from live-set growth, allocation rate, caches, thread stacks, or off-heap usage
- production diagnosis starts with evidence, not with folklore

## Small Concrete Example

Imagine checkout latency rises after a release.

A weak response is:

- tune random JVM flags
- increase heap
- guess that GC is the whole problem

A stronger response is:

1. check latency, CPU, and GC behavior together
2. capture a `JFR` recording
3. inspect hot methods, allocation pressure, lock contention, and blocking
4. compare the live heap after GC, not only total heap usage

Oracle's `jcmd` documentation shows low-impact `JFR.start` and `JFR.dump`
commands for capturing a running recording.

Typical shape:

```bash
jcmd <pid> JFR.start name=learning settings=profile
jcmd <pid> JFR.dump name=learning filename=learning.jfr
```

That is often a better first move than jumping straight to heavy heap-dump work.

## Best Approach or Strong Default

Strong defaults:

- use `JFR` first for runtime evidence
- use heap dumps when retention really looks like the main problem
- compare allocation rate with post-GC live set
- separate CPU hot path problems from memory-retention problems
- treat caches, listeners, queues, thread pools, and large object graphs as normal leak suspects

Good first questions:

- is the live heap growing after full or major collections?
- are allocations exploding even if retained memory is stable?
- is one path producing many short-lived objects?
- are threads blocked on one dependency or lock?
- did the change widen the object graph kept in memory?

## What the JIT Means in Practice

You do not need compiler-phase depth here.

What matters is:

- hot code can run differently after warming up
- microbenchmarks can mislead if warm-up is weak
- production performance can shift when the request mix changes

Plain-English version:

- the JVM spends effort making important code paths faster, so performance is not static from process start to steady state

## What a Java Memory Leak Usually Looks Like

In backend systems, the common shapes are:

- cache entries that never expire
- maps keyed by request or tenant data that keep growing
- listeners or callbacks never deregistered
- large responses or documents retained in queues
- thread-local data that lives longer than expected

The practical sign is not "heap exists".
It is:

- memory stays live longer than the business need justifies

## Main Tradeoff or Failure Mode

The failure mode is skipping measurement and jumping straight into tuning.

Examples:

- changing GC before understanding retention
- increasing heap when the real problem is one hot allocation path
- calling everything a leak when the issue is only bursty short-lived allocation

A second failure is using very heavy diagnostics too early in production.

Strong default:

- low-impact recording first
- heavier artifacts only when the first evidence points there

## Practical Rule

Start with:

- metrics
- `JFR`
- one concrete hypothesis

Then decide whether the real issue is:

- hot code
- allocation pressure
- retention
- lock contention
- blocking on dependencies

## Reusable Takeaway

> A useful JVM investigation is not "tune the VM." It is "measure the runtime,
> identify whether the pain is hot code, allocation, retention, or blocking, and
> only then change something."

## Further Reading

- `jcmd` command reference: https://docs.oracle.com/en/java/javase/21/docs/specs/man/jcmd.html
- JDK Mission Control project: https://openjdk.org/projects/jmc/
- Java diagnostics guide overview: https://docs.oracle.com/en/java/javase/21/troubleshoot/
