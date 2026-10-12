# Layout, Navigation, and Async UI

A screen must remain understandable when space is tight, data is late, or an action fails. Treat layout, route changes, and loading as parts of the same user flow.

## Layout follows constraints

Check a small viewport, long or translated text, a large text scale, keyboard appearance, safe areas, and long lists. If content can grow beyond the available space, plan scrolling or flexible layout. A screenshot with one short payload cannot prove the layout adapts.

## Navigation follows outcomes

Name the flow before wiring routes: what happens on success, cancel, back, deep link, and expired session? Let a feature outcome cause navigation in one visible place. Avoid multiple widgets independently changing the same route stack.

## Show the state users need

| Situation | Useful UI response |
| --- | --- |
| First load | Placeholder or local progress while content is unavailable |
| Background refresh | Keep usable data visible and show subtle progress |
| Empty result | Explain that there is no data, rather than showing an error |
| Recoverable read failure | Keep permitted stale data and offer retry |
| Write in progress | Disable repeat submission and show progress near the action |
| Write outcome unknown | Do not claim success or failure; show pending/recovery behavior tied to the API contract |

Do not turn every delay into a full-screen spinner. A failed refresh and a failed payment need different treatment because one may preserve useful content while the other may leave an unknown business outcome.

## Respect widget lifecycle

Build can run repeatedly; start one-time work from an owner with the right lifecycle, not directly from `build`. Dispose screen-owned controllers and subscriptions. After an asynchronous gap, check that the context is still mounted before using it for navigation or UI changes:

```dart
final result = await checkout.placeOrder();
if (!context.mounted) return;
showResult(context, result);
```

This only protects the widget from acting after disposal. It does not cancel the request or decide what a late server result means; the feature must own that behavior.

## Review check

- Does the screen work with constrained space and realistic content?
- Can users tell empty, loading, stale, failed, and pending apart?
- Is retry safe for a write that may already have reached the server?
- Who owns navigation and cleanup after the screen disappears?

Further reading: [Flutter layout constraints](https://docs.flutter.dev/ui/layout/constraints), [BuildContext.mounted](https://api.flutter.dev/flutter/widgets/BuildContext/mounted.html).
