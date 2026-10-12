# Choosing an Edge Layer

An edge component sits between a client and application services. Choose it
from the job it must do: route traffic, apply shared public policy, adapt an
API for a client, or manage internal service traffic. These jobs can share an
implementation, but they need clear ownership.

## Name the Job First

| Need | Usual fit | Main cost |
| --- | --- | --- |
| Send traffic to available application instances | Load balancer or reverse proxy | Another hop and routing configuration |
| Apply common public routing, authentication checks, or quotas across APIs | API gateway | Shared policy and configuration become operational dependencies |
| Combine and shape data for one client experience | Backend for frontend (BFF) | Client-specific code and another contract to maintain |
| Apply common traffic policy between internal services | Service mesh | Platform complexity across the runtime |

A reverse proxy forwards requests and may balance them across instances. A
gateway adds shared public API policy. A BFF implements client-facing
composition, such as collecting order, delivery, and loyalty data for a
mobile screen. A mesh addresses traffic between services, not the public
response shape.

One backend behind a proxy may need no gateway product. One gateway can serve
many APIs without a BFF. A BFF can be a module in an existing backend rather
than a new deployable service.

## Decide Whether a BFF Earns Its Place

Suppose web and mobile both use the same order API. Keep one contract while
their needs are close. If mobile later needs a compact checkout response from
several owners and repeated client calls cause real latency or complexity,
give that composition one backend owner. Keep the domain services responsible
for order, payment, and inventory truth.

Before adding a BFF, check whether a simpler endpoint in the existing API
solves the need. A BFF that only forwards requests adds a hop without useful
adaptation. A BFF that hides an excessive chain of poorly chosen service
boundaries may only move the complexity closer to the client.

## Put Each Rule Where It Can Be Enforced

| Rule | Edge responsibility | Application responsibility |
| --- | --- | --- |
| Authentication | Reject missing or invalid credentials where configured | Use a trusted identity and protect direct/internal paths |
| Authorization | Enforce coarse route access | Check the action, object, tenant, and workflow state |
| Traffic limits | Bound public request size and caller rate | Bound expensive business work and shared resources |
| Validation | Reject malformed transport input | Enforce business invariants |

Do not treat a passed gateway check as permission to perform every operation.
The service that owns a business action must authorize it. See [API
design](../api/01-api-design.md) and [web and API
security](../security/02-web-and-api-security.md) for those contracts.

## Small Decision Sequence

1. Draw the real client-to-service path and identify who owns each hop.
2. Start with routing and load balancing already provided by the runtime.
3. Add a gateway when shared public policy across APIs is causing repeated
   work or inconsistent enforcement.
4. Add client-specific composition only when a client has a distinct need
   that ordinary API endpoints do not serve well.
5. Consider a mesh only for a demonstrated internal traffic-policy problem
   that the team can operate.

If many teams need to publish routes and policies through one shared edge,
see [self-service edge platforms](./20-self-service-edge-platforms.md).

## Decision Rule

Choose the smallest edge that handles the observed traffic and client need.
Keep shared transport policy at the edge and business correctness with the
service that owns the action.
