# 01 — Dart 3 Features

Dart 3 (released May 2023) is the largest language update since null safety.
Three features — records, patterns, and sealed classes — work together and change
how you model and process data in Flutter apps.

---

## 1. Records

Records are anonymous, immutable, lightweight value types. They are typed tuples —
think of them as `data class` without needing to define a class.

### Positional records

```dart
(String, int) fetchCoordinates() => ('Tokyo', 35);

void main() {
  final (city, latitude) = fetchCoordinates();   // destructuring
  print('$city: $latitude');
}
```

### Named records

```dart
({String name, double price}) fetchProduct() =>
    (name: 'Fleece Jacket', price: 8990.0);

final product = fetchProduct();
print(product.name);    // named field access
print(product.price);
```

### Mixed (positional + named)

```dart
(int, {String label, bool active}) = (42, label: 'primary', active: true);
```

### Records as return values (replaces output parameters)

Before Dart 3, returning multiple values required creating a class or using a `List`:
```dart
// Old — awkward
Map<String, dynamic> validateForm(String email, String password) => {
  'isValid': email.contains('@') && password.length >= 8,
  'error': 'Invalid email',
};

// Dart 3 — clean and typed
(bool isValid, String? error) validateForm(String email, String password) {
  if (!email.contains('@')) return (false, 'Invalid email');
  if (password.length < 8) return (false, 'Password too short');
  return (true, null);
}

final (isValid, error) = validateForm(email, password);
```

### Records as Map entries

```dart
final inventory = {
  'SKU-001': (stock: 42, warehouse: 'Tokyo'),
  'SKU-002': (stock: 0, warehouse: 'Osaka'),
};

for (final MapEntry(key: sku, value: (:stock, :warehouse)) in inventory.entries) {
  print('$sku: $stock units at $warehouse');
}
```

---

## 2. Pattern Matching

Patterns let you destructure and match data in `switch` expressions, `if-case`
statements, and variable declarations.

### switch expression (not statement)

The old `switch` statement returns nothing. The new `switch` expression returns a value.

```dart
// Old — verbose switch statement
String label;
switch (status) {
  case OrderStatus.pending:
    label = 'Pending';
    break;
  case OrderStatus.shipped:
    label = 'Shipped';
    break;
  case OrderStatus.delivered:
    label = 'Delivered';
    break;
}

// Dart 3 — switch expression
final label = switch (status) {
  OrderStatus.pending   => 'Pending',
  OrderStatus.shipped   => 'Shipped',
  OrderStatus.delivered => 'Delivered',
};
```

### Destructuring in switch

```dart
sealed class Shape {}
class Circle extends Shape { final double radius; Circle(this.radius); }
class Rectangle extends Shape { final double w, h; Rectangle(this.w, this.h); }

double area(Shape shape) => switch (shape) {
  Circle(radius: final r)               => 3.14 * r * r,
  Rectangle(w: final w, h: final h)    => w * h,
};
```

### if-case

```dart
// Match and destructure in a single if
final response = {'status': 200, 'data': {'id': '123', 'name': 'Jacket'}};

if (response case {'status': 200, 'data': final Map data}) {
  print('Success: ${data['name']}');
}

// With records
final result = fetchProduct();
if (result case (true, final product?)) {   // non-null pattern
  print('Got: ${product.name}');
}
```

### List patterns

```dart
final items = ['first', 'second', 'third'];

switch (items) {
  case []:
    print('Empty');
  case [final only]:
    print('Single: $only');
  case [final first, ...]:       // rest pattern — matches any remaining elements
    print('Starts with: $first');
}
```

### Guard clauses (when)

```dart
final message = switch (price) {
  final p when p <= 0    => 'Free',
  final p when p < 1000  => 'Affordable: ¥$p',
  final p                => 'Premium: ¥$p',
};
```

---

## 3. Sealed Classes

A `sealed` class can only be subclassed within the same library file. The compiler
knows all possible subtypes, so `switch` expressions on sealed classes are exhaustive
— the compiler errors if you miss a case.

```dart
// payment_result.dart
sealed class PaymentResult {}

class PaymentSuccess extends PaymentResult {
  final String transactionId;
  const PaymentSuccess(this.transactionId);
}

class PaymentFailed extends PaymentResult {
  final String reason;
  const PaymentFailed(this.reason);
}

class PaymentPending extends PaymentResult {
  const PaymentPending();
}
```

```dart
// Usage — exhaustive switch (compiler error if a case is missing)
Widget buildPaymentStatus(PaymentResult result) => switch (result) {
  PaymentSuccess(transactionId: final id) => SuccessBanner(id: id),
  PaymentFailed(reason: final r)          => ErrorBanner(reason: r),
  PaymentPending()                        => const CircularProgressIndicator(),
};
```

**Sealed class vs enum:**
- Enum: all variants have the same shape (no fields per variant).
- Sealed class: each subclass can have different fields — use for domain states.

**Equivalent to:**
- Kotlin: `sealed class` — identical concept
- Android ViewModel: `UiState` sealed class pattern — same thing in Dart now

---

## 4. Class Modifiers

Dart 3 introduced modifiers that restrict how a class can be used outside its library.

### final class

Cannot be extended, implemented, or mixed in outside the library.
Use for value objects whose identity you want to control.

```dart
final class Money {
  final int amountJpy;
  final String currency;
  const Money(this.amountJpy, this.currency);
}

// In another file:
// class DiscountedMoney extends Money {}  // ERROR — cannot extend final class
```

### interface class

Can only be `implements`, never `extends`. Enforces the contract without allowing
implementation reuse.

```dart
interface class ProductRepository {
  Future<List<Product>> getProducts() => throw UnimplementedError();
  Future<Product> getById(String id) => throw UnimplementedError();
}

// Elsewhere:
class MockProductRepository implements ProductRepository { ... }   // OK
// class CachingRepository extends ProductRepository { ... }       // ERROR
```

### base class

Can be `extends` but not `implements` outside the library. Guarantees that all
concrete subclasses go through your base logic.

```dart
base class BaseApiService {
  final Dio _client;
  BaseApiService(this._client);

  Future<T> get<T>(String path) async { ... }
}

class ProductApiService extends BaseApiService { ... }    // OK
// class FakeApi implements BaseApiService { ... }        // ERROR
```

### mixin class

Can be used as both a `mixin` (with `with`) and a regular class (with `extends`).

```dart
mixin class Loggable {
  void log(String message) => debugPrint('[${runtimeType}] $message');
}

class ProductService with Loggable { ... }    // as mixin
class BaseService extends Loggable { ... }    // as class
```

### Quick reference

| Modifier | extend | implement | mixin |
|---|---|---|---|
| (none) | yes | yes | no |
| `abstract` | yes | yes | no |
| `final` | no | no | no |
| `interface` | no | yes | no |
| `base` | yes | no | no |
| `sealed` | no (same library only) | no | no |
| `mixin class` | yes | yes | yes |

---

## Practical Summary

"Dart 3's records replace the pattern of creating a throwaway class just to return
two values from a method — I use them extensively for repository result types and
form validation. Sealed classes with exhaustive switch expressions are the equivalent
of Kotlin's sealed classes: the compiler enforces that every state is handled, which
is exactly the guarantee you want for UI state modeling. Class modifiers let you
express architectural boundaries in the type system — `interface class` for repository
contracts, `final class` for value objects you don't want subclassed. Patterns tie
all of this together: switching on a sealed class and destructuring its fields in
one expression eliminates the boilerplate that made this pattern verbose in Dart 2."
