# From Code to a Running System

A change is usable only after it is built, released, routed to, observed,
and recoverable. This note is the map of that path; the linked notes own the
details.

## The Delivery Loop

1. **Validate:** automated checks give fast feedback on the change.
2. **Package:** create a versioned artifact with its runtime dependencies.
3. **Promote:** deploy the same artifact through environments with external
   configuration and controlled access to secrets.
4. **Route:** send traffic only to instances ready to serve it.
5. **Observe:** check user outcomes and technical signals during rollout.
6. **Recover:** pause, roll back, or mitigate when the change harms users.

Continuous integration (CI) automates validation and artifact creation.
Continuous delivery makes a tested artifact releasable through a repeatable
process. Automation reduces manual drift, but a pipeline cannot prove that
old and new versions work together or that the database migration is safe.

## What Each Runtime Tool Solves

| Need | Typical mechanism | What it does not decide |
| --- | --- | --- |
| Package a process with runtime dependencies | Container image | Whether the application is correct or secure |
| Run a small local stack repeatedly | Docker Compose | Production scheduling or scaling |
| Maintain replicas and route traffic in a cluster | Kubernetes or another platform | Contract and data compatibility |
| Create infrastructure repeatably | Infrastructure as code | Whether the chosen service design fits |

Use only the mechanisms the system needs. A single managed runtime may be
enough; a cluster is a choice with operating costs, not a required step.

## Example: a Checkout Change

CI tests the change and builds one artifact. The rollout places the new
version beside the old one. Readiness checks keep incomplete instances out
of traffic. Checkout completion and error signals show whether to continue.
The database schema supports both versions until the rollout and rollback
window have passed.

## Continue By Decision

- [Safe deployments](./02-zero-downtime-deployments.md): overlap, rollout, and rollback.
- [Observability](./03-observability-and-monitoring.md): signals for diagnosis.
- [Docker runtime](./05-docker-runtime-practices.md): image and process behavior.
- [Docker Compose](./07-docker-compose-local-stacks.md): reproducible local dependencies.
- [Kubernetes basics](./04-kubernetes-crash-course.md): workload and traffic mechanics when the system uses Kubernetes.
- [Reliability and operations](../sre/README.md): targets, alerting, incidents, and capacity.
