# ML Pipelines

An ML pipeline turns data and code into a versioned model artifact. The online service then uses that artifact to answer live requests. These paths have different latency, failure, and ownership needs.

```text
offline: collect → validate data → train or update → evaluate → version artifact
online: request → authorize → serve model → validate result → apply policy
feedback: observe quality and data changes → decide whether to retrain
```

Not every AI application trains its own model. A hosted generative model may replace the training path with provider and prompt selection, while the application still needs evaluation, version tracking, and a safe serving boundary.

## Questions for the Offline Path

- Where did the data come from, and may it be used for this purpose?
- Can a run be reproduced from data, code, configuration, and artifact versions?
- What checks catch schema drift, missing values, leakage, or a worse model?
- Who approves promotion of a new artifact, and how is it rolled back?

Use an orchestrator when dependent jobs, schedules, backfills, and failure recovery justify it. A workflow tool does not decide whether the model is good enough; explicit data and model checks do.

## Questions for the Online Path

Choose between a separate model service and in-process inference based on model format, runtime support, resource needs, latency, and team ownership. In either case, the application owns the request contract, permissions, time budget, and final side effects.

See [serving and rollout](./05-serving-and-rollout.md) for observing live quality, latency, capacity, and model changes.

## Further Reading

- [Google Cloud MLOps guide](https://docs.cloud.google.com/architecture/mlops-continuous-delivery-and-automation-pipelines-in-machine-learning) for data validation, artifact promotion, and operations.
