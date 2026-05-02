# DevOps and Runtime Basics

Use this folder for deployment, runtime, and observability basics that matter
for backend ownership.
This is less about tooling fashion and more about the mechanics that decide
whether a release is safe, observable, and recoverable.

Focus:

- containerization and delivery pipelines
- zero-downtime rollout strategies
- observability
- practical Kubernetes concepts

Working style:

- explain ops jargon in terms of runtime behavior and release risk
- prefer "what fails and how you recover" over tool-name memorization
- keep the topic close to what a backend engineer actually owns in production

## Recommended Order

1. [01-infrastructure-and-devops-basics.md](./01-infrastructure-and-devops-basics.md): what DevOps changes in practice for a backend engineer who owns code in production
2. [02-zero-downtime-deployments.md](./02-zero-downtime-deployments.md): rollout patterns, backward compatibility, and how to avoid breaking live traffic
3. [03-observability-and-monitoring.md](./03-observability-and-monitoring.md): logs, metrics, traces, and how to know whether the system is healthy
4. [04-kubernetes-crash-course.md](./04-kubernetes-crash-course.md): the core Kubernetes objects and behaviors that matter when your app runs there
5. [05-docker-runtime-practices.md](./05-docker-runtime-practices.md): what actually matters at container runtime, not just how to write a `Dockerfile`
6. [06-observability-cheatsheet.md](./06-observability-cheatsheet.md): a compact reopen sheet for the metrics and tracing ideas you should keep warm

## Refresh

- [01-infrastructure-and-devops-basics.md](./01-infrastructure-and-devops-basics.md)
- [02-zero-downtime-deployments.md](./02-zero-downtime-deployments.md)
- [03-observability-and-monitoring.md](./03-observability-and-monitoring.md)

## Required

- [04-kubernetes-crash-course.md](./04-kubernetes-crash-course.md)
- [05-docker-runtime-practices.md](./05-docker-runtime-practices.md)

## Growth

- [06-observability-cheatsheet.md](./06-observability-cheatsheet.md)

## Core Rule

- packaging, deployment, and observability are part of backend ownership
- database changes often decide deployment risk
- Kubernetes knowledge should stay practical, not ceremonial
