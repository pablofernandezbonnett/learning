# Testing Strategy

A test is evidence for a specific claim: a business rule holds, a database constraint works, two components agree on a contract, or a critical flow survives deployment. Name the claim and failure first. Then choose the smallest test that reaches the boundary where the failure could occur.

## Choose the test by risk

| Failure to catch | First useful test | What it proves | What it cannot prove alone |
| --- | --- | --- | --- |
| Wrong calculation or state decision | Unit test around the rule | The rule behaves as specified for chosen inputs | Database, network, and framework wiring |
| Incorrect SQL, migration, constraint, or transaction | Integration test with the relevant database engine | The persistence boundary behaves under realistic semantics | Every caller or production load |
| Changed API or event shape | Contract test at the producer and consumer boundary | The two sides still agree on the fields and behavior they rely on | The whole user journey |
| Broken critical journey | A small end-to-end test | The selected flow works across its major components | Every branch or failure mode |
| Broken deploy or startup | Smoke check against the deployed service | The service starts and one essential path responds | Deeper business correctness |
| Capacity or latency regression | Load test with representative traffic | Behavior under the tested workload | Future production traffic with different data or dependencies |

A **boundary** is where code meets a database, queue, another service, or a public API. A **state transition** is a business change such as `PENDING -> PAID`. Tests should cover the transition and the boundary that makes it durable or observable.

## Start with the behavior

For a stock reservation, a useful set is:

1. A unit test checks that zero or negative quantity is rejected.
2. An integration test checks that two concurrent requests cannot reserve the last unit twice.
3. An API test checks the response for success, conflict, and invalid input.
4. A smoke check after deployment confirms the reservation route responds.

The unit test cannot prove the database condition or lock. The integration test does not replace the public error-contract check. Avoid writing the same happy-path assertion at every level unless each one crosses a different important boundary.

For a payment or webhook flow, add duplicate delivery, timeout with unknown outcome, and crash between steps. A test that only checks a successful call misses the main correctness risk.

## When mocks help

Use a fake or mock to control an external collaborator when testing a local decision: for example, whether a declined authorization leaves an order unconfirmed. Assert the resulting state or response, not every internal method call.

Use a real dependency where its behavior is the point of the test. A mocked repository cannot prove a unique constraint, isolation behavior, SQL query, or migration. An in-memory substitute can be useful for quick feedback but is weak evidence for a different production database engine.

## Contracts and journeys

Use contract tests when independently changed services or event producers can break consumers. Test the fields, status, error behavior, and compatibility rule the consumer actually depends on. See [contract testing and API evolution](../api/08-contract-testing-and-api-evolution.md) for the full method.

Keep end-to-end tests to a few important journeys, such as checkout or login. They offer broad evidence but are slower to diagnose. Put detailed branching and failure cases in narrower tests where the cause is easier to see.

## Performance evidence

Define the expected workload before measuring: request mix, data volume, dependency behavior, concurrency, and duration. Record throughput, error rate, and latency distribution, including the slow tail. Compare against a stated budget and look for the limiting resource. A fast average does not show whether a small group of users waits much longer.

One local run is evidence only for that environment. Repeat with realistic data and watch queue depth, database work, saturation, and timeouts before claiming a capacity limit.

## Review before adding another test

- Which regression would make this test fail?
- Does it reach the boundary where that regression can occur?
- Is a cheaper test already proving the same claim?
- Does it cover an important failure or retry, not only success?
- Will a failure tell the next engineer what broke?

Test count and line coverage are useful signals, but they do not answer those questions.
