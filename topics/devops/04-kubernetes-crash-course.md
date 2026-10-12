# Kubernetes Runtime Basics

Kubernetes keeps declared workloads running across a cluster. Use this note
to understand what happens to an application instance, its traffic, and its
resources. Kubernetes does not remove the need for compatible releases,
useful health checks, or capacity planning.

## The Runtime Path

```text
external route -> Service -> ready Pods
                         ^
                    Deployment maintains replicas
```

- **Pod:** a deployable unit containing one or more tightly coupled
  containers. Pods can be replaced; do not use one Pod's IP as a durable
  application address.
- **Deployment:** maintains the requested replicas for a stateless workload
  and can replace them during a rollout.
- **Service:** stable network abstraction over a changing set of Pods.
- **Ingress or Gateway:** routes external traffic to Services when the
  cluster has a suitable controller or implementation.

These objects explain why a process can be running but still receive no
traffic: it may not be ready, may not match the Service selector, or the
route may be wrong.

## Probes and Lifecycle

- **Readiness:** whether an instance should receive traffic now. A failed
  check removes it from Service endpoints; it does not restart the container.
- **Liveness:** whether the process is stuck and needs a restart. Keep it
  focused on a failure a restart can fix.
- **Startup:** gives a slow-starting application time before other probes
  begin to act.

Do not make liveness depend on a shared database: a brief database outage
could restart every application instance. Readiness may check dependencies
needed to serve traffic, but a too-deep check can remove all capacity
during a temporary shared failure. Match probe design to the application
and test the failure behavior.

On shutdown, stop accepting new work and let in-flight work complete within
a grace period. Probe success alone does not prove the user journey works;
watch business signals during rollout.

## Requests, Limits, and Scaling

Resource requests help the scheduler place Pods and reserve capacity.
Limits constrain usage; a low CPU limit can throttle work, and a low memory
limit can lead to termination. Set values from measurements rather than a
universal template. See
[container sizing](../cloud/04-container-sizing.md).

A Horizontal Pod Autoscaler (HPA) adjusts replica count from configured
metrics. It cannot instantly handle a spike, and adding replicas can
overload a shared database or provider. When using CPU utilization, requests
are part of the calculation. See
[capacity planning](../sre/05-capacity-planning-and-load-shedding.md).

## Check a Failing Rollout

1. Did new Pods start and become ready?
2. Does the Service select the intended Pods?
3. Are requests, limits, or startup behavior causing restarts?
4. Do old and new versions work with the current contracts and data?
5. Did user-facing errors or latency change after traffic shifted?

For deployment compatibility, see
[safe deployments](./02-zero-downtime-deployments.md). For the wider
platform and infrastructure boundary, see
[Kubernetes and Terraform](../cloud/02-infrastructure-as-code-and-platform-boundaries.md).

## Further Reading

- [Kubernetes workload concepts](https://kubernetes.io/docs/concepts/workloads/)
- [Kubernetes probes](https://kubernetes.io/docs/concepts/workloads/pods/probes/)
- [Kubernetes resource management](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/)
