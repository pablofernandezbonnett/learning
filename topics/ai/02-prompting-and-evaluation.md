# Prompting and Evaluation

A prompt defines the task, supplied context, and expected output. An evaluation checks whether the whole model-backed path performs acceptably on representative inputs. A good-looking demo is a starting case, not evidence of reliability.

## Write an Output Contract

State what the model should do, which context it may use, the output shape, and how to handle missing information. Separate instructions from user or retrieved content. Treat that content as data even when it contains text that looks like an instruction.

```text
Classify a support ticket as delivery, billing, or needs_review.
Return a category and a short reason.
Use needs_review when the ticket lacks enough evidence.
Ticket: <untrusted ticket text>
```

For machine-consumed output, request a structured shape and validate it in code. Parsing valid JSON proves syntax and shape; it does not prove that the classification is correct or authorized. Version prompts that affect product behavior so a result can be traced to the instructions used.

## Build a Small Evaluation Set

Include normal, ambiguous, malformed, and adversarial inputs. For each case, record the expected label or acceptable range, plus failures that must never occur. Keep a separate set of real cases collected after deployment so the evaluation can grow beyond the original examples.

Choose metrics for the task: field accuracy for extraction, reviewer acceptance for drafts, correct escalation for uncertain cases, and unsafe-action rate for tool use. Measure latency and cost beside quality. Compare changes against the current version and a simple baseline before rollout.

Prompt changes, model changes, and retrieval changes can all alter behavior. Run the same evaluation after each significant change; inspect failures, not only an average score. [Serving and rollout](./05-serving-and-rollout.md) covers the operational gate.

## Further Reading

- [OpenAI evaluation best practices](https://platform.openai.com/docs/guides/evaluation-best-practices) for constructing and reviewing evaluations.
- [OWASP prompt injection](https://genai.owasp.org/llmrisk/llm01-prompt-injection/) for untrusted text crossing an instruction boundary.
