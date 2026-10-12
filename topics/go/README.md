# Go for Backend Services

Use this block to learn the Go decisions that matter when building a service. Start with the language model, then follow one request through its boundaries. The reservation exercise applies those ideas without supplying an implementation.

1. [Language foundations](./00-go-foundations.md): values, methods, interfaces, errors, and cleanup.
2. [Service shape and context](./05-project-shape-errors-and-context.md): packages, cancellation, error boundaries, and concurrent work.
3. [HTTP and API clients](./06-http-https-and-api-clients.md): inbound limits, outbound calls, TLS, and retries.
4. [Reservation service exercise](./04-gin-service/README.md): design and test a small API; Gin is an optional routing layer.
5. [Testing and tooling](./07-testing-tooling-and-quality.md): verify behavior with Go's standard tools.

The general API design method lives in [API design](../api/README.md). Use this block for Go-specific implementation choices.

## Further Reading

- [A Tour of Go](https://go.dev/tour/) for language basics.
- [Go documentation](https://go.dev/doc/) for the standard library and tooling.
- [Gin documentation](https://gin-gonic.com/en/docs/) if you choose Gin for the exercise.
