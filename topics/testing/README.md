# Testing and Code Review

Use this block to decide what evidence a change needs and whether another engineer can review its risks. Start with the failure you need to catch, then choose the cheapest test that can actually catch it.

1. [Testing strategy](./01-testing-strategies.md): match business rules, persistence, contracts, user journeys, and load risks to suitable tests.
2. [Code review](./02-clean-code-and-code-review.md): make state changes, external effects, and failure handling visible.

For independently deployed API or event boundaries, use [contract testing and API evolution](../api/08-contract-testing-and-api-evolution.md) for the detailed compatibility rules.

The shared rule is simple: a readable implementation makes the important behavior easier to test, and a test is useful only when it can fail for a meaningful regression.
