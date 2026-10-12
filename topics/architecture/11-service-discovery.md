# Finding Current Service Instances

An application should call a stable service name, while the runtime tracks
which instances can receive traffic now. Discovery solves that mapping when
instances start, stop, move, or scale.

## The Smallest Model

```text
caller -> stable name -> resolver or proxy -> current endpoint -> instance
```

Discovery answers *where to send a call*. Readiness and health policy decide
which instances belong in the endpoint set. Load balancing chooses among
those instances. These jobs are related, but a valid name alone does not
prove an instance is ready or that the request will succeed.

| Routing style | How it works | Main cost |
| --- | --- | --- |
| Client-side | Caller receives candidate endpoints and selects one | Every client needs discovery and balancing behavior |
| Infrastructure-side | Caller uses a stable address; a proxy or service endpoint selects an instance | Routing infrastructure needs its own health and availability |

Use the platform's existing discovery mechanism before adding a separate
registry. Keep callers insulated from individual instance addresses. A
literal instance IP in application configuration makes a restart or scale
change a manual coordination problem.

## Readiness During Deployment

An instance should enter the routing set only when it can serve the relevant
traffic. During shutdown, stop new traffic and let in-flight work finish as
far as the runtime permits. **Readiness** answers whether to route new work
to an instance; **liveness** answers whether the process should be restarted.
Do not use liveness as a substitute for handling a slow dependency: repeated
restarts can make the outage worse.

Even a ready endpoint can fail after selection. A rollout, connection race,
or network fault can invalidate the route before the call completes. Use
[timeouts and safe retries](./02-resiliency-patterns.md) for that remaining
failure, and [network path diagnosis](./04-networking-fundamentals.md) to
locate where it occurred.

## Small Example

`Orders` calls the logical `Inventory` service. The runtime routes to one of
its ready instances. During a rollout, a new instance joins only after it
can handle requests, and an old one leaves before shutdown. Orders keeps
using the same service name. If the selected call times out, Orders still
needs an operation-specific retry or outcome check; discovery cannot decide
whether stock was reserved.

For platform-specific deployment and probe configuration, see
[Kubernetes](../devops/04-kubernetes-crash-course.md).

## Decision Rule

When describing discovery, identify the stable name, who resolves it, how
instances join and leave, and who balances traffic. Then handle failures that
can still occur after an endpoint is selected.

For a concrete runtime example, see [Kubernetes readiness and liveness
probes](https://kubernetes.io/docs/concepts/workloads/pods/probes/).
