# Spec-Driven Development (SDD) for AI-Assisted Work

Use this note when an AI assistant is helping with a feature that is large,
risky, or easy to misunderstand. It explains how to make the desired outcome
clear before asking for code.

## Why This Matters

AI can draft code much faster than a team can resolve an unclear requirement.
If a request says only "add checkout retries", the assistant must guess rules
such as which failures are retryable, how duplicate charges are prevented, and
what a user sees after a timeout. The resulting code can look plausible while
implementing the wrong behavior.

SDD moves that clarification earlier. It gives people and AI the same written
description of what must be true, then uses it to guide a small implementation
and its verification.

## Smallest Useful Mental Model

**Spec-Driven Development (SDD)** is a workflow where a version-controlled
specification is the primary description of a change. The specification states
the intended outcome and rules; a plan states the technical approach; small
tasks turn that plan into reviewable code and tests.

The key distinction is deliberately simple:

- **specification:** what problem is solved, for whom, and how success is
  judged
- **plan:** how this repository will solve it without breaking its constraints
- **tasks and code:** the small, testable pieces that carry out the plan

An AI assistant can help draft each artifact and implement a task. A developer
still owns the decisions, validates the result, and keeps the artifacts honest
when the requirement changes.

## SDD Is Not DDD, TDD, or a Tool

These ideas solve different problems and can be used together.

| Practice | Main question it answers | Example |
| --- | --- | --- |
| SDD | What outcome and constraints must this change satisfy? | A payment retry must never create a second charge. |
| DDD | What business language and boundaries should the code model? | `Payment`, `Charge`, and `Refund` have distinct meanings. |
| TDD | Does this unit behave as expected? | A test proves a duplicate event returns the prior result. |

SDD is a process, not a particular generator, repository layout, or prompt.
Tools such as Spec Kit can support this process, but a small Markdown spec and
normal pull-request review are enough to start.

## Bad Mental Model vs Better Mental Model

Bad mental model:

- write a broad prompt, accept a large patch, and use the generated code as the
  specification
- believe that a longer prompt automatically makes the feature safe
- create documents that are never updated after the code changes

Better mental model:

- agree on an observable outcome before implementation
- record rules, exceptions, and constraints where reviewers can inspect them
- derive tests and small tasks from those rules
- treat the specification as a living contract: update it when intent changes

The goal is not paperwork. The goal is to remove costly ambiguity while it is
still cheap to discuss.

## A Practical Workflow

For a normal feature, these six steps are a strong default.

1. **Set durable guardrails.** Keep repository-level rules visible: supported
   stack, security requirements, test commands, API conventions, and decisions
   that agents must not make alone. Existing contribution guidance and ADRs can
   serve this job; they do not need a file called `constitution.md`.
2. **Specify the intent.** Write the user problem, scope, business rules,
   acceptance criteria, and non-functional constraints. Describe behavior,
   not classes or database columns.
3. **Clarify uncertainty.** Ask the questions that could produce a different
   implementation: empty input, authorization, retries, timeouts, migration,
   observability, and out-of-scope behavior. Record the answers in the spec.
4. **Plan the technical change.** Map the approved intent onto the current
   codebase: module boundaries, contracts, data changes, dependencies, rollout,
   and rollback risks.
5. **Split it into small tasks.** Each task should be independently reviewable
   and verifiable. Prefer a vertical slice or a focused PR over one AI-generated
   repository rewrite.
6. **Implement, verify, and maintain.** Ask the assistant to work from one
   task and the relevant context. Run the normal tests and review the diff
   against the specification. If intent changes after release, revise the spec
   first, then make the next change.

## Worked Example: A Safe Payment Retry, Phase by Phase

This example follows one feature from a natural product request to a verified
change. The names and values are illustrative; in a real system they are team
decisions, not values an AI should choose by itself.

### The Starting Request

Maya is buying a ¥6,800 jacket. She clicks **Pay**, the storefront sends the
request to its payment provider, and the provider call times out after 10
seconds. The screen says "Something went wrong." Maya clicks **Pay** again.

The product manager asks:

> Customers should be able to recover from a payment timeout without being
> charged twice.

That is a good outcome statement, but not yet an implementation request. The
team does not know whether the first call reached the provider, what the API
should return on the second click, or when it is safe to tell Maya that the
payment failed.

### Phase 0: Set the Guardrails

Before creating the feature spec, the developer gives the assistant the facts
it must respect:

- `checkout-api` owns the public `POST /payments` endpoint.
- `payment-service` owns payment states and calls the provider.
- `payment_attempt` already stores the provider reference and the client's
  idempotency key, a value that lets the server recognize a repeated request.
- Existing payments use an outbox event; no service may publish a payment event
  directly inside an uncommitted database transaction.
- Card data is never logged. The change must add unit tests and an integration
  test with the provider fake.

These are guardrails, not feature requirements. They prevent the assistant from
proposing a new service, logging sensitive data, or bypassing an existing
reliability boundary simply because it has not seen the repository rules.

### Phase 1: Specify the Intended Behavior

The developer turns the product request into a small specification. It explains
the desired observable behavior, rather than prematurely requesting a
`RetryPaymentService` class or a new table.

```md
# Recover a payment whose provider result is unknown

## Goal
When the provider times out, let Maya safely learn the result or continue her
checkout without submitting a second charge.

## In scope
- Card payments that time out while `payment-service` calls the provider.
- A repeat `POST /payments` request with the same idempotency key.
- Background lookup of the original provider attempt.

## Rules
- Persist the attempt before calling the provider.
- After a provider timeout, mark the payment `PENDING_PROVIDER_RESULT`.
- A repeated request with the same idempotency key returns that same payment;
  it does not call the provider again.
- Only a confirmed provider result can change the payment to `SUCCEEDED` or
  `FAILED`.
- The checkout API returns `202 Accepted` with `PENDING_PROVIDER_RESULT` while
  the result is unknown.

## Acceptance criteria
- Given the provider call times out, the persisted payment is pending and no
  success event has been published.
- Given Maya repeats the request with the same idempotency key, the API returns
  the existing payment ID and provider call count remains one.
- Given reconciliation finds a successful provider charge, the payment becomes
  `SUCCEEDED` and exactly one payment-succeeded event is published.
- Given reconciliation finds no provider charge after the agreed window, the
  payment becomes `FAILED` with a customer-safe error message.

## Out of scope
- Retrying after Maya changes her card or payment amount.
- Changing the payment provider contract.
```

Notice the useful detail: `PENDING_PROVIDER_RESULT` has a purpose. It means
"we do not know yet"; it is not a disguised failure. That state stops the
system from guessing and gives both the API and the background process a
consistent rule to follow.

### Phase 2: Clarify the Decisions That Would Change the Design

The assistant can now help find questions, but the product and engineering
owners answer them. The answers are written back into the spec.

| Question | Decision recorded in the spec | Why it matters |
| --- | --- | --- |
| Can the second click create a new provider call? | No; the same idempotency key returns the existing attempt. | The first call may have charged the card despite the timeout. |
| What does Maya see while the result is unknown? | `202 Accepted` and a pending payment status. | A generic error would invite an unsafe retry. |
| How long do we wait before failing? | 24 hours, chosen by product and operations. | This is a business and support decision, not a default for the model to invent. |
| What if the lookup also times out? | Keep the payment pending, retry the lookup with monitoring, and alert after a threshold. | Temporary uncertainty must not become a false decline. |
| Can staff resolve a stuck payment manually? | Yes, through the existing operations workflow; it records an audit reason and requires provider evidence before a final state. | Exceptional recovery needs accountability without allowing a manual success guess. |

This phase is where SDD earns its cost. A vague prompt often hides these
decisions inside generated code. Here, reviewers can agree or disagree with
them before a database migration or provider call is written.

### Phase 3: Plan the Change in This Codebase

Only after the behavior is agreed does the technical plan map it to existing
components:

```text
Maya's repeat request
        |
        v
checkout-api --same idempotency key--> payment-service
                                          |
                              existing payment? --- yes --> return its current state
                                          |
                                          no
                                          v
                               persist attempt, then call provider
                                          |
                                   timeout v
                          PENDING_PROVIDER_RESULT + outbox event
                                          |
                                          v
                         reconciliation worker queries provider result
                                          |
                         SUCCEEDED / FAILED + one final outbox event
```

The plan also names the important technical decisions:

- add `PENDING_PROVIDER_RESULT` to the existing state machine and ensure only
  the reconciliation path can leave it;
- reuse the current unique constraint on the idempotency key rather than build
  a second deduplication store;
- schedule the existing reconciliation worker from a `payment-pending` outbox
  event; and
- add a metric for pending payments older than 15 minutes, so operations can
  notice a provider or worker problem before the 24-hour deadline.

At this stage, an architecture review can challenge the plan: for example,
whether the uniqueness constraint also includes merchant and currency, or
whether an existing worker has enough provider rate-limit capacity. Those are
repository-specific concerns a generic feature prompt cannot safely answer.

### Phase 4: Break the Plan into Reviewable Tasks

The team converts the plan into small tasks. Each task has a boundary and
evidence of completion, so the AI is asked for a focused patch instead of a
large unreviewable rewrite.

| Task | Deliverable | Evidence |
| --- | --- | --- |
| 1. Model the pending result | State transition and persistence mapping in `payment-service`. | Unit tests reject any final transition from `PENDING_PROVIDER_RESULT` except through reconciliation. |
| 2. Preserve idempotency at the API | Repeat request reads the existing payment before any provider call. | Integration test sends the same key twice and the provider fake receives one call. |
| 3. Reconcile unknown attempts | Worker queries the provider and makes one final state transition. | Fake provider tests cover success, confirmed decline, and a second lookup timeout. |
| 4. Make it operable | Metric, alert threshold, and audit record for manual resolution. | Test checks no card data is logged; dashboard query exposes pending age. |

Tasks 1 and 2 can be one focused PR if they cannot work independently. Tasks
3 and 4 can follow. The specification remains the reason each task exists.

### Phase 5: Give the Assistant a Bounded Implementation Request

Now the developer can make a request that uses AI productively:

```text
Read the payment state machine, idempotency repository, and their tests.
Implement task 2 from the approved payment-timeout recovery specification. Do
not alter provider contracts or retry policies. First report the files and
tests you expect to change; then make the smallest patch. Add the integration
test that proves two requests with the same idempotency key trigger exactly one
provider call.
```

The assistant has a clear source of intent, known boundaries, and a small
output shape. A human still reviews whether the chosen repository query is safe
under concurrent requests and whether the test actually demonstrates the rule.

### Phase 6: Verify, Release, and Keep the Spec Alive

Before merging, the developer checks the acceptance criteria, not merely that
the code compiles:

- simulate the timeout, retry, successful lookup, and no-charge cases with the
  provider fake;
- run the existing API and payment-service test suites;
- inspect the diff for a second provider call, unsafe logging, or a path that
  emits success before the provider result is known; and
- review the metric and alert with the team that will handle pending payments.

After release, suppose support finds that a provider lookup can take 48 hours
during a regional incident. The team first updates the specification and
records the new decision; only then do they change the worker schedule and
customer message. That is what makes the spec a living contract instead of a
document created only to satisfy a process.

## How to Write a Useful Spec

Start small. A feature spec does not need a formal language or a long template.
For most backend changes, include:

- **Goal and user value:** what changes for whom?
- **Scope and non-goals:** what is deliberately excluded?
- **Rules and examples:** normal path, edge cases, and failure behavior.
- **Acceptance criteria:** observable statements that can become tests.
- **Constraints:** security, privacy, compatibility, performance, operational,
  or repository constraints that affect the solution.
- **Open questions and decisions:** label uncertainty instead of letting an AI
  silently guess.

Useful acceptance criteria are observable. "The retry flow is robust" is not
testable. "The same idempotency key never submits a second provider charge" is
specific enough to test and review.

## Strong Default for Working with AI

Use SDD selectively, with the amount of detail proportional to the risk.

- For a typo or local refactor, a short task and normal tests are usually
  enough.
- For a new endpoint, data change, integration, security-sensitive change, or
  multi-session AI task, begin with a small spec and clarification pass.
- For complex work, ask the assistant to identify missing decisions before it
  writes code. Resolve those decisions in the spec, then ask for one planned
  task at a time.

Keep the specification close to the code it governs, review it in the same pull
request when possible, and link it from the change when it is stored elsewhere.

## Main Tradeoffs and Failure Modes

SDD trades a little upfront writing for less rework and less context loss. It
is a poor fit when a tiny experiment needs more ceremony than learning value.

Common failure modes:

- **ceremony without decisions:** a long spec repeats the ticket but leaves
  retry, authorization, or failure semantics undefined
- **implementation disguised as a spec:** prescribing internal classes too early
  prevents the plan from responding to real repository constraints
- **stale source of truth:** the code and spec diverge, so neither can be
  trusted alone
- **blind automation:** generated tests may prove only the behavior the model
  guessed, not the product intent
- **false completeness:** a written criterion does not remove the need for
  security review, integration testing, or production observation

## Practical Rule

> Before asking AI to implement a meaningful change, write the outcome, the
> important rules, the boundaries, and the evidence that will show it is right.
> Then make the assistant solve one small, reviewable task at a time.

## Further Reading

- [Spec Kit: What is Spec-Driven Development?](https://github.github.com/spec-kit/concepts/sdd.html) — a concise view of specification-first, multi-step AI-assisted development.
- [SpecDriven.ai: Spec-Driven Development](https://specdriven.ai/) — a practical six-phase workflow and examples of the artifacts involved.
- [AI-Assisted Development for Backend Engineers](./08-ai-assisted-development-for-backend-engineers.md) — the broader verification and boundary rules for using AI in daily engineering work.
- [Domain-Driven Design](../architecture/09-domain-driven-design.md) — the complementary discipline for domain language and boundaries.
