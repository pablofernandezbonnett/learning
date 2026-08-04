# Go Testing, Tooling, and Quality Baseline

Go's standard tools are deliberately close to the language. Use them early: a
small Go service becomes easier to review when formatting, tests, race checks,
and dependency hygiene are routine rather than a separate platform effort.

---

## Why This Matters

A JVM engineer may expect test discovery, mocking, formatting, static checks,
and build behavior to arrive through a Gradle or Maven setup. Go provides a
smaller default toolchain, but the responsibility to run it is more visible.

The compiler catches type mistakes, not incorrect HTTP status mappings, races,
or a retry that writes an order twice. Tests must target those behavior risks.

## Smallest Useful Mental Model

Tests live in `*_test.go` files and run with `go test`. They can test a package
without starting a real process. `net/http/httptest` supplies in-memory HTTP
requests, recorders, and test servers, so both inbound handlers and outbound
clients can be tested at the protocol boundary.

> Test observable behavior through small real boundaries, then use the standard
> tools to catch formatting, races, and dependency mistakes before continuous
> integration (CI) does.

## Test the HTTP Contract Without Opening a Port

For an inbound handler, create a request and recorder, serve the request, then
assert status and response. This is the Go equivalent of a focused MVC slice,
but uses standard `net/http` primitives.

```go
func TestCreateProductRejectsMissingSKU(t *testing.T) {
	store := newProductStore()
	req := httptest.NewRequest(http.MethodPost, "/products",
		strings.NewReader(`{"name":"T-Shirt","price_jpy":2990}`))
	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()

	store.createProduct(rec, req)

	if rec.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d: %s", rec.Code, rec.Body.String())
	}
}
```

For an outbound client, use `httptest.NewServer`. It verifies method, path,
headers, JSON, and timeout behavior while staying local and deterministic. The
[Gin service slice tests](./04-gin-service/router_test.go) use this pattern.

## Table-Driven Tests Fit Repeated Rules

When many inputs should follow the same contract, a table makes the variations
visible without duplicating test setup.

```go
func TestValidateQuantity(t *testing.T) {
	cases := []struct {
		name  string
		value int
		valid bool
	}{
		{name: "one item", value: 1, valid: true},
		{name: "zero is rejected", value: 0, valid: false},
	}

	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			if got := validateQuantity(tc.value); got != tc.valid {
				t.Fatalf("validateQuantity(%d) = %v, want %v", tc.value, got, tc.valid)
			}
		})
	}
}
```

Do not turn every test into a table. A one-off test is clearer when it teaches
one important scenario, such as timeout propagation or idempotency.

## Mocks and Interfaces: Keep the Seam Small

Bad mental model:

> Every dependency needs a generated mock and an interface before production
> code exists.

Better mental model:

> Test through a real local boundary when it is cheap (`httptest`, temporary
> database). Use a small consumer-owned interface and a hand-written fake when
> the dependency would otherwise be slow, remote, or hard to control.

The implementation behind an HTTP client is usually a better test seam than a
mock of a framework controller. This gives confidence in the actual request
shape, not only in a method call.

## Commands Worth Making Routine

Run these from the module directory:

```bash
go fmt ./...
go test ./...
go test -race ./...
go vet ./...
go mod tidy
```

What they provide:

- `go fmt`: canonical formatting; use it instead of debating style
- `go test ./...`: tests every package in the module
- `go test -race ./...`: detects many unsafe concurrent memory accesses during
  exercised test paths; it is not proof that concurrency is correct
- `go vet ./...`: catches suspicious constructs that compile but are often bugs
- `go mod tidy`: reconciles direct and indirect module requirements with the
  imports in the module

For dependency vulnerabilities, add `govulncheck` to the local or CI workflow.
It analyzes whether known vulnerabilities affect packages and call paths your
code uses; an update alone is not a complete security review.

## Strong CI Baseline

For this kind of service, a useful default order is:

1. format check or formatting step
2. `go vet ./...`
3. `go test ./...`
4. `go test -race ./...` where its runtime cost is acceptable
5. vulnerability check and dependency review
6. build the deployable binary or container

Keep slow integration tests separate from the fast package suite, but do not
replace HTTP-contract tests with only mocked unit tests.

## Further Reading

- [Official Go testing tutorial](https://go.dev/doc/tutorial/add-a-test): test file and naming conventions.
- [Go package `net/http/httptest`](https://pkg.go.dev/net/http/httptest): recorders and local test servers for HTTP behavior.
- [Official `govulncheck` tutorial](https://go.dev/doc/tutorial/govulncheck): dependency vulnerability analysis.
