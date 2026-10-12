# Auth and API Behavior in Flutter

The app owns session experience and how API outcomes appear to users. The server owns authorization and business state. A hidden button or local role flag cannot grant or deny access to a protected resource.

For the threat model, token storage, OAuth flows, and deep-link risks, use [mobile AppSec](../security/08-mobile-appsec-basics.md). This note focuses on the Flutter flow built around those rules.

## Keep the transport decision in one place

An auth-aware client or repository should attach credentials, translate transport errors, and coordinate refresh according to the backend contract. A screen asks for an order; it should not construct auth headers or retry a token refresh itself.

Map distinct outcomes to distinct UI behavior:

| Outcome | App behavior |
| --- | --- |
| Session expired; refresh succeeds | Continue the request and preserve the current flow |
| Session cannot be restored | Clear session state and guide the user to login |
| Authenticated but forbidden | Remain in context and explain that the action is unavailable |
| Network unavailable | Keep safe local data when possible and offer a sensible retry |
| Write timed out with unknown result | Show pending state and reconcile before repeating the action |

These are example outcomes, not a universal HTTP status mapper. Use the actual API contract to decide when retry is safe.

## Example: an order detail screen

The screen asks the feature controller to load an order. The repository maps the API result. A session failure may redirect to login; a forbidden refund stays on the order screen with an action-specific message. A timeout after submitting a refund must not blindly resubmit a money-changing request.

The feature can hide an unavailable action for usability, but the backend still checks the caller and current order state. See [API design](../api/01-api-design.md) for explicit success, failure, and pending contracts.

## Review check

- Does one component own session refresh and credential attachment?
- Do login failure, forbidden action, and network failure look different?
- Can a repeated tap or automatic retry duplicate a business effect?
- Does navigation preserve enough context after re-authentication?
