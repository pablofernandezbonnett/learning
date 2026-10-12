# Exercise: Reservation API in Go

Build a small reservation API to practice request handling, idempotency, outbound calls, and shutdown. Use `net/http` or Gin for routing; the same service decisions apply to both.

## Contract

- `POST /api/v1/reservations` accepts `reservation_id`, `sku`, and a positive `quantity`.
- `GET /api/v1/reservations/{id}` returns a stored reservation or a documented not-found response.
- The first successful creation returns `201`. Repeating the same ID and payload returns the existing reservation without reserving stock again. Reusing the ID with different data returns `409`.
- Invalid JSON or fields return `400`. Define safe responses for unavailable inventory, upstream timeout, and unexpected failures.
- `GET /healthz` confirms the process can answer requests. Decide separately whether dependency health belongs in readiness checks.

Set a body size limit, a bounded inventory call, an explicit shutdown deadline, and structured request logging. If using Gin, keep business rules out of middleware and use its router as an `http.Server` handler.

## Build in This Order

1. Write the request/response contract and error mapping.
2. Define a reservation service and a narrow inventory client boundary.
3. Implement duplicate detection before the inventory call.
4. Implement the outbound client with request context, timeout, status handling, and body cleanup.
5. Wire the router, configuration, logging, and graceful shutdown.
6. Test the public HTTP behavior and dependency failure paths.

## Resolution Guide

```text
POST reservation:
  decode with a size limit; validate ID, SKU and quantity
  find reservation by ID
  if same payload exists: return existing result
  if ID exists with different payload: return conflict
  call inventory with the same idempotency key and a deadline from the request context
  if outcome is uncertain: reconcile before treating a retry as a new write
  persist reservation and return created result
```

This sequence is a learning model, not an atomic production transaction. Two concurrent requests can both pass the first lookup. Use a unique constraint or conditional write for the ID, and design the inventory operation around a durable idempotency key. Decide how to recover when inventory succeeds but local persistence fails.

For a local exercise, an in-memory store and fake inventory client make the cases easy to explore. They lose state on restart and cannot coordinate multiple instances. Do not infer production guarantees from them.

## Checks

- First creation, identical repeat, conflicting repeat, and fetch after creation.
- Invalid JSON, missing fields, nonpositive quantity, oversized body, and unknown ID.
- Inventory rejection, timeout, and an ambiguous upstream result.
- Concurrent requests for the same ID: inventory must not reserve twice.
- Cancellation reaches the outbound call; shutdown stops intake and gives active requests a bounded window.

Read [service shape and context](../05-project-shape-errors-and-context.md), [HTTP and API clients](../06-http-https-and-api-clients.md), and [testing](../07-testing-tooling-and-quality.md) for the reusable rules behind the exercise.
