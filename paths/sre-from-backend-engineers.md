# SRE Path for Backend Engineers

Use this path when you already know how to build backend services and now want
to operate them for reliability, recovery, and safe change.

This is not a restart from zero.
The transition works best when you keep your backend strengths and add the
missing operational layer around them.

## Backend Engineer vs SRE

A backend engineer is mainly responsible for building and evolving service
behavior.
An `SRE` is mainly responsible for keeping service behavior reliable under real
traffic, failures, deploys, and incidents.

The overlap is large, but the center of gravity changes:

- backend engineers optimize correctness, feature behavior, and service design
- `SREs` optimize reliability, observability, recovery, and operational safety
- backend engineers ask "does this flow work?"
- `SREs` ask "how does this fail, how do we detect it, and how do we recover fast?"

In mature teams, the practical difference is often this:

- backend owns service logic
- `SRE` owns the reliability mechanisms, operating model, and production guardrails
- both meet in deployment, observability, capacity, and incident response

## What Already Transfers Well

Your backend background already gives you useful `SRE` leverage in:

- HTTP, RPC, retries, timeouts, and dependency behavior
- thread pools, connection pools, queueing, and JVM runtime failure modes
- database locks, idempotency, consistency, and replay safety
- API latency, error handling, and business-flow debugging
- reading traces, logs, and metrics in service context instead of treating them as abstract ops data

That is why the right path is not "learn Linux first and forget software".
It is "turn software knowledge into production judgment".

## What You Need To Add

The new layer is mostly about operating systems as live services:

- `SLI` and `SLO` thinking, meaning the measured reliability signal and the reliability target, plus error budget judgment
- alert design and on-call judgment
- incident triage, mitigation, and postmortem habits
- Kubernetes runtime behavior, probes, autoscaling, and scheduling basics
- infrastructure as code and platform boundaries
- capacity planning, load shedding, and graceful degradation
- safer deploys, rollback strategy, and operational readiness review

## Working Rule

For each topic:

1. reopen the smallest runtime mental model
2. connect it to one backend failure you have already seen
3. define how you would detect it in production
4. define what signal should page, what should only create noise, and how you would mitigate it
5. run one small lab or failure exercise before moving on

## Recommended Order

### Phase 1. Shift from code ownership to runtime ownership

1. [../topics/devops/01-infrastructure-and-devops-basics.md](../topics/devops/01-infrastructure-and-devops-basics.md)
2. [../topics/cloud/01-cloud-basics.md](../topics/cloud/01-cloud-basics.md)
3. [../topics/architecture/04-networking-fundamentals.md](../topics/architecture/04-networking-fundamentals.md)
4. [../topics/devops/03-observability-and-monitoring.md](../topics/devops/03-observability-and-monitoring.md)

Outcome:

- you stop thinking only in terms of code paths and start thinking in terms of traffic paths, dependencies, saturation, and blast radius

### Phase 2. Learn reliability through backend failure modes you already know

5. [../topics/architecture/02-resiliency-patterns.md](../topics/architecture/02-resiliency-patterns.md)
6. [../topics/databases/01-idempotency-and-transaction-safety.md](../topics/databases/01-idempotency-and-transaction-safety.md)
7. [../topics/databases/02-database-locks-and-concurrency.md](../topics/databases/02-database-locks-and-concurrency.md)
8. [../topics/architecture/03-distributed-transactions-and-events.md](../topics/architecture/03-distributed-transactions-and-events.md)
9. [../topics/api/02-message-brokers-and-delivery-semantics.md](../topics/api/02-message-brokers-and-delivery-semantics.md)
10. [../topics/api/03-webhooks-basics.md](../topics/api/03-webhooks-basics.md)

Outcome:

- you can explain why real incidents often come from retries, duplication, stale reads, queue lag, and slow dependencies instead of from one obvious code bug

### Phase 3. Build the production debugging toolkit

11. [../topics/architecture/05-distributed-tracing.md](../topics/architecture/05-distributed-tracing.md)
12. [../topics/cloud/04-container-sizing-and-observability.md](../topics/cloud/04-container-sizing-and-observability.md)
13. [../topics/devops/06-observability-cheatsheet.md](../topics/devops/06-observability-cheatsheet.md)
14. [../labs/kotlin-backend-examples/README.md](../labs/kotlin-backend-examples/README.md)

Use these runnable topics:

- `correctness/idempotency`
- `correctness/locking`
- `integration/async-boundaries`
- `integration/kafka-patterns`
- `jvm/concurrency-production`

Outcome:

- you can move from "CPU looks fine" to a concrete production story about where latency, errors, or saturation actually come from

### Phase 4. Learn safe change and safe runtime operation

15. [../topics/devops/02-zero-downtime-deployments.md](../topics/devops/02-zero-downtime-deployments.md)
16. [../topics/devops/05-docker-runtime-practices.md](../topics/devops/05-docker-runtime-practices.md)
17. [../topics/devops/04-kubernetes-crash-course.md](../topics/devops/04-kubernetes-crash-course.md)
18. [../topics/cloud/02-kubernetes-and-terraform-for-backend-engineers.md](../topics/cloud/02-kubernetes-and-terraform-for-backend-engineers.md)
19. [../topics/cloud/05-local-kubernetes-lab.md](../topics/cloud/05-local-kubernetes-lab.md)
20. [../topics/architecture/11-service-discovery.md](../topics/architecture/11-service-discovery.md)

Outcome:

- you understand what actually happens when a service is packaged, scheduled, probed, deployed, restarted, or rolled back

### Phase 5. Push toward stronger SRE judgment

21. [../topics/architecture/07-caching-strategies.md](../topics/architecture/07-caching-strategies.md)
22. [../topics/databases/05-database-scaling.md](../topics/databases/05-database-scaling.md)
23. [../topics/architecture/10-consistent-hashing.md](../topics/architecture/10-consistent-hashing.md)
24. [../topics/architecture/12-consensus-and-leader-election.md](../topics/architecture/12-consensus-and-leader-election.md)
25. [../topics/testing/01-testing-strategies.md](../topics/testing/01-testing-strategies.md)

Outcome:

- you start reasoning about platform tradeoffs, coordination risk, cache behavior, and scale limits instead of only service-local correctness

## SRE-Specific Docs In This Repo

The repo now includes an `SRE` topic folder for the operational layer that was previously missing:

1. [../topics/sre/01-backend-engineer-vs-sre.md](../topics/sre/01-backend-engineer-vs-sre.md)
2. [../topics/sre/02-sli-slo-and-error-budgets.md](../topics/sre/02-sli-slo-and-error-budgets.md)
3. [../topics/sre/03-alerting-and-on-call.md](../topics/sre/03-alerting-and-on-call.md)
4. [../topics/sre/04-incident-response-and-triage.md](../topics/sre/04-incident-response-and-triage.md)
5. [../topics/sre/05-capacity-planning-and-load-shedding.md](../topics/sre/05-capacity-planning-and-load-shedding.md)
6. [../topics/sre/06-postmortems-and-operational-review.md](../topics/sre/06-postmortems-and-operational-review.md)

That fills the main conceptual gap between backend reliability foundations and day-to-day `SRE` operating practice.

Add labs next:

1. `labs/reliability-labs/alerting`: noisy alert vs actionable alert
2. `labs/reliability-labs/slo-burn`: compute burn rate from request data
3. `labs/reliability-labs/kubernetes-probes`: readiness vs liveness failure scenarios
4. `labs/reliability-labs/incident-drills`: dependency slowdown, queue backlog, and DB saturation drills
5. `labs/reliability-labs/runbooks`: mitigation playbooks for checkout, payment, and webhook ingestion

## Practical Rule For The Transition

Do not try to become a generalist platform engineer in one jump.
First become the backend engineer who is strongest at:

- production diagnosis
- observability design
- failure-mode reasoning
- rollout safety
- incident communication

That is usually the shortest credible path into `SRE`.
