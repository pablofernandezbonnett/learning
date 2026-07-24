# TypeScript for Java Backend Engineers

Use this folder to become effective in a TypeScript frontend without turning
Node into a second backend specialization. The target is typed UI code that
talks safely to APIs, models state honestly, and remains easy to test.

TypeScript transfers much of the Java mindset: explicit contracts, generics,
and careful handling of absent or variant data. The important shift is that the
type system is erased at runtime. A TypeScript type does not validate an HTTP
response, browser form, or environment variable by itself.

## Recommended Order

1. [01-types-and-oo-for-java-developers.md](./01-types-and-oo-for-java-developers.md): types, unions, generics, narrowing, and when classes help
2. [02-react-api-and-forms.md](./02-react-api-and-forms.md): functional components, hooks, UI states, forms, and API boundaries
3. [03-tooling-testing-and-runtime-choice.md](./03-tooling-testing-and-runtime-choice.md): strict tooling, tests, Vite, and the practical boundary for Next.js

## Smallest Useful Mental Model

- use types to model legal UI states, not merely to annotate variables
- validate untrusted data at the API boundary
- prefer functions, plain data, and composition for UI work
- use classes when they genuinely own stateful behavior or implement a useful
  client boundary
- keep Java/Spring responsible for core business workflows and authorization

## Core Rule

> TypeScript gives Java-like feedback while you write code, but browser and API
> inputs remain runtime data. Model the states, validate the boundary, and keep
> frontend logic focused on presentation and interaction.
