# Gin Service Slice for JVM Backend Engineers

This is the practical Go layer that moves past syntax refresh.

It shows a small service shape that is worth studying or using
as a bridge from Java/Spring into Go service work.

Why `Gin` here:

- it is widely used
- the official Go tutorial still uses it for a web-service walkthrough
- it is enough to learn one framework layer without turning the repo into a
  router zoo

Why not start with five frameworks:

- the real transferable ideas are service shape, middleware, `context`,
  validation, boundary handling, testing, and shutdown
- learning `Gin`, `Echo`, `Fiber`, and `chi` at once mostly teaches duplicated
  routing syntax

---

## What This Slice Covers

- service shape that looks like a real backend instead of one handler file
- routing and route groups
- middleware for request IDs and structured access logs
- `context` timeouts on outbound boundary calls
- config from env
- graceful shutdown with `signal.NotifyContext`
- standard-library logging through `log/slog`
- tests for success, duplicate handling, timeout, and retrieval

It uses:

- `Gin` for HTTP ergonomics
- the standard library for config, logging, shutdown, HTTP client, and tests

---

## Files

| File | Role |
|---|---|
| `main.go` | app wiring and graceful shutdown |
| `config.go` | env-backed config parsing |
| `router.go` | `Gin` router, middleware, and handlers |
| `reservation.go` | request/response models, store, and service logic |
| `inventory.go` | outbound inventory boundary, with HTTP and stub clients |
| `router_test.go` | route and service behavior tests |
| `why-this-service-shape-matters.md` | why each layer exists, what problem it solves, and how to explain it clearly |

---

## Run

From [04-gin-service](.):

```bash
go run .
```

Default behavior:

- starts on `:8080`
- uses an in-process stub inventory client so the example is runnable without a
  second service

Try:

```bash
curl -s http://localhost:8080/healthz

curl -s -X POST http://localhost:8080/api/v1/reservations \
  -H "Content-Type: application/json" \
  -d '{"reservation_id":"res-1","sku":"UT-WHITE-M","quantity":2}'

curl -s http://localhost:8080/api/v1/reservations/res-1
```

Test:

```bash
go test ./...
```

---

## Env Vars

| Variable | Default | Purpose |
|---|---|---|
| `APP_ENV` | `local` | environment label for logs |
| `HTTP_ADDR` | `:8080` | listen address |
| `INVENTORY_MODE` | `stub` | `stub` or `http` |
| `INVENTORY_BASE_URL` | empty | required when `INVENTORY_MODE=http` |
| `INVENTORY_TIMEOUT_MS` | `800` | timeout for outbound inventory calls |
| `SHUTDOWN_TIMEOUT_MS` | `5000` | graceful shutdown timeout |

If you want to exercise the real HTTP boundary client:

```bash
INVENTORY_MODE=http INVENTORY_BASE_URL=http://localhost:9090 go run .
```

---

## What To Notice

- `Gin` helps with routing, groups, JSON binding, and middleware chaining
- `net/http`, `context`, and `http.Server` still own the server lifecycle
- timeout handling belongs near the outbound dependency, not only in a generic
  framework layer
- idempotent duplicate handling belongs in the service logic, not in routing
  syntax

If you want the explicit learning layer, read
[why-this-service-shape-matters.md](./why-this-service-shape-matters.md) next.

Short rule:

> Learn one Go framework as a thin productivity layer, not as a replacement for
> understanding `net/http`, `context`, and explicit service behavior.
