# System Design for Backend Engineers

This folder is for practical system design study.

The focus is not abstract box-drawing.
The focus is reasoning about:

- invariants
- source of truth
- retries and duplicate prevention
- synchronous versus asynchronous boundaries
- scale hotspots
- observability and recovery

## Recommended Order

1. [backend-system-principles.md](./backend-system-principles.md): compact principles worth keeping warm
2. [practical-checkout-design.md](./practical-checkout-design.md): simple, practical way to learn checkout design without starting from jargon
3. [system-design-decision-cheatsheet.md](./system-design-decision-cheatsheet.md): short decision guide for common architecture choices like SQL vs NoSQL or monolith vs microservices
4. [system-design-guide.md](./system-design-guide.md): a reusable structure for correctness-critical backend design
5. [lifecycles-and-flows-cheatsheet.md](./lifecycles-and-flows-cheatsheet.md): bean lifecycle, transaction lifecycle, and business flows kept separate
6. [worked-diagrams.md](./worked-diagrams.md): companion diagrams for checkout, inventory, and order flows

## Working Rule

For any design exercise:

1. define the invariant first
2. name the source of truth early
3. walk the critical write path
4. say what happens on retries, timeouts, and duplicate delivery
5. only then add extra components for scale or convenience
