# Go for Java Backend Engineers

Use this as the shortest useful bridge from Java or Kotlin into Go. It covers
the language differences that change how a backend service is designed and
read, not every feature of the language.

---

## Why This Matters

Go can look familiar enough to invite Java habits: create a class hierarchy,
make an interface for every type, catch errors at a global boundary, then let a
framework hide the HTTP details.

That shape fights the language. Go services are usually clearer when the code
is flatter: data is a struct, behavior is a function or method, failures are
returned values, and dependencies are small explicit fields.

## Smallest Useful Mental Model

Go is compiled and statically typed, with a small core and a strong standard
library. It deliberately makes common backend control flow visible:

- functions can return a result and an `error`
- packages, rather than classes, are the main unit of organization
- interfaces are satisfied implicitly; a type does not declare that it
  implements one
- goroutines make concurrent work cheap to start, but `context` and limits
  still own its lifetime

> Write the direct version first. Add an interface, package, goroutine, or
> abstraction only when it gives a real boundary or makes a real risk clearer.

## Java-to-Go Mental Map

| Java or Kotlin idea | Go equivalent | Important difference |
|---|---|---|
| class with fields | `struct` | A struct is data; methods are declared separately. |
| constructor | `NewThing(...)` by convention | It is an ordinary function, not language magic. |
| package-private / `public` | lowercase / uppercase identifier | Capitalized names are exported from a package. |
| inheritance | composition and embedding | Prefer combining small types over class hierarchies. |
| interface declaration | small interface near its consumer | Implementation is implicit and usually needs no declaration. |
| `Optional<T>` / nullable reference | `(T, bool)`, pointer, or `error` | Choose the form that says why the value may be absent. |
| exception | returned `error` | Handle or propagate it deliberately at each useful boundary. |
| `try-with-resources` | `defer resource.Close()` | `defer` runs when the current function returns. |
| `List<T>` | `[]T` slice | A slice is a view over an array and can share backing storage. |
| `Map<K, V>` | `map[K]V` | A missing lookup returns the zero value; use the `ok` result if absence matters. |
| Maven/Gradle project | module with `go.mod` | The `go` command builds, tests, formats, and manages module dependencies. |

## The Differences That Matter in Service Code

### Structs, methods, and composition

There are no Java-style classes. Attach a method to a named type when behavior
belongs with that data; otherwise a plain function is often the clearest form.

```go
type Price struct {
	AmountJPY int
}

func (p Price) IsValid() bool {
	return p.AmountJPY > 0
}
```

Use a pointer receiver when the method must modify the value, or copying it is
undesirable. Do not start by translating every Spring service into a class.

### Errors are values

The normal flow is explicit. Add useful context while preserving the original
error with `%w`; inspect meaningful errors with `errors.Is` or `errors.As`.

```go
order, err := store.Find(ctx, orderID)
if err != nil {
	if errors.Is(err, ErrOrderNotFound) {
		return Order{}, err
	}
	return Order{}, fmt.Errorf("find order %s: %w", orderID, err)
}
```

Do not compare `err.Error()` strings. At the HTTP boundary, map known domain
errors to a safe status and response; log unexpected failures with context.

### Interfaces are consumer-owned

In Java, an interface often exists before any consumer needs it. In Go, define
the small behavior a caller needs next to that caller.

```go
type InventoryClient interface {
	Reserve(ctx context.Context, sku string, quantity int) error
}
```

Any matching type satisfies this interface. This makes a fake straightforward
in a test and avoids a producer-owned interface hierarchy.

### `nil`, zero values, and absence

Many Go types are useful in their zero value: an empty `string`, `0`, a nil
slice that can be ranged over, and a nil map that can be read from. A nil map
cannot be written to; initialize it with `make` or a literal.

Use a pointer when `nil` means something distinct. For a map lookup, use the
second result when an empty value and a missing value are different:

```go
reservation, found := reservations[id]
if !found {
	return Reservation{}, ErrReservationNotFound
}
```

### `defer` manages local cleanup

`defer` registers work for the end of the surrounding function. Put cleanup
next to successful acquisition so paths cannot forget it.

```go
resp, err := client.Do(req)
if err != nil {
	return err
}
defer resp.Body.Close()
```

Inside a long loop, avoid deferring many closes in the outer function: they all
wait until that function returns. Put one iteration in a helper instead.

## Bad Mental Model Vs Better Mental Model

Bad mental model:

> Go needs a Java architecture with shorter syntax.

Better mental model:

> Go favors explicit behavior and small composition. The package and function
> boundaries should earn their existence by making a dependency, failure, or
> ownership rule clearer.

Bad mental model:

> A goroutine is a free asynchronous thread.

Better mental model:

> A goroutine is work with an owner. It needs cancellation, a result path, and
> a bound imposed by the database, upstream API, or queue it reaches.

## Strong Default for a First Service

For a small API, start with:

- one module and a handful of packages
- `main` only for configuration and wiring
- `net/http` concepts first, then Gin as a thin routing layer
- concrete types by default; interfaces only at an actual consumer boundary
- `context.Context` passed into request-scoped service and I/O calls
- wrapped errors and one explicit HTTP error-mapping policy
- `go test ./...` and `go test -race ./...` before trusting concurrent code

The first goal is not to recreate Spring Boot. It is to build one API that is
easy to start, test, trace, and stop safely.

## Further Reading

- [A Tour of Go](https://go.dev/tour/): use its methods, interfaces, and concurrency sections as a hands-on companion.
- [Effective Go](https://go.dev/doc/effective_go): idioms and design conventions after the basics feel familiar.
- [Go modules reference](https://go.dev/ref/mod): return here when dependency or module behavior becomes unclear.
