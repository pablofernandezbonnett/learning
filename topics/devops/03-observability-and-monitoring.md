# Observability and Monitoring

When a system misbehaves, the team needs evidence to locate the failing path,
measure user impact, and check whether a mitigation worked. Instrument the
critical journeys before an incident, not only after one.

## Three Views of a Failure

| Signal | Helps answer | Caution |
| --- | --- | --- |
| Metrics | When did errors, latency, traffic, or saturation change? | Averages can hide slow users and hot tenants. |
| Traces | Which calls and dependencies consumed one request's time? | Sampling may miss rare failures. |
| Structured logs | What happened in one operation, with useful context? | Avoid secrets and sensitive payloads. |

Use a trace or correlation ID across boundaries so an engineer can move from
a metric spike to a representative trace and its logs. Add business outcomes
such as checkout completion or order processing delay; healthy CPU does not
prove the product flow works.

## Example: Slow Checkout

Checkout completion drops while p95 latency rises. A trace shows most time
is spent waiting for a payment provider. Logs with the same trace ID show
timeouts. Pool saturation and retry counts reveal whether the application
is amplifying the slowdown. This evidence supports a deadline or fallback
decision and later confirms whether users recovered.

## Start With Useful Instrumentation

1. Pick a critical user journey and its success or failure outcome.
2. Measure traffic, errors, latency distribution, and saturation near its
   dependencies.
3. Trace requests across service and database calls.
4. Log state transitions and failures with safe identifiers, without storing
   secrets or private data.
5. Check that someone can answer: who is affected, where did time go, and
   did the mitigation improve the user outcome?

More data is not automatically more useful. High-cardinality labels,
unbounded logs, and full payload capture can be costly and unsafe. Keep
signals tied to questions the team needs to answer.

## Boundary With Reliability Targets

This note covers collecting and using evidence. A service level indicator
selects a user-facing measure; a service level objective sets its target
over time. Define those decisions in
[SLI, SLO, and error budgets](../sre/02-sli-slo-and-error-budgets.md).
Use [alerting and on-call](../sre/03-alerting-and-on-call.md) to decide when
a signal should interrupt someone.

For tracing mechanics, see
[distributed tracing](../architecture/05-distributed-tracing.md).
