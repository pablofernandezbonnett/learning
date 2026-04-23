# Container Sizing Cheatsheet

> Primary fit: `Shared core`


Use this after reading
[04-container-sizing-and-observability.md](./04-container-sizing-and-observability.md).

This is the short version to retain and reopen quickly.

---

## The Core Idea

Do not guess a fixed number.

Use this loop:

1. understand workload shape
2. pick a safe starting size
3. warm the service
4. run a realistic load test
5. watch the right signals
6. refine from data

That is the whole move.

---

## The 5 Things To Remember

1. **Memory kills, CPU slows.**
   Bad memory limit -> `OOMKilled`.
   Bad CPU limit -> throttling and latency spikes.

2. **Request is planning. Limit is the ceiling.**
   Request helps placement.
   Limit is the hard stop.

3. **Measure RSS / working set, not only language-level memory.**
   Heap is not the whole process.

4. **Do not size from averages.**
   Watch p95, p99, peaks, and burst behavior.

5. **Tune with symptoms, not with superstition.**
   High latency does not automatically mean "add CPU."

---

## First Questions To Ask

- Is the service CPU-heavy, memory-heavy, or I/O-heavy?
- What runtime is it using?
- What is the latency target?
- What is normal traffic vs peak traffic?
- Does it keep caches, large payloads, or background jobs in memory?
- Does it depend on DB, queues, or slow downstream APIs?

---

## Initial Practical Rules

- Set memory request near expected steady-state usage.
- Set memory limit above realistic peak with headroom.
- Set CPU request from normal demand.
- Use CPU limits carefully on latency-sensitive services.
- Do not set the runtime heap/ceiling equal to the container limit.

---

## What To Watch

### Container signals

- memory working set / RSS
- memory limit utilization
- restart count
- `OOMKilled`
- CPU usage
- CPU throttling

### Runtime signals

- heap after GC
- GC pause time
- thread count
- connection pool wait time
- goroutine / worker / event-loop backlog depending on runtime

### Service signals

- p95 and p99 latency
- error rate
- dependency latency
- queue lag

### Business signals

- checkout completion rate
- payment success rate
- order intake rate

---

## Fast Diagnosis Patterns

### Latency up, CPU low

Suspect:

- DB pool exhaustion
- slow downstream dependency
- queueing
- lock contention

### Heap looks fine, container dies

Suspect:

- non-heap memory
- thread stacks
- direct buffers
- sidecar / agent overhead

### CPU looks fine, p99 is bad

Suspect:

- CPU throttling
- dependency tail latency
- GC pauses

### Problems only during deploys

Suspect:

- readiness probe issues
- missing graceful shutdown
- too-short drain window

---

## Runtime Notes

### JVM

- Leave room for non-heap memory.
- Watch heap after GC, not only raw heap usage.
- `MaxRAMPercentage` is often safer than a naive fixed heap.

### Go

- Watch RSS and goroutine growth.

### Node.js

- V8 heap is not total process memory.

### Python

- Worker count changes memory shape fast.

---

## Short Answer Shape

Use this order:

1. workload shape
2. request vs limit
3. safe first allocation
4. load-test plan
5. metrics to watch
6. refinement loop

Good short answer:

> I would not guess a fixed size. I would classify the workload, set a safe
> first request and limit, run a realistic load test, and watch memory working
> set, OOMs, p99 latency, CPU throttling, and runtime-specific signals like GC
> pauses or pool wait time. Then I would move request close to stable usage and
> keep enough headroom in the limit for peaks and runtime overhead.

---

## Workload Lens

### Correctness-sensitive services

Lead with:

- correctness
- in-flight request safety
- graceful shutdown
- readiness
- success rate and queue lag

### Peak-traffic services

Lead with:

- peak traffic
- latency under burst
- intake rate
- downstream lag
- business and technical dashboards together
