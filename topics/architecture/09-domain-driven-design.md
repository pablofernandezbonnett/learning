# Domain-Driven Design: Model the Business Boundary

Domain-driven design (DDD) helps when business rules, vocabulary, and ownership
are harder to understand than the code that stores them. Start with the work the
system must do; add patterns only where they protect a real rule.

For Spring implementation details, see [Practical DDD in Spring Boot](../spring-boot/21-practical-ddd-in-spring.md).

## Start With Language and Decisions

Ask people who do the work what each action means. Record the terms they use,
the state changes they expect, and the cases they reject. Use those terms in
requirements, tests, and code within the relevant part of the system.

For example, *reserve stock* and *ship stock* are different decisions. A single
`updateQuantity()` method hides that distinction and makes its rules harder to
find.

## Find Bounded Contexts

A **bounded context** is the boundary within which one model and vocabulary
have a consistent meaning. The same word may mean different things elsewhere:

| Context | What it owns | Meaning of a product |
| --- | --- | --- |
| Catalogue | Descriptions and discoverability | An item customers can browse |
| Inventory | Available and reserved units | Stock tracked by SKU and location |
| Orders | What a customer agreed to buy | An item snapshot with price and quantity |

Do not force one shared `Product` class across these contexts. Give each context
the data and rules it needs, then translate at the boundary. A context can be a
module in one application or be deployed separately. It does not automatically
require a microservice or a separate physical database. See [service boundaries](./01-monolith-vs-microservices.md)
and [data ownership during a split](./18-shared-db-and-independent-deployments.md).

## Protect Rules That Must Hold Together

DDD has a few useful modeling tools:

| Tool | Use it when |
| --- | --- |
| Entity | Identity matters across changes, as with an order. |
| Value object | A value is defined by its attributes, as with money in a currency. |
| Aggregate | Several state changes must preserve one immediate business rule. |
| Repository | A use case needs to load or save an aggregate without owning storage details. |
| Domain event | Something meaningful happened in the model and another action may follow. |

An aggregate exposes operations that enforce its invariants. For example,
`order.cancel()` can reject cancellation after shipment. Callers should not
change the order status directly and bypass that rule. Keep an aggregate small
enough to update and reason about. Its boundary follows an invariant, not a
table, endpoint, or fixed rule that every transaction may touch only one
aggregate.

A domain event can stay inside one process. If other services must observe a
change, define an integration contract separately and plan for reliable
publication. See [distributed workflows and outbox](./03-distributed-transactions-and-events.md).

## Work From Requirement to Model

1. Write a few real scenarios, including rejected actions and failures.
2. Name the business decisions and the people or teams that own them.
3. Identify words whose meaning changes between parts of the system; draw the
   bounded contexts around those models.
4. Find rules that must hold immediately. Put each rule at the smallest useful
   consistency boundary and test it through domain behavior.
5. Define what crosses each context boundary: command, query, or event. State
   who owns the fact and where translation happens.
6. Revisit the model when a new rule exposes a misplaced responsibility.

When one context consumes another's model, translate only what it needs rather
than importing a shared mutable model. See [integration boundaries](./13-enterprise-integration-patterns.md).

For a context map, record each relationship in one line: who owns the fact,
who consumes it, how updates arrive, and who translates differences in meaning.
For example, Inventory owns available stock; Orders consumes a reservation
result and does not edit Inventory's stock model. This map exposes hidden
coupling before choosing APIs, events, or service boundaries.

## When to Keep It Light

Use richer domain modeling for changing rules, important state transitions,
and vocabulary that differs across teams. For straightforward CRUD, clear
ownership and a simple use case may be enough. The goal is a model that makes
business decisions visible, not a prescribed set of classes.
