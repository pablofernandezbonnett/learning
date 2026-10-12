# TypeScript Types and Runtime Boundaries

TypeScript checks relationships in source code, then runs as JavaScript. Use types to make invalid combinations hard to write, and validate values that arrive from the network, storage, or user input before trusting them.

## Describe the valid cases

```typescript
type Result<T> =
  | { kind: "ok"; value: T }
  | { kind: "error"; message: string };

function label(result: Result<{ id: string }>): string {
  return result.kind === "ok" ? result.value.id : result.message;
}
```

The `kind` field lets TypeScript narrow the union. This is clearer than an object with optional `value` and `error` fields that might both be present. A generic `T` preserves the relationship between the result and its payload without duplicating the result type for every API.

Types are structural: a value fits an object type when it has the required members, even if it did not declare that interface by name. Use `interface` or `type` according to which expresses the shape clearly; neither creates runtime validation.

## Treat external values as unknown

```typescript
type OrderSummary = { id: string; total: number };

function parseOrder(value: unknown): OrderSummary {
  if (
    typeof value === "object" &&
    value !== null &&
    "id" in value &&
    typeof value.id === "string" &&
    "total" in value &&
    typeof value.total === "number"
  ) {
    return { id: value.id, total: value.total };
  }
  throw new Error("Invalid order response");
}
```

`await response.json() as OrderSummary` only tells the compiler to accept an assumption. It does not check the response. Parse once at the API boundary, then let the rest of the feature use the typed value. A schema library can replace the explicit parser when the shape is larger.

## Remember JavaScript behavior

- `undefined`, `null`, `0`, `false`, and `""` are not the same state. Check absence explicitly when a falsey value is valid.
- `readonly` prevents assignments through a TypeScript reference; it does not freeze an object at runtime or make nested objects immutable.
- `private` on a TypeScript class helps source-level design. Neither it nor a runtime-private field can hide a secret included in browser code.

Prefer functions and plain data for simple transformations. Use a class when it owns meaningful behavior or a stateful integration boundary, rather than building a hierarchy for every UI operation.

## Review check

- Are impossible combinations represented by separate union cases?
- Does each external value get a runtime check before it becomes trusted?
- Did a cast or `any` hide uncertainty?
- Would a JavaScript falsey value or shared object reference change the result?

Sources: [everyday types](https://www.typescriptlang.org/docs/handbook/2/everyday-types.html), [narrowing](https://www.typescriptlang.org/docs/handbook/2/narrowing.html), [generics](https://www.typescriptlang.org/docs/handbook/2/generics.html).
