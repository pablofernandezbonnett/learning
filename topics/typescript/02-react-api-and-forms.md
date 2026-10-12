# React, APIs, and Forms with TypeScript

A browser feature owns what users see while a request is pending, succeeds, fails, or needs a safe retry. Keep rendering, request coordination, and response validation easy to find.

## Give each concern an owner

| Concern | Useful owner |
| --- | --- |
| Input and local interaction | Component or small feature hook |
| Request timing and visible state | Feature hook, route loader, or query layer |
| HTTP and response parsing | API client |
| Authorization and final business rule | Backend |

The client can disable a forbidden-looking action for usability; the server must still enforce the rule. Use [runtime boundary checks](./01-types-and-runtime-boundaries.md) before returning API data to the feature.

## Show distinct states

```typescript
type ScreenState<T> =
  | { kind: "loading" }
  | { kind: "ready"; data: T }
  | { kind: "empty" }
  | { kind: "error"; message: string }
  | { kind: "pending"; operationId: string };
```

Use only states the product needs. A failed read may leave previous data visible with an inline retry; a timed-out write may have an unknown outcome and needs reconciliation before resubmission. Do not turn both into the same generic error screen.

## Forms protect the interaction, not the server

Validate locally for prompt feedback, show field errors near their inputs, preserve values after a server rejection, and prevent accidental duplicate taps while a write is in progress. Send a narrow request body, not all component state.

Client validation is not authorization. A caller can bypass the form, and a request can arrive after the UI state becomes stale. The backend decides whether the command is allowed and whether a retry could duplicate an effect.

## Use effects for external synchronization

`useEffect` is for work tied to a component lifecycle, such as a subscription or synchronizing with an external system. Do not use it to calculate data that can be derived during render or to handle a button click that belongs in the event handler.

When an effect starts a request, clean it up or ignore a late result after its inputs change. A route loader or query layer may own fetching instead; the same stale-result risk still needs a clear owner. See the [official effect guide](https://react.dev/reference/react/useEffect).

## Review check

- Can users tell loading, empty, error, success, and unknown write outcome apart?
- Who prevents or reconciles duplicate submissions?
- Is external data validated outside the component?
- Is an effect synchronizing with something external, with cleanup where needed?
