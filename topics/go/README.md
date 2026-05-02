# Go Refresh for JVM Backend Engineers

This folder is a small, code-first Go refresher.

It is a secondary reference path, not part of the main backend refresh path.

It is not a full Go roadmap.

It is meant to help you understand the runtime and language ideas that most
often come up when comparing Go with Java or Kotlin in backend contexts.

The goal is not to turn this repo into a full Go curriculum.
It is to make the comparison practical enough that Go runtime and service-shape decisions stop feeling vague.

---

## Recommended Order

### 1. [01-basics/main.go](./01-basics/main.go)

Start here.

Outcome:

- understand structs, methods, and explicit error handling
- get comfortable with Go's minimal syntax and multiple return values
- compare Go's style to Java/Kotlin without forcing OOP onto it

### 2. [03-http-json/main.go](./03-http-json/main.go)

Read this next.

Outcome:

- understand how small Go handlers map request JSON, validate input, and return
  status codes
- compare Go's `net/http` style with Spring MVC without pretending there is a
  Spring-like framework built in
- keep backend code explicit around decoding, validation, and response writing

### 3. [04-gin-service/README.md](./04-gin-service/README.md)

Read this after the stdlib HTTP example.

Outcome:

- see one real service slice with `Gin` without turning Go into framework soup
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

---

## How To Run

From [go](.):

```bash
go run ./01-basics
go run ./03-http-json
cd 04-gin-service && go run .
go run ./02-concurrency
```

---

## What To Internalize

- Go optimizes for simplicity and concurrency, not OOP expressiveness
- explicit errors change control flow and API design
- HTTP, JSON, and validation are usually more explicit than in Spring
- one framework is enough for this repo; `Gin` is the practical one we cover
- goroutines are lightweight, but concurrency design still needs discipline

---

## What To Practice

If you want this folder to stay practical:

1. run `01-basics` and rewrite one small Java/Kotlin DTO/service idea in Go
2. run `03-http-json` and compare the handler flow to a small Spring MVC
   controller
3. run `04-gin-service` and inspect the service shape, tests, timeout handling,
   and graceful shutdown path
4. run `02-concurrency` and compare the flow where one request branches into
   several parallel calls to executors or coroutines

The goal is not to learn Go academically.
The goal is to feel how Go changes backend design choices.

---

## Where This Helps

This refresh is most useful when you want to understand:

- why Go is common in platform and infrastructure-heavy teams
- how explicit errors shape APIs differently from exceptions
- how a small JSON API feels without a heavy framework layer
- how much framework you actually need before `Gin` becomes useful
- when goroutines and channels feel cleaner than thread-pool thinking
