# System Design Decision Reference

Use this after the [general design method](./00-general-system-design-method.md)
has identified the outcome, critical rule, data owner, trust boundary, and
likely limit. Each row points to the detailed note for one decision. Start
small, then add a component when a concrete need pays for its failure and
operating cost.

| Decision | Start by asking | Add complexity when |
| --- | --- | --- |
| [Application boundaries](../architecture/01-monolith-vs-microservices.md) | Can related work stay in one deployable with clear internal ownership? | Independent teams, releases, or scaling justify a network boundary. |
| [Data ownership](../architecture/18-shared-db-and-independent-deployments.md) | Who alone may change each business fact, even if the database is shared? | Separate storage helps ownership or operation after cross-domain writes are removed. |
| [Storage model](../databases/04-sql-vs-nosql.md) | Which queries, atomic writes, and data relationships must the store support? | The measured access pattern no longer fits the current store well. |
| [Immediate or later work](../api/07-sync-vs-async-integration-choice.md) | Does the caller need this result before the response? | Slow or uncertain work can finish later with durable state and recovery. |
| [Calls, queues, or events](../api/02-message-brokers-and-delivery-semantics.md) | Does one owner need an answer, one task need a worker, or several consumers need the same fact? | Buffering, independent consumption, or replay is worth delivery and duplicate-handling costs. |
| [Read acceleration](../architecture/07-caching-strategies.md) | Must this read be current, or can it be stale? Can the source query be improved first? | Repeated cost or load justifies a cache, [replica, or projection](../databases/05-database-scaling.md). |
| [API contract style](../api/00-rest-vs-graphql.md) | What operations and response shapes do clients actually need? | A different query or aggregation model solves a real client problem. |
| [Shared edge or BFF](../architecture/17-gateway-vs-bff-vs-edge-patterns.md) | Which entry concerns are shared, and which belong to one client? | Client-specific aggregation or latency needs justify a separate edge component. |
| [Provider updates](../api/03-webhooks-basics.md) | How does the provider report changes, and how will missing updates be found? | Delayed or lost callbacks require reconciliation, often through a status query. |
| [Runtime model](../cloud/03-serverless-runtime-choice.md) | How long does work run, how often, and what does it need to keep open? | Traffic shape or operating cost justifies changing the runtime model. |
| [Build or buy](../solutions-architecture/02-build-vs-buy.md) | Is the capability core, and what are integration and exit costs? | A product meets hard requirements at lower total ownership cost. |

## Two decisions that are easy to rush

### A busy read API

Use this order when search, catalogue, or history reads become slow. Stop when
the measured limit is addressed; the steps are options, not a required stack.

1. **Locate the wait.** Separate application CPU, database execution,
   connection-pool wait, cache misses, and remote calls. More API replicas help
   only if request handling is the limit.
2. **Make one request cheaper.** Bound filters and page size, remove repeated
   queries, and inspect the actual plan before adding an index. See
   [query optimization](../databases/08-query-optimization.md).
3. **Reuse repeated safe answers.** Cache a hot result only when its freshness
   and access scope are clear. Bound refresh work so expiry does not flood the
   source. See [caching](../architecture/07-caching-strategies.md).
4. **Spread broad read pressure.** A replica helps many ordinary reads when
   lag is acceptable; a projection helps a query whose shape remains costly.
   Neither makes the final decision for a competing write. See
   [database scaling](../databases/05-database-scaling.md).

Check database connections after increasing API instances. Multiplying each
instance's pool can move the bottleneck to the same database.

### A local write starts later work

First decide which fact must be committed before the caller can receive
success. If the later action can finish after the response, represent its
pending state durably and give it an owner and recovery path.

| Need | Starting mechanism | Failure to account for |
| --- | --- | --- |
| One worker should perform a task | Durable queue or work table | Retry can repeat the task; a stuck item needs inspection. |
| Several consumers need the same fact | Event with stable identity | Consumers may receive duplicates or lag independently. |
| A database change must trigger publication | Outbox committed with the change | Relay may publish again after a crash; consumers deduplicate effects. |
| Caller needs the remote answer now | Bounded synchronous call | Timeout means the remote result may be unknown. |

The mechanism does not replace the business state machine. Record what the
caller sees after a timeout and how unfinished work is found. See
[delivery semantics](../api/02-message-brokers-and-delivery-semantics.md)
and the [checkout case](./checkout-to-fulfillment-end-to-end.md).

Revisit a choice when its assumption changes: the workload, failure target,
security boundary, ownership, or cost. The [checkout case](./checkout-to-fulfillment-end-to-end.md)
shows several decisions interacting in one flow.
