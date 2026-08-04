# Go Refresh for JVM Backend Engineers

Use this folder as a secondary, code-first Go refresher. It explains the Go
service habits that differ most from Java or Kotlin without becoming a full Go
course.

## Smallest Useful Mental Model

Go removes framework and language ceremony, making HTTP, errors, and
concurrency more explicit. That can make code easier to inspect, but it also
means the engineer must be deliberate about `context`, timeouts, ownership, and
package shape.

Keep this baseline warm:

- explicit errors with useful wrapping
- `net/http` request and response flow
- request-scoped `context.Context`
- small packages rather than Java-style abstraction layers
- goroutines and channels bounded by real downstream limits

## Recommended Order

1. [00-go-for-java-developers.md](./00-go-for-java-developers.md): the Java-to-Go mental map that matters in backend work
2. [01-basics/main.go](./01-basics/main.go): syntax, structs, methods, and explicit errors
3. [03-http-json/main.go](./03-http-json/main.go): JSON handler and validation flow
4. [05-project-shape-errors-and-context.md](./05-project-shape-errors-and-context.md): project shape, errors, interfaces, and `context`
5. [06-http-https-and-api-clients.md](./06-http-https-and-api-clients.md): server lifecycle, HTTPS, and outbound API calls
6. [04-gin-service/README.md](./04-gin-service/README.md): one realistic service slice with Gin
7. [07-testing-tooling-and-quality.md](./07-testing-tooling-and-quality.md): tests, race detection, formatting, and dependency hygiene
8. [02-concurrency/main.go](./02-concurrency/main.go): goroutines, channels, `select`, and wait groups

Run the examples from this folder:

```bash
go run ./01-basics
go run ./03-http-json
go run ./02-concurrency
cd 04-gin-service && go run .
```

## Core Rule

> Go makes more behavior visible. Cheap goroutines do not remove database,
> provider, network, timeout, or ownership constraints.

Treat Go as a focused growth lane when current work or a target role needs it,
not as a replacement for the main Java backend roadmap.

## Further Reading

- [A Tour of Go](https://go.dev/tour/): interactive syntax and concurrency introduction; use it after the Java-to-Go map, not instead of it.
- [Official Go tutorials](https://go.dev/doc/tutorial/): modules, tests, databases, fuzzing, and an official Gin API tutorial.
- [Effective Go](https://go.dev/doc/effective_go): idioms worth returning to once basic syntax is familiar.
- [Gin Quickstart](https://gin-gonic.com/en/docs/quickstart/): create and run a minimal Gin API.
- [Gin middleware](https://gin-gonic.com/en/docs/middleware/) and [testing](https://gin-gonic.com/en/docs/testing/): the two Gin concepts most useful after the quickstart.
