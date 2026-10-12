# Cloud: Choose the Responsibility Boundary

Cloud services let a team rent infrastructure or use managed capabilities
instead of operating every layer itself. The first design question is which
work the provider handles and which work the team still owns.

## The Smallest Model

| Runtime shape | Team still owns | Provider takes on |
| --- | --- | --- |
| Virtual machine | OS updates, runtime, application, scaling design | Physical infrastructure |
| Managed container runtime | Image, application, configuration, workload sizing | Much of host and orchestration operation |
| Managed function | Handler, trigger, limits, retry and state design | Runtime fleet and instance provisioning |
| Managed database | Schema, queries, access, backup and recovery requirements | Database host operation and service-specific maintenance |

These are examples, not a ranking. More management can reduce routine
operations but may limit control and change cost or portability. Security is
shared: provider-managed infrastructure does not make application access,
data handling, or configuration safe automatically.

## Start From the Workload

1. **Work:** Is this a long-lived API, a bursty job, an event handler, or a
   stateful service? How long does one unit of work run?
2. **State:** Which data is authoritative? What must survive a process or
   zone failure? What may be cached or rebuilt?
3. **Traffic:** What are the normal and peak rates, latency targets, and
   connection needs?
4. **Failure:** Which dependency can fail, what can degrade, and how will
   the team restore data and service?
5. **Ownership:** Which layers can the team operate well, and which managed
   service would remove meaningful work?
6. **Cost and change:** What is paid for when idle or under peak load, and
   how hard would it be to change the service later?

Pick the simplest runtime that meets these needs. Do not choose a cluster
only because the application has several services, or a function only
because traffic is variable.

## Regions, Zones, and Network Boundaries

A region is a geographic deployment area; zones are separate locations
within it. Placement affects latency, availability, data rules, and cost.
Decide whether one zone failure must leave the service available and whether
cross-region recovery is actually required. Replicas do not protect data
unless storage and failover are designed too.

Keep databases and internal services off public entry paths unless exposure
is intentional. Define who may reach each component, and use workload
identity and short-lived credentials where the platform supports them.

## Example: Order Service

An order API runs continuously, uses a relational database for durable
orders, and sends noncritical notifications later. A managed container
runtime and managed relational database may fit if the team wants less
host operation. The team still owns schema changes, access rules, request
limits, backup verification, and recovery from a failed release.

## Go Deeper by Decision

- [Serverless runtime choice](./03-serverless-runtime-choice.md): functions and managed compute for triggered work.
- [Container sizing](./04-container-sizing.md): memory, CPU, and workload measurements.
- [Infrastructure as code](./02-infrastructure-as-code-and-platform-boundaries.md): repeatable resource changes and state ownership.
- [AWS service map](./08-aws-service-map.md): one provider's names for common capabilities.
- [Delivery and runtime](../devops/README.md): packaging, rollout, and observability.
