# AI-Assisted Development

An assistant can speed up reading, drafting, and reviewing code. It does not know which behavior the repository requires unless that intent and context are supplied. Use it on bounded tasks and verify the result against the actual system.

## A Small Working Loop

1. Read the relevant code, tests, contract, and repository rules.
2. State the outcome, constraints, and evidence of success.
3. Ask for one reviewable change or analysis task.
4. Inspect the diff and run the checks that exercise the risky behavior.
5. Update documentation or the task description when the intended behavior changes.

This works for test cases, refactors, migration plans, documentation, and incident analysis. For a tiny edit, a short request and normal review are enough. A payment retry, authorization change, or data migration needs the rules made explicit before code is generated.

## Use a Short Specification When Risk Warrants It

A specification says **what** must be true; a plan says **how** this repository will make it true. Keep them small and reviewable:

```text
Goal: recover a payment after a provider timeout without a second charge.
Rules: repeated requests with the same key return the same attempt;
       unknown provider result remains pending.
Acceptance: two repeated requests make one provider call;
            reconciliation records one final outcome.
Open decision: how long may the result remain pending?
```

Resolve open decisions with the product and system owners. Then map the accepted rules to existing state, storage, provider contracts, tests, rollout, and rollback. Ask the assistant to implement one task at a time. A generated test is useful only if it proves the intended behavior rather than a guess the assistant made.

The [payment retry case](./09-spec-driven-development-for-ai-assisted-work.md)
shows this progression from a vague request to a reviewed specification and
small implementation tasks.

## Tool Access and Review

Give the assistant only the files and external capabilities needed for the task. Review tool side effects and protect secrets in prompts and logs. An assistant's explanation can guide investigation, but source code, current documentation, tests, and runtime evidence decide whether the change is correct.

## Further Reading

- [Git diff documentation](https://git-scm.com/docs/git-diff) for reviewing changes.
- [Domain-driven design](../architecture/09-domain-driven-design.md) when the hard part is agreeing on domain terms and boundaries.
