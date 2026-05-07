# Threat Modeling and Business Abuse

Threat modeling is one of the most useful AppSec skills for a senior engineer
because it moves security earlier, before code and incidents.

It is also where backend/product engineers have leverage: you understand the
real workflow, the real data, and the real failure modes.

---

## Why This Matters

Threat modeling is one of the fastest ways to make security practical before an
incident happens. It turns vague security concerns into specific questions about
assets, trust boundaries, state transitions, and abuse paths.

This matters because strong product security work usually comes from catching a
bad design early, not from discovering it later in production.

## Smallest Mental Model

Treat threat modeling as a structured way to ask:

- what are we protecting
- where are the boundaries
- how could this flow be abused or broken
- which control or test would reduce that risk

## Bad Mental Model vs Better Mental Model

Bad mental model:

- threat modeling is a heavyweight enterprise ritual
- it only matters for infrastructure or crypto-heavy systems
- vulnerability lists are enough

Better mental model:

- threat modeling is a lightweight design review habit
- product and workflow abuse often matter more than low-level exploit theory
- the goal is to turn risks into explicit controls, tests, and alerts

Small concrete example:

- weak approach: "checkout needs auth and input validation"
- better approach: "checkout must prevent step skipping, inventory hoarding,
  webhook replay, and duplicate payment actions, so we add state validation,
  rate limits, replay protection, and monitoring"

Strong default:

- for any important flow, identify the asset, boundary, dangerous state
  transition, and likely abuse path before implementation gets too far

Interview-ready takeaway:

> I use threat modeling as a lightweight way to map assets, boundaries, and
> abuse paths, then convert them into explicit controls and tests.

---

## 1. What Threat Modeling Is

Threat modeling is a structured way to answer four questions:

1. What are we building?
2. What can go wrong?
3. What are we going to do about it?
4. Did we cover the important risks?

You do not need a huge formal process to get value from it.

For product teams, a lightweight threat model is usually enough.

---

## 2. Minimal Threat Modeling Workflow

### Step 1. Define the assets

Examples:

- user accounts
- order data
- inventory state
- payment authorization state
- webhook secrets
- admin capabilities

### Step 2. Define trust boundaries

Examples:

- browser or mobile app to backend
- backend to payment provider
- public API to internal services
- support/admin tooling to production systems

### Step 3. Map the critical flow

Concrete example:

cart -> reserve inventory -> authorize payment -> create order -> capture later

### Step 4. Ask what can go wrong

This is where STRIDE is useful:

- Spoofing: who can pretend to be someone else?
- Tampering: what data can be modified?
- Repudiation: what cannot be proven later?
- Information Disclosure: what can leak?
- Denial of Service: what can be exhausted?
- Elevation of Privilege: who can do too much?

### Step 5. Turn threats into controls and tests

Each threat should lead to:

- a design decision
- an implementation control
- a test or alert

---

## 3. Business Abuse Matters

For commerce and SaaS systems, the biggest damage often comes from business
abuse, not only classic vulnerability categories.

Examples:

### Out-of-Order Checkout

An attacker calls `confirm` before `pay`.

Control:

- server-side workflow state validation

### Inventory Hold Abuse

An attacker reserves stock repeatedly to block real buyers.

Control:

- reservation limits, TTLs, anti-abuse heuristics, and rate limits

### Promotion and Coupon Abuse

A caller applies discount logic outside intended rules.

Control:

- server-side promotion validation and idempotent order pricing

### Webhook Replay

A valid payment or fulfillment webhook is replayed.

Control:

- signature verification, timestamp tolerance, replay protection, idempotency

### Order or Refund ID Tampering

A user changes an order ID or refund ID and accesses another user's object.

Control:

- object-level authorization on every request

### Admin or Support Tool Overreach

Internal tools bypass normal business protections.

Control:

- stronger auth, audit logs, separation of duties, narrow privileges

---

## 4. A Good Output Artifact

A useful threat model does not need fancy tooling.

A simple table is enough:

| Asset / Flow | Threat | Control | Test / Detection |
|---|---|---|---|
| Checkout confirm | Step skipping | state machine validation | integration test + alert on invalid transitions |
| Payment webhook | replay | signature + timestamp + idempotency | replay test + security log |
| Order lookup | BOLA | object-level auth | authorization tests |

If the table is concrete, it is already useful.

---

## 5. Interview Framing

Good short answer:

> I use threat modeling to make security practical. I identify the assets, trust
> boundaries, and dangerous state transitions in the flow, then turn those risks
> into explicit controls and tests. In product systems, business abuse cases are
> often more important than abstract vulnerability lists.

---

## 6. Further Reading

- OWASP Threat Modeling Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Threat_Modeling_Cheat_Sheet.html
- OWASP Authorization Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html
- OWASP Transaction Authorization Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Transaction_Authorization_Cheat_Sheet.html
- OWASP ASVS: https://owasp.org/www-project-application-security-verification-standard/
- AWS Well-Architected Security Pillar: https://docs.aws.amazon.com/wellarchitected/latest/framework/security.html
