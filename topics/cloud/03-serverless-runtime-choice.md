# When Serverless Fits

Serverless means the provider operates more of the compute fleet and scales
execution from requests or events. Functions and managed container services
offer different runtime shapes; neither removes responsibility for data,
security, retries, or cost.

## Choose From the Work

| Workload | Possible fit | Check before choosing |
| --- | --- | --- |
| Short event handler, scheduled job, or bursty task | Function | Duration, startup latency, retries, concurrency, and downstream limits |
| HTTP service needing a container but little host operation | Managed container runtime | Idle behavior, startup, resource sizing, and deployment controls |
| Steady, long-running, stateful, or highly customized process | Long-lived service or VM may fit better | Operational effort and actual traffic cost |

These are starting points. Compare the provider's current limits and prices
for the actual workload; a "pay only when used" slogan does not settle the
cost of queues, logs, data, or sustained traffic.

## Design a Triggered Handler

1. Define the trigger and what counts as successful acceptance.
2. Keep durable state outside a replaceable execution instance.
3. Give the work a stable identity; retries and duplicate delivery must not
   create duplicate business effects.
4. Set an execution deadline and bound downstream calls.
5. Limit concurrency when a database or provider cannot absorb a burst.
6. Observe failures, age of pending work, retries, and dead-letter backlog.

The managed runtime can scale faster than a relational database's connection
capacity. Buffering, connection management, or a different data access
shape may be needed. Cold starts can matter for a latency-sensitive path;
measure them rather than assuming they are always negligible or fatal.

## Example: Payment Webhook

```text
provider -> HTTP handler verifies request
         -> durable queue accepts event
         -> handler acknowledges provider
         -> worker claims stable event ID and applies valid state transition
```

If the queue cannot accept the event, the handler does not acknowledge
success; the provider can retry according to its contract. If the queue
accepts the event but the HTTP response is lost, the provider may send it
again, so the worker must deduplicate. A function, container, or other
runtime can implement this same boundary.

See [webhooks](../api/03-webhooks-basics.md) and
[broker delivery semantics](../api/02-message-brokers-and-delivery-semantics.md)
for the full correctness rules.

## Review Questions

- What triggers the work, and who retries it?
- What state survives a stopped or repeated execution?
- Can the dependency handle the maximum concurrency?
- Does the latency and cost profile fit real traffic?
- How will failed work be found and replayed?
