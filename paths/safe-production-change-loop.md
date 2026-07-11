# Safe Production Change Loop

Use this note when a backend change is more than a local refactor: it changes a
business rule, API, schema, integration, runtime limit, or production behavior.

This is not another delivery process to follow mechanically. It is one short
loop that connects the existing Git, testing, API, database, DevOps, and SRE
material in this repository.

## Why This Matters

Most avoidable production failures are not caused by one bad line of code.
They happen because a change was correct in isolation but incompatible with an
older client, an existing row, a retry, a deployment sequence, or the real load
path.

The useful goal is not "merge quickly." It is to make the smallest change whose
behavior, rollout, and recovery path are all clear.

## Smallest Useful Mental Model

A production change is complete only when you can answer:

1. what behavior is changing and what must remain true
2. how the change is verified before release
3. whether old and new code, contracts, or data can coexist during rollout
4. what signal tells you the release is healthy
5. how you stop or reverse the change if that signal worsens

Plain-English version:

> Treat a release as a controlled experiment around one business change, not as
> the moment code happens to leave a branch.

## Weak Approach vs Better Approach

Weak approach:

> The tests pass, so deploy it. We can investigate if something happens.

Better approach:

> Name the business invariant, verify the risky boundary, keep the rollout
> compatible, watch a concrete signal, and know the first mitigation before
> releasing.

## Small Concrete Example

Suppose checkout must reject a duplicate client request.

The change is not only an `if` statement in a controller. A safe change asks:

- What makes two requests the same business attempt: a client idempotency key, cart, or payment reference?
- Does a database uniqueness rule protect concurrent requests, not only one JVM process?
- What response does a retry receive after the first request succeeded or is still processing?
- Can old clients call the endpoint while the new key is optional during rollout?
- Which production signals show a bad client retry loop or an unexpected rise in duplicate rejection?

The code, test, schema decision, rollout, and dashboard belong to one change.

## The Loop

### 1. Define the change and its invariant

Write one sentence for the business rule that must stay true. Examples:

- one idempotency key creates at most one order
- an old API client still receives a valid response during the rollout
- a refund cannot exceed the captured amount

Also name the primary failure mode: duplicate write, incompatible client,
incorrect migration, overload, data leak, or wrong authorization.

### 2. Shape the change for review

Keep one clear intention per pull request where possible. Make state changes,
database writes, network calls, and asynchronous publication visible. Avoid
mixing unrelated cleanup with a risky behavior change.

Use [Git](../topics/git/README.md) for history and collaboration safety and
[clean-code and review guidance](../topics/testing/02-clean-code-and-code-review.md)
for code shape.

### 3. Verify the boundary that can actually fail

Choose tests by risk, not by habit:

- business branching or pure mapping: focused unit test
- database, serialization, security, or transaction behavior: integration test
- independently deployed API or event consumers: contract and compatibility test
- retry-sensitive write: duplicate and concurrent-request test

Use [testing strategy](../topics/testing/01-testing-strategies.md),
[idempotency](../topics/databases/01-idempotency-and-transaction-safety.md), and
[contract evolution](../topics/api/08-contract-testing-and-api-evolution.md)
for the deeper choices.

### 4. Check compatibility before rollout

Ask whether old and new versions can run at the same time. This matters for:

- additive versus breaking API changes
- producer and consumer event changes
- schema expand, backfill, and later contract steps
- cache keys and serialized values

Strong default: make the new shape acceptable first, deploy code that supports
both shapes, migrate or backfill, and remove the old shape only after it is no
longer used. See [schema migrations](../topics/spring-boot/19-flyway-and-schema-migrations.md).

### 5. Release deliberately

Pick the smallest release method that fits the blast radius. A normal rolling
deployment can be enough. For a payment path, schema change, or high-traffic
behavior change, define a canary or rollback threshold first.

The important question is not which deployment pattern has the best name. It
is whether you can stop user impact before it becomes broad. See
[zero-downtime deployments](../topics/devops/02-zero-downtime-deployments.md).

### 6. Watch the behavior, then mitigate or continue

Before release, choose a few signals tied to the change:

- checkout success rate and payment-provider timeout rate
- duplicate-request rejection rate
- database error and connection-wait rate
- queue depth and consumer failure rate

Logs help investigate. Metrics and traces show whether the system is degrading.
If the signal crosses the agreed threshold, stop rollout, roll back, disable a
safe feature path, or shed non-critical work. See
[observability](../topics/devops/03-observability-and-monitoring.md) and
[incident triage](../topics/sre/04-incident-response-and-triage.md).

### 7. Close the loop

After a meaningful incident or near miss, capture the decision, the missing
guardrail, and one concrete follow-up. Do not write a postmortem for ceremony;
write one when it changes a test, alert, limit, runbook, or release check.

## Strong Default

Prefer the smallest compatible and observable change. A change is not safer
because it is small in lines of code; it is safer when its invariant, failure
mode, verification, rollout, and recovery are proportionate to its risk.

## Reusable Takeaway

> A senior backend change is not finished at merge. I define the invariant,
> verify the boundary that can fail, keep the rollout compatible, watch the
> production signal, and know the first recovery action.
