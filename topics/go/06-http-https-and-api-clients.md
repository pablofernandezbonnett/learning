# Go HTTP and API Clients

Routing is only one part of an API. For each request, bound untrusted input, carry cancellation through I/O, and return a stable response when a dependency fails.

## Inbound HTTP

`http.Server` owns the server lifecycle even when Gin handles routing. Set a header-read timeout, choose other read/write/idle limits for the endpoint's behavior, and call `Shutdown` with a bounded context when stopping. A streaming endpoint needs a different write policy from a small JSON API.

At the handler boundary:

- Accept only supported methods and content types.
- Limit request body size before decoding JSON; then validate domain rules.
- Pass `r.Context()` (or `c.Request.Context()` in Gin) to request-scoped work.
- Map expected failures to documented statuses and safe error bodies.

```go
const maxBody = 1 << 20 // Example only: choose a limit for this endpoint.
r.Body = http.MaxBytesReader(w, r.Body, maxBody)
```

## Outbound HTTP

Reuse an `http.Client` with a finite timeout. Build each request with its caller's context, check the status, and close the response body after a successful `Do`, even when only the status matters.

```go
req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
if err != nil {
    return err
}
resp, err := client.Do(req)
if err != nil {
    return err
}
defer resp.Body.Close()
```

Give a dependency call its own shorter deadline when needed. Wrap failures with the dependency and operation, without logging secrets or returning raw upstream bodies.

## TLS and Retries

Decide where TLS terminates: at a controlled proxy or in the Go process. Protect any hop that crosses an untrusted network. Keep certificate verification enabled for outbound HTTPS. Trust forwarded client and scheme headers only from a controlled proxy.

A timeout after a write leaves the result uncertain. Before retrying, define how repeated requests with the same operation ID return the existing result and how conflicting payloads are rejected. Bound retries by attempt count and remaining deadline; backoff prevents retries from amplifying a struggling dependency.

Apply these rules in the [reservation exercise](./04-gin-service/README.md). For general API contract design, see [API design](../api/01-api-design.md).

## Further Reading

- [Go `net/http`](https://pkg.go.dev/net/http) for server, client, and request behavior.
- [Go `context`](https://pkg.go.dev/context) for cancellation and deadlines.
- [Gin server configuration](https://gin-gonic.com/en/docs/server-config/) when using Gin as an `http.Handler`.
