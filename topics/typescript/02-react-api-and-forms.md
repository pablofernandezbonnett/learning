# React, APIs, and Forms with TypeScript

Use this note to connect TypeScript types to a real browser UI: functional
components, state, asynchronous requests, forms, and the failure states users
actually see.

## Why This Matters

A frontend is not a collection of screens that happen to call endpoints. It is
the client-side state machine for an API: inputs are edited, requests are sent,
responses succeed or fail, and users retry or navigate away.

## Smallest Useful Mental Model

- a component is a function from props and state to UI
- `useState` owns data that changes and must cause a re-render
- `useEffect` synchronizes with something outside React, not general-purpose
  "run code after render"
- API responses and form values are untrusted at their boundary
- show loading, error, empty, and success states deliberately

## Bad Mental Model vs Better Mental Model

Bad mental model:

> Put fetch logic, mutable form fields, API parsing, and every UI branch in one
component, then add `useEffect` until it appears to work.

Better mental model:

> Keep a typed API client, a small feature hook or state owner, and a component
that renders explicit UI states. Use an effect only to synchronize with an
external system.

## 1. Functional Components and State

```tsx
import { useState, type FormEvent } from "react";

type Props = { initialQuantity: number; onSubmit(quantity: number): void };

export function QuantityForm({ initialQuantity, onSubmit }: Props) {
  const [quantity, setQuantity] = useState(initialQuantity);

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (quantity > 0) onSubmit(quantity);
  }

  return (
    <form onSubmit={submit}>
      <input
        type="number"
        min="1"
        value={quantity}
        onChange={(event) => setQuantity(Number(event.target.value))}
      />
      <button type="submit">Reserve</button>
    </form>
  );
}
```

Do not use a class component to reproduce a Java service object. A functional
component plus hooks is the normal React unit of UI behavior.

## 2. Give API Calls an Owner

Keep HTTP and JSON details out of rendering code. Return a typed result that
does not confuse a failed request with a successful one.

```typescript
type ApiResult<T> =
  | { ok: true; value: T }
  | { ok: false; status: number; message: string };

type ReserveResponse = { reservationId: string };

export async function reserveStock(
  sku: string,
  quantity: number,
): Promise<ApiResult<ReserveResponse>> {
  const response = await fetch("/api/reservations", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ sku, quantity }),
  });

  if (!response.ok) {
    return { ok: false, status: response.status, message: "Reservation failed" };
  }

  return { ok: true, value: await response.json() as ReserveResponse };
}
```

The `as ReserveResponse` is acceptable only as a compact example. In a
production boundary, validate the JSON at runtime before returning it, as
explained in `01`.

## 3. Model the User-Visible States

```tsx
type LoadState<T> =
  | { kind: "idle" }
  | { kind: "loading" }
  | { kind: "success"; data: T }
  | { kind: "failure"; message: string };

function ReservationStatus({ state }: { state: LoadState<ReserveResponse> }) {
  if (state.kind === "loading") return <p>Reserving stock…</p>;
  if (state.kind === "failure") return <p role="alert">{state.message}</p>;
  if (state.kind === "success") return <p>Reserved: {state.data.reservationId}</p>;
  return null;
}
```

The important frontend capability is not issuing `fetch`; it is correctly
handling cancellation, duplicate submits, server validation errors, and a
request that fails after the user presses a button.

## 4. Forms: Client Validation Helps, Server Validation Decides

Use the browser and TypeScript to give quick feedback. The backend remains the
authority because a client can be modified, bypassed, or stale.

Good default:

- disable or ignore duplicate submissions while a request is pending
- render field errors accessibly near the relevant input
- preserve values when a server validation error is returned
- send a narrow request DTO, not the whole component state
- let Spring own authorization and final business validation

## 5. Use Effects for External Synchronization

An effect is appropriate for a subscription, browser API, or request whose
lifecycle must follow a component's lifecycle. It needs cleanup or cancellation
when relevant.

```tsx
import { useEffect, useState } from "react";

type OrderTitle = { title: string };

function isOrderTitle(value: unknown): value is OrderTitle {
  return (
    typeof value === "object" &&
    value !== null &&
    "title" in value &&
    typeof value.title === "string"
  );
}

async function loadOrderTitle(
  orderId: string,
  signal: AbortSignal,
): Promise<ApiResult<OrderTitle>> {
  const response = await fetch(`/api/orders/${orderId}`, { signal });
  if (!response.ok) {
    return { ok: false, status: response.status, message: "Could not load order" };
  }

  const payload: unknown = await response.json();
  if (!isOrderTitle(payload)) {
    return { ok: false, status: 502, message: "Order response was invalid" };
  }
  return { ok: true, value: payload };
}

function OrderTitle({ orderId }: { orderId: string }) {
  const [state, setState] = useState<LoadState<OrderTitle>>({ kind: "loading" });

  useEffect(() => {
    const controller = new AbortController();
    void loadOrderTitle(orderId, controller.signal)
      .then((result) => {
        if (controller.signal.aborted) return;
        setState(
          result.ok
            ? { kind: "success", data: result.value }
            : { kind: "failure", message: result.message },
        );
      })
      .catch(() => {
        if (!controller.signal.aborted) {
          setState({ kind: "failure", message: "Could not load order" });
        }
      });
    return () => controller.abort();
  }, [orderId]);

  if (state.kind === "failure") return <p role="alert">{state.message}</p>;
  if (state.kind !== "success") return <p>Loading order…</p>;
  return <h1>{state.data.title}</h1>;
}
```

For complex server state, a purpose-built query library can reduce repeated
cache and loading code. Learn the state and failure model first; do not add a
library only to avoid understanding it.

## Practical Rule

> Components render state. A narrow client owns HTTP. The backend owns trust,
> authorization, and business decisions.

## Further Reading

- [React `useState`](https://react.dev/reference/react/useState)
- [React `useEffect`](https://react.dev/reference/react/useEffect)
- [React forms](https://react.dev/reference/react-dom/components/form)
