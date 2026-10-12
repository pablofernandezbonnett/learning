# Delivery and Runtime

Use this block to follow a change from source code to a running system:
validate it, package it, release it safely, observe the result, and recover
when behavior degrades.

## Start Here

1. [From code to a running system](./01-infrastructure-and-devops-basics.md): the delivery loop and where each tool fits.
2. [Safe deployments](./02-zero-downtime-deployments.md): version overlap, rollout choice, schema compatibility, and rollback.
3. [Observability](./03-observability-and-monitoring.md): metrics, traces, and logs for explaining failures.

## Choose the Runtime Detail You Need

- [Docker image and runtime](./05-docker-runtime-practices.md): packaging, configuration, privilege, data, and shutdown.
- [Docker Compose](./07-docker-compose-local-stacks.md): a repeatable local stack with real dependencies.
- [Kubernetes basics](./04-kubernetes-crash-course.md): Pods, traffic, probes, and resources when the system runs in a cluster.

For reliability targets, paging, incidents, and capacity, continue with
[reliability and operations](../sre/README.md). For cloud responsibility
and managed-service choices, see [cloud](../cloud/README.md).
