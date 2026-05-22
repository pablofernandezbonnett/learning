# AI for Backend Engineers

Use this folder when you want practical `AI fluency`, not `ML` (`machine learning`) theory.

In this repo, `AI fluency` means:

- knowing what `LLM` (`large language model`) systems are good at and where they fail
- prompting clearly enough to get reliable output
- knowing when tools, agents, and retrieval, meaning fetching relevant knowledge at runtime, help
- adding evaluation and guardrails instead of trusting demos

This folder is not about training foundation models.
It is about using modern AI systems with backend engineering judgment.

Focus:

- prompt quality
- structured output and tool use
- agents, workflows, and handoffs
- evaluation and guardrails
- backend integration boundaries

Working style:

- explain AI terms in backend language first
- prefer small examples over vague slogans
- keep the focus on reliability, cost, and misuse risk
- verify product-specific AI behavior against official vendor docs when the details may change over time

## Recommended Order

1. [01-ai-fluency-for-backend-engineers.md](./01-ai-fluency-for-backend-engineers.md): the smallest useful mental model for LLMs, agents, and product-fit judgment
2. [02-prompting-and-evals.md](./02-prompting-and-evals.md): how to prompt more reliably and how to test whether the prompt actually works
3. [03-tools-agents-and-guardrails.md](./03-tools-agents-and-guardrails.md): how agentic systems use tools, why guardrails matter, and what a good skill or agent definition looks like

## Refresh

- [01-ai-fluency-for-backend-engineers.md](./01-ai-fluency-for-backend-engineers.md)

## Required

- [02-prompting-and-evals.md](./02-prompting-and-evals.md)
- [03-tools-agents-and-guardrails.md](./03-tools-agents-and-guardrails.md)

## Growth

- [../python/README.md](../python/README.md): Python as the fastest glue language for AI-adjacent backend work
- [../architecture/13-enterprise-integration-patterns.md](../architecture/13-enterprise-integration-patterns.md): where AI belongs in backend integration systems and where it should stay outside the core write path

## Core Rule

- AI fluency is mainly about reliability and boundary judgment, not about sounding futuristic
- prompt quality matters, but evaluation matters more
- tools, retrieval, and agents help only when they reduce real product or workflow friction
