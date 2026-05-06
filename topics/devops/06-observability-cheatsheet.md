# Observability Cheatsheet

> Primary fit: `Shared core`

You do not need to be an SRE, but you do need a clear mental model for how to
notice, narrow, and debug a production failure.

Use this after reading [03-observability-and-monitoring.md](./03-observability-and-monitoring.md).

This is the short version to retain and reopen quickly.

---

## Why This Matters

Incidents rarely announce their root cause clearly. This sheet matters because
the shortest useful observability model should help you move from symptom to
explanation fast, not just remember tool names.

## Smallest Mental Model

Use three views together:

- traces for one request path
- metrics for trend and alerting
- logs for exact event detail

## Bad Mental Model vs Better Mental Model

- bad: observability means dashboards plus log search after the fact
- better: observability is the signal set that lets you explain which request,
  dependency, or business flow is actually failing

---

## What This Solves

Monitoring tells you that the system is unhealthy.
Observability helps you find why.

---

## Smallest Example

- checkout is slow
- gateway looks fine
- order service is healthy
- payment provider span is taking 1.8 seconds
- payment success rate is dropping

Without observability, you only know "users are complaining".
With observability, you can see where time is going and what business impact exists.

---

## Where It Matters Fast

- checkout and payment flows
- queue or broker-backed async processing
- stock, catalog, and fulfillment synchronization
- any system where user impact appears before infra alarms

---

## The 4 Signals To Keep In Your Head

1. **Latency**
2. **Traffic**
3. **Errors**
4. **Saturation**

If you forget everything else, remember these four.

---

## The 3 Layers

### Logs

- structured
- searchable
- correlation ID / trace ID included
- useful for exact event detail

### Metrics

- cheap numeric trends
- dashboards
- alerting
- useful for "is this widespread or getting worse?"

### Traces

- where the time went
- which downstream step (the next dependent service or provider) is slow or failing
- useful for one request path across many services

---

## The 5 Things To Remember

1. **Never trust averages.**
   Watch p95 and p99.
   `p95` means 95% of requests are faster than that number. `p99` shows the slower tail, which is often where user pain really appears.

2. **Every request needs a correlation ID or trace ID.**

3. **Business metrics matter with technical metrics.**
   CPU can look fine while checkout completion is collapsing.

4. **Alert on user-impacting symptoms.**
   Error rate, p99 latency, queue lag, checkout success.

5. **Observability is part of architecture, not debugging afterthought.**

---

## What To Watch In Real Systems

- p95 / p99 latency
- request rate
- error rate
- CPU and memory saturation
- DB pool wait time
- queue lag
- payment success rate
- checkout completion rate

---

## Fast Diagnosis Flow

1. check traces for the slow/failing span
2. pull logs by trace ID
3. check metrics in the same time window
4. confirm whether the problem is technical, downstream (in a dependent service or provider), or business-flow impact

---

## Practical Caution

- do not alert only on CPU and memory
- do not rely only on averages
- do not log without correlation or trace IDs
- do not ignore business metrics in payment or checkout systems

---

## Short Answer Shape

Good short answer:

> I want structured logs with trace IDs, metrics for the golden signals (latency, traffic, errors, saturation), and
> distributed tracing for critical paths. I alert on p99 latency, error rate,
> and business metrics like checkout completion, not only on CPU or memory.
