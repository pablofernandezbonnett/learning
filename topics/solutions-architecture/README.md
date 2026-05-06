# Solutions Architecture for Backend Engineers

Use this folder when you want to move from service-level backend ownership to
solution-level design across systems, teams, and constraints.

This topic is not a replacement for `architecture`, `system-design`, `cloud`,
or `security`.
It is the layer that connects them into a defensible end-to-end solution.

Focus:

- what changes when you stop thinking only about one service and start shaping a whole solution
- requirements and `NFRs` (`non-functional requirements`), meaning what qualities and constraints the solution must preserve under load, change, and failure
- build-vs-buy judgment, meaning when a managed or third-party capability is the better solution than custom code
- multi-tenancy, trust boundaries, and data placement
- migration and modernization strategy, meaning how to move from current reality to the target state safely
- `ADR` writing (`Architecture Decision Records`) and stakeholder communication, meaning how decisions are recorded and explained clearly

Working style:

- explain architecture jargon before relying on it
- keep examples close to business flows, integration boundaries, and operational tradeoffs
- prefer "why this solution shape is defensible" over product-name memorization or diagram theater

Short rule:

- no architecture term should appear as if its name explains itself

## Recommended Order

1. [01-architect-vs-solutions-architect.md](./01-architect-vs-solutions-architect.md): what changes when service design becomes solution design
2. [02-requirements-and-nfrs.md](./02-requirements-and-nfrs.md): how to turn vague asks into solution-shaping constraints
3. [03-build-vs-buy.md](./03-build-vs-buy.md): how to decide between custom implementation and external capability
4. [04-multi-tenancy-and-data-boundaries.md](./04-multi-tenancy-and-data-boundaries.md): how tenant shape, isolation, and data placement change architecture
5. [05-migration-and-modernization-strategy.md](./05-migration-and-modernization-strategy.md): how to move safely from the current system to the target solution
6. [06-adrs-and-stakeholder-communication.md](./06-adrs-and-stakeholder-communication.md): how to document decisions and explain them to engineering, security, operations, and business stakeholders

## Refresh

- [01-architect-vs-solutions-architect.md](./01-architect-vs-solutions-architect.md)
- [02-requirements-and-nfrs.md](./02-requirements-and-nfrs.md)
- [03-build-vs-buy.md](./03-build-vs-buy.md)

## Required

- [04-multi-tenancy-and-data-boundaries.md](./04-multi-tenancy-and-data-boundaries.md)
- [05-migration-and-modernization-strategy.md](./05-migration-and-modernization-strategy.md)

## Growth

- [06-adrs-and-stakeholder-communication.md](./06-adrs-and-stakeholder-communication.md)

## Related Path

If your goal is the role transition from backend engineer to `Solutions Architect`, start with:

- [../../paths/solutions-architect-from-backend.md](../../paths/solutions-architect-from-backend.md)

## Related Internal Topics

- [../system-design/README.md](../system-design/README.md): correctness, invariants, source of truth, and defensible design shape
- [../architecture/README.md](../architecture/README.md): service boundaries, resiliency, events, networking, caching, and integration
- [../cloud/README.md](../cloud/README.md): responsibility boundaries, compute models, and platform tradeoffs
- [../security/README.md](../security/README.md): trust boundaries, auth models, payment-adjacent correctness, and secure delivery
- [../sre/README.md](../sre/README.md): reliability targets, incident thinking, capacity, and operational review

## Core Rule

- solution architecture is where technical decisions meet business constraints
- a strong solution explanation names tradeoffs, constraints, and migration path, not only target-state boxes
- a good solution is not the most impressive design; it is the design that fits the problem, constraints, and operating reality
