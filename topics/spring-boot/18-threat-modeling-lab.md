# 15. AppSec Lab — Threat Modeling a Checkout and Admin Flow

This lab is a guided exercise, not a coding task.

It teaches a senior AppSec habit:

> think about the dangerous transitions in the system before you discuss controls.

Use it with a notebook or plain markdown.

---

## Scenario

You own a simplified commerce backend with these flows:

- customer logs in
- customer places an order
- system reserves inventory
- payment is authorized
- order is confirmed
- support agents can issue refunds

Admin/support tooling exists in the same backend environment.

---

## Step 1. List the Important Assets

Write down at least these:

- user account identity
- order data
- inventory state
- payment authorization state
- refund capability
- admin/support privileges
- webhook signing secret

If an asset is important, ask:

- who can read it
- who can change it
- what happens if it is wrong

---

## Step 2. Draw Trust Boundaries

Minimum boundaries:

- browser/mobile -> public backend
- public backend -> payment provider
- payment provider -> webhook endpoint
- support/admin actor -> privileged backend actions

You only need a quick diagram.

---

## Step 3. Walk the Flow

Use this baseline:

1. login
2. create checkout
3. reserve stock
4. authorize payment
5. confirm order
6. capture/refund later

Ask at each step:

- what input is trusted here
- what state can be abused here
- what identity is assumed here

---

## Step 4. Fill the Threat Table

Use a table like this:

| Flow step | Threat | Control | Detection |
|---|---|---|---|
| reserve stock | scripted inventory hoarding | TTL (time to live) + per-user limits + rate limits | alert on repeated reservation churn |
| payment webhook | replay | signature + timestamp + event-id dedupe | security log on duplicates |
| read order | broken object level authorization (BOLA) | object-level authorization | authorization test failures |
| refund | privilege abuse | stronger authz + audit log | alert on unusual refund rate |

Write at least 8 rows.

---

## Step 5. Include Business Abuse, Not Only OWASP (Open Worldwide Application Security Project) Labels

At least half of your threats should be business abuse cases.

Examples:

- reserving scarce inventory without intent to buy
- abusing coupon or promotion order
- support agent refunding outside policy
- calling confirm/ship/refund endpoints out of order
- replaying a real webhook

This is what makes the exercise useful for product systems.

---

## Step 6. Decide What To Test

For each important threat, decide whether it should be caught by:

- unit/integration tests
- runtime guardrails
- logging and alerting
- operational review

If there is no test or detection path, the control is weak.

---

## Suggested Output

By the end of the exercise you should have:

- one simple diagram
- one threat table
- three highest-risk issues called out clearly
- one sentence on what you would fix first

---

## Strong Answer Example

Good summary:

> The highest-risk areas are object-level authorization on orders, replay-safe
> payment webhooks, and abuse of inventory reservation. They affect customer
> trust, revenue, and operational correctness directly, so I would prioritize
> those before lower-impact technical hardening.

---

## Completion Checklist

- Did you list the real assets, not generic ones?
- Did you include trust boundaries?
- Did you include business abuse cases?
- Did you connect threats to controls?
- Did you connect controls to tests or detection?

---

## What You Should Be Able to Say After This Lab

> I use threat modeling to make security practical. I identify the assets,
> trust boundaries, and state transitions in the flow, then convert the main
> risks into explicit controls, tests, and detection signals.

---

## Related Reading

- [../Security/06-threat-modeling-and-business-abuse.md](../../Security/06-threat-modeling-and-business-abuse.md)
- [../Security/04-web-and-api-security.md](../../Security/04-web-and-api-security.md)
