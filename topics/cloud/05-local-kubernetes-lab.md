# Local Kubernetes Deployment Lab

Use a disposable local cluster to follow one application from image to
traffic and recovery. Pick one local cluster tool, such as kind or k3d.
The lab teaches workload behavior; it does not imitate a cloud provider.

## Minimum Setup

- One small HTTP service with a health endpoint
- A container image the cluster can pull or load
- A Deployment and a Service
- Readiness and liveness probes
- CPU and memory requests

Use the [Kubernetes runtime note](../devops/04-kubernetes-crash-course.md)
for the meaning of these objects. Follow the chosen cluster tool's current
image-loading instructions; a host image is not automatically visible
inside every local cluster.

## Exercises

1. **Start:** deploy one replica and reach it through the Service or a
   temporary port forward. Explain which component owns the stable address.
2. **Release:** build a second image, roll it out, and observe when the new
   Pod becomes ready. Keep both image versions compatible while they overlap.
3. **Failure:** make readiness fail. Check whether traffic stops reaching the
   instance without forcing a restart. Then test a liveness failure
   separately.
4. **Capacity:** apply an unrealistically small memory limit, observe the
   failure, restore a measured value, and record the difference between a
   request and a limit.
5. **Recovery:** deploy a bad version, detect it from an application signal,
   roll back, and confirm the user-facing path works again.

Keep one service until these steps are clear. Add a database or external
route only if it teaches a new boundary you want to inspect.

## Solution guide

```text
start: Service selects ready Pods; an unready Pod receives no new traffic
release: new Pods become ready before old capacity is removed
failure: readiness removes traffic; liveness asks for a restart
capacity: requests influence placement; limits bound usage
recovery: detect bad behavior, roll back, verify the user-facing route
```

Compare that expected sequence with what you observed. If the rollout loses
capacity or rollback does not restore service, inspect the probe, resource
settings, and application signal before adding another component.

## Optional IaC Extension

Once the workload works, manage one stable resource through Terraform.
Review a plan, apply it, inspect state, and make a second change. Use either
Terraform or another chosen source of truth for that resource; do not let
two tools overwrite each other's changes. See
[infrastructure as code](./02-infrastructure-as-code-and-platform-boundaries.md).

## What to Record

- Image version and how it reached the cluster
- Which probe controlled traffic and which caused a restart
- Time to ready, rollout result, and rollback result
- Resource settings and the observed failure
- One difference between local success and production readiness

## Further Reading

- [kind documentation](https://kind.sigs.k8s.io/)
- [k3d documentation](https://k3d.io/)
- [Kubernetes tutorials](https://kubernetes.io/docs/tutorials/)
