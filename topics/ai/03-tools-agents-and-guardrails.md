# Tools, Agents, and Guardrails

Use this note when the conversation moves from "one prompt" to "a system that acts".
For this repo's main AI lane, treat it as optional growth once local/private
serving, prompting, and evaluation basics are already clear.

---

## Why This Matters

Many teams hear `agent` and imagine a smarter chatbot.

The real engineering question is different:

- what can the model do on its own
- what tools can it call
- what rules stop it from doing the wrong thing

If those boundaries are weak, the system becomes expensive, hard to trust, and
hard to operate.

---

## Smallest Useful Mental Model

An `agent` is usually:

- a model
- an instruction set
- one or more tools
- state or context
- guardrails around what it may do

A `tool` is an explicit capability such as:

- search
- file read
- database lookup
- CRM action
- order-status lookup

A `guardrail` is a rule or mechanism that limits unsafe or weak behavior.

Practical translation:

- tools expand what the model can do
- guardrails limit what the model is allowed to do

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- an agent is just a bigger prompt
- if the model can call a tool, it should usually call it
- guardrails are a compliance afterthought

Better mental model:

- an agent is a workflow boundary with instructions, tools, and control rules
- tools are valuable only when they reduce real uncertainty or automate a useful step
- guardrails belong in the design from the start

Small word example:

- weak approach: let the model decide whether to refund, and also let it call the refund tool directly
- better approach: let the model gather evidence and propose an action, but require policy checks and explicit backend authorization before the refund call is allowed

---

## What A Good Tool Definition Looks Like

A good tool definition is:

- narrow
- explicit about input shape
- explicit about side effects
- hard to misuse accidentally

Bad example:

- `run_any_query(sql_text)`

Better example:

- `get_order_summary(order_id)`
- `list_recent_refund_attempts(customer_id)`
- `create_support_draft(order_id, template_type)`

Short rule:

> tool design should reduce ambiguity, not export your whole backend surface to the model

---

## What A Good Agent Definition Looks Like

A good agent definition usually makes these things explicit:

- role
- allowed tools
- output shape
- escalation rules
- stop conditions

Small example shape:

```text
Role:
- summarize refund cases for support agents

Allowed tools:
- get_order_summary
- list_recent_refund_attempts

Never do:
- approve or execute refunds

Return:
- JSON with summary, risk_signals, and recommended_next_step
```

Why this is stronger:

- role is clear
- tools are bounded
- authority boundary is clear
- output contract is clear

---

## Guardrails That Matter Most

High-value guardrails for backend/product systems:

- permission checks before tool execution
- sensitive-action deny lists
- input validation on tool arguments
- output validation on structured responses
- escalation to a human for high-risk cases
- logging and traceability for tool calls and final actions

Good default:

- keep guardrails outside the prompt alone
- use backend enforcement where real authority is involved

---

## Skills and Specialized Agents

In practice, a `skill` or specialized agent is useful when:

- one narrow task repeats often
- the task has a stable input and output shape
- focused instructions outperform one giant general prompt

Examples:

- code-review agent
- incident-triage agent
- ticket-summarization agent
- doc-redlining agent

Bad use:

- one giant universal agent that tries to do everything

Better use:

- several narrow specialists with clear handoff boundaries

Short rule:

> specialization usually improves reliability faster than trying to build one agent that knows everything

---

## Main Tradeoff

More tools and more autonomy can improve capability.
They also increase:

- failure surface
- prompt complexity
- misuse risk
- debugging difficulty

That is why autonomy should grow only when the evals and guardrails keep up.

---

## Reusable Takeaway

> A useful agent is not "a smart model." It is a bounded workflow with explicit tools, output contracts, permission checks, and stop rules.

---

## Further Reading

- OpenAI agents guide: https://platform.openai.com/docs/guides/agents
- OpenAI Agents SDK guide: https://platform.openai.com/docs/guides/agents-sdk/
- OpenAI practical guide to building agents: https://cdn.openai.com/business-guides-and-resources/a-practical-guide-to-building-agents.pdf
