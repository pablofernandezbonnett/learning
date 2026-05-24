# Async, Null Safety, and Collections in Modern Dart

Use this after `01-dart3-features.md` when your Dart feels rusty in the places
that actually affect day-to-day coding speed.

This note is about the fast refresh that makes real Dart and Flutter code feel
normal again.

---

## 1. Why This Matters

If you already understand Flutter basics, the next friction is usually not
widgets.

It is this layer:

- async control flow
- null safety
- collection literals and small data transformations

That is where modern Dart code feels cleaner than old Dart, but only if you
reopen the language habits properly.

---

## 2. Smallest Useful Mental Model

Modern Dart feels good when you treat these as first-class tools:

- `Future<T>` for one async result
- `Stream<T>` for a sequence of async values
- non-null by default types
- null-aware operators for boundary-safe code
- collection literals with `if`, `for`, and spread

Plain-English version:

> Modern Dart wants you to model absence, async work, and small collection
> shaping directly in the language instead of through helper clutter.

---

## 3. Async: The Real Refresh

### `Future<T>`

Use `Future<T>` when one result arrives later.

```dart
Future<int> fetchStock(String sku) async {
  await Future<void>.delayed(const Duration(milliseconds: 100));
  return 42;
}
```

This is the most common async shape in Dart and Flutter app code.

### `async` and `await`

Strong default:

- use `async`/`await` for readability
- keep the path from request to result obvious
- catch errors near the boundary that can translate them meaningfully

```dart
Future<String> loadLabel(String sku) async {
  try {
    final stock = await fetchStock(sku);
    return '$sku has $stock units';
  } catch (error) {
    throw StateError('Failed to load stock for $sku: $error');
  }
}
```

### `Future.wait`

Use it when several independent async calls can run in parallel.

```dart
final results = await Future.wait([
  fetchStock('UT-WHITE-M'),
  fetchStock('FLEECE-GREY-M'),
]);
```

Bad mental model:

- "async means I should make everything concurrent"

Better mental model:

- "run work concurrently only when the tasks are independent and the result is
  still easy to reason about"

### `Stream<T>`

Use `Stream<T>` when values arrive over time.

Good fits:

- user input
- websocket updates
- repeated sensor or location updates
- event-driven app state

Do not use a stream just because a value is async once.

### Isolates

The minimum useful rule:

- `Future` is for normal async I/O
- isolates are for CPU-heavy work that should not block the main isolate

Examples where isolates may help:

- large JSON parsing
- image processing
- compression
- expensive local transformations

Examples where isolates usually do not help:

- simple HTTP calls
- repository fetches
- small validation logic

---

## 4. Null Safety: What To Reopen Fast

Dart is non-null by default.

That means:

```dart
String name = 'Uniqlo';
String? nickname;
```

### The operators that matter most

- `?` nullable type
- `!` assert non-null now
- `?.` call only if non-null
- `??` fallback value
- `??=` assign fallback once

```dart
final label = product.nickname ?? product.name;
final city = profile?.address?.city;
```

### Strong default

- prefer modeling nullable data honestly
- use `!` rarely and close to a proven invariant
- validate external JSON at the boundary instead of spreading nullable dynamic maps across the app

Bad mental model:

- "I will just use `!` where the compiler complains"

Better mental model:

- "The compiler is telling me where the data shape or lifecycle is still unclear"

### `late`

`late` is useful, but it is not a free escape hatch.

Good fit:

- value initialized once after construction lifecycle is clear

Weak fit:

- avoiding proper constructor or state modeling

---

## 5. Collections: The Dart Style Worth Remembering

Modern Dart collection literals are more expressive than many people remember.

### Spread

```dart
final baseHeaders = {'Accept': 'application/json'};
final authHeaders = {
  ...baseHeaders,
  'Authorization': 'Bearer $token',
};
```

### Collection `if`

```dart
final actions = [
  'view',
  if (canEdit) 'edit',
  if (canDelete) 'delete',
];
```

### Collection `for`

```dart
final labels = [
  for (final sku in skus) 'SKU: $sku',
];
```

Why this matters:

- less helper noise
- easier UI list shaping
- easier DTO-to-viewmodel shaping

---

## 6. Small Practical Example

This shape is common in real Dart code:

```dart
class ProductDto {
  final String sku;
  final int stock;

  ProductDto({required this.sku, required this.stock});

  factory ProductDto.fromJson(Map<String, Object?> json) {
    return ProductDto(
      sku: json['sku'] as String,
      stock: json['stock'] as int,
    );
  }
}

Future<List<String>> loadStockLabels(List<String> skus) async {
  final products = await Future.wait(
    skus.map((sku) async {
      final stock = await fetchStock(sku);
      return ProductDto(sku: sku, stock: stock);
    }),
  );

  return [
    for (final product in products)
      if (product.stock > 0) '${product.sku}: ${product.stock}',
  ];
}
```

What this reopens:

- async orchestration with `Future.wait`
- typed DTO boundary
- collection `for` and `if`
- no null or dynamic leakage into the rest of the logic

---

## 7. Strong Default

For modern Dart, a good everyday baseline is:

- `async`/`await` first
- `Future.wait` for independent parallel work
- `Stream` only for repeated async values
- model nullable values honestly
- avoid casual `!`
- use collection literals to shape small data cleanly

---

## 8. Takeaway

The most useful Dart refresh after Dart 3 features is this:

> Async flow, null safety, and collection shaping are now part of the normal
> language style, not advanced extras.

## Further Reading

- [Asynchronous programming: futures, async, await](https://dart.dev/libraries/async/async-await)
- [Null safety](https://dart.dev/null-safety)
- [Collections](https://dart.dev/language/collections)
- [Concurrency in Dart](https://dart.dev/language/concurrency)
