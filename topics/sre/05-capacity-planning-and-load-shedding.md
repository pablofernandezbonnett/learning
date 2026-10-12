# Capacity Planning and Load Shedding

Capacity is the amount of useful work a system can complete before one
resource saturates and user outcomes degrade. Plan around the limiting
resource and a realistic peak, not only average CPU or replica count.

## Find the Limit

For one critical user journey, measure incoming work, completed work,
latency, errors, and saturation. Look at application workers, database
connections, queues, memory, external provider quotas, and network limits.
Headroom is spare capacity for spikes and failures.

Example: adding API replicas may increase requests to a database whose
connection pool is already full. The visible service scales out, but
checkout gets slower. The first bottleneck, not the easiest component to
scale, sets the practical limit.

Use realistic peak traffic and a load test to find the point where latency
or errors rise sharply. Recheck after major query, dependency, or traffic
changes. Averages hide hot tenants and short bursts.

## Protect the Critical Work

- **Bound work:** cap payloads, query breadth, concurrency, and waiting queues.
- **Load shedding:** reject less important new work before it consumes
  capacity needed by critical operations.
- **Graceful degradation:** omit an optional recommendation or stale-safe
  enrichment while preserving checkout correctness.
- **Backpressure:** slow or stop upstream work when consumers cannot keep up.
- **Retry control:** use deadlines, limited retries, and backoff so a
  dependency slowdown does not create a retry storm.

Rejecting work deliberately can be safer than accepting everything and then
timing out. For HTTP, a caller-specific limit may use `429`; service-wide
unavailability may use `503`. Give a useful `Retry-After` when the caller
can safely retry. The exact policy follows the operation and overload cause.

## Repeated Expensive Reads

If many callers ask for the same safe read, a short cache can reuse a
completed answer. Request coalescing lets matching callers share one
in-progress computation. Build the sharing key from normalized input and
the caller's access scope, so one tenant cannot receive another's data.

Sharing work does not remove incoming connections or make unlimited
different queries cheap. Cap waiting callers and concurrent requests as
well. Protect the public entry point with request-size, connection, and
time limits; use per-caller limits when one client can exhaust shared
resources.

## Example: Flash Sale

Before a campaign, estimate the peak checkout rate and the database and
payment-provider limits behind it. Reserve headroom for the critical path,
pre-scale where measurements support it, and define which optional work can
pause. During the event, watch checkout success, latency, pool saturation,
queue age, and rejected work. If demand exceeds safe capacity, shed
non-critical work before the whole flow collapses.

## First Pass

1. Pick one critical operation and its expected peak.
2. Find the first saturated resource under load.
3. Define a safe concurrency and queue bound.
4. Decide which work can be rejected, delayed, or degraded.
5. Test a spike and a slow-dependency case.
6. Define signals and actions for when the limit is approached.

For dependency-level timeouts, retries, and isolation, see
[resilience patterns](../architecture/02-resiliency-patterns.md).
For the signals that should interrupt a responder, see
[alerting and on-call](./03-alerting-and-on-call.md).
