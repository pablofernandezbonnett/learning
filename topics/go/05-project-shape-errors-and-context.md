# Go Service Shape, Errors, and Context

A small service needs clear boundaries between HTTP, business rules, and external I/O. Add packages when those boundaries become useful; a file or type does not need its own package by default.

## Start with the Request Flow

```text
HTTP handler → service decision → store or external client
      ↑               ↑                    ↑
 status and JSON   domain error       I/O error or timeout
```

The handler parses and validates the request, calls the service, and translates known outcomes into HTTP responses. The service owns rules such as duplicate handling. A store or client owns one external interaction. Keep configuration and wiring at the entry point.

Define a small interface near the code that consumes a dependency only when substitution is useful. [Go foundations](./00-go-foundations.md) covers how interfaces and wrapped errors work.

## Preserve Cancellation

Pass the incoming request context through service, database, and client calls. Derive a shorter timeout for a slow dependency; call its cancel function when finished.

```go
callCtx, cancel := context.WithTimeout(ctx, inventoryTimeout)
defer cancel()
result, err := inventory.Reserve(callCtx, request)
```

Creating `context.Background()` inside request work discards the caller's cancellation and deadline. A timeout limits waiting; it does not tell you whether an upstream write happened. Resolve that uncertainty with the operation's idempotency contract.

## Keep Error Meaning Across Boundaries

Return a domain error for expected outcomes, such as “reservation ID used with different data.” Wrap an I/O error with the operation that failed. At the HTTP boundary, map known errors to stable, safe responses; log unexpected details internally. Do not expose upstream response bodies or internal error strings to clients.

## Own Concurrent Work

For each goroutine started by a request, answer:

- Which context stops it when the request ends?
- Who waits for its result or error?
- What bounds parallel calls to the database or provider?

Use a bounded worker pool or concurrency limit for fan-out. A buffered result channel can prevent a sender from blocking after the caller leaves, but the downstream operation must also observe cancellation. Run concurrent tests with the race detector; it finds only races exercised by those tests.

## Further Reading

- [Go context package](https://pkg.go.dev/context) for cancellation and deadlines.
- [Go error wrapping](https://go.dev/blog/go1.13-errors) for preserving error identity.
- [Go race detector](https://go.dev/doc/articles/race_detector) for exercised data races.
