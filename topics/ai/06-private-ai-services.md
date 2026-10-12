# Local and Private AI Services

A local model runtime is an API that loads a model and answers requests. It can be useful for experimentation, privacy, or predictable access, but locality alone does not establish security, quality, or reliability.

## Choose the Boundary

Start with one task and test whether a local model meets its quality and latency needs. A personal machine may be enough for individual work. A shared service adds callers, documents, network exposure, capacity contention, and operational ownership.

Before sharing it, decide:

- Who can reach the endpoint and authenticate?
- Which prompts, documents, and source code may each caller submit or retrieve?
- Where does data persist, and what enters logs or backups?
- How are request size, rate, concurrency, and time limited?
- Who updates the model and runtime, observes failures, and rolls back?

Put a controlled gateway in front of a shared runtime when it needs authentication, quotas, audit, and a stable client contract. Keep the raw model endpoint reachable only from the intended network boundary. A gateway is also where application rules can reject unsafe inputs or outputs, though it cannot make every model answer correct.

## Protect Data and Actions

Filter retrieval by the caller's permissions before text reaches the model. Treat retrieved and pasted text as untrusted: it may contain instructions that try to redirect the assistant. Give any tool the minimum access needed for its task and check authorization when the tool executes. Avoid routine logging of full prompts, responses, tokens, or private documents.

For local hosting, measure model load time, memory or accelerator use, queueing, and response latency before adding users. The [serving note](./05-serving-and-rollout.md) covers rollout and quality monitoring for product-facing use.

## Further Reading

- [OWASP prompt injection](https://genai.owasp.org/llmrisk/llm01-prompt-injection/).
- [OWASP excessive agency](https://genai.owasp.org/llmrisk/llm062025-excessive-agency/).
