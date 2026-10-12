# Flutter Architecture and State Ownership

Start with the flow a user needs, then decide who owns each state change and external call. A state-management package can carry that design, but choosing the package does not define the design.

## A small feature split

| Part | Owns | Keep out |
| --- | --- | --- |
| View or widget | Rendering, input, local visual state | HTTP, raw JSON, token refresh, business decisions |
| Feature state/controller | Loading, success, error, and actions for one flow | Transport details and platform plugin calls |
| Repository | Data the feature uses, mapping and freshness rules | Widget navigation |
| Service/client | HTTP, storage, or plugin interaction | Screen-specific display decisions |

For example, a checkout page shows the current state and sends `placeOrder` to its feature controller. The controller prevents duplicate taps and records the pending or final outcome. A repository calls the API and maps its response. The backend remains the source of truth for whether the order was accepted.

## Put state near its owner

- Widget-local: a selected tab, focus, or an animation.
- Feature-local: checkout progress, order history, or a retryable fetch.
- Shared: session and other facts several features truly need.

Move state upward only when another feature needs the same fact. A single global object for every screen makes change impact hard to see.

Group code by feature when it helps people find one flow together. Add shared `core` code only for a genuinely shared contract or service:

```text
lib/
  features/
    checkout/
      checkout_page.dart
      checkout_controller.dart
      checkout_repository.dart
  core/
    api_client.dart
```

The exact folders are less important than the dependency direction: widgets use feature behavior; feature behavior uses data boundaries; transport and platform details stay at the edge.

## Decide what the UI model needs

An API response may contain more fields than one screen needs. Map it near the data boundary to a typed model the feature can use. Keep raw maps and transport errors out of widgets. See [Dart data boundaries](../dart/03-tooling-json-and-boundaries.md) for parsing details.

Before adding a new layer, ask what change it isolates. If it only forwards one call and has no rule, it may add ceremony. If it owns mapping, caching, retry, or a business decision, make that responsibility explicit.

## Review check

- Who owns the user's action and its result?
- Which state is local, feature-wide, or truly shared?
- Can the feature be tested with a fake data boundary?
- Would an API field change affect one adapter or many widgets?

Further reading: [Flutter app architecture](https://docs.flutter.dev/app-architecture/guide).
