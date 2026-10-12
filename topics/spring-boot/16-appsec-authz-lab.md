# AppSec Lab: Object-Level Authorization

Build `GET /api/orders/{orderId}` in a Spring API. The order owner and a support agent may read it; another authenticated user may not. Use a real authenticated principal, not an identity supplied by an untrusted request header. This exercise is about the decision at the business boundary. See [web and API security](../security/02-web-and-api-security.md).

## Tasks

1. Model the principal with a stable user ID and roles.
2. Load the requested order and enforce the owner-or-support rule before returning its data.
3. Decide how unknown and forbidden resources appear to callers and apply that policy consistently.
4. Add a refund action that needs both an authorized support role and a valid order state.
5. Test through the HTTP security path, including authentication and authorization.

## Solution guide

```text
principal = authenticate(request)
order = findOrder(orderId) or return chosen not-found response
if principal.id != order.ownerId and SUPPORT not in principal.roles:
    deny access
return order view
```

Keep the object-level rule in a testable service or authorization component. Route protection alone cannot decide access to a particular order. The refund adds a separate workflow rule: an authorized caller still cannot refund an ineligible order.

Check owner `200`, other user denied, support `200`, missing order, unauthenticated request, and a support refund against an ineligible state. If you use method security, make sure the rule is actually invoked through Spring's managed boundary.
