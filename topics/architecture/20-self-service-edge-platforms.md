# Self-Service Edge Platforms and Programmable Control Planes

> Primary fit: `Shared core / Platform / Cloud`

Use this topic when the architecture discussion sounds like:

- platform team is becoming a ticket bottleneck for ingress, routing, certificates, or public exposure
- every service team keeps re-implementing auth, rate limits, logging, or request policy differently
- we want self-service without giving every team raw control over dangerous edge infrastructure
- we need to centralize repeated edge behaviour without centralizing every product decision

This is a case-study pattern topic.
It is inspired by public high-level descriptions of Atlassian infrastructure
work discussed by former engineer Vasilios Syrakis, but the goal here is not to
copy one company's exact stack.
The goal is to understand the reusable architecture idea.

---

## Why This Matters

Many growing backend organizations hit the same problem:

- product teams want to ship faster
- platform teams want safety and consistency
- the public edge is too important to leave unmanaged
- manual tickets and one-off configuration do not scale

If every team needs a platform engineer to expose a route, attach auth, add a
certificate, or publish a service, the platform becomes a human bottleneck.

If every team gets broad direct control instead, the edge becomes inconsistent
and fragile.

This topic exists because the interesting design move is not "buy a better load
balancer."

It is:

> expose safe self-service intent while keeping the dangerous infrastructure
> logic centralized and governed

---

## Smallest Useful Mental Model

Separate these pieces:

- **data plane**: the proxies or edge instances that actually handle traffic
- **control plane**: the system that decides and distributes how those edge nodes should behave
- **intent interface**: the narrow API or config shape product teams are allowed to submit

Short rule:

> the important product is often the control plane, not the proxies

Small concrete example:

- team wants `api.example.com/payments` exposed publicly
- they provide constrained inputs such as route, service target, auth mode, and rate-limit class
- the platform validates those inputs
- the control plane generates safe runtime config
- edge proxies receive the config dynamically

The team gets self-service.
The platform keeps governance.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- this is mainly a load-balancer problem
- let teams configure the edge directly if they are senior enough
- the platform should expose all underlying power for flexibility

Better mental model:

- this is mainly a governance and safe-abstraction problem
- teams should express constrained intent, not raw infrastructure behavior
- the control plane should centralize the repeated dangerous parts once

Bad mental model:

- centralizing edge concerns means building a giant magic gateway that owns everything

Better mental model:

- centralize repeated cross-cutting concerns at the edge, but keep product-specific business decisions inside services

Small concrete example:

- weak approach: each service team writes its own edge auth, rate limiting, request logging, and certificate setup
- better approach: one platform layer handles shared edge behaviour, while service teams still own their domain APIs and business logic

---

## 1. The Real Problem Behind "We Need A Gateway"

The initial request often sounds simple:

- teams want public ingress
- teams want DNS and certificates
- teams want routing
- teams want it without opening tickets every time

But the deeper platform problem is:

- how do you let many teams move fast without letting them break each other or weaken global security?

That is why a self-service edge platform is not only a networking solution.
It is an organizational scaling solution.

---

## 2. The Reusable Architecture Pattern

The pattern usually looks like this:

```text
Developer or service team
  -> constrained self-service request
  -> platform API or broker
  -> async provisioning and validation
  -> control plane config synthesis
  -> edge runtime distribution
  -> proxies or edge nodes enforce the behavior
```

Important parts:

- teams do not hand-write raw proxy config
- provisioning work is asynchronous
- runtime behaviour is synthesized from templates and policy
- edge nodes stay relatively generic
- behavioural intelligence lives higher up in the control plane

This is the same broad architectural move you see in other mature systems:

- Kubernetes
- service meshes
- policy engines
- internal platform APIs

The edge nodes become replaceable workers.
The control plane becomes the real product.

---

## 3. What Should Be Centralized At The Edge

Good candidates:

- authentication
- coarse authorization or policy checks
- rate limiting and quotas
- request size limits
- TLS, certificates, and public ingress policy
- standard logging, tracing, and correlation
- coarse routing and exposure rules

Why this helps:

- duplicated effort falls
- security becomes more consistent
- product teams move faster
- operational behaviour becomes easier to reason about

Short rule:

> centralize repeated transport and trust concerns, not product-specific business logic

---

## 4. What Should Not Be Centralized

Do not treat the edge as the new home for every kind of logic.

Bad candidates:

- domain-specific pricing rules
- order workflow state transitions
- transaction coordination
- rich domain authorization that depends on deep business context
- service-specific business composition that really belongs in a BFF or backend service

Weak approach:

- "the edge already sees every request, so it can decide everything"

Better approach:

- "the edge should enforce shared trust and traffic policy, while domain services still own business correctness"

---

## 5. Why The Intent Interface Matters

The safest version of this pattern does not expose raw power.

It exposes constrained intent.

Good self-service inputs:

- hostname or path
- service target
- auth mode chosen from an allow list
- rate-limit class chosen from an allow list
- exposure type such as internal-only, partner, or public

Bad self-service inputs:

- arbitrary script execution at the edge
- raw proxy config
- unrestricted policy hooks
- custom network behavior with no review path

Why this matters:

- a platform succeeds when teams can ask for common capabilities safely
- a platform fails when every team can express unsafe or unreviewable edge behavior

Practical rule:

> self-service should expose safe intent, not raw infrastructural power

---

## 6. Async Provisioning Is Part Of The Design

Provisioning public exposure, certificates, policy, or infrastructure changes is
not the same thing as serving a request.

Keep those concerns separate.

Strong default:

- request path stays fast
- provisioning path is async
- provisioning state is explicit
- failures and retries are visible

This matters because platform teams often accidentally mix:

- one-off resource creation
- long-running validation
- live request handling

That makes the operational model much harder to reason about.

One subtle but important caution:

- dynamic control planes are not magic instant truth
- config distribution is usually eventually consistent
- rollout ordering, validation, and rollback discipline still matter

Practical consequence:

- do not assume every edge node sees every change at exactly the same moment
- design config rollout so temporary skew does not create avoidable blackholes or policy surprises

---

## 7. Governance Is The Hard Part

This pattern sounds like infrastructure, but the real difficulty is governance.

The platform must answer:

- who is allowed to expose a service publicly
- what default auth is required
- how dangerous changes are reviewed
- how config rollout and rollback work
- how blast radius stays bounded
- how platform policy evolves without breaking hundreds of teams

That is why this architecture is not "just automation."

It is:

- policy management
- abstraction management
- safe multi-team coordination

Plain-English version:

> the hard part is not moving packets; it is exposing power safely to many teams at once

---

## 8. The Good Part Of Centralization

When this works well, centralization gives real leverage:

- one implementation of auth instead of many
- one place for edge telemetry
- one policy rollout path
- one consistency layer for rate limits and request governance
- fewer team-by-team security surprises

This is why the design is attractive.
It compounds platform capability over time.

---

## 9. The Risk Of Centralization

Centralization can also fail badly.

Failure modes:

- platform team becomes a harder bottleneck than before
- the control plane becomes too magical to debug
- product teams lose autonomy for changes that are not actually risky
- one bad global config impacts too many services
- edge policy becomes a dumping ground for logic that should live elsewhere

Strong default:

- centralize only repeated high-value concerns
- keep config models small and explainable
- make rollback easy
- keep blast radius visible
- preserve service ownership where the platform should not intrude

Good engineer detail:

- prefer progressive rollout of edge config where possible
- keep the last known-good version easy to re-activate
- validate synthesized config before distribution, not only after incidents

---

## 10. A Small Reusable Case Study

Imagine a growing SaaS company with 120 internal services.

Current pain:

- teams open tickets for public routes
- auth is inconsistent
- some services forget rate limits
- certificates and DNS are managed by too few people
- incident debugging is harder because edge behavior is inconsistent

### Weak response

- give each team direct access to edge config and let them manage their own exposure

Why this is weak:

- fast locally
- inconsistent globally
- security and operations drift quickly

### Better response

- create a platform API where teams request exposure through constrained fields
- validate inputs
- provision infrastructure asynchronously
- generate edge config centrally
- distribute runtime behavior to generic proxies
- enforce shared auth, logging, and rate-limit policy once

Why this is better:

- teams move faster without raw edge access
- security and observability become consistent
- the platform solves a repeated organization-wide problem once

---

## 11. What To Reuse From This Case

Do not over-copy the stack.

Reuse these ideas instead:

- control plane vs data plane separation
- constrained intent instead of raw config
- async provisioning instead of synchronous ticket work
- edge centralization for repeated trust and transport concerns
- policy and rollout as first-class design concerns

Do not infer:

- every company needs Envoy
- every company needs a custom broker
- every company needs a full internal platform before they have real scale pain

The reusable lesson is the pattern, not the brand names.

---

## 12. Where This Fits In The Repo

Use this together with:

- [17-gateway-vs-bff-vs-edge-patterns.md](./17-gateway-vs-bff-vs-edge-patterns.md)
- [11-service-discovery.md](./11-service-discovery.md)
- [02-resiliency-patterns.md](./02-resiliency-patterns.md)
- [05-distributed-tracing.md](./05-distributed-tracing.md)
- [../security/02-web-and-api-security.md](../security/02-web-and-api-security.md)
- [../devops/03-observability-and-monitoring.md](../devops/03-observability-and-monitoring.md)

---

## 13. Takeaway

The useful architecture lesson here is not "build your own hyperscale edge."

It is:

> when many teams need the same dangerous infrastructure capability, the right
> move is often to centralize the risky behaviour in a control plane while
> exposing only safe self-service intent to the teams consuming it

## Further Reading

- [Envoy official: What is Envoy?](https://www.envoyproxy.io/docs/envoy/latest/intro/what_is_envoy)
- [Envoy official: architecture overview](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/arch_overview)
- [Envoy official: xDS protocol](https://www.envoyproxy.io/docs/envoy/latest/api-docs/xds_protocol.html)
- [Open Service Broker API](https://www.openservicebrokerapi.org/)
- [Vasilios Syrakis video summary on Podwise](https://podwise.ai/episodes/7982214)
- [Analysis of the public architecture discussion](https://horkan.com/2026/05/23/the-real-infrastructure-was-not-the-load-balancer-what-atlassians-envoy-platform-really-built)
- [Press summary in Xataka](https://www.xataka.com/empresas-y-economia/ingeniero-atlassian-fue-despedido-acto-seguido-publico-video-youtube-explicando-como-funciona-empresa/amp)
