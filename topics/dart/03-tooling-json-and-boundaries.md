# Dart Tooling and Data Boundaries

The everyday loop is short: install dependencies, format, analyze, and test. At runtime, convert untrusted JSON into typed values near the edge so the rest of the code does not carry string keys and `dynamic` assumptions.

## Feedback loop

For a plain Dart package:

```bash
dart pub get
dart format .
dart analyze
dart test
```

Inspect proposed automatic fixes before applying them with `dart fix --dry-run`. Formatting and analyzer checks reduce review noise; they do not establish business correctness. Tests should cover valid and malformed boundary data.

## Decode, validate, then model

`jsonDecode` produces Dart collections whose shape still needs checking. Keep that check in one constructor or adapter:

```dart
class Reservation {
  final String id;
  final int quantity;

  Reservation(this.id, this.quantity);

  factory Reservation.fromJson(Map<String, Object?> json) {
    if (json case {'id': String id, 'quantity': int quantity}
        when quantity > 0) {
      return Reservation(id, quantity);
    }
    throw const FormatException('Invalid reservation');
  }
}
```

This makes missing fields, wrong types, and invalid quantities fail at the boundary. Decide whether the caller receives a rejected response, a logged bad event, or another explicit outcome. Do not pass raw maps into business logic.

## Extension methods and extension types

An extension method adds convenient behavior to an existing type. An extension type gives a representation a distinct static interface:

```dart
extension YenLabel on int {
  String toYenLabel() => '¥$this';
}

extension type Sku(String value) {}
```

An extension type alone does not validate `value`; enforce the SKU rule when constructing it. Its runtime representation is the wrapped value, so do not treat it as a normal wrapper object or a runtime security boundary. Use a regular class when runtime identity or additional stored state matters.

Use these features where they clarify an API. A typed DTO and a well-placed validation check usually matter more than adding wrappers to every primitive.

## Review before shipping

- Did external data become typed and validated at one visible boundary?
- Are bad payloads covered by tests?
- Does the analyzer pass without hiding errors behind `dynamic`?
- Does each extension add meaning rather than another spelling for the same value?

Sources: [Dart CLI](https://dart.dev/tools/dart-tool), [JSON](https://dart.dev/libraries/serialization/json), [extension methods](https://dart.dev/language/extension-methods), [extension types](https://dart.dev/language/extension-types).
