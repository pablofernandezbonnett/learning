# Webhooks: Safe External Event Intake

A webhook is an HTTP callback sent when another system reports an event. It
avoids repeated polling when timely updates matter, but the receiving
endpoint must handle forged requests, retries, duplicates, and delayed
delivery.

## Webhook or Polling?

- **Polling:** your system asks for current state when it needs it. It is
  simple for occasional checks but may waste calls or learn about changes late.
- **Webhook:** the sender pushes an event to your endpoint. Updates can arrive
  sooner, but you must operate a reachable, secure receiver and a recovery path.

A webhook is an external HTTP boundary. An internal application event stays
inside one process; a broker carries work between components. A receiver may
put verified webhook work on a queue after accepting it durably.

## Safe Intake Sequence

1. Identify the provider and verify the request using its documented
   signature or authentication scheme. Verify against the exact message
   representation the provider specifies.
2. Check payload size, event type, and any supported timestamp or replay
   window before expensive processing.
3. Persist an event identity that stays the same across retries with a
   uniqueness constraint. If the event was already accepted, return the
   provider's expected acknowledgement without repeating the business effect.
4. Apply only valid business state transitions. A genuine event can still be
   stale, out of order, or inconsistent with current local state.
5. Acknowledge only after the work is durable enough to survive a crash.
   Move slow follow-up processing out of the provider-facing request.

Signature rules, retry schedules, acknowledgement codes, and event IDs vary
by provider. Check that the ID you use for deduplication remains stable across
retries. Follow that provider's contract rather than assuming one universal
webhook protocol. If the provider offers an API to fetch current state, use
it to reconcile missing or conflicting events when needed.

## Example: Payment Confirmation

The provider sends `payment.succeeded` for payment `pay-42`. The receiver
verifies the signature, records the event ID once, and checks whether the
payment may move to `PAID`. A repeated delivery returns acknowledgement but
does not charge, fulfill, or email twice. If order fulfillment takes time,
queue it after durable acceptance.

Do not mark an order paid just because the JSON contains a familiar payment
ID. Authentication proves who sent a message; the current business state
still decides whether the transition is valid.

## Failure and Operations

- Reject requests that fail provider verification. Do not process them.
- Treat a temporary storage failure differently from an accepted duplicate;
  the provider may need to retry the former.
- Track accepted, rejected, duplicate, and failed deliveries with event IDs
  and safe diagnostic data. Keep secrets and sensitive payloads out of logs.
- Define who investigates missing events and how to replay or reconcile them.

For duplicate-effect handling, see
[idempotency and transaction safety](../databases/01-idempotency-and-transaction-safety.md).
For an internal handoff, see
[brokers and delivery semantics](./02-message-brokers-and-delivery-semantics.md).
For broader boundary checks, see
[web and API security](../security/02-web-and-api-security.md).
