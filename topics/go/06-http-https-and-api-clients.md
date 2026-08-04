# Go HTTP, HTTPS, and API Clients

This note is about the HTTP behavior that makes a Go API safe to run and easy
to reason about. Routing syntax is secondary: whether you use `net/http` or
Gin, request lifetime, timeouts, response handling, and TLS rules remain yours.

---

## Why This Matters

Most production failures at an API boundary are not caused by a missing route.
They come from unbounded waits, leaked response bodies, retrying unsafe writes,
returning internal errors to callers, or assuming HTTPS is somebody else's
problem.

Java and Spring often provide defaults around the edges. Go exposes the pieces
more directly, which is useful only when you set the few critical rules on
purpose.

## Smallest Useful Mental Model

An inbound HTTP request has a context. Pass it down to database and outbound
HTTP calls so cancellation and deadlines propagate. An outbound client must be
reused, bounded by timeouts, and have its response body closed.

HTTPS is HTTP protected by TLS: it encrypts the connection and authenticates
the server to the client when certificate validation succeeds. It does not
replace authorization, validation, rate limits, or safe error responses.

> Every request needs a time budget, every external response needs cleanup, and
> HTTPS protects the transport—not the application behavior.

## Inbound APIs: Server Lifecycle and Request Boundaries

`http.Server` owns the server lifecycle. Set at least a header-read timeout;
then use `Shutdown` with a bounded context so the process stops accepting new
traffic and lets in-flight work finish within a known time.

```go
server := &http.Server{
	Addr:              ":8080",
	Handler:           router,
	ReadHeaderTimeout: 5 * time.Second,
}

// On SIGTERM, derive a short shutdown context and call server.Shutdown(ctx).
```

`ReadHeaderTimeout` limits only the time spent reading request headers. Decide
the remaining read, write, and idle timeout policy from the endpoint behavior;
a streaming upload, download, or response may need a different policy from a
small JSON API.

At the handler boundary:

- allow only the methods and content types the endpoint supports
- put a size limit on untrusted request bodies before decoding JSON
- validate semantic rules after decoding: valid JSON is not necessarily a valid command
- return stable, safe error bodies; do not return stack traces or upstream bodies
- pass `r.Context()` (or `c.Request.Context()` in Gin) into the work it starts

For a JSON endpoint, a size limit prevents a client from making the decoder
read an arbitrarily large body. The handler should map the resulting read error
to a safe client response.

```go
const maxJSONBodyBytes = 1 << 20 // 1 MiB; choose a limit for this endpoint.
r.Body = http.MaxBytesReader(w, r.Body, maxJSONBodyBytes)
```

`net/http` gives these mechanisms directly. Gin provides convenient routing,
JSON binding, and middleware, but it should not hide the lifecycle rules.

## Outbound HTTP: One Reused Client and One Request Budget

Create and share an `http.Client`; do not create a fresh one for each request.
A reusable client also reuses its transport, which can reuse connections.

Use the incoming context to give the particular dependency call a shorter
budget. This distinguishes a request deadline from the broader protection
provided by a client timeout.

```go
type InventoryHTTPClient struct {
	baseURL string
	client  *http.Client
}

func (c InventoryHTTPClient) Reserve(ctx context.Context, sku string, quantity int) error {
	body, err := json.Marshal(map[string]any{"sku": sku, "quantity": quantity})
	if err != nil {
		return fmt.Errorf("encode inventory request: %w", err)
	}

	req, err := http.NewRequestWithContext(ctx, http.MethodPost,
		c.baseURL+"/inventory/reservations", bytes.NewReader(body))
	if err != nil {
		return fmt.Errorf("build inventory request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := c.client.Do(req)
	if err != nil {
		return fmt.Errorf("call inventory: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusCreated {
		return fmt.Errorf("inventory returned unexpected status %d", resp.StatusCode)
	}
	return nil
}
```

Strong default:

- configure one `http.Client` per dependency or policy, with a finite timeout
- derive per-call deadlines from the incoming context
- close `resp.Body` on every successful `Do` call, even if you only need the status
- check status codes before decoding a success model
- wrap errors with the operation and dependency name, not secrets or full untrusted bodies

## HTTPS and TLS Boundaries

Two deployment shapes are common:

1. A reverse proxy or load balancer terminates TLS, then forwards HTTP to the
   Go process on a private trusted network.
2. The Go process terminates TLS itself using an `http.Server` configured with
   a certificate and key.

Either can be appropriate. The important question is where certificates are
managed, where unencrypted traffic is allowed, and whether the hop between the
proxy and process is a trusted private boundary.

For outbound HTTPS, use normal certificate validation. Avoid a custom TLS
configuration that disables verification just to make a local or broken
certificate work; fix local development with a trusted development certificate
or an explicitly isolated development setup instead.

If a proxy passes client address or scheme headers, trust them only when the
proxy is a controlled network boundary. A public caller can otherwise forge
such headers.

## Retries, Idempotency, and Failure Meaning

Bad mental model:

> A timeout means the upstream definitely did nothing, so retry every request.

Better mental model:

> A timeout can happen after the upstream accepted the operation but before we
> received its answer. Retrying a write needs an idempotency key or another
> server-side deduplication rule.

For a read, a bounded retry may be useful when the API contract and deadline
allow it. For a write such as `POST /payments` or an inventory reservation,
make the duplicate behavior explicit first. The Gin reservation lab demonstrates
this with a reservation ID.

Do not add retries without a cap, backoff, and deadline. They otherwise turn a
slow dependency into more load exactly when it is already failing.

## Strong First API Baseline

- `http.Server` with a bounded shutdown path and `ReadHeaderTimeout`
- request context passed to database and outbound calls
- an explicit JSON size limit and validation policy
- stable HTTP error mapping for known domain failures
- one reusable HTTP client with finite timeouts
- HTTPS terminated at a deliberate, controlled boundary
- idempotency before retries for externally visible writes

The next step is the [Gin service slice](./04-gin-service/README.md), where
these ideas are exercised in a small API.

## Further Reading

- [Go package `net/http`](https://pkg.go.dev/net/http): server, client, request, response, and timeout APIs.
- [Go package `context`](https://pkg.go.dev/context): request cancellation and deadlines.
- [Gin middleware](https://gin-gonic.com/en/docs/middleware/): middleware execution and scope.
