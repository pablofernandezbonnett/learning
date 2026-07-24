# TypeScript Tooling, Testing, Vite, and Next.js

This note gives a practical setup boundary for a TypeScript UI. It is not a
survey of every JavaScript build tool or a reason to turn Node into the primary
backend lane.

## Why This Matters

TypeScript types catch many mistakes before running code, but they do not prove
that a browser interaction, network response, or build configuration works.
Tooling should make the feedback loop short: type-check, test the important
behavior, build the application, and keep the runtime choice intentional.

## Smallest Useful Mental Model

- `tsc --noEmit` checks types; it does not run tests or validate JSON
- unit tests protect pure transformations and state behavior
- component tests protect user-visible interaction
- Vite is a strong default for a client-side React application
- Next.js is useful when its server rendering, routing, and full-stack web
  boundary solve a real product need

## 1. A Small, Strict Baseline

Start with `strict: true` rather than adding strictness later. Treat `any` as
an escape hatch: it discards the help you chose TypeScript for.

```json
{
  "compilerOptions": {
    "strict": true,
    "noUncheckedIndexedAccess": true,
    "noEmit": true
  }
}
```

`noUncheckedIndexedAccess` is especially useful when data comes from arrays or
maps: it reminds you that an index lookup can be absent.

## 2. Test Behavior, Not Implementation Trivia

Test the output of pure business-adjacent functions and the user-visible result
of a component. Do not make a test depend on an internal hook arrangement or
CSS class unless that class is the feature.

```typescript
import { expect, test } from "vitest";

type ApiResult<T> =
  | { ok: true; value: T }
  | { ok: false; status: number; message: string };

function reservationMessage(result: ApiResult<{ reservationId: string }>): string {
  return result.ok ? `Reserved: ${result.value.reservationId}` : result.message;
}

test("keeps the conflict message visible to the caller", () => {
  const result: ApiResult<{ reservationId: string }> = {
    ok: false,
    status: 409,
    message: "Stock changed; refresh and try again",
  };

  expect(reservationMessage(result)).toBe("Stock changed; refresh and try again");
});
```

Keep the test layers small:

- unit: formatting, state transitions, request construction
- component: submit, loading, error, and accessible feedback
- end-to-end: a few critical browser-to-backend flows

### One component test should prove the user-facing failure path

For React components, query the page as a user or assistive technology would:
by label, button name, and semantic role. This protects both interaction and
basic accessibility without coupling the test to component internals.

```tsx
import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import { ReservationForm } from "./ReservationForm";

test("shows an accessible error after a failed reservation", async () => {
  const user = userEvent.setup();
  const reserve = vi.fn().mockRejectedValue(new Error("stock changed"));

  render(<ReservationForm reserve={reserve} />);
  await user.type(screen.getByLabelText(/quantity/i), "2");
  await user.click(screen.getByRole("button", { name: /reserve/i }));

  expect(await screen.findByRole("alert")).toHaveTextContent("Stock changed");
  expect(screen.getByRole("button", { name: /reserve/i })).not.toBeDisabled();
});
```

The component receives `reserve` as a narrow dependency, so the test controls
the failure without relying on a real backend. In a larger application, use a
network mock at the HTTP boundary instead of mocking implementation details
inside every component.

## 3. Vite vs Next.js

Use Vite when the application is primarily a browser client consuming existing
Spring APIs. It gives a focused development server and production build without
asking the frontend to become a second application platform.

Use Next.js when you deliberately need its web framework capabilities, such as
file-based routing and server/client rendering boundaries. It is not merely
"React but more professional." It introduces server execution, caching, and
data-boundary decisions that a team must own.

For this repository's Java-first profile:

- Vite + React + TypeScript is the strong first default
- learn Next.js practically enough to read and work in an existing codebase
- do not add NestJS unless the target work genuinely requires Node backend

## 4. Keep Frontend Configuration Public by Design

Values bundled into browser JavaScript are visible to users. A frontend API URL
or public analytics identifier is not a secret; an OAuth client secret, model
provider token, or database password must never be placed in browser config.

That rule matters equally for Vite and Next.js. Put confidential integration
calls behind a trusted backend boundary.

## Strong Default

Use strict TypeScript, Vite for a normal React client, a unit/component testing
tool that fits the project, and a small number of end-to-end checks for risky
flows. Adopt Next.js because its runtime model helps the product, not because
it is a fashionable replacement for a Java backend.

## Further Reading

- [Vite guide](https://vite.dev/guide/): setup and build model
- [Vitest guide](https://vitest.dev/guide/): test runner documentation
- [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/): component tests through user-facing queries
- [Next.js App Router](https://nextjs.org/docs/app/getting-started): runtime and routing model
- [Next.js server and client components](https://nextjs.org/docs/app/getting-started/server-and-client-components)
