# System Design Guide

> Primary fit: `Shared core`

Use this when you want a repeatable structure for a real backend or system
design prompt.

Practice next:

- [system-design-drills.md](./system-design-drills.md)
- [distributed-workflow-case-studies.md](./distributed-workflow-case-studies.md)

This is not a company-specific playbook.
It is the shared execution guide for questions such as:

- design a payment or checkout flow
- prevent duplicate processing
- handle partial failure across systems
- explain a correctness-critical write path

Use it as a general guide for correctness-critical backend design.

---

## Why This Matters

Many system design answers sound organized but still miss the real job:
protecting correctness when retries, timeouts, partial failures, and external
systems are involved.

This guide matters because strong answers are not mainly about drawing many
components. They are about showing that you can protect the business invariant,
name the source of truth, and explain what the system does when something goes
wrong.

## Smallest Mental Model

Treat system design as an exercise in protecting one important write path under
real failure.

That usually means:

- name what must stay true
- name where final truth lives
- walk the write path
- explain retries, async work, and recovery

## Bad Mental Model vs Better Mental Model

Bad mental model:

- system design means listing technologies
- more boxes means a more senior answer
- adding Kafka, Redis, or microservices early makes the design stronger

Better mental model:

- system design means making one business flow safe, explainable, and operable
- components only matter after the invariant and source of truth are clear
- the answer gets stronger when failure and recovery are explicit

Small concrete example:

- weak answer: "checkout API -> Redis -> Kafka -> payment service -> database"
- stronger answer: "one purchase intent must not create two paid orders; the
  order and payment attempt tables are the source of truth; if the provider
  times out, the order stays `PENDING` and later moves by webhook or
  reconciliation"

Interview-ready takeaway:

> In backend system design, I first anchor on the invariant, the source of
> truth, and the critical write path. Then I explain retries, async boundaries,
> and recovery before I add scale components.

---

## 1. What A Strong Answer Must Show

A strong backend system design answer usually proves seven things:

1. you clarified the real business risk
2. you identified the critical write path
3. you named the source of truth
4. you separated synchronous work from asynchronous work
5. you explained retries, timeouts, and duplicate prevention
6. you explained observability and recovery
7. you stated tradeoffs instead of pretending there is a perfect design

Short rule:

> if the answer never names the source of truth, failure mode, or recovery path,
> it is still incomplete

---

## 2. The 15-Minute Answer Shape

Use this as the default flow in a live design review or practice session.

### Minute 0 to 2. Clarify the prompt

Ask only the questions that change the design:

- what is the expected peak traffic
- what latency matters most to the user
- what must be strongly correct
- is this greenfield or an existing system
- which external systems or providers are involved
- is the final result synchronous or can it be asynchronous

Good opening:

> Before I add components, I want to clarify where strong correctness matters,
> what the peak traffic looks like, and whether final confirmation is synchronous
> or asynchronous.

### Minute 2 to 4. Name the invariant and the source of truth

State early:

- what must never happen twice or end in an impossible state
- what final state must remain trustworthy
- which durable store owns that truth

In this repo, `invariant` means the business rule that must always remain true.
Examples: "do not charge twice", "do not oversell final stock", or "one purchase
intent should not create two final orders."

`Source of truth` means the durable place whose final state you trust when a
cache, worker, or downstream system disagrees.

Examples:

- payment: do not charge or capture twice
- checkout: do not create two orders from one purchase intent
- inventory: do not oversell final stock

### Minute 4 to 7. Draw the simplest high-level shape

Start with a small sketch:

Client  
-> API  
-> service or orchestrator  
-> source-of-truth store  
-> external dependency or async path

Only add:

- cache if read scale needs it
- broker if async durability or fan-out needs it
- worker if background processing is real

Rule:

> do not add Kafka, Redis, or microservices until you can explain why the prompt needs them

### Minute 7 to 10. Walk the critical write path

This is the center of the answer.

State:

1. request identity
2. validation
3. durable write
4. external call if any
5. valid state transitions
6. response behavior under timeout or uncertainty

Questions to answer explicitly:

- where does idempotency live
- what happens if the client retries
- what happens if the provider times out
- what happens if the same callback or event arrives twice

### Minute 10 to 12. Separate async work and recovery

State what happens later:

- webhook or callback confirmation
- outbox publication
- downstream consumers
- notifications
- reconciliation

Say clearly:

- local transaction rollback is not a global rollback
- distributed flows need replay-safe handling
- recovery may mean compensation, retry, or reconciliation

### Minute 12 to 14. Explain scale and hotspots

Now discuss where the pressure goes first:

- read-heavy vs write-heavy path
- hot keys or hot rows
- lock contention
- queueing or admission control
- cache placement

### Minute 14 to 15. Close with observability and tradeoffs

Mention:

- logs with correlation IDs
- metrics for latency, errors, retries, and backlog
- traces across the critical path
- business metrics such as checkout success, payment success, or stuck pending count

Then name one tradeoff:

- stronger correctness vs lower throughput
- simpler synchronous API vs safer async confirmation
- fresher data vs lower latency

Strong default:

- start with one clear source of truth, one safe write path, and one explicit
  recovery story
- add brokers, caches, projections, or service splits only when the prompt
  really needs them

Main tradeoff or failure mode:

- the most common failure is not lack of technology
- it is hiding uncertainty under a fake synchronous success path and then having
  no good answer for retries, duplicate work, or timeouts

---

## 3. Default Clarifying Questions

Use these when the prompt is underspecified.

- What is the user-facing action we are protecting?
- What must be strongly correct, and what can be eventually consistent?
- What is the expected peak traffic or contention point?
- Are we integrating with an external provider or only internal systems?
- Is there already a monolith or source system we must keep?
- Do we need one final immediate answer, or is `PENDING` acceptable?
- What business outcome is worst here: duplicate action, stale read, or high latency?

Only ask `2-4` of these.
Then move into the design.

---

## 4. Default Checklist

Before you finish the answer, make sure you covered these points.

- source of truth is explicit
- invariant is explicit
- duplicate prevention is explicit
- timeout behavior is explicit
- retry behavior is explicit
- async boundary is explicit
- observability is explicit
- recovery path is explicit

Bad signs:

- cache appears before the source of truth
- you say `exactly once` as if the whole system guarantees it
- you describe the happy path only
- you use `@Transactional` as if it solves cross-system consistency

---

## 5. Worked Question: Design A Retry-Safe Payment-Adjacent Request Flow

This works well for payment-adjacent backend design.

### What this exercise emphasizes

- can you protect a money-sensitive write path
- do you understand uncertainty under timeout
- can you separate request idempotency from callback deduplication

### Good opening

> I would treat this as a correctness-critical state machine, not as a normal POST that either fully succeeds or fully fails in one synchronous step.

### What to draw first

Client  
-> Payment API  
-> Postgres  
-> Provider boundary  
-> webhook or callback path  
-> outbox  
-> downstream consumers

### Good answer order

1. clarify throughput, consistency, and timeout expectations
2. define the critical write path
3. name Postgres as the source of truth
4. explain request idempotency
5. explain provider timeout and uncertain state
6. explain async confirmation
7. explain outbox and consumer idempotency
8. explain observability and stuck-state recovery

### Safe flow

1. validate the request and require an idempotency key or stable business key
2. create a durable payment attempt in `PENDING`
3. call the provider with a provider-safe idempotent request if supported
4. if the provider returns success clearly, move state through a valid transition
5. if the provider times out or the result is uncertain, return `PENDING` instead of lying
6. confirm finality through webhook, callback, or later reconciliation
7. publish downstream work from the outbox, not from a fragile direct broker call

### Strong sentence

> I would rather return a clear pending state than claim final success while the provider boundary is still uncertain.

---

## 6. Worked Question: Design A Checkout Or Order Commit Path

This works well for commerce and checkout flows.

### What this exercise emphasizes

- do you understand the commit path across pricing, inventory, payment, and order state
- do you know what can be soft and what must be final
- can you keep the user path fast without losing correctness

### Good opening

> I would treat checkout as both a correctness path and a customer journey. The design has to protect inventory and order truth without creating unnecessary user friction.

### What to draw first

Client  
-> Checkout API  
-> Pricing  
-> Inventory reservation  
-> Payment authorization  
-> Order store  
-> async downstream work

### Good answer order

1. clarify peak traffic, stock sensitivity, and latency targets
2. define the commit path
3. name the source of truth for inventory and order state
4. separate reservation from final commit
5. explain payment authorization and duplicate-safe order creation
6. explain async downstream work
7. explain observability and reconciliation

### Safe flow

1. validate cart and pricing inputs
2. place a short-lived inventory reservation or guardrail if needed
3. authorize payment with idempotency protection
4. create the order in the source-of-truth store
5. confirm the stock change in the authoritative inventory boundary
6. publish later work asynchronously for fulfillment, analytics, and notifications
7. release reservation or compensate if failure happens before final commit

### Strong sentence

> Soft reservation can protect the user path, but the final stock and order truth still have to go through the authoritative write path.

---

## 7. Worked Question: Design A User Action That Must Not Be Processed Twice

This is the most reusable shared prompt.
It fits payments, orders, coupon redemption, payout requests, or any user action
with duplicate risk.

### What this exercise emphasizes

- do you know how to make an action replay-safe
- do you understand uniqueness, state transitions, and idempotent retries
- can you keep the answer generic without becoming vague

### Good opening

> My first concern is not scale. It is defining the business key and making sure the same action cannot produce the business effect twice.

### What to draw first

Client  
-> Action API  
-> source-of-truth table with unique business key  
-> async worker if needed  
-> processed-event tracking on the consumer side

### Good answer order

1. define what counts as the same action
2. protect that identity with a unique key or idempotency key
3. persist the action in durable storage
4. move through explicit states such as `RECEIVED`, `PROCESSING`, `COMPLETED`, `FAILED`
5. return the existing result on duplicate retry when appropriate
6. make downstream events replay-safe too

### Safe implementation shape

- one stable business key such as `user_id + action_type + request_id`
- one unique constraint in the source-of-truth store
- one state machine so repeated success does not apply the side effect twice
- one processed-event table for duplicate callbacks or broker delivery

### Strong sentence

> I assume duplicate delivery can happen, so I make the business effect idempotent instead of trusting the transport to be perfect.

---

## 8. Safe Lines To Reuse

- `I want to start with the invariant before adding components.`
- `The first HTTP response is not always the final truth.`
- `Redis can help with speed or coordination, but it is not the final source of truth here.`
- `I would keep the critical state change durable first, then handle later work asynchronously.`
- `A local transaction can rollback local state, but it does not give me a global rollback across systems.`
- `I assume retries and duplicate delivery are normal, so I design the processing path to be replay-safe.`

---

## 9. Related Use

Use this guide together with topic material on:

- transactions and idempotency
- webhook handling
- distributed flows
- caching and consistency
- observability and recovery
