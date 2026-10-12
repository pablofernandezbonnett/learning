# Recovering a Workflow Across Services

A local database transaction can commit an order and its own records together.
It cannot roll back a payment or stock reservation already committed by a
different service. A cross-service workflow therefore needs explicit states,
safe retries, and a way to recover from partial progress.

Use the [pattern choice guide](./16-distributed-workflow-pattern-choice.md)
first if you have not yet established that the work crosses independent write
owners. This note covers the recovery mechanics.

## Model the Business Outcome

For an order requiring payment authorization and stock reservation, record an
order as `PENDING` before assuming it can be confirmed. Each step has a local
transaction and reports a result. The workflow moves to `CONFIRMED` only after
the required results are known.

A timeout leaves the outcome **unknown**. The remote step might have succeeded
even though its response was lost. Before retrying, use a stable operation ID
so the owner can recognize the same request, or query its recorded result.

If stock cannot be reserved after payment was authorized, cancel the
authorization and mark the order `CANCELLED`. That cancellation is a new
business action, called a **compensation**; it does not erase the earlier
database commit. If payment was already captured, the compensating action may
instead be a refund. Compensation itself can fail and must be retried or
escalated until the business outcome is settled.

## Coordinate the Steps

A **saga** is a workflow of local transactions with explicit forward and
compensating actions. There are two common coordination styles:

| Style | How it works | Main cost |
| --- | --- | --- |
| Choreography | Each participant reacts to events and emits its result | The overall state can be hard to see when the chain grows |
| Orchestration | One workflow component records state and commands the next step | The coordinator needs durable state and recovery logic |

Start with a small, visible workflow. Choose orchestration when a long or
business-critical sequence needs one place to track progress and compensation.
Neither style removes the need for idempotent commands, timeouts, and clear
ownership of stuck cases.

## Publish a Committed Change Reliably

Saving an order and publishing `OrderPlaced` to a broker are two writes to
different systems. If the database commits and publishing fails, a downstream
step may never start. An **outbox** stores the business change and an event
record in the same local database transaction:

```text
transaction: save pending order + save outbox event
after commit: relay publishes event to broker
after broker acknowledgment: relay marks event as published
```

The relay must retry unpublished records. If it publishes successfully but
crashes before recording that success, it may publish again. Therefore each
event needs a stable ID and the consumer must tolerate duplicates. Monitor the
age of unpublished records: a growing backlog means committed work is waiting
for downstream processing.

The outbox solves publication after a local commit. It does not, by itself,
finish or compensate a multi-service business workflow. A saga may use an
outbox for its messages, but the two patterns solve different gaps.

## Make Each Effect Safe to Retry

For a consumer changing its own database, claim the event ID and perform the
business update in one local transaction. A uniqueness constraint on that ID
prevents concurrent deliveries from applying the effect twice. Checking
whether an ID exists and then writing it later is not enough: two consumers
can pass that check together.

For a remote payment, email, or other external effect, the local transaction
cannot cover the remote system. Pass a stable idempotency key when supported,
record the result, and provide a way to reconcile an unknown outcome. See
[broker delivery and duplicate effects](../api/02-message-brokers-and-delivery-semantics.md)
for the delivery mechanics.

## Operate the Unfinished States

Track how many workflows are pending, how long each step has waited, how many
compensations fail, and whether outbox records are stuck. Keep enough history
to answer which step last succeeded and whether retrying it is safe. Give
operators a way to inspect and repair cases that cannot complete automatically.

## Decision Rule

Write down every partial state and unknown outcome before choosing a broker or
workflow engine. Use a saga for cross-owner recovery, an outbox for reliable
publication of local changes, and idempotency for effects that can be retried.
