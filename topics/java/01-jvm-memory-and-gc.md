# JVM Memory and GC for Backend Engineers

> Primary fit: `Shared core`


This is the memory refresher that matters most for real backend work and
production work.

The goal is not to become a JVM tuning specialist.

The goal is to understand how Java applications use memory, why GC behavior
changes under real load, and how to talk about memory problems clearly.

---

## 1. The Main Memory Areas

### Heap

The heap stores objects.

This is where most application memory pressure appears:

- request DTOs
- entity graphs
- cached objects
- collections that grow too much

### Stack

Each thread has its own stack.

The stack stores:

- method frames
- local variables
- references to heap objects

Rule:

> Large object graphs live on the heap. Method-local references live on the
> stack.

### Metaspace

Metaspace stores class metadata.

In modern Java it replaced PermGen.

Problems here usually come from:

- excessive class loading
- dynamic proxies
- repeated redeploy issues
- classloader leaks

### Native / Off-Heap Memory

Not all memory usage is heap usage.

A Java service can also consume memory through:

- direct byte buffers
- thread stacks
- native libraries
- the JVM itself

This matters when RSS is high but heap usage does not fully explain it.

---

## 2. Object Lifetime and Why It Matters

Most backend objects are short-lived:

- request objects
- temporary collections
- serialization buffers

Some objects become long-lived:

- caches
- session state
- static maps
- objects accidentally retained by listeners or background tasks

This matters because GC handles short-lived and long-lived objects differently.

Practical rule:

> High allocation is normal in Java. Unnecessary retention is what usually hurts.

---

## 3. Young Generation, Old Generation, and Promotion

You do not need to memorize every collector detail.

Keep this model fresh:

- new objects usually start in the young generation
- many die quickly and are cheap to collect
- objects that survive enough collections get promoted to the old generation

Why this matters:

- request-heavy services often allocate a lot but remain healthy
- caches or retained graphs increase old-generation pressure
- old-generation pressure usually hurts latency more

Short explanation:

> In backend services, high allocation rate is not automatically a problem.
> The bigger risk is unnecessary promotion and long-lived object retention.

---

## 4. What GC Is Really Doing

Garbage collection reclaims objects that are no longer reachable.

For backend engineers, the important points are:

- GC is normal, not a failure
- pauses still matter for latency-sensitive services
- allocation rate, live set size, and object lifetime shape GC behavior

You do not need to speak like a collector implementer.

You do need to understand why these patterns are dangerous:

- large in-memory caches without limits
- loading too much data into memory at once
- huge result sets
- background jobs that keep references forever

---

## 5. Common Memory Problems in Backend Systems

### Unbounded Collections

Examples:

- static maps
- in-memory deduplication sets
- caches without TTL or size limits

### Reading Too Much at Once

Examples:

- loading a full export into memory
- returning giant query results
- collecting streams into large lists unnecessarily

### Entity Graph Explosion

Examples:

- fetching too many associations
- serializing large graphs by mistake
- N+1 plus oversized object trees

### Too Many Threads

Every thread has a stack.

If thread count grows too much, memory usage grows too.

This is one reason virtual threads changed the conversation for blocking I/O.

### Hidden Retention

Examples:

- listeners never removed
- thread-local values kept too long
- executor tasks capturing large object graphs

---

## 6. What To Monitor

If a Java service looks memory-stressed, start with:

- heap usage after GC
- GC pause time
- allocation rate
- old-generation occupancy
- thread count
- direct memory if relevant

The goal is to answer:

- are we allocating too much
- are we retaining too much
- are pauses hurting latency
- are threads part of the problem

---

## 7. Practical Production Habits

- keep caches bounded
- page large queries
- stream large exports when possible
- avoid loading whole object graphs without need
- keep background jobs memory-aware
- treat thread count as a resource, not a default

Good senior rule:

> Memory problems are often design problems first and JVM problems second.

---

## 8. Kotlin and Go Comparison

### Kotlin

Kotlin on the JVM shares the same memory model and GC behavior as Java for most
backend work.

So the big memory concepts stay the same:

- heap pressure
- object retention
- thread count
- GC pauses

Kotlin helps readability and null-safety.
It does not remove the need to think about memory.

### Go

Go also uses garbage collection, but the runtime model is different:

- goroutines are much lighter than traditional Java platform threads
- escape analysis affects allocation behavior
- the runtime scheduler is built into the language runtime

Practical comparison:

> Java and Kotlin give you a richer JVM ecosystem and stronger enterprise tools.
> Go usually gives you a simpler runtime model, but you still need to think
> carefully about allocation, contention, and object retention.

---

## 9. Key Lines To Keep Ready

- "Most backend objects are short-lived. The real risk is retaining more data for longer than intended."
- "If heap stays high after GC, I start thinking about retention, caches, or large object graphs."
- "Too many threads can become a memory problem, not only a scheduling problem."
- "I treat memory issues as a combination of code shape, data shape, and workload shape."

---

## 10. Further Reading

- Java Virtual Threads: https://openjdk.org/jeps/444
- Java Records: https://openjdk.org/jeps/395
