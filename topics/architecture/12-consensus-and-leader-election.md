# Coordinating a Single Active Worker

Several application instances may be capable of running the same background
task. Coordination can reduce overlapping work, but it cannot by itself
guarantee that a payment, email, or stock update happens exactly once. Design
the business effect to survive retries and uncertain outcomes.

## Choose the Smallest Coordination Boundary

| Need | Starting point | Remaining risk |
| --- | --- | --- |
| Run a periodic job | Platform scheduler or job runner | A run may be late, missed, or repeated |
| Let one instance own work temporarily | Lease with renewal and expiry | An old holder may keep running after losing the lease |
| Update one shared row safely | Conditional write or database transaction | Other effects outside that transaction still need protection |
| Replicate critical coordination state across failures | Existing quorum-backed coordinator | Writes or elections can pause without a quorum |

Do not build a consensus cluster just to avoid duplicate scheduled work.
First check whether one durable job queue or an idempotent operation already
solves the problem.

## Why a Lease Is Not Enough

A **lease** grants one worker temporary ownership. The holder renews it; if
renewal stops, another worker may acquire it. A paused old worker can wake up
after its lease expired and still try to write. At that moment two workers
may act, even though the coordinator has only one current holder.

For a critical shared resource, use a **fencing token**: a monotonically
increasing ownership number issued with each new lease. The resource rejects
writes carrying an older token. This requires the resource to check the
token; a lease name or timeout alone does not provide fencing. If the target
cannot check tokens, use idempotent operations and reconciliation so stale
work cannot silently corrupt the outcome.

## What Consensus Adds

A consensus system such as Raft keeps replicated coordination decisions in
agreement. A majority, or **quorum**, must participate to commit a decision.
That prevents two partitions from independently committing conflicting
coordination state, but the minority side may be unable to make progress.

This protects the coordinator's state. It does not automatically stop a
former leader from sending a late request to an external system, and it does
not make business effects exactly once. Use the platform's established
coordination service rather than implementing Raft inside an application.

## Small Example: Inventory Import

An import runs every few minutes across three application replicas. Give each
scheduled run a stable ID. The worker records progress and applies stock
updates idempotently by source record and version. A platform scheduler can
start the work; a lease may reduce overlap if only one import should be active.
If ownership changes mid-run, the new worker resumes from recorded progress.
The stock owner still rejects stale or duplicate updates.

For Kubernetes specifically, a CronJob can create more than one Job for a
scheduled time or miss one. A `concurrencyPolicy` controls overlap among Jobs
from that CronJob; it does not turn the business effect into exactly once.
Kubernetes Lease objects support leader election, but application effects
still need the protections above.

For a single-row write rule, see [database locks and
concurrency](../databases/02-database-locks-and-concurrency.md). For duplicate
message effects, see [broker delivery
semantics](../api/02-message-brokers-and-delivery-semantics.md).

## Decision Rule

Name the shared resource and the effect that must not repeat. Use a scheduler
for timing, a lease for temporary ownership, and a shared write rule or
fencing for correctness. Treat retries, failover, and reconciliation as part
of the design.

## Further Reading

- [Kubernetes CronJob limitations](https://kubernetes.io/docs/concepts/workloads/controllers/cron-jobs/).
- [Kubernetes Leases](https://kubernetes.io/docs/concepts/architecture/leases/).
- [Raft paper](https://raft.github.io/raft.pdf).
