# Incident Response and Triage

An incident is a live problem with meaningful user, business, or operational
impact. The first job is to reduce that impact safely. A complete root-cause
explanation can follow after the system is stable.

## First Minutes

1. State the user-visible symptom and when it began.
2. Estimate scope: affected users, regions, operations, and growth of impact.
3. Check recent changes and dependency health as clues, not assumptions.
4. Choose the safest reversible mitigation: pause a rollout, roll back, fail
   over, disable optional work, or shed excess load.
5. Verify that the user-facing signal improves; change course if it does not.
6. Assign an owner for coordination and send a short factual update.

Do not wait for certainty while impact grows. Do not change several things at
once without recording them; that makes recovery harder to judge.

## Diagnose While Containing Impact

Use metrics to see when and where the problem grew, traces to find the slow or
failing request path, and logs for event details. Compare healthy and
unhealthy segments. Check saturation in pools, queues, and dependencies.
See [observability and monitoring](../devops/03-observability-and-monitoring.md).

Example: a queue backlog grows after a release. First check whether orders
are missing a processing deadline. Pause the release or reduce incoming work
if needed, then inspect consumer errors, processing time, and downstream
capacity. Confirm the backlog is shrinking and old orders are completing.
Adding workers blindly can overload the same downstream dependency.

## Coordinate and Communicate

For a significant incident, make ownership explicit: one person coordinates
decisions, others investigate or execute changes, and someone updates
stakeholders. State what is known, current impact, mitigation in progress,
and when the next update will come. Distinguish observations from hypotheses.

Escalate when impact grows, recovery requires another team's control, or the
current responders cannot safely mitigate. Severity should reflect impact and
urgency; local labels can vary.

## Confirm Recovery

Recovery means more than a healthy process or a green dashboard. Verify the
user journey, delayed work, data consistency, and whether the mitigation
created new problems. Keep temporary safeguards visible until a permanent
fix is ready.

Record a compact incident timeline and continue with
[postmortems and operational review](./06-postmortems-and-operational-review.md)
when the event deserves a deeper review.

## Simple Incident Record

- Symptom, start time, and affected users or operations
- Actions taken and their observed effect
- Current status and next update time
- Remaining risks, backlog, and follow-up owner
