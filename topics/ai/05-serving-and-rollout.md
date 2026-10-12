# AI Serving and Rollout

A model-backed feature must work under traffic while its answers remain useful. Track normal service health alongside output quality and cost; a successful HTTP response can still contain a bad answer.

## Observe the Whole Request

Record latency, error rate, traffic, saturation, and queue time. Add task-specific quality signals, invalid-output rate, retrieval misses when retrieval exists, and tool failures when tools exist. Keep the model, prompt, and retrieval versions associated with each outcome without logging sensitive source material by default.

Set request size, time, concurrency, and spending limits. Decide what the product does when the model is slow or unavailable: a simpler path, a retry-safe queue, a human review path, or an explicit failure. Do not let an optional AI step block a critical transaction indefinitely.

## Change with Evidence

Run the [evaluation set](./02-prompting-and-evaluation.md) before changing a model, prompt, tool, or retrieval policy. Compare quality, latency, cost, and unsafe outcomes with the current version.

- **Shadow:** run the new path on copied traffic without using its result for users; watch privacy and extra cost.
- **Canary:** serve a small, controlled share of traffic with the new path; monitor and roll back on a defined trigger.
- **Full rollout:** expand only after the earlier evidence and operational checks are acceptable.

Keep rollback practical: retain the previous model or configuration version and know how to route traffic back. Define who can stop the feature when quality degrades.

## Retrieval Health

When retrieval supplies context, check source freshness, permission filtering, coverage, and source IDs. A relevant-looking chunk may be stale or unauthorized. Measure how often the system finds no good source, and give it a safe way to say so. [Private AI services](./06-private-ai-services.md) covers the access boundary for shared documents.

## Further Reading

- [Google Cloud reliability guidance for AI and ML](https://docs.cloud.google.com/architecture/framework/perspectives/ai-ml/reliability).
- [OpenAI evaluation best practices](https://platform.openai.com/docs/guides/evaluation-best-practices).
