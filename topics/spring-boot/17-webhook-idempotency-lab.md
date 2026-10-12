# AppSec Lab: Webhook Authenticity and Idempotency

Build `POST /webhooks/payments`. The endpoint accepts a provider event only after verifying its signature, applies each event ID once, and responds quickly. Use the provider's actual signing specification when implementing the exercise; the model below does not define a provider-specific signature format. See [webhook basics](../api/03-webhooks-basics.md) and [idempotency](../databases/01-idempotency-and-transaction-safety.md).

## Tasks

1. Identify the raw bytes and headers needed for signature verification. Reject missing or invalid signatures before parsing business fields.
2. Decide whether timestamps and replay windows are part of the provider contract.
3. Store a unique event ID with the business transition in one local transaction.
4. Decide which failures receive a retryable response and which are permanently rejected.
5. Keep long-running downstream work behind a durable handoff when needed.

## Solution guide

```text
receive raw request
verify signature using provider's rules
parse and validate event
begin transaction
  claim event ID with unique constraint
  if already completed: return previous successful acknowledgement
  load order; check allowed state transition
  apply transition and record event outcome
commit
acknowledge
```

Do not mark the event complete before the business change commits. If later work is asynchronous, commit its durable work record with the local transition and allow the worker to retry. Authenticity proves who sent the message; idempotency protects the local effect when a valid message is delivered again.

Check forged signature, missing signature, first delivery, duplicate delivery, concurrent duplicates, invalid order transition, and crash before versus after commit.
