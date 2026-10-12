# Code Review for Safe Changes

Readable code lets a reviewer follow the business decision, the state it changes, and every external effect. A short method is not automatically safe: a payment call hidden inside a helper can still duplicate a charge after a timeout.

Use [testing strategy](./01-testing-strategies.md) to choose evidence for the risks found here.

## Trace the change

For each affected flow, ask in order:

1. **Promise:** What outcome does the caller expect?
2. **Decision:** Which rule allows or rejects the change?
3. **State:** What is committed, and who owns the source of truth?
4. **Effects:** Which database, network, queue, cache, or file operations occur?
5. **Failure:** What happens after a timeout, retry, duplicate, or partial success?
6. **Evidence:** Which test or observation would catch a wrong answer?

This order keeps naming and formatting review in proportion to correctness.

## Small example: visible boundaries

Weak shape:

```text
process(order):
  validate, call payment provider, mark paid, save order, send email
```

The method name hides several effects. More seriously, a provider may complete the charge while the local save fails. Retrying the whole method could charge again.

Clearer design sketch:

```text
authorize(orderId, operationId):
  record a durable pending attempt for operationId
  ask provider using the same idempotency identity where supported
  if result is unknown: keep attempt pending and reconcile
  if confirmed: commit the allowed order transition
  hand off confirmation notification durably
```

This is a guide, not a universal payment algorithm. The provider's contract determines how to query or retry an unknown outcome. The important review point is that every effect and recovery path has an owner. For the cross-system workflow, see [distributed transactions and events](../architecture/03-distributed-transactions-and-events.md).

## What good structure buys

- **Names carry intent.** `authorizePayment` identifies a business action; `process(data, true)` forces readers to infer it.
- **State changes are visible.** Put the transition near the rule that permits it. Avoid a mapper or getter that quietly writes state.
- **Dependencies are explicit.** A reviewer can find database writes, external calls, and publication without searching through generic helpers.
- **Errors keep their meaning.** Invalid input, a retryable dependency failure, and an unknown payment outcome need different handling.
- **Abstractions solve a real boundary.** Add an adapter when provider details leak into the domain, or a strategy when genuine policies vary. Avoid a chain of factories for one fixed case.

Use a small business type when raw values are easy to mix up, such as `OrderId` and `PaymentAttemptId`. Use a plain value when the type adds no useful constraint.

## Review checklist

- Is the business invariant and owner of the final write clear?
- Could a retry or concurrent call duplicate an effect or overwrite state?
- Are transaction, timeout, cancellation, and asynchronous handoff boundaries visible?
- Does the public API reveal the right success, rejection, or pending state?
- Do tests reach the risky boundary and cover at least one meaningful failure?
- Is the change understandable without speculative layers or duplicated logic?

For the complete change-to-production sequence, use the [safe production change loop](../../paths/safe-production-change-loop.md). This note focuses on what to inspect in the code; the path also covers rollout and recovery.
