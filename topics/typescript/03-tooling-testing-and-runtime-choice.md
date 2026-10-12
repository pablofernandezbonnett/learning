# TypeScript Tooling, Tests, and Runtime Choice

The feedback loop should catch type mistakes, user-visible regressions, and build failures. A browser bundle remains public regardless of which tool built it.

## Keep type checking strict

```json
{
  "compilerOptions": {
    "strict": true,
    "noUncheckedIndexedAccess": true,
    "noEmit": true
  }
}
```

`strict` makes uncertainty explicit. `noUncheckedIndexedAccess` reminds you that an indexed value may be absent. `tsc --noEmit` checks types; it does not validate JSON, run tests, or build the app.

## Test the risk at its boundary

| Risk | Useful evidence |
| --- | --- |
| Wrong transformation or state transition | Unit test |
| Broken form feedback or interaction | Component test using labels, roles, and visible messages |
| Wrong HTTP contract or response parser | API-boundary test with valid and malformed payloads |
| Broken critical browser journey | A small end-to-end test |

For the general rule of choosing test layers, use [testing strategy](../testing/01-testing-strategies.md). Test user behavior rather than internal hook calls or CSS class names unless those are the feature.

## Choose the runtime from the product need

- A browser client consuming an existing API can use a client build tool such as Vite.
- A product needing server rendering, server routes, or a framework-owned data and routing model may justify Next.js.

Those choices affect hosting, caching, and where code executes. Confirm whether a module runs on the server, in the browser, or both before putting credentials or business effects in it. Browser-exposed configuration is public; keep provider secrets and private keys in a trusted server boundary.

Use the project's established toolchain unless a concrete requirement earns a change. Avoid treating a framework choice as a proxy for code quality.

## Review check

- Do type checking, tests, and a production build run in the normal feedback loop?
- Is malformed API data tested at the parser?
- Are browser-visible values safe to expose?
- Does the chosen runtime solve a named rendering, routing, or server need?

Sources: [TypeScript configuration](https://www.typescriptlang.org/tsconfig/), [Vite guide](https://vite.dev/guide/), [Next.js documentation](https://nextjs.org/docs), [React testing guidance](https://react.dev/learn).
