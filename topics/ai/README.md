# AI for Backend Engineers

Use this folder when you want practical AI fluency for backend work, not `ML`
(`machine learning`) theory.

In this repo, `AI fluency` means:

- knowing what `LLM` (`large language model`) systems are good at and where they fail
- being able to run useful local or private AI services without treating them like magic
- prompting clearly enough to get reliable output
- knowing when tools, agents, and retrieval, meaning fetching relevant knowledge at runtime, help
- adding evaluation and guardrails instead of trusting demos

This folder is not about training foundation models.
It is about using modern AI systems with backend engineering judgment.

Focus:

- local and private AI service shape
- security and runtime boundaries around AI services
- AI-assisted development workflows around code, docs, and incidents
- prompt quality
- structured output and evaluation
- tools, agents, and workflows as bounded awareness topics, not as the default destination
- offline pipelines and online serving boundaries
- evaluation and guardrails
- backend integration boundaries

Working style:

- explain AI terms in backend language first
- prefer small examples over vague slogans
- keep the focus on reliability, cost, and misuse risk
- verify product-specific AI behavior against official vendor docs when the details may change over time

Decision boundary for AI material:

- use `01` for the smallest useful mental model and to keep scope realistic
- use `06` when the goal is a local or private AI server for daily engineering work
- use `07` when that setup becomes a shared or reachable internal service
- use `08` when the goal is practical productivity in code, docs, review, or incident work
- use `02` when output quality, structure, and repeatability matter
- use `05` once the AI service becomes team-shared, product-facing, or operationally important
- use `03` as optional awareness once the prompt, eval, and trust boundaries are already clear
- use `04` as optional growth material when you need wider serving or pipeline context, not as the first lane

## Recommended Order

1. [01-ai-fluency-for-backend-engineers.md](./01-ai-fluency-for-backend-engineers.md): the smallest useful mental model for LLMs, agents, and product-fit judgment
2. [06-local-ai-server-for-backend-engineers.md](./06-local-ai-server-for-backend-engineers.md): when a local or private model service is useful and how to keep the first setup practical
3. [07-securing-private-ai-services.md](./07-securing-private-ai-services.md): how to treat a private AI runtime like a real internal service boundary
4. [08-ai-assisted-development-for-backend-engineers.md](./08-ai-assisted-development-for-backend-engineers.md): where AI helps most in daily backend work and where verification must stay human-driven
5. [02-prompting-and-evals.md](./02-prompting-and-evals.md): how to prompt more reliably and how to test whether the prompt actually works
6. [05-ai-serving-observability-and-rollout.md](./05-ai-serving-observability-and-rollout.md): what you need to observe, gate, and roll out safely so an AI feature behaves like a production system
7. [03-tools-agents-and-guardrails.md](./03-tools-agents-and-guardrails.md): how agentic systems use tools, why guardrails matter, and what a good skill or agent definition looks like
8. [04-ml-and-ai-pipelines-for-jvm-backend.md](./04-ml-and-ai-pipelines-for-jvm-backend.md): where offline pipelines, online inference, Java serving, Ollama, ONNX, and workflow orchestration actually fit once the service boundary is already clear

## Refresh

- [01-ai-fluency-for-backend-engineers.md](./01-ai-fluency-for-backend-engineers.md)
- [06-local-ai-server-for-backend-engineers.md](./06-local-ai-server-for-backend-engineers.md)
- [08-ai-assisted-development-for-backend-engineers.md](./08-ai-assisted-development-for-backend-engineers.md)

## Required

- [06-local-ai-server-for-backend-engineers.md](./06-local-ai-server-for-backend-engineers.md)
- [07-securing-private-ai-services.md](./07-securing-private-ai-services.md)
- [08-ai-assisted-development-for-backend-engineers.md](./08-ai-assisted-development-for-backend-engineers.md)
- [02-prompting-and-evals.md](./02-prompting-and-evals.md)

## Growth

- [05-ai-serving-observability-and-rollout.md](./05-ai-serving-observability-and-rollout.md): required once the AI service becomes team-shared, product-facing, or operationally important
- [03-tools-agents-and-guardrails.md](./03-tools-agents-and-guardrails.md)
- [04-ml-and-ai-pipelines-for-jvm-backend.md](./04-ml-and-ai-pipelines-for-jvm-backend.md)
- [../python/README.md](../python/README.md): Python as the fastest glue language for AI-adjacent backend work
- [../architecture/13-enterprise-integration-patterns.md](../architecture/13-enterprise-integration-patterns.md): where AI belongs in backend integration systems and where it should stay outside the core write path
- [../cloud/README.md](../cloud/README.md): runtime, Kubernetes, and ownership choices around containerized workflow execution

## Runnable Companion

- [../../labs/kotlin-backend-examples/README.md](../../labs/kotlin-backend-examples/README.md): run `./run-topic.sh integration/ai-boundary` after reading `04` and `05`

## Core Rule

- AI fluency is mainly about reliability and boundary judgment, not about sounding futuristic
- local or private AI hosting still needs normal backend discipline
- AI-assisted development should reduce friction, not replace verification
- prompt quality matters, but evaluation matters more
- pipeline shape and serving boundaries matter more than AI buzzwords
- tools, retrieval, and agents help only when they reduce real product or workflow friction
