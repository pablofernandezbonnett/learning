# Go Foundations for Service Code

Go makes control flow visible: a function returns values and errors, a package groups related code, and a type gets behavior through methods. Learn these rules before choosing a framework.

## Values and Behavior

A `struct` holds fields. A method is a function with a receiver. Use a pointer receiver when the method must change the value or copying it is undesirable.

```go
type Reservation struct {
    ID       string
    Quantity int
}

func (r Reservation) Valid() bool {
    return r.ID != "" && r.Quantity > 0
}
```

Compose small types instead of building an inheritance hierarchy. Exported names start with an uppercase letter; lowercase names stay within the package.

## Errors and Absence

An `error` is a returned value. Add context when passing it upward and use `errors.Is` or `errors.As` to inspect its meaning. Do not compare error text.

```go
item, err := store.Find(ctx, id)
if err != nil {
    return Reservation{}, fmt.Errorf("find reservation %s: %w", id, err)
}
```

Every type has a zero value. A map lookup returns that value when a key is absent, so use the second result when absence matters:

```go
item, found := reservations[id]
if !found {
    return Reservation{}, ErrNotFound
}
```

A nil map can be read but must be initialized before writing. A nil slice can be ranged over and appended to.

## Interfaces and Cleanup

An interface describes behavior a caller needs; a matching type satisfies it without an explicit declaration. Start with concrete types. Add a small interface at a real dependency boundary, such as an inventory client that tests must replace.

`defer` runs when the surrounding function returns. Place cleanup after successful acquisition:

```go
resp, err := client.Do(req)
if err != nil {
    return err
}
defer resp.Body.Close()
```

Inside a long loop, use a helper function if each iteration must close its own resource promptly.

## Concurrent Work

A goroutine starts work concurrently; it does not make downstream capacity unlimited. Before starting one, decide who cancels it, who receives its result, and what limits the number running. [Service shape and context](./05-project-shape-errors-and-context.md) applies this rule to requests.

## Further Reading

- [A Tour of Go](https://go.dev/tour/) for hands-on syntax and concurrency.
- [Effective Go](https://go.dev/doc/effective_go) for language conventions.
