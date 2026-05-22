# Prompting and Evals for Backend Engineers

Prompting matters.
But in product work, prompting without evaluation is usually just a nicer demo.

---

## Why This Matters

Most weak AI implementations fail in one of these ways:

- the prompt is vague
- the output shape is hard to trust
- the team never measured whether the answers are good enough
- the feature works in a notebook but fails under realistic input variation

If you keep one line warm, keep this:

> prompting improves output quality, but evaluation is what turns output quality into an engineering claim

---

## Smallest Useful Mental Model

A prompt is part instruction, part context, and part output contract.

An eval is a repeatable way to test whether the whole AI behavior is good enough
for the real task.

Practical translation:

- prompting is how you ask
- evals are how you verify

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- I will tweak the prompt until the answer looks good
- if it worked on five examples, it is production-ready

Better mental model:

- I define the task, the output shape, and the failure cases clearly
- I test the prompt on a small but representative eval set before trusting it

Small concrete example:

- weak approach: "summarize these refund tickets"
- better approach: "summarize each refund ticket in JSON with `reason`, `sentiment`, `policy_risk`, and `next_action`, then test that output against examples with known labels"

---

## Prompting Strong Defaults

These defaults align well with official OpenAI guidance:

- put the instruction early
- separate instruction from context clearly
- be explicit about output shape
- start `zero-shot`, meaning instruction without examples, then add `few-shot` examples only when they help
- say what to do, not only what to avoid

Small prompt example:

```text
Classify the refund ticket below.

Return JSON with exactly these keys:
- category
- urgency
- policy_risk
- summary

Ticket:
"""
Customer says the duplicate charge happened after changing card details.
"""
```

Why this is stronger:

- task is explicit
- output contract is explicit
- context is clearly separated

---

## Structured Output Matters

Backend engineers usually get the most value when model output is easy to parse.

Good default:

- prefer stable JSON or another explicit structure for machine-consumed output

Bad default:

- asking for a free-form paragraph and then trying to scrape it later

Short rule:

> if the next system step is code, the output should usually look like a contract, not like prose

---

## Prompt Caching And Cost Shape

Prompt cost and latency improve when the static prefix stays stable.

Practical rule from OpenAI's prompt caching guidance:

- keep common instructions and shared examples at the beginning
- put variable user content later

Why this matters:

- lower latency
- lower cost
- more consistent prompt shape across requests

This is not only a billing issue.
Stable prompt structure also makes evaluation and debugging easier.

---

## Evals: The Smallest Useful Starting Shape

You do not need a large benchmark to start.
You do need a small set of representative cases.

Good first eval set:

- easy examples
- edge cases
- ambiguous cases
- cases that should fail safely

For each case, define:

- input
- expected output or acceptable label
- failure rule

Plain-English version:

- an `eval set` is just a small set of realistic test cases for the AI task

Small word example:

- if the task is support-ticket routing, include one ticket that looks urgent but is actually low policy risk
- if the task is extraction, include malformed or incomplete source text

---

## What To Measure

Not every AI task needs the same metric.

Useful backend/product metrics include:

- exact-match rate for structured fields
- pass/fail against business rules
- reviewer acceptance rate
- unsafe-output rate
- latency and cost per request

Good practical default:

- combine output quality with one operational metric such as latency or cost

---

## Main Tradeoff

More context and more examples can improve answers.
They can also:

- increase cost
- increase latency
- make prompt drift harder to notice
- reduce cache reuse

That is why "just make the prompt longer" is not a strong default.

---

## Reusable Takeaway

> Prompting is part instruction design and part output-contract design. Evals are how I prove the prompt still works on realistic inputs instead of trusting a demo.

---

## Further Reading

- OpenAI prompt engineering best practices: https://help.openai.com/en/articles/6654000-playground-and-prompt-engineering
- OpenAI prompting fundamentals: https://openai.com/academy/prompting/
- OpenAI prompt caching guide: https://platform.openai.com/docs/guides/prompt-caching
- OpenAI evaluation best practices: https://platform.openai.com/docs/guides/evaluation-best-practices
