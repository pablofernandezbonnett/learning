# Go Refresh for JVM Backend Engineers

Use this folder when you want a small, code-first Go refresher from a JVM
backend point of view.

This is not a full Go roadmap.
It is a secondary reference surface that helps you understand the runtime and
service-shape ideas that come up most often when comparing Go with Java or
Kotlin in backend contexts.

Focus:

- explicit error handling instead of exceptions
- small standard-library HTTP services
- lightweight concurrency with goroutines and channels
- where a small framework such as `Gin` helps and where plain `net/http` is enough
- the minimum project-shape and `context` habits that keep Go code idiomatic

## Stable Practical Baseline

If your goal is to become productive in Go backend work quickly, keep this
baseline warm first:

- explicit error handling with wrapped semantic errors
- `net/http` request and response flow without hidden framework behavior
- `context.Context` passed through real request-scoped work
- small package and module shape that avoids Java-style over-abstraction
- goroutines and channels used with request timeouts, ownership, and downstream limits in mind

That baseline already covers most of the day-to-day engineering judgment that
matters when you first start writing Go services.

## Recommended Order

### 1. [01-basics/main.go](./01-basics/main.go)

Start here.

Outcome:

- understand structs, methods, and explicit error handling
- get comfortable with Go's minimal syntax and multiple return values
- compare Go's style to Java/Kotlin without forcing OOP onto it

### 2. [03-http-json/main.go](./03-http-json/main.go)

Outcome:

- understand how small Go handlers map request JSON, validate input, and return
  status codes
- compare Go's `net/http` style with Spring MVC without pretending there is a
  Spring-like framework built in
- keep backend code explicit around decoding, validation, and response writing

### 3. [04-gin-service/README.md](./04-gin-service/README.md)

Outcome:

- see one real service slice with `Gin`, a lightweight web framework, without
  turning Go into framework soup
- practice route groups, middleware, config, graceful shutdown, logging, tests,
  and timeout-aware boundary calls
- understand where a framework helps and where the stdlib still does the real
  server work

### 4. [02-concurrency/main.go](./02-concurrency/main.go)

Read this next.

Outcome:

- understand goroutines, channels, `select`, and wait groups
- compare Go concurrency with threads and coroutines more clearly
- know when message passing fits better than shared-state locking

### 5. [05-project-shape-errors-and-context.md](./05-project-shape-errors-and-context.md)

Read this once the runnable examples feel familiar.

Outcome:

- understand the minimum Go project shape that works well in backend code
- know where interfaces belong and how to avoid Java-shaped abstraction
- use `errors.Is`, wrapping, and `context.Context` in a way that scales past toy examples

## If You Want To Get Productive Fast

1. run [01-basics/main.go](./01-basics/main.go) only to warm up on syntax and explicit errors
2. run [03-http-json/main.go](./03-http-json/main.go) to understand the normal handler and JSON flow
3. read [05-project-shape-errors-and-context.md](./05-project-shape-errors-and-context.md) before you design your own small service
4. run [04-gin-service/README.md](./04-gin-service/README.md) to see one realistic service slice
5. run [02-concurrency/main.go](./02-concurrency/main.go) after that, so goroutines and channels are anchored in service behavior instead of treated as a toy feature

This order keeps the Go path focused on backend judgment first and language
novelty second.

## How To Run

From [go](.):

```bash
go run ./01-basics
go run ./03-http-json
cd 04-gin-service && go run .
go run ./02-concurrency
```

## Smallest Mental Model

Go removes a lot of framework and language ceremony.
That makes the control flow easier to see, but it also means more behavior is
spelled out directly in the code.

The main trade is simple:

- less magic and smaller runtime surface
- more explicit handling for errors, HTTP, and concurrency boundaries

Plain-English version:

> Go backend code is usually easier to read because less is hidden for you, but
> that also means you must be deliberate about errors, `context`, timeouts, and
> package shape.

## What To Internalize

- Go optimizes for simplicity and concurrency, not OOP expressiveness
- explicit errors change control flow and API design
- HTTP, JSON, and validation are usually more explicit than in Spring
- one framework is enough for this repo; `Gin` is the practical one we cover
- goroutines are lightweight, but concurrency design still needs discipline
- package shape, `context`, and error flow matter more than design-pattern vocabulary
- cheap goroutines do not remove database, provider, or network bottlenecks

## What To Practice

If you want this folder to stay practical:

1. run `01-basics` and rewrite one small Java/Kotlin DTO/service idea in Go
2. run `03-http-json` and compare the handler flow to a small Spring MVC
   controller
3. run `04-gin-service` and inspect the service shape, tests, timeout handling,
   and graceful shutdown path
4. run `02-concurrency` and compare the flow where one request branches into
   several parallel calls to executors or coroutines
5. read [05-project-shape-errors-and-context.md](./05-project-shape-errors-and-context.md) and rewrite one small service boundary without over-abstracting it

The goal is not to learn Go academically.
The goal is to feel how Go changes backend design choices.

## Where This Helps

This refresh is most useful when you want to understand:

- why Go is common in platform and infrastructure-heavy teams
- how explicit errors shape APIs differently from exceptions
- how a small JSON API feels without a heavy framework layer
- how much framework you actually need before `Gin` becomes useful
- when goroutines and channels feel cleaner than thread-pool thinking

In the current repo, `Go` is best treated as a secondary growth lane unless you
are deliberately moving toward platform, infrastructure, or Go-heavy backend
teams.
