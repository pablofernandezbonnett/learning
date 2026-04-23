# Why This Go Service Shape Matters

This note is the learning layer for the runnable `Gin` service in this folder.

The code shows **how**.
This note explains **why** each part exists, **what problem it solves**, **what
mistake it prevents**, and **how to explain the design clearly**.

Use it after reading the folder [README.md](./README.md).

---

## 1. Service Shape

### Why this matters

A real backend service is not only a route handler.

You need to keep separate:

- app wiring
- transport concerns
- business decisions
- external boundaries

If those collapse into one file, the code works for a demo but becomes harder to
change, test, and explain.

### Smallest mental model

- `main.go` wires the app together
- `router.go` handles HTTP and middleware
- `reservation.go` decides business behavior
- `inventory.go` talks to the outside world

That is enough structure for a small service without pretending you need a big
enterprise architecture.

### What problem this solves

- route code stays thin
- business rules stop leaking into middleware or HTTP glue
- external failures are easier to test

### Common mistake

- putting validation, duplicate logic, HTTP client calls, and response shaping
  into one handler

### Short explanation

> I keep the route thin, the service responsible for business rules, and the
> outbound dependency explicit so failures and tests stay easy to reason about.

---

## 2. Routing and Middleware

### Why this matters

Routing answers:

- which endpoint handles this request
- which middleware runs around it
- which version or area the route belongs to

Middleware answers:

- what must happen for many requests in the same consistent way

In this slice:

- request ID middleware gives one correlation key per request
- access log middleware gives one obvious place for structured request logs

### What problem this solves

- repeated logging code does not spread across handlers
- request tracing is easier when debugging
- route groups make API shape easier to scan

### Common mistake

- putting business decisions into middleware because it runs "before everything"

Middleware is for cross-cutting transport concerns, not for hidden domain logic.

### Short explanation

> I use middleware for repeated transport concerns like request IDs and access
> logs, but I keep business rules in the service layer so behavior does not
> become implicit.

---

## 3. Context Timeout and Cancellation

### Why this matters

If an external call hangs, your handler should not wait forever.

That is what `context.WithTimeout` is protecting you from.

In this example the timeout is created before calling the inventory boundary, so
the route can fail fast with a `504` instead of hanging.

### What problem this solves

- protects request latency
- avoids waiting forever on a slow dependency
- makes timeout behavior deliberate instead of accidental

### Common mistake

- adding timeouts only in a global HTTP server config and assuming that is enough

The more important timeout is usually the one around the outbound boundary that
can stall.

### Short explanation

> I use request-scoped context timeouts around outbound dependencies so one slow
> downstream service does not pin the whole request path indefinitely.

---

## 4. Testing

### Why this matters

The value of tests here is not "coverage".

It is proving the service behavior that is easy to break:

- successful reservation
- idempotent duplicate request
- conflicting duplicate request
- timeout on the dependency
- invalid input

### Smallest mental model

- use `httptest`
- drive the real router
- stub or fake the external boundary
- assert on status and response shape

### What problem this solves

- checks the full HTTP contract, not only one function
- keeps timeout and duplicate behavior from regressing quietly

### Common mistake

- mocking everything so hard that the HTTP behavior is no longer under test

### Short explanation

> I test the router with `httptest` and stub the external dependency so I can
> verify the actual HTTP contract and the risky failure paths, not only the happy
> path.

---

## 5. External Boundary

### Why this matters

The inventory dependency is modeled as an explicit boundary because real backend
services almost always depend on something outside themselves:

- another service
- a provider API
- a database-like system behind a client

The important lesson is not the specific inventory call.
It is the separation between:

- business decision
- boundary call

### What problem this solves

- lets the service stay testable
- makes retry and timeout behavior visible
- lets you swap stub vs HTTP client without rewriting handlers

### Common mistake

- hiding the external call inside helper code that the route or service cannot
  reason about clearly

### Short explanation

> I keep external boundaries explicit because timeout, retry, and error mapping
> belong to a known edge, not buried inside generic helpers.

---

## 6. Graceful Shutdown

### Why this matters

Stopping a service cleanly is part of backend correctness.

If the process receives `SIGTERM`, you usually want to:

- stop accepting new work
- let in-flight requests finish within a limit
- exit predictably

That is what `signal.NotifyContext` plus `server.Shutdown(...)` is doing.

### What problem this solves

- cleaner deploys and restarts
- fewer half-finished requests during shutdown
- less operational surprise

### Common mistake

- killing the process immediately and assuming the platform will hide the damage

### Short explanation

> I use graceful shutdown so deploys and restarts stop taking new traffic first,
> then give in-flight requests a bounded window to finish.

---

## 7. Config and Logging

### Why this matters

Small services still need:

- explicit runtime settings
- observable behavior when something goes wrong

Config by env keeps the example deployable in a normal Go style.
Structured logging through `slog` keeps logs machine-friendly without pulling in
extra logging frameworks.

### What problem this solves

- avoids hidden defaults
- makes environment-specific behavior visible
- gives predictable logs for debugging and operations

### Common mistake

- hardcoding timeouts and URLs inside handlers

### Short explanation

> I keep runtime values in config and use structured logs so the service stays
> easier to operate and reason about across environments.

---

## 8. What To Practice Next

To turn this from passive reading into actual learning:

1. Change the timeout and make the timeout test fail, then fix it.
2. Add a new route that cancels a reservation and decide which state rules belong
   in the service.
3. Replace the stub inventory client with a local `httptest.Server` in manual
   runs and observe the timeout behavior.
4. Add one log field you would care about in production, such as `reservation_id`.

Short rule:

> If you can explain why each layer exists, change one piece safely, and predict
> which tests should fail, you are learning the service shape instead of only
> reading it.
