# Dart 3: Records, Patterns, and Closed States

Use these features when they make the possible values or outcomes clearer. They work together: a record can return a few values, a pattern can unpack them, and a sealed class can make a `switch` cover every known outcome.

## Records: a few values without a new class

A record is an immutable group of typed fields. It fits a local result whose fields do not need a long-lived domain identity:

```dart
({String sku, int quantity}) summarizeLine() =>
    (sku: 'SKU-1', quantity: 2);

void main() {
  final (:sku, :quantity) = summarizeLine();
  print('$sku: $quantity');
}
```

Named fields help the caller understand the values. Use a class for a public DTO or domain concept that needs validation, methods, or a stable contract. A record is not automatically the right replacement for such a type.

## Patterns: inspect and unpack one shape

Patterns appear in declarations, `if-case`, and `switch`. They are useful when the structure already tells you what to do:

```dart
String stockLabel(Object? value) => switch (value) {
  int n when n > 0 => '$n available',
  int() => 'Out of stock',
  _ => 'Unknown stock',
};
```

The guard after `when` narrows one case. Keep a simple `if` when matching a structure would make the rule harder to read.

## Sealed classes: every outcome has a case

A sealed class defines a type family whose direct subtypes stay in the same library. The analyzer can check whether a switch handles the family:

```dart
sealed class PaymentResult {}

final class Paid extends PaymentResult {
  final String transactionId;
  Paid(this.transactionId);
}

final class Rejected extends PaymentResult {
  final String reason;
  Rejected(this.reason);
}

String message(PaymentResult result) => switch (result) {
  Paid(transactionId: final id) => 'Paid: $id',
  Rejected(reason: final reason) => 'Rejected: $reason',
};
```

If you add another result variant, review each exhaustive switch. An enum is enough when variants do not need different data; a sealed family helps when each outcome carries its own fields.

## Class modifiers: choose a real boundary

| Modifier | Main purpose outside its library |
| --- | --- |
| `final class` | Prevent extending or implementing the class |
| `interface class` | Allow implementation of a contract, but not inheritance of its implementation |
| `base class` | Allow inheritance while requiring subclasses to use the base implementation |
| `sealed class` | Keep the subtype family closed for exhaustive handling |

Start with an ordinary class. Add a modifier when the public inheritance or implementation rule matters to callers. See the [official class modifier guide](https://dart.dev/language/class-modifiers) for the full combinations.

## Decision check

- Is this a temporary group of values or a durable domain type?
- Does the code need to handle every outcome explicitly?
- Is a pattern clearer than a simple condition?
- Is the class modifier protecting an actual library boundary?

Sources: [records](https://dart.dev/language/records), [patterns](https://dart.dev/language/patterns), [sealed classes](https://dart.dev/language/class-modifiers#sealed), [class modifiers](https://dart.dev/language/class-modifiers).
