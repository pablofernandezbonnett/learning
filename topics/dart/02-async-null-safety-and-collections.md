# Async, Null Safety, and Collections in Dart

Use the type to show what can happen: `Future<T>` is one later result, `Stream<T>` is a sequence over time, and `T?` is a value that may be absent. Decide what errors and missing values mean at the boundary where you can handle them.

## One result or many

```dart
Future<int> availableStock(String sku) async {
  final response = await stockClient.get(sku);
  return response.quantity;
}
```

`async`/`await` makes the sequence readable; it does not mean every step should run concurrently. Use `Future.wait` when calls are independent and both results are needed:

```dart
Future<List<int>> loadBoth() => Future.wait([
  availableStock('SKU-1'),
  availableStock('SKU-2'),
]);
```

Define the error policy for the group. If one call fails, decide whether the entire operation fails, whether partial results are acceptable, and who stops work no longer needed. For a sequence of updates, use a `Stream<T>` and decide who owns the subscription and when it ends.

Use an isolate for substantial CPU work that would block the current isolate. It is not a default replacement for ordinary async I/O.

## Represent absence honestly

```dart
String label(String? nickname, String name) => nickname ?? name;
```

`?` makes absence part of the type. `?.` accesses a member only when present, and `??` supplies a fallback. Use `!` only when a nearby invariant proves the value exists; otherwise it turns an unclear state into a runtime failure. `late` delays initialization but does not remove the need to prove it happens before use.

External JSON should be checked and converted at its [data boundary](./03-tooling-json-and-boundaries.md), rather than sending nullable or dynamic maps through the rest of the program.

## Shape small collections directly

```dart
final visibleLabels = [
  for (final item in items)
    if (item.isAvailable) item.label,
];
```

Collection `if`, `for`, and spread (`...`) are useful for a small local transformation. If the rule has many branches or side effects, name the transformation instead of compressing it into one literal.

## Decision check

- Is the value one later result, a sequence, or optional data?
- Are concurrent calls independent, and what happens when one fails?
- Who owns cancellation or a stream subscription?
- Does null mean normal absence or invalid input?
- Is the collection expression still easy to scan?

Sources: [asynchronous programming](https://dart.dev/libraries/async/async-await), [concurrency](https://dart.dev/language/concurrency), [null safety](https://dart.dev/null-safety), [collections](https://dart.dev/language/collections).
