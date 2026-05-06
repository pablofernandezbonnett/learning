# Requirements And NFRs

Many weak solution designs fail before implementation starts.
The design looks clean, but it solved the wrong problem or ignored the constraints that actually shape the system.

This note is about the first job of solution design:

turning vague asks into usable architectural inputs.

---

## Why This Matters

Many weak designs are technically tidy but strategically wrong.
They solved the visible feature request while ignoring the qualities and
constraints that decide whether the solution is actually acceptable.

That is why requirements discovery is architecture work, not only project
management work.

---

## Smallest Mental Model

- functional requirements say what the system must do
- `NFRs` (`non-functional requirements`) say what qualities and constraints the solution must preserve while doing it

Features tell you what exists.
`NFRs` often decide which designs should be rejected.

---

## 1. Functional Requirements vs NFRs

Functional requirements are what the system must do.

Examples:

- customers can place orders
- merchants can approve quotes
- admins can export invoices

`NFRs` means `non-functional requirements`.
They describe the qualities and constraints the solution must preserve while doing that work.

Simple definition:

- functional requirement = what the system does
- `NFR` = how safely, how fast, how isolated, or under what limits it must do it

Examples:

- 99.9% monthly checkout success
- EU customer data stays in the EU
- no double charge on retry
- audit trail retained for 7 years
- partner integration should recover from duplicate webhook delivery

Both matter.
But `NFRs` often decide the shape of the architecture more than the feature list does.

Why:

- many different designs can implement the same feature
- the `NFRs` are often what eliminate the weak ones

---

## 2. Two Bad Starting Points

Bad starting point 1:

- "we need a scalable modern architecture"

Why weak:

- it says almost nothing usable

Bad starting point 2:

- "we need microservices"

Why weak:

- it jumps to a solution style before naming the real requirements

Better starting point:

- what is the business flow?
- what must never go wrong?
- what scale, latency, isolation, and compliance constraints actually exist?

That is where defensible architecture begins.

---

## 3. The NFRs That Usually Matter Most

Not every project needs the same `NFRs`.
But these usually shape backend and solution design strongly:

- availability
- latency
- consistency and correctness
- security
- privacy and data residency
- auditability
- tenant isolation
- operability
- cost
- migration risk

Plain-English version:

An `NFR` is a limit, quality bar, or rule that changes what solutions are acceptable.

That is why `NFRs` are not background notes.
They are usually decision filters.

---

## 4. Why NFRs Change Architecture

The same feature set can lead to very different solutions depending on the `NFRs`.

Example:

- "customers can upload documents"

If the only concern is basic storage:

- object storage plus metadata table may be enough

If the `NFRs` add:

- virus scanning
- EU-only data residency
- 7-year retention
- role-based download auditing

then the solution shape changes a lot.

This is why solution architecture must ask about constraints early.

The core idea:

- same feature
- different constraints
- different defensible architecture

---

## 5. Good Discovery Questions

Useful early questions include:

- which business flows are critical?
- which failures are unacceptable?
- what is the expected traffic and peak shape?
- what data is sensitive or regulated?
- do tenants need soft separation or strong isolation?
- what recovery time is acceptable?
- what existing systems must this fit around?
- how fast must the first version ship?

These questions often matter more than choosing a framework.

---

## 6. Good vs Weak NFR Handling

Weak handling:

- list many `NFRs`
- never rank them
- treat all of them as equally critical

Why weak:

- it produces confusion and over-design

Better handling:

- identify the few constraints that truly shape the architecture
- rank them
- explain the tradeoff when two of them pull in different directions

Example:

- fastest delivery may conflict with highest isolation
- lowest cost may conflict with strongest regional separation

Architecture gets clearer when those tensions are named explicitly.

Best short test:

- if a requirement does not help you reject a weak design, it is still too vague

---

## 7. Example: B2B Ordering Platform

Suppose the functional requirement is:

- merchants can place and manage bulk orders

Possible solution-shaping `NFRs`:

- support account hierarchies and approvers
- preserve tenant isolation between merchants
- keep quote and order history for audit
- allow regional data residency differences
- keep checkout and pricing fast during catalog spikes

These are not side notes.
They may decide:

- database shape
- tenant model
- integration strategy
- deployment topology
- monitoring and audit design

---

## 8. Best Approach To Early Discovery

A strong early solution pass usually does this:

1. describe the main business flow
2. name the invariants, meaning what must never break
3. identify the top `NFRs`
4. identify the hard external constraints
5. separate must-have constraints from nice-to-have qualities
6. only then compare solution options

This sequence prevents premature design.

---

## 9. Common Mistakes

Common mistakes:

- accepting vague requirements as if they were precise
- treating every quality concern as equally important
- ignoring migration and operating constraints
- asking only engineering questions and missing legal or business ones
- discovering data residency or audit constraints too late

Another common mistake:

- writing `NFRs` as generic slogans such as "highly scalable" or "secure"

If a requirement is too vague to reject a bad design, it is probably too vague to help.

---

## 10. A Simple Requirement Shape

For a first architecture pass, capture:

- business goal
- primary users
- critical flows
- invariants
- top `NFRs`
- external constraints
- known dependencies
- migration realities

This is enough to make architecture discussion much stronger than starting from a target-state diagram alone.

---

## 11. 20-Second Answer

> Functional requirements say what the system must do. `NFRs` say what qualities and constraints the solution must preserve while doing it.
> In solution architecture, `NFRs` often shape the design more than the feature list does, so they need to be discovered, ranked, and made explicit early.

---

## 12. What To Internalize

- vague requirements produce vague architecture
- `NFRs` are often the real shape of the system
- ranking constraints is more useful than collecting a long wish list
- a requirement is only useful if it helps reject weak design options
