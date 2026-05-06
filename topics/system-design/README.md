# System Design for Backend Engineers

Use this folder for practical system design study.

The focus is not abstract box-drawing.
It is reasoning about the concrete rules and failure cases that decide
whether a backend flow is trustworthy.

Focus:

- invariants: the business rules that must always stay true, such as "do not charge twice" or "do not oversell final stock"
- source of truth: the durable system whose final state you trust when caches, retries, or downstream systems disagree
- retries and duplicate prevention: what happens when a client, worker, or provider repeats the same request
- synchronous versus asynchronous boundaries: what must finish before you answer the user and what can safely complete later
- scale hotspots: the places where load, contention, hot keys, or hot rows will hurt first
- observability and recovery: how you detect uncertainty, replay stuck work, and recover from partial failure

Working style:

- explain the first practical meaning of design jargon before layering on patterns
- keep answers close to write paths, state transitions, and recovery behavior
- prefer a small defensible design over a large diagram with vague correctness

## Recommended Order

1. [backend-system-principles.md](./backend-system-principles.md): the core rules behind safe backend design, written as short but fully explained principles
2. [practical-checkout-design.md](./practical-checkout-design.md): a concrete checkout example that shows how those rules appear in a real flow
3. [system-design-guide.md](./system-design-guide.md): a repeatable answer structure for correctness-critical backend design prompts
4. [system-design-drills.md](./system-design-drills.md): small practice prompts plus a rubric so design work does not stay passive
5. [system-design-decision-cheatsheet.md](./system-design-decision-cheatsheet.md): a decision helper for common architecture choices like SQL vs NoSQL or monolith vs microservices
6. [lifecycles-and-flows-cheatsheet.md](./lifecycles-and-flows-cheatsheet.md): a way to keep framework lifecycle, transaction lifecycle, and business lifecycle clearly separated
7. [worked-diagrams.md](./worked-diagrams.md): companion diagrams for checkout, inventory, and order flows, with the write path and failure path in view

## Working Rule

For any design exercise:

1. define the invariant first, meaning the rule that must never be broken
2. name the source of truth early, meaning the durable place whose final state you trust
3. walk the critical write path
4. say what happens on retries, timeouts, and duplicate delivery
5. only then add extra components for scale or convenience

## Related Paths

Use this topic directly when you want stronger design judgment in general.
If you want a role-transition path built on top of these ideas, start with:

- [../../paths/solutions-architect-from-backend.md](../../paths/solutions-architect-from-backend.md)
- [../../paths/sre-from-backend-engineers.md](../../paths/sre-from-backend-engineers.md)
