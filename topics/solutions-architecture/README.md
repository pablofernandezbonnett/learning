# Solution Decisions

Use this folder when a design must fit business constraints, other systems,
team ownership, and a migration path. These decisions matter to any engineer
shaping a system; they do not require a particular job title.

Start with the [general system design method](../system-design/00-general-system-design-method.md)
for the whole problem. Use a focused note here when that decision becomes
important:

1. [Requirements and quality constraints](./01-requirements-and-nfrs.md): turn a vague request into testable needs and rank the constraints that change the design.
2. [Build vs buy](./02-build-vs-buy.md): compare implementation, integration, operating, and exit costs.
3. [Multi-tenancy and data boundaries](./03-multi-tenancy-and-data-boundaries.md): choose isolation, access, and placement rules for shared systems.
4. [Migration strategy](./04-migration-and-modernization-strategy.md): move from the current system to the target without assuming a one-step replacement.
5. [Decision records and communication](./05-adrs-and-stakeholder-communication.md): keep important choices reviewable and explain their consequences to the people affected.

The folder covers constraints and adoption. Use [architecture](../architecture/README.md)
for component and integration patterns, [security](../security/README.md) for
trust boundaries, and [reliability](../sre/README.md) for live-system behavior.
