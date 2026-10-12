# TypeScript

Use this block to model valid states, check data at runtime boundaries, and build a browser UI whose failures users can understand.

1. [Types and runtime boundaries](./01-types-and-runtime-boundaries.md): structural types, unions, narrowing, and validation of external values.
2. [React, APIs, and forms](./02-react-api-and-forms.md): state ownership, requests, form behavior, and effects.
3. [Tooling, tests, and runtime choice](./03-tooling-testing-and-runtime-choice.md): strict checks, useful tests, and when the UI needs server execution.

Start with the language and runtime boundary. A TypeScript type helps while writing code; it does not inspect an HTTP response or protect a secret shipped to the browser.
