# Go Testing and Tooling

Test the contract a caller observes. For a service, that means response status and shape, duplicate behavior, invalid input, and dependency failures. Formatting and static checks support those tests but cannot prove the behavior.

## Test Through Real Boundaries

`net/http/httptest` can exercise a handler without opening a port. Use a request and recorder for inbound HTTP. Use a local test server for an outbound client so the test checks the actual method, path, headers, response handling, and timeout.

```go
req := httptest.NewRequest(http.MethodGet, "/reservations/res-1", nil)
rec := httptest.NewRecorder()
router.ServeHTTP(rec, req)
if rec.Code != http.StatusOK {
    t.Fatalf("status = %d, want 200", rec.Code)
}
```

The snippet assumes `router` is an `http.Handler`. Test one-off failure paths directly. Use a table when several inputs share one rule and setup. Replace a remote dependency with a small fake or local test server; avoid mocking the HTTP boundary under test.

## Routine Commands

Run from your own Go module:

```bash
go fmt ./...
go vet ./...
go test ./...
go test -race ./...
go mod tidy
```

`go test -race` reports data races reached by the test run; it does not prove every possible interleaving safe. `go mod tidy` updates dependency declarations, so review its diff. Add `govulncheck` when maintaining a real module with dependencies.

The [reservation exercise](./04-gin-service/README.md) lists the behaviors worth testing. For a broader test strategy, see [Testing](../testing/README.md).

## Further Reading

- [Go testing tutorial](https://go.dev/doc/tutorial/add-a-test).
- [`httptest` package](https://pkg.go.dev/net/http/httptest).
- [Go race detector](https://go.dev/doc/articles/race_detector).
- [`govulncheck` tutorial](https://go.dev/doc/tutorial/govulncheck).
