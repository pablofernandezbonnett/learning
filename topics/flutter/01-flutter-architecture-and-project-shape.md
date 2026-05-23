# Flutter Architecture and Project Shape

Flutter architecture gets messy for the same reason backend architecture gets
messy:

- boundaries are unclear
- state leaks
- one layer starts doing everyone else's job

This note keeps the first practical decisions visible.

---

## Why This Matters

A Flutter codebase gets painful when:

- UI widgets call HTTP directly everywhere
- one giant state class grows across the whole app
- navigation, state, and business rules blur together
- backend contract changes ripple through random UI files

This matters because the real goal is not "use the modern package".
It is to keep app behavior understandable while screens, APIs, and flows change.

---

## Smallest Useful Mental Model

Use this split:

- **presentation**: widgets, routes, loading, error, and user interaction
- **application / feature logic**: use cases and state changes for a feature flow
- **data / integration**: API calls, local storage, and mapping

Short rule:

> Keep UI concerns near widgets, keep contract and state transitions in feature logic, and keep transport details at the edge.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- Flutter architecture means choosing one state package first
- every screen can fetch its own data however it wants
- project structure is just personal style

Better mental model:

- architecture is mainly about making feature boundaries, state ownership, and contracts visible
- state management is a tool, not the architecture itself
- the project shape should make change impact predictable

Small concrete example:

- weak approach: `CheckoutPage` calls HTTP, parses JSON, stores token state, and
  decides retry behavior
- better approach: `CheckoutPage` renders state, a feature controller or
  notifier owns checkout behavior, and a repository or client handles transport

---

## 1. Package By Feature Is Usually The Stronger Default

Weak shape:

```text
screens/
widgets/
services/
models/
```

This often becomes:

- all screens know too much
- all services become giant shared buckets
- feature ownership stays blurry

Stronger shape:

```text
lib/
  features/
    checkout/
      presentation/
      application/
      data/
    account/
      presentation/
      application/
      data/
  core/
    networking/
    auth/
    design_system/
```

Why this is stronger:

- feature boundaries stay visible
- backend-facing contracts are easier to track
- mobile and backend engineers can reason about one feature at a time

---

## 2. What State Should Live Where

### Widget-local state

Use for:

- animation toggles
- selected tab
- temporary form visibility

Good fit:

- state that matters only to one small UI area

### Feature state

Use for:

- loading order history
- applying a coupon
- confirming checkout
- retrying a failed fetch

Good fit:

- state that belongs to one user-facing flow

### App-wide state

Use for:

- auth session
- theme or locale
- top-level feature flags

Good fit:

- state that several features really need

Short rule:

> Make state as local as you can, and as shared as you must.

---

## 3. State Management Choice: What Actually Matters

The repo does not need one ideology here.

What matters more than the package name:

- where state is owned
- how side effects are triggered
- whether loading and error states are explicit
- whether tests can drive the state without pumping the whole app

Bad sign:

- state tool chosen first
- architecture explained second

Better sign:

- feature boundary clear first
- state tool chosen to fit that boundary

---

## 4. Repositories And Clients

A Flutter feature should usually not know:

- HTTP details
- token header assembly
- raw JSON shapes

That belongs closer to repositories or API clients.

Why:

- contract changes stay more localized
- tests can focus on feature behavior
- UI logic stops depending on transport trivia

---

## 5. DTOs vs Domain-ish Models In Flutter

You do not need rich DDD everywhere in Flutter.
But you do need to avoid leaking raw transport shapes through the whole app.

Useful split:

- DTO or response model: what the API returned
- feature model: what the screen or flow really needs

Example:

- API returns ten fields for an order
- order card screen only needs id, total, status, and ETA

That screen should not depend on every backend field by accident.

---

## 6. Concrete Example

Imagine a checkout feature.

Good practical shape:

```text
features/checkout/
  presentation/
    checkout_page.dart
    checkout_state.dart
    checkout_notifier.dart
  application/
    place_order.dart
  data/
    checkout_repository.dart
    checkout_api_client.dart
    checkout_dto.dart
```

What each part owns:

- page: rendering and user interaction
- notifier/controller: feature state transitions
- use case: checkout action intent
- repository/client: transport and mapping

---

## 7. Strong Default

For most product apps:

- package by feature
- keep state feature-local unless it is genuinely global
- keep API transport details out of widgets
- model loading, success, and failure states explicitly

That default is usually stronger than:

- one giant shared services folder
- UI-driven networking everywhere

---

## 8. Big Traps

1. **Package by layer only**
   Example: feature boundaries disappear into huge shared folders.

2. **Global state too early**
   Example: small screen state becomes app-wide complexity.

3. **Widgets owning transport logic**
   Example: retry and auth behavior leak into UI code.

4. **Raw backend shapes leaking everywhere**
   Example: one API field rename breaks many screens.

5. **Choosing a state library before naming the state boundary**
   Example: tool choice hides the actual design problem.

---

## 9. Practical Summary

Practical summary:

> In Flutter, I prefer package-by-feature, keep state as local as possible, and keep API and storage details out of widgets. The architecture matters less as a pattern name and more as a way to keep feature boundaries, state ownership, and backend contract changes understandable.
