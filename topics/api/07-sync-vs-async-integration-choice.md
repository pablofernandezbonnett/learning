# Sync vs Async Integration Choice

Many API and integration design discussions go wrong because teams ask:

- should this be REST or event-driven?

That is too vague.

The better question is:

- which parts need a synchronous answer now, and which parts become safer or cheaper as asynchronous work later?

---

## Why This Matters

This choice affects two things at once:

- user-facing behavior
- failure behavior under retries, slow dependencies, and partial outages

A weak integration choice often produces APIs that look simple on a diagram but
become fragile in production.

---

## Smallest Mental Model

- synchronous integration = the caller waits for the callee to finish now
- asynchronous integration = the caller hands work off and final completion happens later

Short version:

- sync gives immediate certainty when you truly need it
- async gives safer decoupling when later work should not block the caller

The job is deciding where that boundary belongs.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- async is more scalable, so we should prefer it by default

Why weak:

- async adds state transitions, retries, replay, and observability burden

Another bad mental model:

- synchronous APIs are simpler, so keep everything in one request

Why weak:

- one request can become too coupled to several later systems and fail for the wrong reasons

Better mental model:

- keep only the user-critical confirmation synchronous
- move slow, fan-out, or replay-heavy work behind an async boundary

That is usually the strongest default.

---

## Use Synchronous Integration When

Use sync when the caller truly needs the answer now.

Typical cases:

- validate credentials
- fetch price before showing checkout total
- reserve something that must be confirmed before the next user action
- ask a dependency for data that directly shapes the current response

Best approach:

- keep the sync path short
- set clear timeouts
- know what fallback or pending state exists if the dependency is slow

Tradeoff:

- simpler user flow
- tighter coupling to dependency latency and failure

---

## Use Asynchronous Integration When

Use async when the follow-up work does not need to block the current response.

Typical cases:

- send email
- update analytics
- refresh search index
- trigger fraud review
- process webhooks or partner callbacks

Best approach:

- make the critical local write durable first
- acknowledge quickly
- hand later work to queue, broker, or worker flow

Tradeoff:

- lower user-facing latency and better buffering
- more state transitions, retries, replay, and delayed completion reasoning

---

## Problem -> Better Boundary Choice

### 1. User needs final confirmation now

Example:

- login success
- card authorization response shown immediately

Stronger default:

- synchronous boundary

Reason:

- the user action is blocked on the answer itself

### 2. User can accept `PENDING`

Example:

- payment provider timed out
- final confirmation may come from webhook later

Stronger default:

- synchronous request returns `PENDING`
- async confirmation finalizes later

Reason:

- pretending certainty is weaker than exposing honest pending state

### 3. One business fact triggers several later actions

Example:

- order placed
- analytics, warehouse, and email all care

Stronger default:

- critical write now
- async fan-out later

Reason:

- this is follow-up work, not part of the immediate user answer

### 4. Dependency is slow, flaky, or rate-limited

Example:

- external provider or partner API

Stronger default:

- minimize what must stay synchronous
- protect the user path with timeout, fail-fast, or pending state

Reason:

- one weak dependency should not own the whole user request if it does not need to

---

## Small Concrete Example

Problem:

- checkout creates an order
- payment provider confirmation may arrive later
- email and analytics should not slow the user path

Possible solutions:

1. order creation, payment confirmation, email, and analytics all happen synchronously
2. order is saved, and later work is pushed async, but payment state is lied about as final
3. order is saved durably, user gets `PENDING` when provider certainty is missing, later work happens async after the durable write

Best approach:

- option 3

Why:

- it keeps the response honest
- it protects the current request from too much coupling
- it still allows later systems to react safely

Patterns that usually appear:

- idempotency on the request
- webhook deduplication or consumer idempotency later
- outbox if state change and event publication must stay coordinated

---

## Strong Defaults

- if the user needs the answer now -> synchronous
- if the work is follow-up or fan-out -> asynchronous
- if the provider result is uncertain -> return `PENDING`, do not invent false success
- if the local write and the event must stay coordinated -> outbox
- if the same callback or event may arrive twice -> assume at-least-once and design idempotently

These defaults are usually better than forcing one style everywhere.

---

## Interview-Ready Cases

If the interviewer asks:

- "should checkout call warehouse synchronously?"
  answer: usually no, unless the user truly needs final warehouse confirmation now

- "what if the provider times out after we sent the request?"
  answer: keep durable local state, return `PENDING`, reconcile through webhook or later confirmation

- "when do you move work behind a broker?"
  answer: when follow-up work should not block the user path or when multiple consumers care about the same fact

- "what is the danger of making everything async?"
  answer: more state transitions, delayed failure, idempotency burden, and harder observability

The strongest interview answer explains the boundary, not only the transport.

---

## 20-Second Answer

> Keep only the user-critical certainty synchronous. Move slow, fan-out, or replay-heavy follow-up work behind an asynchronous boundary. If the truth is uncertain, return `PENDING` instead of pretending success. Good integration design is mostly about putting that sync-async boundary in the right place.

---

## What To Internalize

- sync vs async is a failure-behavior choice, not only a transport choice
- synchronous certainty is valuable, but expensive when overused
- async decoupling is valuable, but not free
- `PENDING` is often stronger than lying about final success
- the best integration boundary is the smallest one that protects the user path
