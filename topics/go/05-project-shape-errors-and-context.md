# Go Project Shape, Errors, and Context

Use this after the runnable examples when you want the minimum Go mental model
that helps you start writing backend code without accidentally writing
Java-shaped Go.

---

## 1. Why This Matters

The first Go trap for a JVM engineer is usually not syntax.

It is one of these:

- too many abstractions too early
- treating errors like exception messages instead of values
- losing cancellation and timeout control across boundaries

If you get package shape, errors, and `context.Context` roughly right, small Go
services become much easier to read and extend.

---

## 2. Smallest Useful Mental Model

For small backend code, start with four rules:

1. packages group code by responsibility, not by framework layer ceremony
2. interfaces belong near the code that consumes them
3. errors are returned values, often wrapped with more context
4. `context.Context` carries request-scoped cancellation, deadlines, and trace-like values across boundaries

Plain-English version:

> Keep the code flat, pass cancellation through, and make failures explicit.

---

## 3. Bad Mental Model Vs Better Mental Model

Bad mental model:

- "I should create controller, service, repository, mapper, DTO, and interface
  for each one before the first endpoint exists."

Better mental model:

- "I should add only the structure that makes one boundary or one risky behavior
  easier to reason about."

Bad mental model:

- "Errors are mostly strings that I log and move on."

Better mental model:

- "Errors are part of control flow. I return them, wrap them, inspect them, and
  map them clearly at the boundary."

Bad mental model:

- "If I need a context, I can just create `context.Background()` anywhere."

Better mental model:

- "Request work should usually inherit the caller's context so timeout and
  cancellation still mean something downstream."

---

## 4. Small Concrete Example

This is the kind of shape that fits many small services:

```go
package reservation

import (
	"context"
	"errors"
	"fmt"
)

var ErrReservationNotFound = errors.New("reservation not found")

type Reservation struct {
	ID     string
	SKU    string
	Amount int
}

// The interface lives next to the service that needs it.
// It describes the dependency from the consumer side.
type Store interface {
	Save(ctx context.Context, r Reservation) error
	FindByID(ctx context.Context, id string) (Reservation, error)
}

type Service struct {
	store Store
}

func NewService(store Store) Service {
	return Service{store: store}
}

func (s Service) Get(ctx context.Context, id string) (Reservation, error) {
	reservation, err := s.store.FindByID(ctx, id)
	if err != nil {
		if errors.Is(err, ErrReservationNotFound) {
			return Reservation{}, err
		}
		return Reservation{}, fmt.Errorf("find reservation %s: %w", id, err)
	}
	return reservation, nil
}
```

What matters here:

- the service accepts `context.Context` because request-scoped work may timeout
  or be cancelled
- the service owns the interface because it defines what it needs
- the error is wrapped with more context using `%w`, so callers can still use
  `errors.Is(...)`

At the HTTP boundary, that often becomes:

```go
result, err := service.Get(r.Context(), reservationID)
switch {
case err == nil:
	writeJSON(w, http.StatusOK, result)
case errors.Is(err, ErrReservationNotFound):
	writeJSON(w, http.StatusNotFound, errorResponse{Error: "not found"})
default:
	writeJSON(w, http.StatusInternalServerError, errorResponse{Error: "internal error"})
}
```

This is the main Go flow:

- do the work
- return the error
- inspect the error at the boundary that knows how to translate it

---

## 5. Strong Default

For a small Go backend service, this is a good default:

- start with one module and a small number of packages
- keep `main` for wiring only
- define interfaces only where a real boundary or test seam exists
- accept `context.Context` on request-scoped service, DB, or outbound HTTP methods
- wrap errors once per boundary with enough detail to explain what failed
- compare semantic errors with `errors.Is`, not string matching

Small project shape:

```text
my-service/
  go.mod
  cmd/api/main.go
  internal/config/config.go
  internal/reservation/service.go
  internal/reservation/store.go
  internal/httpapi/router.go
```

Why this shape is enough:

- `cmd/api` makes the entrypoint obvious
- `internal/...` says the packages are app-internal
- the service code is still small enough to scan without framework archaeology

---

## 6. Where JVM Engineers Usually Overbuild

Common overcorrections:

- creating an interface for every struct before there is a consumer need
- splitting packages too early
- building annotation-like magic through helper layers
- hiding errors behind generic wrappers that lose the real failure

Short rule:

> In Go, duplication is often cheaper than premature abstraction.

That does not mean "write messy code".
It means "earn each layer by making one real problem easier."

---

## 7. Context Rule That Matters Most

Use the caller's context for request-scoped work.

Good:

```go
func (s Service) Reserve(ctx context.Context, req Request) error {
	return s.inventoryClient.Reserve(ctx, req.SKU, req.Amount)
}
```

Weak:

```go
func (s Service) Reserve(_ context.Context, req Request) error {
	ctx := context.Background()
	return s.inventoryClient.Reserve(ctx, req.SKU, req.Amount)
}
```

Why the weak version is weak:

- request timeout no longer propagates
- shutdown and cancellation signals no longer propagate
- tracing and request metadata can get lost

If you need a shorter deadline for one dependency call, derive from the incoming
context:

```go
ctx, cancel := context.WithTimeout(ctx, 800*time.Millisecond)
defer cancel()
```

---

## 8. One Practical Rule About Interfaces

Good default:

- return concrete types
- accept interfaces when the caller should be able to swap behavior

Why:

- concrete return values are easier to understand
- tiny consumer-owned interfaces are easier to fake in tests
- producer-owned interface hierarchies often bring Java habits into Go without payoff

---

## 9. Takeaway

The first useful Go design rule is not "use patterns."

It is:

> keep the project small, pass `context` through real work, and treat errors as
> explicit control flow instead of hidden exceptional flow.

## Further Reading

- [Go: Errors are values](https://go.dev/blog/errors-are-values)
- [Go blog: Working with errors in Go 1.13](https://go.dev/blog/go1.13-errors)
- [Go blog: Context](https://go.dev/blog/context)
- [Effective Go](https://go.dev/doc/effective_go)
