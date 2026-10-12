# Following a Request Across Boundaries

Distributed tracing shows where one operation spent time as it moved through
services and dependencies. Use it when a slow or failed request has several
possible owners. For the broader choice of metrics, logs, and traces, see
[observability and monitoring](../devops/03-observability-and-monitoring.md).

## Trace, Span, and Context

A **trace** is the recorded journey of one operation. A **span** records one
piece of work, such as an inbound request, database query, or outbound call.
The trace context connects spans across process boundaries.

```text
checkout request
  order handler       80 ms
    inventory call   120 ms
    payment call     740 ms
```

This trace points to the payment call as the main latency contributor. It
does not by itself explain *why* payment was slow; use the relevant logs,
dependency metrics, and provider status to confirm the cause.

## Preserve the Connection Between Hops

- Propagate trace context through outgoing HTTP calls and message headers
  when the next component supports it. Without context, each hop may look
  like an unrelated trace.
- For delayed messages, record the producer and consumer relationship even
  when processing starts much later. Avoid assuming all asynchronous work is
  one uninterrupted request.
- An external webhook is usually a new incoming request. Correlate it with
  the original business operation using a safe order or payment attempt ID;
  do not rely on the provider returning your original trace context.
- Put trace or correlation IDs in structured logs so an engineer can move
  from one slow span to the events recorded by that component.

Automatic instrumentation can capture common network and database spans.
Add a business span only when it explains a meaningful step that automatic
spans cannot distinguish. The specific instrumentation library is secondary
to preserving context across the actual boundaries.

## Keep the Signal Useful

Sampling controls trace volume but can miss a rare failure. Decide how to
retain enough error and slow-path evidence to investigate incidents. Avoid
putting secrets, full payloads, or personal data in span attributes. High
cardinality fields can also make trace storage and search expensive.

When a trace ends at a timeout, the remote operation may still be running or
may have completed. Tracing shows observed timing; it is not proof of the
remote business outcome. Use the owner's state or a reconciliation path for
that question.

## Diagnosis Sequence

1. Find the affected user journey and time window from metrics.
2. Open a representative slow or failed trace.
3. Identify the span where waiting or failure begins and its dependency.
4. Use correlated logs and dependency metrics to test the likely cause.
5. Check the business outcome separately if the call timed out or crossed an
   asynchronous boundary.

## Decision Rule

Trace the critical path, carry context across supported hops, and correlate
delayed work by business ID. Treat traces as evidence about execution, not as
the source of truth for completed business effects.
