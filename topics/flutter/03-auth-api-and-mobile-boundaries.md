# Auth, APIs, and Mobile Boundaries in Flutter

Flutter client work becomes stronger when you stop treating the app like a
trusted extension of the backend.

That is the most important boundary to remember.

---

## Why This Matters

Mobile apps often fail in the same two ways:

- the app takes on security responsibility that belongs to the backend
- the UI flow ignores how tokens, retries, and offline behavior actually work

This matters because mobile auth is not only login UI.
It is also:

- public-client constraints
- token lifecycle
- backend trust boundaries
- safe failure handling around protected APIs

---

## Smallest Useful Mental Model

A Flutter mobile app is a `public client`.

That means:

- the app package can be inspected
- the device is not a trusted server environment
- tokens can be targeted
- the backend must still enforce authorization and workflow rules

Short rule:

> The Flutter app can present identity and tokens. The backend must still decide what actions are allowed.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- mobile app can safely hide long-lived secrets
- client role checks are meaningful enforcement
- auth is finished once the access token exists

Better mental model:

- mobile app needs safe login and token handling, but still runs on an untrusted device
- backend owns authorization and critical workflow checks
- UI should be built around token expiry, retry, and session refresh reality

Small concrete example:

- weak approach: Flutter app decides refund permission from local role flags and
  backend trusts the request shape
- better approach: Flutter app manages session UX and retries, while backend
  enforces refund authorization and state transitions

---

## 1. What The Client Should Own

Good client responsibilities:

- login initiation
- session-aware routing
- safe token storage using platform facilities
- attaching credentials to API calls
- handling expiry and refresh UX
- presenting recoverable failure states

Bad client responsibilities:

- final authorization
- business workflow approval
- hidden long-lived secret ownership

---

## 2. API Client Boundaries

A Flutter feature should usually not know:

- raw token refresh protocol
- header assembly
- retry policy details
- transport error translation

Those should live near the API client or auth-aware repository layer.

Why:

- every feature sees more consistent failure behavior
- auth changes stay localized
- retry loops are easier to reason about

---

## 3. Session And Expiry Behavior

Strong default:

- short-lived access token
- refresh path that can fail cleanly
- app knows when to:
  - retry silently
  - force re-login
  - keep stale data visible

This matters because a protected screen usually fails in one of several ways:

- token expired, refresh succeeds
- token expired, refresh fails
- user lost connectivity
- backend returns forbidden because business authorization failed

Those are not the same UI state.

---

## 4. Navigation And Auth

Auth changes app flow.

Examples:

- logged-out user goes to login
- expired session may try refresh before redirect
- forbidden action may stay on the same screen with an explicit message

Short rule:

> Authentication failure and authorization failure should not feel like the same route transition.

---

## 5. Concrete Example

Imagine a Flutter order details screen with a refund action.

Better behavior:

- app fetches order details through an auth-aware client
- token expiry is handled in the client layer
- refund button state depends on current screen state
- backend still decides whether refund is allowed
- forbidden result shows domain-specific feedback, not generic "session expired"

That is stronger than:

- screen owns token refresh
- screen assumes role flag means refund is allowed
- every failure redirects to login

---

## 6. Strong Default

For Flutter mobile clients:

- keep auth and API concerns in dedicated client or repository layers
- treat the app as a public client
- model auth failure, forbidden action, and transient network failure separately
- keep backend authorization and workflow checks server-side

---

## 7. Big Traps

1. **Putting auth protocol logic in screens**
   Example: expiry handling gets duplicated everywhere.

2. **Treating local role flags as real authorization**
   Example: UI assumption leaks into security thinking.

3. **Using one failure path for all protected API errors**
   Example: `401`, `403`, and timeout all look like "go to login".

4. **Hardcoding secrets into the app package**
   Example: mobile client is treated like a confidential server.

5. **Letting token state leak through every feature**
   Example: auth concern overwhelms feature code.

---

## 8. Practical Summary

Good short answer:

> In Flutter, I treat the app as a public client. The client should handle session UX, token storage, and protected API calling consistently, but the backend still owns authorization and critical workflow rules. I keep auth-aware transport near the client layer instead of scattering it across screens.
