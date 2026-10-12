# Protecting a Service From Dependency Failures

When a dependency slows down, the caller can fill its connections, workers,
and waiting queue. Retries can then add more load to the same failing path.
Resilience means bounding that damage while preserving the right business
outcome.

## Start With the Failure Contract

For each outbound call, ask:

- Does the caller need the answer now, and by what deadline?
- If the call times out, could the remote action already have succeeded?
- Is another attempt safe, and how will both sides recognize it as the same
  operation?
- Can this feature return a partial result, or would that misstate a payment,
  stock reservation, or other critical fact?

A timeout is an **unknown outcome** for a write, not proof that it failed.
For retryable writes, use a stable operation or idempotency key and a way to
check the eventual result. See [idempotency and transaction
safety](../databases/01-idempotency-and-transaction-safety.md).

## Controls and Their Costs

| Control | What it protects | Main failure if misused |
| --- | --- | --- |
| Deadline and per-call timeout | Bounds how long one request waits and holds capacity | Too short causes false failures; too long exhausts the caller |
| Limited retry with backoff and jitter | Recovers temporary failures without synchronized retries | Retries amplify overload or repeat a business effect |
| Circuit breaker | Stops repeated calls to a dependency that is clearly failing | Poor thresholds reject useful work or recover too slowly |
| Bulkhead or concurrency limit | Keeps one dependency or optional path from consuming all workers or connections | A limit set too low creates its own bottleneck |
| Fallback | Returns a useful reduced result for an optional feature | A fallback can hide a correctness failure |

**Backoff** increases the wait between attempts; **jitter** varies it so many
callers do not retry together. A **circuit breaker** fails fast while a
dependency is unhealthy and later permits limited probes. A **bulkhead**
separates or limits capacity for a path. These controls do not repair the
dependency; they protect the caller while recovery happens.

Set a deadline for the whole user request, then allocate smaller budgets to
its calls. Three calls with independent long timeouts can exceed the user's
deadline even if each is configured. Count retries inside that budget.

## Small Example: Checkout

Checkout asks a payment provider to authorize one payment. The provider does
not respond before the deadline. Checkout cannot assume the authorization
failed. It records an unresolved state and checks the result using the same
payment attempt ID before trying again. It does not tell the user that payment
succeeded until the result is known.

Recommendations on the same page are optional: they can have a short timeout
and disappear when their service is unavailable. They should also have a
separate concurrency limit so slow recommendations do not consume checkout
capacity. This is a safe fallback because it removes decoration, not a
business-critical fact.

## Avoid a Retry Storm

Retry only failures likely to be temporary, and only when the operation is
safe to repeat. Bound attempts, spread them with backoff and jitter, and stop
when the request deadline or dependency capacity is exhausted. Do not retry
validation failures or business rejections. Watch retry volume alongside
dependency errors; a rising retry rate can make recovery harder.

At system capacity limits, reject or delay less important new work instead
of accepting work that will time out later. [Capacity planning and load
shedding](../sre/05-capacity-planning-and-load-shedding.md) covers that
system-level decision; this note covers the dependency call.

## Decision Rule

Bound waiting first. Retry only safe temporary failures within a deadline.
Isolate critical capacity, fail fast when continued calls add harm, and
degrade only features whose absence does not misstate business truth.
