# Tooling, JSON, and Boundaries in Modern Dart

Use this after the language refresh notes when you want the minimum practical
loop for writing trustworthy Dart again.

This is the part that helps you reopen the "real work" flow:

- package tooling
- analyzer and tests
- JSON and DTO boundaries
- extension methods and extension types

---

## 1. Why This Matters

You can remember the syntax and still feel slow if you do not remember:

- the current tool loop
- how modern Dart code models data at boundaries
- which extension feature solves which problem

This note is the minimum reset for writing code that feels current instead of
half-remembered.

---

## 2. Smallest Useful Mental Model

For normal Dart work:

- `dart pub` manages packages
- `dart analyze` catches many quality issues early
- `dart test` is the normal fast feedback loop
- JSON should become typed objects quickly
- extension methods add behavior to an existing type
- extension types create a new zero-cost wrapper type when a boundary needs stronger meaning

Plain-English version:

> Get out of `dynamic` quickly, let the analyzer help you early, and use
> extension features for clarity instead of cleverness.

---

## 3. The Tool Loop Worth Reopening

For a plain Dart package or module, these commands matter most:

```bash
dart pub get
dart format .
dart analyze
dart test
```

If you are starting a new package:

```bash
dart create -t package-simple inventory_client
cd inventory_client
dart pub get
dart analyze
dart test
```

Why this matters:

- formatting removes review noise before the analyzer runs
- the analyzer catches type and style issues fast
- tests stay cheap to run
- the package shape stays predictable

Small package shape:

```text
inventory_client/
  pubspec.yaml
  lib/
    inventory_client.dart
    src/
      models.dart
      service.dart
  test/
    inventory_client_test.dart
```

Strong default:

- export the public API from `lib/...`
- keep implementation details in `lib/src/...`
- keep tests close to behavior, not only happy-path demos

### Give The Analyzer A Useful Baseline

The analyzer is more valuable when it is allowed to reject a few weak
shortcuts. Add the `lints` package as a development dependency, then start with
this small `analysis_options.yaml`:

```yaml
include: package:lints/recommended.yaml

analyzer:
  language:
    strict-casts: true
    strict-raw-types: true
```

`strict-casts` makes implicit downcasts visible. `strict-raw-types` catches
generic types written without their type arguments. Both help a Java developer
avoid accidentally reintroducing `dynamic` at a boundary.

Before applying automatic fixes, inspect what they propose:

```bash
dart fix --dry-run
dart fix --apply
```

Use `dart fix --apply` for mechanical cleanup, then review and test the result
like any other change. It does not replace design judgment.

---

## 4. JSON And DTO Boundaries

One of the fastest ways to make Dart code messy again is to let `Map<String, dynamic>`
travel too far.

Bad mental model:

- "The payload is already parsed, so I can pass maps around for a while."

Better mental model:

- "Raw maps belong at the edge. Domain or app logic should receive typed data."

Good baseline:

```dart
class ReservationDto {
  final String reservationId;
  final String sku;
  final int quantity;

  ReservationDto({
    required this.reservationId,
    required this.sku,
    required this.quantity,
  });

  factory ReservationDto.fromJson(Map<String, Object?> json) {
    return ReservationDto(
      reservationId: json['reservation_id'] as String,
      sku: json['sku'] as String,
      quantity: json['quantity'] as int,
    );
  }

  Map<String, Object?> toJson() => {
    'reservation_id': reservationId,
    'sku': sku,
    'quantity': quantity,
  };
}
```

Why this is better:

- type expectations are explicit
- bad payloads fail near the boundary
- the rest of the code no longer depends on magic string keys

---

## 5. Extension Methods Vs Extension Types

These are related, but they do different jobs.

### Extension methods

Use them when you want to add helper behavior to an existing type.

```dart
extension PriceFormatting on int {
  String toYenLabel() => '¥$this';
}
```

Good fit:

- formatting
- small convenience helpers
- readable local behavior

### Extension types

Extension types were added in Dart 3.3.
They let you create a stronger wrapper type around an existing representation
without the cost of a normal wrapper object.

```dart
extension type Sku(String value) {
  bool get isWinterItem => value.startsWith('HEATTECH');
}
```

Good fit:

- boundary values where a primitive is too weak
- IDs, SKUs, currency codes, or JS interop surfaces

Practical rule:

- use extension methods often
- use extension types when the domain meaning of the wrapped value matters

Do not force extension types everywhere just because they are new.

---

## 6. Small Practical Example

```dart
extension type ReservationId(String value) {}

class ReservationDto {
  final ReservationId id;
  final String sku;

  ReservationDto({required this.id, required this.sku});

  factory ReservationDto.fromJson(Map<String, Object?> json) {
    return ReservationDto(
      id: ReservationId(json['reservation_id'] as String),
      sku: json['sku'] as String,
    );
  }
}

extension ReservationIdFormatting on ReservationId {
  String shortLabel() => value.substring(0, 8);
}
```

What this buys you:

- `reservation_id` is no longer "just any string"
- formatting stays readable
- the type system carries a bit more domain meaning

This is useful, but still secondary to the bigger wins:

- typed DTO boundaries
- analyzer feedback
- tests

---

## 7. Testing Default

For logic that matters, write tests around:

- DTO parsing
- result and state mapping
- repository or service behavior
- bad payload or missing field cases

Small example:

```dart
test('ReservationDto parses valid JSON', () {
  final dto = ReservationDto.fromJson({
    'reservation_id': 'res-1',
    'sku': 'UT-WHITE-M',
  });

  expect(dto.id.value, 'res-1');
  expect(dto.sku, 'UT-WHITE-M');
});
```

Why this matters:

- data boundaries break often
- tests around parsing and mapping are cheap and high-value

---

## 8. Strong Default

If you want the shortest modern Dart tool-and-boundary reset, use this:

- `dart pub get`
- `dart format .`
- `dart analyze`
- `dart test`
- convert JSON maps into typed objects quickly
- use extension methods for helpers
- use extension types selectively where a primitive needs stronger meaning

---

## 9. Takeaway

The most useful Dart productivity loop is this:

> keep raw maps at the edge, let the analyzer complain early, and use the type
> system to make boundary values harder to misuse.

## Further Reading

- [Packages](https://dart.dev/tools/pub/packages)
- [dart analyze](https://dart.dev/tools/dart-analyze)
- [dart format](https://dart.dev/tools/dart-format)
- [dart fix](https://dart.dev/tools/dart-fix)
- [dart test](https://dart.dev/tools/dart-test)
- [Serialization](https://dart.dev/libraries/serialization/json)
- [Extension types](https://dart.dev/language/extension-types)
