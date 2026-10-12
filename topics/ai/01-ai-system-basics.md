# AI System Basics

A model produces a prediction or generated output from an input. The useful system includes everything around it: input selection, permissions, retrieval, validation, policy, side effects, and feedback. Design those boundaries before choosing a model or framework.

## Start with the Decision

Ask what the user needs and what a wrong answer costs. Drafting, summarization, extraction, and classification can benefit from a model when outputs are checked or reviewed. A model answer alone should not authorize a payment, disclose a private document, or commit an irreversible action.

```text
request → authenticate and validate → permitted context → model
        → validate output → policy or human decision → action
```

The model may suggest an action. Deterministic application rules decide whether the action is allowed and record what happened. Keep an explicit path for uncertainty, malformed output, and unavailable model service.

## Choose the Smallest Useful Shape

- **Direct call:** one bounded task, such as classifying a support ticket.
- **Retrieval:** fetch relevant, permitted source material when the task depends on knowledge outside the model. Retrieval provides context, not proof that the answer is true.
- **Tool use:** let the model request a narrow lookup or action when it needs live data or a controlled capability.
- **Workflow:** coordinate several steps only when a direct call cannot meet the task and failure rules.

Start with a baseline that does not need a model. Compare quality, latency, cost, and failure behavior against it. If a deterministic rule solves the problem well, use that rule.

## Questions to Carry Forward

- Which inputs may the model see, and who checked access to them?
- What output shape is required, and who verifies its meaning?
- What happens when the model is wrong, slow, unavailable, or too expensive?
- Which component owns each side effect and its audit trail?
- How will a model, prompt, or retrieval change be evaluated and rolled back?

Use [prompting and evaluation](./02-prompting-and-evaluation.md) for answer quality, [tools and agents](./03-tools-and-agents.md) for capabilities, and [serving and rollout](./05-serving-and-rollout.md) for production behavior.
