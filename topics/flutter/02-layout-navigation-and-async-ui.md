# Layout, Navigation, and Async UI in Flutter

Many Flutter bugs are not "widget bugs".
They are boundary bugs between:

- layout
- navigation
- loading and retry behavior

This note keeps those boundaries practical.

---

## Why This Matters

A UI can look fine in the happy path and still fail badly when:

- content is longer than expected
- navigation stacks get tangled
- the API is slow
- retry and stale states are not modeled clearly

This matters because product quality is often decided more by those states than
by the first screenshot.

---

## Smallest Useful Mental Model

Use this split:

- **layout**: can the screen adapt to real device constraints
- **navigation**: can the user move through flows cleanly
- **async UI**: what does the screen do while work is loading, failing, or retrying

Short rule:

> A good screen is not only correct when data is ready. It stays understandable while data is late, missing, stale, or failing.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- layout is mostly styling
- navigation is mostly routes
- loading is just a spinner

Better mental model:

- layout is constraint handling
- navigation is flow ownership
- async UI is state design made visible

Small concrete example:

- weak approach: checkout page shows a full-screen spinner for every network
  action and pops routes manually after every success
- better approach: checkout flow has explicit loading, recoverable error, and
  success states, with navigation driven by real flow outcome

---

## 1. Layout: Think In Constraints, Not Screenshots

Good layout judgment asks:

- what happens on smaller widths
- what happens when text expands
- what happens when a list gets longer
- what happens when keyboard or safe areas change the space

Good default:

- make important UI survive constrained space first

Bad sign:

- design only verified with one demo payload and one device size

---

## 2. Navigation: Own The Flow

Navigation should reflect the user journey, not random widget reach.

Good examples:

- login flow
- checkout flow
- password reset flow

What matters:

- which screen owns the next step
- what happens after cancel or back
- how success and failure affect the route stack

Short rule:

> Treat navigation as part of the business flow, not just as button wiring.

---

## 3. Async UI States You Should Make Explicit

Most important states:

- initial
- loading
- success
- empty
- recoverable error
- retry in progress
- stale-but-visible data

Teams often model only:

- loading
- loaded
- failed

That is often too thin for real app behavior.

---

## 4. Good Loading Behavior

Use the lightest loading treatment that still makes sense.

Examples:

- first screen load: maybe skeleton or placeholder
- small background refresh: keep old data and show inline progress
- write action like "Pay now": disable action and show local progress near the action

Bad default:

- full-screen spinner for every async action

Why weak:

- user loses context
- retry behavior becomes unclear

---

## 5. Good Error Behavior

The user should be able to tell:

- what failed
- whether retry makes sense
- whether previous data is still usable

Good examples:

- product list keeps previous items, shows retry banner
- payment confirmation blocks next step and shows explicit failure state

This is where backend and UI design meet:

- not every error should collapse the whole screen

---

## 6. Concrete Example

Imagine an order history screen.

Better state handling:

- first load -> skeleton
- loaded data -> list visible
- refresh fails -> keep old list, show inline retry
- empty history -> empty state, not generic error

That is stronger than:

- always replace the whole screen with spinner or error page

---

## 7. Strong Default

For most product screens:

- design layout for constrained space early
- let navigation follow feature flows
- model more than one async failure shape
- preserve useful data on partial failure when you can

---

## 8. Big Traps

1. **Layout tested only with one payload**
   Example: long merchant names or translated text breaks the screen.

2. **Navigation driven by scattered widget callbacks**
   Example: flow ownership becomes hard to reason about.

3. **Full-screen spinner for every async action**
   Example: user loses context and local failures look global.

4. **One generic error state for everything**
   Example: recoverable refresh failure looks the same as checkout failure.

5. **Destroying usable stale data too quickly**
   Example: one background failure wipes the whole screen.

---

## 9. Practical Summary

Good short answer:

> In Flutter, layout means handling real constraints, navigation means owning the user flow, and async UI means making loading, stale, empty, and failure states explicit. I try to keep useful data visible when possible instead of turning every delay into a full-screen spinner.
