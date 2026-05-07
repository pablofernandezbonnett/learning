# Site Reliability Engineering for Backend Engineers

Use this folder when you want to learn `SRE` from the point of view of someone
who already understands backend systems.

The goal is not to turn reliability into abstract ops vocabulary.
The goal is to understand how live systems fail, how teams detect the failure,
how they reduce user impact, and how they keep future changes safer.

Focus:

- what changes when you move from building services to operating them
- `SLI` and `SLO`, meaning the measured reliability signal and the target you operate against, plus error budget and the difference between measuring and promising
- alerting, on-call judgment, and how to avoid paging people for noise
- incident triage, mitigation, and service recovery under pressure
- capacity planning, load shedding, and safe degradation
- postmortems and operational review habits that actually improve systems

Working style:

- explain reliability jargon before relying on it
- keep examples close to APIs, queues, databases, and user-facing product flows
- prefer "how it fails, how you detect it, and how you respond" over vendor or tool memorization

## Recommended Order

1. [01-backend-engineer-vs-sre.md](./01-backend-engineer-vs-sre.md): what changes when code ownership becomes runtime ownership
2. [02-sli-slo-and-error-budgets.md](./02-sli-slo-and-error-budgets.md): how reliability targets are defined and used in practice
3. [03-alerting-and-on-call.md](./03-alerting-and-on-call.md): what should page, what should not, and how on-call should work
4. [04-incident-response-and-triage.md](./04-incident-response-and-triage.md): how to diagnose, mitigate, communicate, and recover during incidents
5. [05-capacity-planning-and-load-shedding.md](./05-capacity-planning-and-load-shedding.md): how systems stay stable under growth, spikes, and saturation
6. [06-postmortems-and-operational-review.md](./06-postmortems-and-operational-review.md): how to learn from incidents without blame and turn them into real improvements

## Refresh

- [01-backend-engineer-vs-sre.md](./01-backend-engineer-vs-sre.md)
- [02-sli-slo-and-error-budgets.md](./02-sli-slo-and-error-budgets.md)
- [03-alerting-and-on-call.md](./03-alerting-and-on-call.md)

## Required

- [04-incident-response-and-triage.md](./04-incident-response-and-triage.md)
- [05-capacity-planning-and-load-shedding.md](./05-capacity-planning-and-load-shedding.md)

## Growth

- [06-postmortems-and-operational-review.md](./06-postmortems-and-operational-review.md)

## Related Path

If your goal is the role transition from backend engineer to `SRE`, start with:

- [../../paths/sre-from-backend-engineers.md](../../paths/sre-from-backend-engineers.md)

## Related Internal Topics

- [../devops/03-observability-and-monitoring.md](../devops/03-observability-and-monitoring.md): logs, metrics, traces, and baseline `SLI` / `SLO` vocabulary for reliability measurement and targets
- [../devops/02-zero-downtime-deployments.md](../devops/02-zero-downtime-deployments.md): rollout safety and backward compatibility
- [../devops/04-kubernetes-crash-course.md](../devops/04-kubernetes-crash-course.md): probes, scaling, and runtime behavior
- [../architecture/02-resiliency-patterns.md](../architecture/02-resiliency-patterns.md): retries, timeouts, circuit breakers, and graceful degradation
- [../cloud/04-container-sizing-and-observability.md](../cloud/04-container-sizing-and-observability.md): sizing, saturation, and runtime limits

## Core Rule

- reliability is not separate from software behavior
- a good `SRE` explanation connects symptoms, causes, blast radius, and mitigation
- the goal is not zero failure; the goal is fast detection, controlled impact, and safer recovery
