# Tools and Agents

A model can request information or actions through tools. An agent adds a workflow that chooses and sequences those calls. Each tool increases what the system can affect, so its authority must be explicit and enforced outside the prompt.

## Start with One Capability

A useful tool has a narrow purpose, typed inputs, a defined result, and clear side effects. `get_order_summary(order_id)` is easier to authorize and test than `run_any_query(sql)`. The server checks the caller's identity and permissions before every call; the model's choice to call a tool is not authorization.

For a support assistant:

```text
allowed: read order summary, draft a response
not allowed: issue refund
final output: draft, evidence used, unresolved questions
```

If a refund is needed, policy code or an authorized person decides and executes it. Validate tool arguments, bound call count and time, record which tool was called, and avoid returning secrets the model does not need.

## Add Workflow Only When Needed

Define a stop condition, error path, and escalation point. A tool failure, missing evidence, or conflicting records should not silently turn into a confident answer. Test the workflow with adversarial source content and unavailable tools.

More autonomy can save work, but it increases the number of failure paths and the damage possible from a mistaken or manipulated call. Grow capability only after [evaluation](./02-prompting-and-evaluation.md) covers the new actions.

## Further Reading

- [OWASP excessive agency](https://genai.owasp.org/llmrisk/llm062025-excessive-agency/) for risks from broad tool access.
