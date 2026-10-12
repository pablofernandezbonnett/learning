# Cloud and Managed Runtimes

Start with [cloud basics](./01-cloud-basics.md): choose where work runs,
where state lives, and which operational responsibility the provider takes.
Then open the note for the decision at hand.

## Decision Notes

- [Serverless runtime choice](./03-serverless-runtime-choice.md): fit triggered or managed compute to latency, state, retries, and cost.
- [Container sizing](./04-container-sizing.md): set CPU and memory from measured workload behavior.
- [Infrastructure as code](./02-infrastructure-as-code-and-platform-boundaries.md): review and repeat infrastructure changes without losing state ownership.
- [AWS service map](./08-aws-service-map.md): translate those decisions into common AWS service families.

## Practice

- [Local Kubernetes lab](./05-local-kubernetes-lab.md): inspect image loading, traffic, probes, resources, and rollback in a small cluster.

The [delivery and runtime block](../devops/README.md) owns packaging,
deployment, observability, and Kubernetes workload basics. The
[reliability block](../sre/README.md) owns targets, paging, incidents,
and capacity protection.
