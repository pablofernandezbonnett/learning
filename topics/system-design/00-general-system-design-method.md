# A General Method for Designing a System

Use this when the problem is broad and the implementation is not yet clear. The
method works for a user-facing service, an internal tool, or a background
pipeline. The important decisions change by case; the order of questions stays
useful.

## The Method

1. **Define the job.** Who or what uses the system? What outcome must it
   produce? Name what is outside the first version.
2. **Rank the promises.** Identify the rules that must stay true and the quality
   targets that could change the design: response time, availability, data loss,
   security, privacy, cost, and ease of change. Make the important targets
   testable. For discovery questions, use
   [requirements and quality constraints](../solutions-architecture/01-requirements-and-nfrs.md).
3. **Trace the work.** Walk one important action from input to result. For an
   interactive system, trace a read and a write. For a pipeline, trace one item
   from intake to durable output. Mark the data owner, trust boundaries, and
   any work that can finish later.
4. **Choose the smallest design that meets those promises.** Start with clear
   responsibilities and a durable source of truth. Add a cache, queue, separate
   service, or extra data copy only for a named need. Use the
   [decision reference](./system-design-decision-cheatsheet.md) when two options
   seem plausible.
5. **Break the flow.** Try a retry, timeout, duplicate, bad input, crashed
   worker, unavailable dependency, and a traffic spike. Say what the caller
   sees, what remains durable, and how unfinished work recovers.
6. **Check security and capacity.** Decide who may perform each action on which
   resource. Bound expensive work. Estimate the first likely limit: CPU, memory,
   database query, lock, connection pool, provider, or queue backlog. Scale the
   measured limit rather than every component at once.
7. **Plan operation and change.** Choose signals tied to the promised outcome,
   a recovery path, and a safe rollout. Record the important tradeoff and what
   evidence would make you revisit the design.

This order prevents a common weak approach: naming technologies before knowing
which behavior they must protect.

## Small Example: Appointment Booking

The job is to let a customer book one available slot. The key rule is that one
slot cannot have two confirmed bookings. A booking store owns that decision;
search results may be slightly stale. The booking write checks permission and
uses a guarded write or uniqueness constraint. A stable request identity makes
a retry safe. Reminder delivery happens later, so its failure does not erase a
confirmed booking. Under load, measure search cost and booking contention
before adding caches or more instances.

The same questions apply to a file-processing pipeline, but its key promises
may be durable intake, bounded backlog, replay, and an inspectable failed-item
state rather than an immediate response.

## Review Before Building

- Can we state the outcome, the top constraints, and what can be deferred?
- Which component owns each final decision, and what may be stale?
- What happens on success, rejection, retry, timeout, and partial failure?
- Which shared resource reaches its limit first?
- Who may perform each action, and where is untrusted data checked?
- How will we detect a broken promise, recover, and change the system safely?

Use the [system design drills](./system-design-drills.md) to practise the
method. The [checkout case](./checkout-to-fulfillment-end-to-end.md) shows how
several decisions interact in one concrete product flow.
