# Choosing a Synchronous or Asynchronous Boundary

Start with what the caller needs to know. A synchronous call makes the caller
wait for an answer. Asynchronous work finishes later, so the caller needs an
honest accepted or pending state and a way to learn the outcome.

## Put the Boundary Around the User's Decision

| Situation | Starting choice | Why |
| --- | --- | --- |
| The next user action depends on a final answer | Synchronous, with a deadline | The answer is part of the current task. |
| The work is a follow-up, such as email or analytics | Asynchronous | Its failure should not undo the confirmed user action. |
| A provider may answer later | Pending state plus later confirmation | A timeout does not prove success or failure. |
| Several systems react to one committed fact | Publish an event after a durable write | They can process independently. |

Synchronous work is easier to follow but couples the request to dependency
latency and availability. Asynchronous work can buffer load and isolate later
tasks, but requires state transitions, retries, duplicate protection, and
backlog monitoring.

## Example: Checkout

The system can confirm that it accepted and saved an order now. If payment
confirmation is still uncertain, return `PENDING` and expose a status lookup.
When the provider confirms later, update the order through a valid state
transition. Email and analytics run after the durable business change.

Do not claim the payment succeeded because a provider call timed out. Do not
make the user wait for email or analytics unless the product explicitly
requires their completion before confirming the order.

## Design the Handoff

1. Define the last fact that must be true before replying to the caller.
2. Persist that fact and the work that must follow. Use an outbox when a
   database change and event publication must stay coordinated.
3. Give the later work a stable identity so retries do not duplicate effects.
4. Expose pending, failed, and completed states when the caller needs them.
5. Set timeouts and a recovery path for stuck or failed work.

A background task inside one process can be enough for disposable work. If
losing the task on restart would be unacceptable, use durable storage or a
broker. See [brokers and delivery semantics](./02-message-brokers-and-delivery-semantics.md).

## Review Questions

- What answer does the caller need before continuing?
- What can safely happen after the response?
- If the dependency times out, what do we actually know?
- How does the caller learn the final result?
- Can a replay or duplicate callback change the business result twice?
