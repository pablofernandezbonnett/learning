# Payment Integration Patterns

A payment call can time out after the provider accepted it. Design the local payment state around that uncertainty so retries, duplicate notifications, and crashes cannot silently create another charge or a paid order without evidence.

## Keep One Attempt and an Explicit State

```text
order → local payment attempt → provider operation
                       ↘ pending / succeeded / failed / needs reconciliation
```

Store an attempt with a unique business operation key before calling the provider. Repeated requests with the same key and payload return or inspect that attempt; a conflicting payload is rejected. Enforce uniqueness in durable storage, not with a read-then-insert check. Pass a stable provider idempotency key on retries according to that provider's contract.

A timeout means the outcome is unknown. Keep a pending state and reconcile through provider lookup or notifications before deciding success or failure. Do not assume that every network error is retryable with a new key. Record the provider's payment identifier so later events can be matched to the local attempt.

## Treat Notifications as Untrusted and Repeatable

Verify a webhook's signature against the raw payload and the configured secret. Record or claim its event ID durably, apply allowed state transitions idempotently, and handle duplicate or out-of-order delivery. Acknowledge only after the work or its durable queue handoff is safe; an in-process asynchronous call can be lost after an early success response.

Reconciliation covers missing or delayed notifications. The local state machine should accept a confirmed provider result once and reject conflicting or backward transitions. Keep order fulfillment, inventory release, and customer messages tied to those confirmed states.

## Capture, Refund, and Provider Choice

Some flows authorize first and capture later; others capture immediately. Choose from the actual payment method, provider contract, fulfillment model, and business requirements. Store capture and refund attempts with their own operation IDs, idempotency rules, and final status. A refund is a separate operation that may also complete asynchronously or fail.

Compare providers by supported markets and methods, auth/capture behavior, idempotency lifetime, notification and reconciliation contracts, dispute handling, and data responsibilities. Keep provider-specific payloads behind an adapter, while the application's payment and order invariants remain local.

## Card Data Boundary

Hosted or provider-controlled collection can reduce the card data your systems touch, but outsourcing does not remove every PCI DSS responsibility. Map the exact pages, scripts, systems, and third parties in the flow with the provider and the entity that accepts your compliance evidence. Never put card data, client secrets, or provider keys in routine logs.

## Checks

- Repeat and concurrent requests create one local attempt and at most one intended provider operation.
- Timeout before response, crash after provider acceptance, and lost notification all have a reconciliation path.
- Duplicate, invalid-signature, and out-of-order events do not cause duplicate fulfillment.
- Refund or capture retries preserve the same intended operation and leave an auditable result.

## Further Reading

- [Stripe idempotent requests](https://docs.stripe.com/api/idempotent_requests) and [webhook signatures](https://docs.stripe.com/webhooks/signatures) as concrete provider examples.
- [PCI SSC on outsourced processing](https://www.pcisecuritystandards.org/faqs/does-pci-dss-apply-to-merchants-who-outsource-all-payment-processing-operations-and-never-store-process-or-transmit-cardholder-data/).
