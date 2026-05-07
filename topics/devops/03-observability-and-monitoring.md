# Observability and Monitoring

> Primary fit: `Shared core`

Observability is not mainly about buying a dashboard tool.
It is about having enough signals to explain a failure when the system misbehaves.

Quick review version:

- [06-observability-cheatsheet.md](./06-observability-cheatsheet.md)

This note follows the same reusable study pattern:

- what the topic really means
- the smallest concrete example
- how it works in real systems
- how to explain it clearly

---

## Why This Matters

Systems do not fail in useful ways. They fail with partial slowness, unclear
symptoms, and disagreement between what users feel and what a single service
metric says.

Observability matters because it turns "something is wrong" into a path toward
locating the failing dependency, the affected business flow, and the right
recovery move.

## Smallest Mental Model

Use three views together:

- traces for one request path
- metrics for trend and alerting
- logs for detailed event context

That is usually enough to move from symptom to explanation.

## Bad Mental Model vs Better Mental Model

Bad mental model:

- observability means dashboards and log search
- if CPU and memory look fine, the system is probably healthy
- one metric per service is enough

Better mental model:

- observability means building enough signals to explain failure, not only
  detect it
- technical health and business health both matter
- the useful question is not only "is the pod alive?" but "which dependency,
  tenant, or workflow is degrading?"

Small concrete example:

- weak approach: alert only on 5xx rate
- better approach: combine request latency, error rate, trace data, and business
  metrics such as checkout or payment success so slow dependency failures show
  up before users fully lose the flow

Strong default:

- start with structured logs, correlation IDs, latency/error/saturation
  metrics, and request traces on critical flows

Interview-ready takeaway:

> I use observability to explain failure, not just notice it. Traces show one
> request path, metrics show trends and alerting, and logs provide detailed
> context, ideally tied to business outcomes as well as technical health.

---

## 1. What Observability Actually Means

Short version:

- **monitoring** tells you something is wrong
- **observability** helps you find why it is wrong

Smallest example:

- user clicks checkout
- API gateway calls order service
- order service calls payment service
- payment service calls a `PSP` (`Payment Service Provider`, for example Stripe or Adyen)
- response is slow or fails

Without observability, you know "checkout is broken".
With observability, you can answer:

- where the time went
- which dependency failed
- which users or tenants are affected
- whether the problem is technical, in a dependency, or specific to that business flow

---

## 2. The Smallest Useful Failure Model

A practical failure diagnosis usually needs three views:

- one request view
- one trend view
- one detailed event view

That maps directly to:

- **trace**: one request moving through the system
- **metric**: trend over time
- **log**: detailed event record

Good mental model:

> traces show one request, metrics show the trend, logs show the detail

That is the whole topic in one sentence.
If you can say that clearly and connect it to a failure example, you already sound much stronger.

---

## 3. Logs: The Detailed Event Record

Plain text logs are not enough in a distributed system.

The minimum useful logging setup is:

- structured logs
- correlation or trace IDs on every log line
- enough context to filter by request, tenant, user, or operation

Bad log:

```text
payment failed
```

Useful log:

```json
{
  "timestamp": "2026-03-26T10:15:00Z",
  "level": "ERROR",
  "traceId": "1f2c3d",
  "correlationId": "checkout-req-42",
  "service": "payment-service",
  "operation": "capturePayment",
  "orderId": 12345,
  "provider": "stripe",
  "message": "payment capture failed"
}
```

What matters here:

- every request should carry a correlation ID or trace ID
- logs should be queryable, not only readable
- do not leak secrets or raw card data

In Spring, the usual implementation idea is:

- MDC or Micrometer Observation to pass correlation context across the request
- JSON logs shipped to Datadog, ELK, Splunk, or a similar backend

`MDC` means `Mapped Diagnostic Context`.
In practice, it is just a way to attach values such as `traceId` or `orderId` to logs created during one request flow.

---

## 4. Metrics: The Trend View

Metrics are how you answer:

- is the problem getting worse?
- is it isolated or systemic?
- when did it start?
- should we alert an on-call engineer?

The four golden signals are the fastest reliable baseline:

1. **latency**
2. **traffic**
3. **errors**
4. **saturation**

`saturation` just means the system is running close to its limit.
Examples:

- CPU stays pinned high
- database connection pool is almost full
- queue backlog keeps growing

Important rule:

> do not trust averages for latency

Look at:

- p95
- p99

not only mean latency.

Typical useful backend metrics:

- request rate
- p95 and p99 latency
- 5xx rate
- DB pool wait time
- queue lag
- JVM memory
- CPU saturation

Typical business metrics:

- checkout completion rate
- payment success rate
- order intake rate

---

## 5. Traces: Where The Time Went

If the system is slow, a trace tells you which service call consumed the time.

Smallest mental model:

- one user request creates one trace
- each service-to-service call or DB call becomes a span
- child spans show the work happening in the next service or provider

Example:

- gateway span: 10ms
- order service span: 20ms
- payment service span: 40ms
- PSP call span: 1800ms

Now you know the problem is not "the whole system".
It is the PSP call.

That is why traces are so strong:

- they show dependency latency clearly
- they explain distributed failures cleanly
- they connect very naturally to logs via trace ID

OpenTelemetry is the default modern mental model:

- instrument once
- export to your chosen backend
- avoid getting trapped in one vendor where possible

---

## 6. Business Observability

Senior backend discussions usually expect one step beyond logs, metrics, and traces.

They want to hear that you monitor:

- technical health
- business health

Why this matters:

- CPU can be normal while checkout success collapses
- a dependency can degrade conversion before it trips infrastructure alarms

Good examples:

- payment success rate
- refund failure rate
- stock sync lag
- checkout completion rate

Short rule:

> if users cannot buy, the incident is real even if the pods look healthy

---

## 7. SLI, SLO, And SLA

These words are often used loosely, but discussions go better when you separate them.

- **SLI**: what you measure
- **SLO**: the target you operate against
- **SLA**: the promise made externally

Plain-English version:

- SLI = metric
- SLO = internal target
- SLA = contractual or customer-facing promise

Smallest example:

- SLI: checkout success rate
- SLO: 99.9% successful checkouts over 30 days
- SLA: 99.5% promised to customers

Why the gap matters:

- the difference is your error budget
- if you burn it too fast, feature velocity should slow down until reliability improves

---

## 8. Fast Diagnosis Loop

A clean practical answer is:

1. check the symptom metric first
2. use tracing to find the slow or failing span
3. pull structured logs with the same trace ID
4. compare with business metrics in the same time window
5. decide whether the root cause is local, in a dependency, or systemic

Example:

- p99 checkout latency spikes
- trace shows PSP span is slow
- logs confirm timeout errors from payment provider
- checkout completion rate drops

That is a high-priority user-impacting incident.

---

## 9. 20-Second Answer

> Monitoring tells me something is broken; observability helps me explain why. I want
> structured logs with trace IDs, metrics for latency, traffic, errors, and saturation,
> and distributed tracing for critical request flows. I also want business metrics like
> checkout success or payment success, because healthy infrastructure does not always mean
> healthy user outcomes.

---

## 10. 1-Minute Answer

> I think about observability as the ability to explain failures in distributed systems.
> My baseline is structured logs, metrics, and traces. Logs need correlation or trace IDs
> and enough context to filter by request, tenant, or operation. Metrics should cover the
> four golden signals, and I alert on p95 or p99 latency and error rate rather than on
> averages. Tracing tells me exactly where time was spent across service calls and database
> calls, which is critical for debugging checkout, payment, or order flows. For senior
> systems, I also want business observability: checkout completion, payment success, queue
> lag, or stock sync lag. My normal diagnosis flow is metrics first, then traces, then
> logs for the same trace, then business-impact confirmation in the same time window.

---

## 11. What To Internalize

- monitoring and observability are related but not identical
- logs, metrics, and traces solve different parts of the diagnosis problem
- every request flow should carry correlation or trace IDs
- p95 and p99 matter more than averages
- business metrics belong on the same dashboard as technical metrics
- SLI, SLO, and SLA are different layers of reliability language
