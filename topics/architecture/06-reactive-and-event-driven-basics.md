# Webhooks, Events, and Reactive Code

These terms describe different boundaries. Mixing them can lead to a broker
where an HTTP callback is enough, or reactive code where ordinary blocking
code already meets the need.

| Term | What it is | Question it answers |
| --- | --- | --- |
| Webhook | An external system calls your HTTP endpoint after something happens | How does an outside system notify us? |
| Event-driven flow | A committed business fact triggers later work in one or more components | What should happen after this fact? |
| Reactive programming | A programming model for composing asynchronous, often non-blocking I/O | How should this code handle concurrent I/O or streams? |

An in-process event is local to one application and does not by itself provide
durable delivery. A broker can carry events across processes, but then retries,
duplicates, backlog, and delayed state become part of the design. Reactive
code is optional in either case.

## Small Example

A payment provider sends a webhook confirming a result. The receiving service
verifies and durably records the notification, updates its owned payment
state, and starts internal follow-up work. The webhook is the entry point;
events may carry follow-up work; reactive code is only an implementation
choice if the service's I/O and concurrency needs justify it.

For webhook verification, acknowledgment, and replay, read [webhook
intake](../api/03-webhooks-basics.md). For choosing a synchronous or
asynchronous handoff, read [integration
choice](../api/07-sync-vs-async-integration-choice.md). For broker behavior,
read [delivery semantics](../api/02-message-brokers-and-delivery-semantics.md).

## Decision Rule

Name the boundary first: external notification, internal follow-up, or code
execution model. Choose each mechanism only for the problem it solves.
