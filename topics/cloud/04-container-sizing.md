# Size Containers From Workload Evidence

Container size affects placement, cost, latency, and failure under load.
There is no universal CPU or memory value for a service. Start with a
reasonable allocation, test the expected workload, and refine it from
observed bottlenecks.

## Know What Is Being Limited

In Kubernetes, a **request** helps schedule capacity and a **limit**
constrains use. A memory limit that is too low may cause a process to be
killed; a restrictive CPU limit may throttle work and increase latency.
Managed container services expose different controls, so translate the
principle rather than copying Kubernetes fields.

Process memory is more than a language heap. It can include thread stacks,
buffers, native libraries, runtime metadata, and agents. Leave room for
these when setting an internal heap ceiling or a container limit.

## A Practical Sizing Loop

1. Classify the work: CPU-heavy, memory-heavy, I/O-bound, or mixed. Include
   payload sizes, concurrency, caches, and background jobs.
2. Choose an initial allocation that allows the process to start and serve
   realistic traffic.
3. Warm the service, then test normal, peak, burst, and slow-dependency
   cases with representative data.
4. Observe memory working set, restarts, CPU use and throttling, request
   latency, errors, queue depth, and dependency wait time.
5. Adjust requests and limits, then repeat the test. Keep headroom for
   short peaks and for replacement capacity during a rollout.

For a JVM process, compare heap with total process memory and watch
garbage-collection pauses. For Go, Node.js, or Python, inspect their
worker, buffer, and runtime memory behavior too. Do not treat a quiet
development run as the production memory profile.

## Diagnose Before Adding Resources

| Symptom | Check first |
| --- | --- |
| Latency rises while CPU is low | Database pool wait, locks, queueing, or slow dependencies |
| Heap looks healthy but container exits | Non-heap memory, native buffers, worker count, or memory limit |
| p99 latency rises near a CPU limit | Throttling and burst behavior |
| Restarts occur mainly during rollout | Probe settings, startup time, and shutdown grace period |

These are hypotheses, not diagnoses. Use traces and metrics to confirm them.
Adding replicas can make a database or provider bottleneck worse. See
[observability](../devops/03-observability-and-monitoring.md) and
[capacity planning](../sre/05-capacity-planning-and-load-shedding.md).

## First Review

- What is normal and peak work, including dependent services?
- Which resource saturates first in a realistic test?
- How close does process memory get to the container ceiling?
- Is latency caused by CPU, waiting, or another shared limit?
- Will more instances help or overload a dependency?

For Kubernetes resource behavior, see
[runtime basics](../devops/04-kubernetes-crash-course.md).
