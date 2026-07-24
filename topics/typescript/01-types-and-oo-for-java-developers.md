# TypeScript Types and Object Orientation for Java Developers

This is a practical reset for writing TypeScript with the clarity of good Java
without translating Java class hierarchy habits into every component.

## Why This Matters

The most valuable TypeScript skill is not adding type annotations everywhere.
It is making invalid UI and API states difficult to represent: loading is not
success, an anonymous user is not an administrator, and a failed request does
not contain an order.

## Smallest Useful Mental Model

- `type` and `interface` describe shapes; neither creates a runtime object
- unions model "one of these valid cases"
- narrowing proves which union member is present before it is used
- generics preserve a relationship between inputs and outputs
- classes work, but functions and composition are the normal default for UI

## 1. Interfaces, Types, and Structural Typing

Use an `interface` for a named object contract that is likely to be extended or
implemented. Use `type` for unions, aliases, mapped shapes, and composition.

```typescript
interface OrderSummary {
  id: string;
  totalJpy: number;
}

type LoadState<T> =
  | { kind: "idle" }
  | { kind: "loading" }
  | { kind: "success"; data: T }
  | { kind: "failure"; message: string };
```

TypeScript is structurally typed: a value is compatible because it has the
required shape, not because it explicitly declares a shared parent type. This
makes small adapters and test fakes easy, but it also means names alone do not
create a runtime boundary.

## 2. Narrowing Is the Everyday Safety Feature

Do not use `as` to silence uncertainty. Narrow the value with a discriminant,
`typeof`, `in`, or a dedicated type guard.

```typescript
function renderOrder(state: LoadState<OrderSummary>): string {
  switch (state.kind) {
    case "idle":
      return "Choose an order";
    case "loading":
      return "Loading…";
    case "success":
      return `Total: ¥${state.data.totalJpy}`;
    case "failure":
      return `Could not load order: ${state.message}`;
  }
}
```

This is more honest than `{ data?: OrderSummary; error?: string }`, where
`data` and `error` could accidentally be present together.

## 3. JavaScript Runtime Edges That Types Do Not Remove

TypeScript checks the code you write, but JavaScript still decides what values
arrive and whether two variables point to the same object. These are the small
differences that often cause more bugs than a missing interface.

### `undefined`, `null`, and truthiness are different questions

An optional property is normally `T | undefined`. Check explicitly when `0`,
an empty string, or `false` are valid values; a truthiness check would treat all
of them as absent.

```typescript
function quantityLabel(quantity: number | undefined): string {
  if (quantity === undefined) return "Quantity was not supplied";
  return `Quantity: ${quantity}`;
}
```

`if (!quantity)` would incorrectly reject a valid quantity of `0`. Use
`strictNullChecks` and narrow `null` or `undefined` before use. Prefer `===`
and `!==`; they avoid JavaScript's coercing equality rules.

### Objects are shared by reference

`readonly` prevents some assignments through that TypeScript reference, but it
does not freeze an object and is not deeply immutable. For React state, create
the next value instead of changing the current one in place.

```typescript
type Cart = { readonly lines: readonly string[] };

function addLine(cart: Cart, sku: string): Cart {
  return { ...cart, lines: [...cart.lines, sku] };
}
```

Use `unknown` for untrusted values and narrow it before use. `any` disables
that check and lets an assumption escape into the rest of the application.

## 4. Generics Preserve Contracts

Use a generic when the function does the same job for many types while keeping
the input/output relationship intact.

```typescript
type ApiResult<T> =
  | { ok: true; value: T }
  | { ok: false; status: number; message: string };

function success<T>(value: T): ApiResult<T> {
  return { ok: true, value };
}
```

This is close to a Java `ApiResult<T>`, but avoid generic abstractions that
only hide a one-line operation.

## 5. Object Orientation: Use It for Behavior, Not Ceremony

Classes, `implements`, abstract classes, visibility modifiers, and generics all
exist in TypeScript. Use them when an object owns stateful behavior or provides
a useful integration boundary.

```typescript
interface PaymentGateway {
  createIntent(orderId: string, totalJpy: number): Promise<string>;
}

class HttpPaymentGateway implements PaymentGateway {
  constructor(private readonly baseUrl: string) {}

  async createIntent(orderId: string, totalJpy: number): Promise<string> {
    const response = await fetch(`${this.baseUrl}/payment-intents`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ orderId, totalJpy }),
    });
    if (!response.ok) throw new Error("payment intent request failed");
    return (await response.json() as { id: string }).id;
  }
}
```

Good fits for classes:

- stateful SDK or HTTP adapters
- a domain object whose methods protect a real invariant
- an explicit `implements` contract shared by production and test code

Weak fit:

- a React component with a Java-style service, manager, factory, and interface
  for every small transformation

### `private` helps design; it does not hide browser secrets

TypeScript `private` is enforced by the type checker and is erased from normal
JavaScript output. ECMAScript `#private` fields keep an implementation detail
private at runtime, which can be useful inside an SDK or library.

Neither form protects a value shipped to a browser. A client secret, provider
token, or database password must stay in a trusted backend, not in a class
field with a reassuring name.

For ordinary UI transformations, prefer a pure function:

```typescript
function formatJpy(amount: number): string {
  return new Intl.NumberFormat("ja-JP", {
    style: "currency",
    currency: "JPY",
  }).format(amount);
}
```

## 6. TypeScript Does Not Validate JSON

This compiles but trusts the network blindly:

```typescript
const order = (await response.json()) as OrderSummary;
```

`as` tells the compiler to trust you; it does not inspect the response. At an
important boundary, parse and validate unknown JSON with a runtime schema
library or a small explicit parser. Keep that validation near the client, not
scattered through components.

## Strong Default

Turn on `strict`, use discriminated unions for async UI state, use generics to
preserve real contracts, and prefer functions first. Bring in OO when it makes
behavior or an integration boundary clearer than a function would.

## Further Reading

- [TypeScript everyday types](https://www.typescriptlang.org/docs/handbook/2/everyday-types.html)
- [TypeScript narrowing](https://www.typescriptlang.org/docs/handbook/2/narrowing.html)
- [TypeScript generics](https://www.typescriptlang.org/docs/handbook/2/generics.html)
- [TypeScript classes and runtime private fields](https://www.typescriptlang.org/docs/handbook/2/classes.html)
