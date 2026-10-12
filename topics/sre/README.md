# Reliability and Operations

Use this block to keep a live system useful when traffic, dependencies,
deployments, and people behave imperfectly. The same questions apply to
services, applications, data pipelines, and other systems: what matters to
users, how will failure be noticed, what can reduce impact, and what must
change afterward?

## Work Through the Operating Loop

1. [Measure reliability](./02-sli-slo-and-error-budgets.md): define a user-facing indicator, target, and acceptable failure budget.
2. [Alert and respond](./03-alerting-and-on-call.md): page only when timely human action can reduce impact.
3. [Handle an incident](./04-incident-response-and-triage.md): assess scope, mitigate, communicate, and confirm recovery.
4. [Protect capacity](./05-capacity-planning-and-load-shedding.md): find bottlenecks and decide what to reject or degrade under pressure.
5. [Learn from failure](./06-postmortems-and-operational-review.md): turn incidents and near misses into specific improvements.

## Related Decisions

- [Observability](../devops/03-observability-and-monitoring.md): collect logs, metrics, and traces to explain what happened.
- [Resilience patterns](../architecture/02-resiliency-patterns.md): timeouts, retries, isolation, and fallback in the design.
- [Safe deployments](../devops/02-zero-downtime-deployments.md): release and rollback behavior.
- [Container sizing](../cloud/04-container-sizing.md): runtime resource limits.
