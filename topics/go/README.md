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

1. [01-basics/main.go](./01-basics/main.go): syntax, structs, methods, and explicit errors
2. [03-http-json/main.go](./03-http-json/main.go): JSON handler and validation flow
3. [05-project-shape-errors-and-context.md](./05-project-shape-errors-and-context.md): project shape, errors, interfaces, and `context`
4. [04-gin-service/README.md](./04-gin-service/README.md): one realistic service slice with Gin
5. [02-concurrency/main.go](./02-concurrency/main.go): goroutines, channels, `select`, and wait groups

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
