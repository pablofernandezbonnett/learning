# AI Systems

Use this block to decide where a model helps, what the surrounding application must control, and how to verify the result. Start with the general system boundary; open a specific note when that decision appears in your work.

1. [System basics](./01-ai-system-basics.md): model, data, policy, and side-effect boundaries.
2. [Prompting and evaluation](./02-prompting-and-evaluation.md): define the task and measure output quality.
3. [Tools and agents](./03-tools-and-agents.md): grant narrow capabilities and control actions.
4. [ML pipelines](./04-ml-pipelines.md): move data and model artifacts from preparation to deployment.
5. [Serving and rollout](./05-serving-and-rollout.md): operate a model-backed feature under real traffic.
6. [Private AI services](./06-private-ai-services.md): choose local or shared hosting and protect its data boundary.
7. [AI-assisted development](./07-ai-assisted-development.md): use an assistant on code with explicit intent and verification.

For a worked change from product request through specification, tasks, and
verification, use the [payment retry case](./09-spec-driven-development-for-ai-assisted-work.md).

The [Python block](../python/README.md) covers Python implementation choices. The [API block](../api/README.md) covers general service contract design. Try the [AI boundary exercise](../../labs/kotlin-backend-examples/README.md) after the serving note.
