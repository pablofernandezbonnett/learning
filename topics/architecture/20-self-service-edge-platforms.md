# Self-Service Edge Platforms

An edge platform becomes useful when many teams repeatedly need public routes,
certificates, and shared traffic policy, but every change depends on a
platform ticket or gives teams unrestricted access to shared infrastructure.
The platform's job is to make common changes safe and routine.

This note starts after the [edge-layer decision](./17-gateway-vs-bff-vs-edge-patterns.md).
It covers how teams request and operate shared edge capability, not whether
one application needs a gateway or BFF.

## Separate Intent From Traffic Handling

- The **data plane** handles live requests, such as proxying and enforcing an
  active route policy.
- The **control plane** validates desired routes and policies, distributes
  configuration, and records rollout state.
- The **intent interface** is the small form or API a service team uses to
  request an allowed change.

Example intent: “Expose `/orders` to partners, route it to the Orders service,
require partner authentication, and apply the standard partner rate class.”
The team should not need to edit raw proxy configuration to express that
request. The platform translates it into runtime configuration.

## Design the Self-Service Contract

Useful fields include route owner, hostname or path, target service, exposure
class, approved authentication mode, and approved quota class. Validate that
the requester owns the target and that the route does not conflict with an
existing one. Make the default safe when a field is omitted.

Keep product-specific pricing, order state, object authorization, and response
composition in the owning application. The platform should expose shared
traffic and trust policy, not become the owner of every business rule.

## Treat Changes as a Lifecycle

```text
requested -> validated -> prepared -> rolled out -> active
requested -> rejected
rolled out -> rolled back
```

1. Validate intent and generated configuration before affecting live traffic.
2. Apply changes in a controlled rollout. Different edge instances may see
   the new configuration at different times.
3. Record the active version, rollout status, and owner so teams can inspect
   what is live.
4. Keep a known-good version and a clear rollback path. Test that old and new
   policy can coexist during a rollout.
5. Reconcile desired state with actual state; retry failed provisioning and
   surface changes that remain stuck.

Provisioning a route or certificate is separate from serving requests. It may
take time or fail partway through; a submitted request is not proof that the
new route is active.

## Keep the Shared Boundary Safe

- Limit which teams can expose a public service or change a shared hostname.
- Offer policy choices that are understandable and reviewable. Reserve an
  explicit review path for exceptions.
- Roll out risky global changes gradually and watch error and latency
  signals by route and owner.
- Make incident ownership clear: the platform owns routing and policy
  delivery; service teams own their API behavior and business effects.
- Measure whether self-service actually reduces waiting and repeated manual
  work. A custom control plane is costly to build and run.

For trace correlation and route-level diagnosis, see [distributed
tracing](./05-distributed-tracing.md). For application-level authorization,
see [web and API security](../security/02-web-and-api-security.md).

## Decision Rule

Build a shared self-service edge only when repeated cross-team needs justify
its control plane. Expose constrained intent, make rollout and rollback
visible, and leave domain decisions with the teams that own them.
