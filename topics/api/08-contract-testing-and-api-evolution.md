# Contract Testing and API Evolution

API contracts do not mainly fail because people forgot HTTP.
They fail because independently changing systems drift apart.

This note keeps contract testing and contract evolution practical.

---

## Why This Matters

If two systems evolve independently, one of the most expensive failures is:

- provider changes shape
- consumer still expects old shape
- integration breaks late

This matters because:

- end-to-end tests are often too slow or too brittle
- versioning alone does not protect every internal boundary
- many teams break contracts through normal refactors, not dramatic redesigns

---

## Smallest Useful Mental Model

Use this split:

- **contract**: the request and response behavior another system depends on
- **contract test**: early proof that provider and consumer still agree
- **contract evolution**: how that contract changes without surprising consumers

Short rule:

> Contract testing catches shape drift early. Contract evolution avoids creating unnecessary breakage in the first place.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- if the provider tests pass, the integration is safe
- versioning solves all contract problems
- contract testing replaces integration testing completely

Better mental model:

- provider tests do not prove consumer expectations
- additive change is often safe, breaking change is where versioning matters
- contract tests sit between isolated tests and end-to-end flows

Small concrete example:

- weak approach: provider renames `firstName` to `name`, all local tests pass,
  consumer breaks later
- better approach: contract test fails in `CI` before the provider change ships

---

## 1. What Contract Testing Actually Solves

Contract testing helps when:

- one service calls another
- one UI depends on a backend response shape
- one provider emits events or webhooks another system consumes

It answers:

- does the consumer assumption still match the provider promise

It does **not** answer everything:

- auth works end to end
- network is healthy
- business flow is correct across many systems

That is why contract tests are helpful, not magical.

---

## 2. Consumer-Driven Contract Testing In Plain Language

`Consumer-driven` means the consumer defines the response shape it actually
needs.

Why this is useful:

- provider may return more than one consumer needs
- the important question is whether the provider still supports the fields and
  semantics that consumer depends on

Good fit:

- internal service boundaries
- important backend dependencies
- event or webhook consumers

---

## 3. Versioning vs Backward-Compatible Evolution

This is where many teams overreact.

### Usually safe

- adding a new optional response field
- adding a new endpoint
- adding a new event field that consumers can ignore safely

### Usually breaking

- renaming or removing fields
- changing field meaning
- changing required request shape
- changing status code or error shape that consumers depend on

Short rule:

> Version only for real breaking change. For normal growth, prefer additive evolution.

---

## 4. Contract Testing Across Different Boundaries

### Service-to-service HTTP

Good fit:

- request and response shape matters
- independent deployment exists

### Event contracts

Good fit:

- one service publishes business events
- consumers depend on field shape and meaning

### Webhooks

Good fit:

- your system is the consumer of someone else's callback contract
- or your system is the provider and third parties depend on your callback shape

The contract idea still applies even when the transport is not direct request
response.

---

## 5. Strong Default

Use this default:

1. design contracts to evolve additively where possible
2. use contract tests on important independent boundaries
3. keep a few end-to-end flows for full integration risk
4. version only when the change is genuinely breaking

That is stronger than:

1. version every little change
2. rely only on provider unit tests

---

## 6. Concrete Example

Imagine:

- `Order Service` calls `Customer Service`
- it needs `customerId`, `firstName`, and `status`

Weak evolution:

- provider renames `firstName` to `name`
- provider local tests pass
- order service fails later

Better evolution:

- contract test proves `firstName` is still present if that is what consumers
  depend on
- provider adds `name` first
- consumers migrate
- old field is removed only after a planned breaking change path

This is boring in a good way.
It turns surprise breakage into planned change.

---

## 7. Internal Services vs Public APIs

### Internal service contracts

Can often evolve faster, but still need discipline.

Good tools:

- contract tests
- additive changes
- explicit compatibility windows

### Public APIs

Need more caution because:

- consumers are slower to change
- compatibility expectations are stronger

Good tools:

- clearer versioning policy
- deprecation windows
- migration notes

Same principle, different tolerance for breakage.

---

## 8. Where Contract Tests Sit In The Test Stack

Contract tests are stronger than:

- provider-only unit tests

But narrower than:

- full integration or end-to-end tests

They are best used when:

- boundary shape drift is the risk you want to catch early

Short rule:

> Use contract tests for boundary compatibility, not as a replacement for all integration testing.

---

## 9. Big Traps

1. **Versioning every additive change**
   Example: too much churn for no real compatibility gain.

2. **Skipping contract tests because provider tests are green**
   Example: consumer expectations still drift.

3. **Treating contract tests as enough for full system confidence**
   Example: auth, timeouts, and orchestration failures are still uncovered.

4. **Changing field meaning without changing field name**
   Example: contract looks compatible but semantics broke.

5. **Letting event contracts drift without ownership**
   Example: one publisher change silently breaks several consumers.

---

## 10. Practical Summary

Good short answer:

> I use contract tests when an independent boundary matters enough that request, response, or event shape drift would hurt us, but full end-to-end tests would be too slow or brittle. I try to evolve contracts additively, version only when the change is truly breaking, and treat contract tests as boundary-compatibility checks rather than total integration proof.
