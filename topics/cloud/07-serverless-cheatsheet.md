# Serverless Cheatsheet

> Primary fit: `Platform / Growth lane`


Use this after reading
[04-serverless-for-backend-engineers.md](./04-serverless-for-backend-engineers.md).

This is the short version to retain and reopen quickly.

---

## The Core Idea

Serverless does not mean "no servers."

It means:

- you do not manage the servers directly
- the platform handles provisioning and scaling
- you still own distributed-systems correctness

Short version:

> Serverless removes server management, not system complexity.

---

## When To Use It

Use serverless when the workload is:

- bursty
- event-driven
- infrequent enough to benefit from scale-to-zero
- small, stateless, and easy to retry

Good fits:

- webhooks
- cron jobs
- queue consumers
- file processing
- idempotency and TTL-backed side-state with DynamoDB

---

## When Not To Force It

Be careful when:

- latency is very sensitive
- cold starts are unacceptable
- the service is always busy anyway
- you need many persistent DB connections
- the service is stateful or long-running

---

## The Design Pattern To Remember

The common good pattern is:

1. receive the trigger
2. validate/authenticate it
3. deduplicate or persist minimal state
4. acknowledge quickly
5. push slow work to a queue
6. process asynchronously

Example:

`webhook -> function -> dedupe store -> queue -> worker -> DLQ`

---

## The 5 Things To Remember

1. **Start from the trigger.**
   HTTP, queue, cron, and file upload lead to different designs.

2. **Make retries safe.**
   Idempotency is mandatory.

3. **Keep compute stateless.**
   State belongs in external systems.

4. **Separate fast acknowledgement from slow work.**
   Do not block webhook/API paths with heavy processing.

5. **Serverless compute does not force serverless data.**
   Postgres may still be the right source of truth.

---

## Common Traps

- cold starts
- hidden DB connection pressure
- doing too much synchronous work
- weak observability
- assuming retries are rare

---

## Fast Runtime Notes

### JVM

- cold starts are more visible
- leave room for runtime overhead
- serverless containers may fit better than functions for public APIs

### Go / Node

- often fit short-lived functions better
- still need idempotency and observability

---

## Short Answer Shape

Good short answer:

> I would use serverless when the workload is event-driven, bursty, or operationally
> simple enough to benefit from managed scaling. I would still design it like any
> distributed system: idempotent handlers, fast acknowledgement, queues for slow
> work, explicit retries, and strong observability.
