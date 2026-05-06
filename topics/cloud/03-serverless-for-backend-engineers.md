# Serverless for Backend Engineers

> Primary fit: `Platform / Growth lane`


You do not need to become a cloud-platform specialist.

Quick review version:

- [07-serverless-cheatsheet.md](./07-serverless-cheatsheet.md)

This document explains serverless as a practical backend runtime model.

The goal is not to memorize AWS product names first.

The goal is to understand:

- what serverless actually means
- when it is a good fit
- when it creates problems
- how to implement it without the common mistakes

Use it in the same reusable study shape as the rest of the repo:

- definition
- minimal example
- real implementation
- practical summary

This guide intentionally references existing material instead of repeating every
database, messaging, or security detail from scratch.

If you only keep the shortest retention layer:

- start from the trigger
- keep compute stateless
- assume retries and duplicate delivery
- be honest about cold starts, limits, and whether a normal service is the simpler answer

---

## Why This Matters

Serverless is easy to oversell and easy to dismiss. In practice, it is just one
runtime model with a specific tradeoff shape: less server management, but still
real distributed-systems, cost, and operational decisions.

This matters because many teams choose serverless for the wrong reason, then
discover too late that the workload, latency profile, or connection model did
not fit.

## Smallest Mental Model

Treat serverless as event-driven or request-driven compute that the platform
runs for you.

The real decision is not "is serverless modern?" It is:

- what triggers the work
- how stateful the work is
- how sensitive the latency is
- how much operational control you actually need

## Bad Mental Model vs Better Mental Model

Bad mental model:

- serverless means no operations
- all backend workloads can be decomposed into functions if we try hard enough
- using a provider-managed runtime automatically simplifies the whole system

Better mental model:

- serverless removes server ownership, not architecture ownership
- it works best for stateless, bursty, event-driven, or naturally decoupled
  workloads
- the cost model, runtime limits, and cold-start profile must still match the
  business need

Small concrete example:

- weak approach: move a latency-sensitive always-busy public API into JVM
  functions because "Lambda scales automatically"
- better approach: use functions for webhook intake or scheduled jobs, and keep
  a steady low-latency API in a container runtime if that fits the traffic
  better

Strong default:

- choose serverless first for bursty jobs, queue consumers, scheduled work, or
  event intake
- be skeptical when the workload is always hot, long-lived, highly stateful, or
  extremely latency-sensitive

Interview-ready takeaway:

> I treat serverless as a runtime tradeoff, not a maturity badge. It fits
> stateless and event-driven workloads well, but I still need to reason about
> retries, idempotency, cold starts, limits, and cost shape.

---

## 1. What Serverless Actually Means

Serverless does **not** mean "there are no servers."

It means **you do not manage the servers directly**:

- no VM patching
- no cluster provisioning
- no manual capacity planning for every traffic spike
- no keeping idle machines alive just in case

You deploy code, a container, or configure a managed service.
The cloud provider handles the underlying runtime fleet.

What you still own:

- service boundaries
- data model choices
- timeouts and retries
- idempotency
- authentication and authorization
- secrets and configuration
- logging, metrics, and tracing
- cost control

Core sentence:

> Serverless removes server management, not distributed-systems complexity.

Minimal mental model:

`webhook -> function -> dedupe store -> queue -> worker`

---

## 2. Serverless Is Not One Thing

When people say "serverless," they often mean different runtime shapes.

### A. Functions as a Service

Examples:

- AWS Lambda
- Google Cloud Functions
- Azure Functions

Shape:

- code runs only when triggered
- billing is based on invocations and execution time
- scale can jump very quickly

Good for:

- webhook handlers
- scheduled jobs
- file processing
- queue consumers
- small event-driven APIs

Main risk:

- cold starts and runtime limits

### B. Serverless Containers

Examples:

- AWS Fargate
- Google Cloud Run
- AWS App Runner

Shape:

- you package the app as a container
- the platform runs it without exposing the underlying cluster

Good for:

- HTTP services
- containerized apps that are too awkward to split into many functions
- teams that want lower ops burden without full Kubernetes ownership

Main risk:

- people call it "serverless" and then forget it is still a normal service
  design problem with health checks, memory sizing, and dependency management

### C. Serverless Managed Data and Integration Services

Examples:

- DynamoDB
- S3
- SQS
- EventBridge

These matter because real serverless systems are usually not "just a Lambda."
They are combinations of:

- a trigger
- some compute
- a queue or event bus
- a data store

Related reading:

- [01-cloud-basics.md](./01-cloud-basics.md)
- [../api/02-message-brokers-and-delivery-semantics.md](../api/02-message-brokers-and-delivery-semantics.md)
- [../api/03-webhooks-basics.md](../api/03-webhooks-basics.md)

---

## 3. When Serverless Is a Good Fit

Use serverless when one or more of these are true:

- the workload is bursty or highly variable
- the workload is event-driven
- you want scale-to-zero for infrequent jobs
- the team wants lower operational overhead
- the unit of work is naturally small and stateless
- different parts of the system need to scale independently

Typical use cases:

- payment or GitHub webhook intake
- image or document processing after upload
- scheduled report generation
- queue-based background jobs
- lightweight internal integration services
- idempotency-key storage and TTL-based records with DynamoDB

Serverless is especially attractive when the alternative would be keeping a full
service running 24/7 just to handle occasional traffic.

---

## 4. When Serverless Is a Bad Fit

Do not force serverless onto workloads that naturally fight it.

Be careful when:

- latency is extremely sensitive and cold starts are unacceptable
- the service is always busy anyway, making per-invocation pricing less compelling
- requests are long-running or stateful
- you rely on many persistent DB connections
- the codebase is a large tightly-coupled monolith
- you need very custom networking or low-level runtime control

Important JVM note:

Java and Spring can work in serverless environments, but cold starts are much
more visible than with Go or Node.js. For latency-sensitive public APIs, a
serverless container or a normal containerized service is often the better fit.

Serverless is not a badge of modernity.
It is a tradeoff.

---

## 5. Practical Design Rules

These are the rules that matter more than the provider logo.

### 1. Start From the Trigger

Ask first:

- what triggers this execution
- what latency is acceptable
- is the caller synchronous or asynchronous
- can the work be retried safely

The trigger defines the design.

An HTTP-triggered function should usually validate, persist what is necessary,
and hand off slow work quickly.

A queue-triggered function must be built assuming **at-least-once delivery**.

### 2. Keep Compute Stateless

Do not rely on local memory or local disk as durable truth.

Use external stores for state:

- DynamoDB for key-based serverless-friendly state
- S3 for objects and files
- Postgres for strong relational correctness
- Redis only when ephemeral speed is the actual need

### 3. Make Retries Safe With Idempotency

Retries are normal in serverless:

- providers retry failed invocations
- queues redeliver messages
- webhook senders retry on timeout
- clients resend HTTP requests

That means idempotency is not optional.

Use stable request or event IDs and make duplicate processing safe.

Related reading:

- [../databases/01-idempotency-and-transaction-safety.md](../databases/01-idempotency-and-transaction-safety.md)
- [../api/03-webhooks-basics.md](../api/03-webhooks-basics.md)

### 4. Separate Fast Acknowledgement From Slow Work

A common mistake is doing expensive business work directly inside the request
path.

A better pattern is:

1. verify and validate the request
2. record deduplication state
3. return quickly if possible
4. push the heavy work to a queue or event bus

This is one of the main strengths of serverless designs.

### 5. Be Deliberate About Data Stores

A key-value or document store can work well here when access patterns are stable
and the workload is high-throughput. This guide does not go deep into vendor-specific
choices.

Short version:

- use DynamoDB when access patterns are known in advance and the workload is
  key-based, high-throughput, and operationally lightweight
- do not use DynamoDB when you need complex relational queries, joins, or
  evolving ad-hoc filtering

Serverless compute does **not** force serverless data.

If correctness depends on relational transactions, Postgres may still be the
right source of truth even when the surrounding compute is Lambda-based.

### 6. Watch Connection Pressure on Relational Databases

Functions can scale out very fast.
Traditional relational databases do not appreciate sudden connection storms.

If you put a bursty function directly in front of Postgres without connection
strategy, you can create more operational pain than you removed.

That is why serverless designs often prefer:

- queue buffering
- write-behind patterns
- connection pooling/proxy layers
- DynamoDB for very simple key-based state

### 7. Treat Observability as Required

A serverless system can fail in many small places:

- trigger failure
- permission error
- queue retry loop
- dead-letter buildup
- downstream timeout
- duplicate processing

Minimum operational baseline:

- structured logs
- correlation IDs
- metrics on success, failure, latency, retry, DLQ depth
- tracing where supported
- alarms on DLQ growth and repeated errors

---

## 6. A Reference Implementation Pattern

Here is a practical serverless pattern that fits the material already in this
repository.

### Example: Payment Webhook Intake

```text
Payment Provider
    -> API Gateway / HTTP endpoint
    -> Function validates signature
    -> Function writes eventId to DynamoDB with conditional put + TTL
    -> Function returns 200 quickly
    -> Function publishes normalized message to SQS
    -> Worker Function consumes from SQS
    -> Worker updates system of record / downstream services
    -> Failures after retries go to DLQ
```

Why this works:

- the HTTP path stays short
- duplicate webhook delivery is handled safely
- spikes are absorbed by the queue
- worker retries are decoupled from the provider timeout window
- DynamoDB TTL lets dedupe records expire automatically

What each piece is doing:

- **API Gateway / HTTP endpoint:** public entry point
- **Function:** authentication, verification, normalization, fast acknowledgement
- **DynamoDB:** event ID deduplication with conditional write
- **SQS:** async buffering and retry boundary
- **Worker Function:** slower business processing
- **DLQ:** operational safety net for poison messages

This pattern connects directly to:

- [../api/03-webhooks-basics.md](../api/03-webhooks-basics.md)
- [../databases/01-idempotency-and-transaction-safety.md](../databases/01-idempotency-and-transaction-safety.md)
- [../api/02-message-brokers-and-delivery-semantics.md](../api/02-message-brokers-and-delivery-semantics.md)

### Important Boundary

If the worker must update a relational database **and** publish a business event,
the usual dual-write risk still exists.

Serverless does not remove that problem.

Use the same patterns you would use elsewhere:

- transactional outbox
- idempotent consumers
- explicit event IDs

Related reading:

- [../architecture/03-distributed-transactions-and-events.md](../architecture/03-distributed-transactions-and-events.md)

---

## 7. Pros

- low server-operations overhead
- strong fit for event-driven and scheduled workloads
- independent scaling of small execution units
- pay-for-use economics on sporadic traffic
- easy composition with queues, object storage, and managed services

---

## 8. Tradeoffs

- cold starts
- harder local testing for event-driven flows
- more moving parts once the system stops being trivial
- vendor coupling around triggers, permissions, and managed integrations
- observability becomes mandatory very quickly
- relational database connection pressure can become a hidden bottleneck

---

## 9. Quick Runtime Choice

| Choose | When it fits | Watch out for |
|---|---|---|
| **Function as a Service** | Bursty events, webhooks, cron, short async jobs | Cold starts, runtime limits, DB connections |
| **Serverless containers** | HTTP services, container portability, lower ops without Kubernetes | Still need normal service hygiene and capacity thinking |
| **Kubernetes / VM / long-lived service** | Stateful or always-on workloads, custom runtime control, stable high traffic | Higher operational ownership |

---

## 10. Practical Summary

If you need a short senior-level answer:

> I would use serverless when the workload is event-driven, bursty, or operationally
> simple enough to benefit from managed scaling. I would still design it like any
> distributed system: idempotent handlers, queues for slow work, explicit
> retries and timeouts, and strong observability. For low-latency JVM APIs,
> heavy relational traffic, or long-lived services, container-based deployment is
> often the safer fit than forcing everything into functions.

---

## 11. Related Reading

- [01-cloud-basics.md](./01-cloud-basics.md)
- [../api/03-webhooks-basics.md](../api/03-webhooks-basics.md)
- [../api/02-message-brokers-and-delivery-semantics.md](../api/02-message-brokers-and-delivery-semantics.md)
- [../databases/01-idempotency-and-transaction-safety.md](../databases/01-idempotency-and-transaction-safety.md)
- [../architecture/03-distributed-transactions-and-events.md](../architecture/03-distributed-transactions-and-events.md)
