# Safe Deployments Without Planned Interruption

Keeping traffic available during a release requires compatible old and new
behavior while versions overlap, enough capacity, useful health checks, and a
way to stop a bad rollout. A rolling deployment alone does not guarantee
zero user impact.

## Choose a Rollout Shape

| Shape | How it works | Main cost or risk |
| --- | --- | --- |
| Rolling | Replace instances gradually while both versions serve traffic. | Mixed-version compatibility and slower rollback. |
| Canary | Send a small portion of real traffic to the new version, then expand. | Needs reliable comparison signals and traffic control. |
| Blue/green | Prepare a separate new environment and switch traffic. | Extra capacity and a shared-data compatibility problem. |

Start with a rolling update when overlapping versions are safe and the
platform has spare capacity. Use a canary when limiting blast radius and
observing business outcomes is worth the added routing work. Blue/green can
make traffic switching simpler, but it does not make shared database changes
reversible or guarantee instantaneous recovery.

Before rollout, decide which signal stops promotion: request failure,
latency, and a user journey such as completed checkout may all matter.
Test the rollback path, not only the forward deployment.

## Make Contracts Survive Version Overlap

During a rollout, old and new application instances may use the same API,
events, and database. A client may also keep an older version much longer
than a server rollout lasts. Prefer additive changes until consumers have
moved; do not rename a field or change its meaning in one step.

For a database column replacement:

1. **Expand:** add the new nullable column or table without removing the old.
2. **Bridge:** deploy code that can read both shapes and writes the values
   needed by still-running versions.
3. **Backfill:** copy historical data in bounded batches and verify counts
   and correctness.
4. **Switch:** move reads to the new shape after data and consumers are ready.
5. **Contract:** stop old writes, then remove the old shape only after old
   versions and the rollback window are gone.

The exact bridge depends on who writes the data and whether concurrent writes
can race with backfill. Test mixed-version behavior with representative data.
A migration tool makes changes repeatable; it does not make an incompatible
schema change safe by itself.

## Readiness, Shutdown, and Capacity

A new instance should receive traffic only when it can serve requests.
During shutdown, stop accepting new work and allow in-flight work to finish
within a defined grace period. Keep enough healthy instances to handle load
while some are starting or leaving. Readiness checks should reflect ability
to serve traffic without causing a restart cascade when one dependency has
a short failure.

See [Kubernetes basics](./04-kubernetes-crash-course.md) for one platform's
readiness and rollout mechanics.

## Release Check

- Can old and new code both use the current contracts and database?
- Is the new version ready before it receives traffic?
- Which user-facing signal stops the rollout?
- Can we roll back the application without rolling back shared data?
- What happens to in-flight requests and background work?

For ongoing service targets and incident response, see
[reliability and operations](../sre/README.md).
