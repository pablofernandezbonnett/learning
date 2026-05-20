# Platform Integration and Testing in Flutter

Flutter does not stop at widgets.
Sooner or later, the app hits:

- camera
- notifications
- secure storage
- deep links
- platform plugins

And once that happens, test strategy matters more than demo success.

---

## Why This Matters

Many Flutter codebases are easy to demo but hard to trust because:

- plugin behavior is hidden inside screens
- platform boundaries are not isolated
- tests cover pure logic but miss the real failure points

This matters because mobile product bugs often live at the platform boundary,
not in simple widget rendering.

---

## Smallest Useful Mental Model

Use this split:

- **pure feature logic tests**: state transitions and decisions
- **widget tests**: rendering and interaction of one UI area
- **integration tests**: real app flows and boundary behavior
- **platform boundary isolation**: plugins or channels wrapped behind your own interface

Short rule:

> Test the risk where it actually lives, and keep plugins behind boundaries you control.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- if widget tests pass, the app is mostly covered
- plugin calls can sit directly in UI code
- testing means maximizing test count

Better mental model:

- the most dangerous failures often live at API, storage, and platform edges
- plugins should sit behind explicit wrappers or services
- each test layer should catch a different failure type

Small concrete example:

- weak approach: login screen calls secure storage plugin directly and tests only
  one happy widget render
- better approach: auth storage is wrapped behind an interface, feature logic is
  tested directly, and one integration flow proves session restore behavior

---

## 1. Platform Boundaries

Good candidates for a wrapper:

- secure storage
- push notifications
- deep links
- biometrics
- camera or file picker

Why wrap them:

- app logic depends on your interface, not the plugin API
- tests can fake the boundary more cleanly
- plugin replacement is less invasive

---

## 2. What Each Test Layer Should Catch

### Feature or state tests

Catch:

- loading, success, error transitions
- retry decisions
- auth gating logic

### Widget tests

Catch:

- screen rendering
- button and form interaction
- small local UI behavior

### Integration tests

Catch:

- multi-screen flow
- navigation correctness
- API/auth/session interaction
- critical plugin or platform handoff behavior

Short rule:

> Do not expect one widget test layer to prove the whole product flow.

---

## 3. Strong Default

For most real apps:

- many feature/state tests
- selective widget tests for key screens and interactions
- a small number of integration flows for the highest-risk journeys

High-risk journeys often include:

- login and session restore
- checkout or payment confirmation
- deep link into an authenticated screen
- offline or retry-prone sync flow

---

## 4. Concrete Example

Imagine a session restore flow.

Useful test split:

- feature test: restored token leads to authenticated state
- widget test: splash or login gate renders correct branch
- integration test: app starts, secure storage returns token, home flow appears

This is stronger than:

- ten isolated widget tests
- no proof that startup auth boundary really works

---

## 5. Main Failure Modes To Remember

- plugin unavailable or misconfigured
- deep link arrives in wrong auth state
- secure storage returns stale or missing data
- navigation and session restore disagree
- retry flow loops or duplicates user action

These are the failures worth testing intentionally.

---

## 6. Practical Summary

Good short answer:

> In Flutter, I keep platform plugins behind boundaries I control, test feature logic directly, use widget tests for local UI behavior, and reserve integration tests for the highest-risk app flows such as login, session restore, checkout, and deep links. The goal is not maximum test count; it is confidence at the boundaries where real failures happen.
