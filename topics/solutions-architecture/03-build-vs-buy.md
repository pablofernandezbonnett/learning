# Build vs Buy

One of the most important solution-architecture decisions is not "which framework?"
It is "should we build this capability ourselves at all?"

Teams often under-discuss this because writing code feels more concrete than evaluating alternatives.
But many expensive architecture mistakes start here.

---

## Smallest Mental Model

Build-vs-buy is not mainly a feature comparison.
It is a boundary decision about differentiation, ownership, operating burden,
and how much of the capability should live inside your product at all.

That is why the same product can look attractive in a demo and still be the
wrong solution in production.

---

## 1. What Build vs Buy Really Means

Build means:

- your team implements and operates the capability

Buy means:

- your team adopts an external product, managed platform, or vendor service

There is also a middle ground:

- integrate and extend

That means you use a vendor or managed capability, but keep some business logic or orchestration in your own system.

This middle ground matters because most real solution decisions are not pure extremes.

---

## 2. Two Bad Defaults

Bad default 1:

- build everything because we want control

Why weak:

- control also means ownership, maintenance, and incident burden

Bad default 2:

- buy everything because managed services are modern

Why weak:

- external products can create lock-in, constraint mismatch, and hidden operating costs

Better default:

- evaluate where custom differentiation matters, where commodity capability is enough, and where the operating burden belongs

---

## 3. Questions That Usually Decide The Outcome

Useful questions:

- is this capability core to our business differentiation?
- do we need custom behavior that products rarely fit well?
- how quickly do we need the first working version?
- who will operate this in production?
- what compliance or residency constraints apply?
- what data and workflow coupling will exist with our own systems?
- how painful would migration away from this choice be later?

These questions are often more useful than feature checklist comparison alone.

Why:

- a feature checklist shows what a product can do
- it does not automatically show what it will cost to live with

---

## 4. Where Buying Is Often Strong

Buying or using a managed capability is often strong when:

- the capability is commodity infrastructure
- the vendor solves a real burden you do not want to own
- the integration boundary is clean enough
- speed to market matters more than deep internal customization

Examples:

- email delivery
- payment processing
- object storage
- `CDN` (`Content Delivery Network`, meaning geographically distributed edge caching for static or cacheable content)
- identity provider

The point is not that these areas are trivial.
The point is that they often have heavy operational or regulatory cost if you own them yourself.

---

## 5. Where Building Is Often Strong

Building is often stronger when:

- the capability is close to product differentiation
- the workflow is deeply tied to your domain
- vendor products would force awkward compromises
- integration complexity would be almost as high as building
- long-term control of behavior matters a lot

Examples:

- pricing logic tightly tied to your market
- custom workflow orchestration
- domain-specific inventory or fulfillment rules

The best answer is often not pure build or pure buy.
It is careful boundary placement.

---

## 6. Good vs Weak Build-vs-Buy Thinking

Weak thinking:

- compare feature checklists only
- ignore migration-out cost
- ignore incident and support burden
- ignore compliance and data-shape implications

Better thinking:

- compare solution shape, operating burden, lock-in, customization fit, and migration path

A product with ten nice features may still be the weaker choice if:

- its data model fights your domain
- its API limits a critical workflow
- or leaving it later would be extremely painful

---

## 7. Example: Identity And Access

Suppose a new B2B platform needs:

- merchant accounts
- user roles
- `SSO` (`Single Sign-On`, meaning users log in through a trusted identity system instead of separate local passwords) for enterprise customers
- audit trail for auth events

Weak instinct:

- build our own auth and identity stack because user management is important

Why risky:

- auth, sessions, SSO, credential recovery, and attack resistance create heavy security and operational burden

Stronger approach:

- buy or adopt a strong identity capability
- keep domain-specific authorization and tenant rules close to your own product

That is a classic "integrate and extend" solution.

Why this is often stronger:

- identity itself is commodity-heavy and risk-heavy
- authorization rules close to your domain are usually where product specificity lives

---

## 8. Best Approach

A strong build-vs-buy pass usually asks:

1. is this a differentiating capability or a commodity one?
2. what operating burden comes with owning it?
3. what constraints make external products awkward?
4. what data or workflow boundaries would the choice create?
5. how reversible is the decision later?

This usually leads to better decisions than arguing from team preference alone.

---

## 9. Common Mistakes

Common mistakes:

- building commodity capability out of engineering pride
- buying a platform that solves yesterday's problem but not this domain
- ignoring contract, support, and data-exit realities
- underestimating integration complexity
- forgetting that operating burden is part of total cost

Another common mistake:

- treating license price as the whole cost

Total cost usually also includes:

- integration
- support
- migration
- incident ownership
- training

That is why a cheaper product on paper can still be the more expensive solution in practice.

---

## 10. A Simple Decision Shape

For a first decision memo, compare:

- business fit
- customization need
- time to first value
- operating burden
- compliance fit
- lock-in and exit cost
- migration complexity

That is a stronger basis than "managed is easier" or "custom gives more control."

---

## 11. 20-Second Answer

> Build-vs-buy is really a decision about differentiation, ownership, operating burden, constraints, and reversibility.
> Good solution architecture does not compare features only. It asks whether the capability should belong inside our product boundary at all, and what the long-term cost of that ownership will be.

---

## 12. What To Internalize

- control always comes with ownership cost
- managed or vendor capability is strongest when the boundary is clean and the burden is not worth owning
- build-vs-buy decisions should include exit and migration thinking, not only first-release convenience
