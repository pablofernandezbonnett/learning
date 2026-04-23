# Container Sizing and Observability for Backend Engineers

> Primary fit: `Shared core`


Quick review version:

- [06-container-sizing-cheatsheet.md](./06-container-sizing-cheatsheet.md)

This document is about the practical cloud question many backend teams
eventually reach:

> How would you choose CPU and memory for a containerized service, and how would
> you know whether the numbers are right?

The goal is not to pretend there is a magic formula.

The goal is to build a sane decision loop:

- choose an initial size without guessing blindly
- measure the right signals
- recognize what kind of bottleneck you actually have
- explain the tradeoffs clearly

---

## 1. First Principle: Size From Workload, Not Folklore

Do not answer this question with:

- "I always give Spring Boot 1GB"
- "I always set 2 CPUs"
- "I just match whatever the last service used"

Start with the workload shape:

- is the service CPU-heavy, memory-heavy, or I/O-heavy
- is the runtime JVM, Go, Node, Python, or something else
- what is the concurrency model
- what are the latency targets
- what are the expected steady-state and peak traffic levels
- does the service keep caches, large payloads, or background jobs in memory
- does it talk to databases or queues that can create backpressure

Good senior sentence:

> I do not choose container size by habit. I start from workload shape, set a
> conservative first allocation, then validate it under load and adjust from
> telemetry.

---

## 2. Request vs Limit

In Kubernetes, the practical concepts are:

- **Request:** what the scheduler treats as the service's minimum expected need
- **Limit:** the hard cap the runtime enforces

General cloud translation:

- request-like value -> guaranteed or reserved capacity for placement
- limit-like value -> hard ceiling the process cannot exceed safely

In ECS/Fargate, the UI is different because you usually choose task-level CPU
and memory sizes rather than Kubernetes `requests` and `limits`.

The practical reasoning is still the same:

- choose a safe baseline
- load test it
- observe real usage
- refine from data

The most important difference:

- **Memory limit too low** -> the container can be killed (`OOMKilled`)
- **CPU limit too low** -> the service is throttled and latency rises

Practical rules:

- memory request should be close to the service's real steady-state need
- memory limit should sit above expected peak, but not absurdly above it
- CPU request should reflect normal demand
- CPU limits should be used carefully on latency-sensitive services because
  throttling can create ugly p99 spikes

Memory is the sharper edge.
CPU usually degrades.
Memory usually kills.

---

## 3. A Practical Sizing Loop

This is the practical process I would use and describe.

### Step 1. Start With a Conservative Baseline

Pick an initial size that is safe enough to run and observe.

Do not optimize for minimal cost on day one.

You need enough room to answer:

- what is the real idle footprint
- how much memory rises under normal traffic
- how much headroom remains under peak

### Step 2. Warm the Service Properly

Do not measure a cold service and call that the answer.

Warm-up matters because:

- JIT compilation changes JVM behavior
- caches fill
- connection pools stabilize
- traffic patterns become more realistic

### Step 3. Run a Load Test

Use realistic traffic with realistic payload sizes.

Good cases to include:

- normal steady-state load
- expected peak
- short burst above peak
- slow downstream dependency

Tools:

- `k6`
- JMeter
- Gatling

Related reading:

- [../testing/01-testing-strategies.md](../testing/01-testing-strategies.md)

### Step 4. Measure the Right Signals

Do not look only at CPU%.

You want:

- memory working set / RSS
- limit utilization
- restart count and `OOMKilled` events
- p95 and p99 latency
- error rate
- CPU throttling if limits are present
- thread count / worker count
- GC pause time for managed runtimes
- connection pool wait time
- queue lag or downstream latency where relevant

### Step 5. Tighten the Numbers

After observing real usage:

- move memory request near the stable working set
- leave headroom between request and limit for short spikes
- reduce waste if the service is dramatically overprovisioned
- increase memory if peaks are too close to the ceiling

Example shape:

- stable working set: ~420Mi
- peak during realistic load: ~610Mi
- no OOMs, no large GC regressions

Reasonable first refinement:

- memory request: `512Mi`
- memory limit: `768Mi` or `1Gi`

Not because the numbers are holy.
Because they reflect observed behavior plus headroom.

---

## 4. Memory Strategy

Memory sizing is not only "how much heap do my objects need."

The process memory of a service can include:

- managed heap
- non-heap metadata
- thread stacks
- buffers
- native libraries
- TLS/network overhead
- sidecars or agents if present

That is why you should not equate:

- application heap
- process RSS
- container limit

### Portable Rule

Do not set the runtime's internal memory ceiling equal to the container limit.

Always leave room for:

- runtime overhead
- concurrency overhead
- short-lived spikes
- monitoring/agent overhead

### Runtime Notes

#### JVM / Java / Kotlin

Heap is only part of total process memory.

Watch for:

- heap after GC
- GC pause time
- old-generation pressure
- thread count
- direct/off-heap memory

Portable JVM habit:

- cap heap below container memory
- keep explicit headroom for non-heap memory

Useful JVM-specific knobs when relevant:

- `-XX:MaxRAMPercentage`
- Native Memory Tracking for investigations

Related reading:

- [../java/01-jvm-memory-and-gc.md](../java/01-jvm-memory-and-gc.md)

#### Go

Go usually has a simpler runtime shape than the JVM, but the same practical
rule remains:

- measure RSS, not only language-level memory views
- watch goroutine growth
- watch buffer growth and retained objects

#### Node.js

Do not confuse V8 heap limits with total process memory.

Buffers, native modules, and process-level overhead still matter.

#### Python

Worker model matters a lot.

If you run multiple workers, memory can scale with worker count much faster than
people expect.

The language changes the memory profile.
It does not remove the need to measure it.

---

## 5. CPU Strategy

CPU needs a different mindset.

If memory is too low, the container can die.
If CPU is too low, the service is often just slower.

Practical rules:

- set CPU request from normal traffic behavior
- be cautious with hard CPU limits on latency-sensitive services
- if you use CPU limits, monitor throttling directly
- scale decisions should use latency and error signals, not CPU alone

Why:

- a service can have low CPU and terrible latency because the database is slow
- a service can have high CPU and still be healthy if latency is within target
- a service can hit CPU throttling while average CPU graphs look deceptively fine

Good sentence:

> I treat CPU as a latency and throughput question, not only a utilization
> question.

---

## 6. What To Monitor in Production

You want three layers of signals.

### A. Container / Node Signals

- memory working set / RSS
- memory limit utilization
- restart count
- `OOMKilled`
- CPU usage
- CPU throttling
- file descriptor pressure if relevant

### B. Runtime / Process Signals

- heap after GC
- GC pause time
- old-generation occupancy
- thread count
- connection pool usage and wait time
- goroutine count / event-loop lag / worker backlog depending on runtime

### C. Service and Business Signals

- request rate
- p95 and p99 latency
- 5xx or business-failure rate
- dependency latency
- queue lag
- payment success rate
- checkout completion rate
- order intake rate

The exact tool can be:

- CloudWatch
- Prometheus + Grafana
- Datadog
- New Relic

The signal set matters more than the vendor.

Related reading:

- [../devops/03-observability-and-monitoring.md](../devops/03-observability-and-monitoring.md)
- [../databases/03-sql-refresh-for-backend-engineers.md](../databases/03-sql-refresh-for-backend-engineers.md)

---

## 7. Common Failure Patterns

### Pattern 1. High Latency, Low CPU

Likely suspects:

- database connection pool exhaustion
- slow downstream dependency
- lock contention
- queueing in the application

Do not respond by blindly adding CPU.

### Pattern 2. Heap Looks Fine, Container Still Dies

Likely suspects:

- non-heap memory
- thread explosion
- direct buffers
- sidecar or agent overhead

This is common in JVM services where people only watch heap%.

### Pattern 3. CPU Looks Fine, But p99 Is Bad

Likely suspects:

- CPU throttling from a hard limit
- dependency tail latency
- GC pauses
- uneven traffic burst handling

### Pattern 4. Restarts Happen Only During Deployments

Likely suspects:

- bad readiness probes
- missing graceful shutdown
- too-short drain window
- long startup or cache warm-up

Related reading:

- [02-kubernetes-and-terraform-for-backend-engineers.md](./02-kubernetes-and-terraform-for-backend-engineers.md)

---

## 8. Common Sizing Questions

These are realistic questions for cloud-shaped backend roles even if your direct
production ownership is still growing.

- How would you choose memory and CPU for a new Spring Boot or Go service?
- What would you monitor before a traffic event or campaign?
- How do you distinguish memory leak, traffic spike, and DB bottleneck?
- Why is memory limit more dangerous than CPU limit?
- What would you alert on for a checkout or payment service?
- How do readiness probes and graceful shutdown affect in-flight requests?

---

## 9. A Strong Reasoning Pattern

Use this order:

1. clarify workload shape and SLOs
2. explain request vs limit
3. choose a conservative first allocation
4. describe the load test and warm-up plan
5. name the metrics you would watch
6. explain how you would refine the numbers

Example:

> I would not guess a fixed container size from habit. First I would classify
> the service: runtime, expected concurrency, latency SLO, dependency profile,
> and whether it keeps memory-heavy state like caches or large payloads. Then I
> would set a conservative first request and limit, run a realistic load test,
> and watch memory working set, restart/OOM events, p99 latency, CPU
> throttling, and any runtime-specific signals like GC pause time or connection
> pool wait time. After that, I would move the memory request close to the real
> steady-state usage and keep enough headroom in the limit for peaks and runtime
> overhead. For memory, I optimize for safety first because a bad limit kills
> the container. For CPU, I optimize for latency and throughput, and I am
> careful with hard limits because throttling can hide behind average CPU
> graphs.

---

## 10. What To Internalize

- memory sizing is a safety problem first and a cost problem second
- CPU sizing is a latency and throughput problem
- request and limit are planning tools, not magic values
- runtime internals matter, but the production signals matter more
- strong answers explain the measurement loop, not a fixed number

---

## 11. Related Reading

- [01-cloud-basics.md](./01-cloud-basics.md)
- [02-kubernetes-and-terraform-for-backend-engineers.md](./02-kubernetes-and-terraform-for-backend-engineers.md)
- [03-serverless-for-backend-engineers.md](./03-serverless-for-backend-engineers.md)
- [../devops/03-observability-and-monitoring.md](../devops/03-observability-and-monitoring.md)
- [../testing/01-testing-strategies.md](../testing/01-testing-strategies.md)
- [../java/01-jvm-memory-and-gc.md](../java/01-jvm-memory-and-gc.md)
- [../databases/03-sql-refresh-for-backend-engineers.md](../databases/03-sql-refresh-for-backend-engineers.md)
