# Local Kubernetes Lab with kind or k3d and Terraform

> Primary fit: `Platform / Growth lane`


This document turns the cloud path into a practical lab.

The goal is not to simulate all of AWS locally.

The goal is to build a small but realistic deployment flow:

- package services with Docker
- run them in a local Kubernetes cluster
- provision Kubernetes resources with Terraform
- expose the system through a real cluster edge

---

## Why kind or k3d

For a backend engineer learning locally, `kind` and `k3d` are both good choices.

Why they are good:

- lightweight
- fast startup
- good enough for local cluster learning
- easier to treat as disposable lab environments

Practical rule:

- `kind`: closer to upstream Kubernetes, simple mental model
- `k3d`: very fast, lightweight, convenient local k3s workflow

You do not need both. Pick one and stick with it for the lab.

If your main goal is learning Kubernetes concepts and Terraform flow, either is
fine.

---

## What The Lab Should Teach You

At minimum, your lab should force you to understand:

- image building
- container registry or image loading into the cluster
- Deployment, Service, and edge routing
- ConfigMap and Secret usage
- readiness and liveness probes
- internal service-to-service networking
- how Terraform creates and updates Kubernetes resources

That is already a very good lab.

---

## Use Terraform, Not Manual kubectl

If the learning goal includes infrastructure and platform thinking, do not stop
at:

- `kubectl apply -f deployment.yaml`

That is still useful, but it misses the Infrastructure as Code part.

A better lab is:

- Terraform creates the namespace
- Terraform creates the Deployment
- Terraform creates the Service
- Terraform creates the edge routing resources

This teaches a more realistic workflow:

- declarative infra
- plan/review/apply mindset
- repeatable local environments

---

## Recommended Lab Shape

Do not start with five services unless you really want the complexity.

A sensible progression is:

### Phase 1

- one backend service
- one database
- one public route

### Phase 2

- add a gateway or edge service
- add a second backend service
- add service-to-service communication

### Phase 3

- expand to your three backend systems plus the gateway
- introduce shared concerns like config, tracing headers, and failure handling

This reduces the chance of turning the lab into platform chaos too early.

---

## Edge Recommendation

Yes, using a real cluster edge controller is the right move.

For this lab, prefer:

- `Traefik`
- `Gateway API`

Why this is the better default now:

- `ingress-nginx` is in retirement
- `Gateway API` is the modern Kubernetes direction
- `Traefik` is a practical local choice, especially in `k3d` / `k3s` style setups

Why it matters:

- you learn the public entry point concept
- you route traffic by host/path
- you see the difference between internal `Service` access and external traffic

Your local browser flow can then look like:

- `http://localhost/api/...` -> Traefik -> Gateway/HTTPRoute -> internal services

That is much closer to a real deployment shape than port-forwarding every
service separately.

---

## Image Flow in Local Clusters

One of the first surprises in local Kubernetes is:

- your Docker image existing on the host does not automatically mean the cluster
  can use it

Depending on the setup, you may need to:

- load images into `kind`
- use the local registry pattern
- configure `imagePullPolicy` carefully

This is a good learning moment because it forces you to understand that the
cluster runtime is a separate environment.

---

## Terraform Minimum For This Lab

You do not need advanced Terraform.

You do need enough to model Kubernetes resources clearly.

Minimum concepts:

- provider configuration
- variables
- resources
- outputs
- `plan` and `apply`

If you want a clean first milestone, aim for Terraform-managed:

- namespace
- config map
- secret
- deployment
- service
- `Gateway` / `HTTPRoute`

That is enough.

---

## Suggested Project Requirements

If you want the lab to be meaningful, give yourself these rules:

1. Every service runs from a Docker image built with a multi-stage Dockerfile.
2. Every app container runs as non-root.
3. The database uses a volume for persistence.
4. All Kubernetes resources are created through Terraform.
5. The gateway is reachable through Traefik and a real routing resource, not
   just direct service ports.
6. At least one service exposes readiness and liveness health endpoints.

That already teaches the right habits.

---

## How This Connects to Your Background

This kind of lab fits your profile well because it builds on things you already
know:

- Spring Boot services
- gateway boundaries
- databases and caches
- integrations and runtime behavior

What is new is the deployment and infrastructure layer around them.

That makes it a strong expansion path without forcing a full stack reset.

---

## Interview Framing

You can later describe it like this:

> I built a local cloud lab to close the gap between backend service
> implementation and runtime operations. The goal was not to imitate AWS in
> detail, but to practice the deploy chain end-to-end: Docker packaging, local
> Kubernetes orchestration, Terraform-managed resources, Traefik/Gateway API
> routing, and production-style health and configuration patterns.

---

## Further Reading

- kind:
  https://kind.sigs.k8s.io/
- k3d:
  https://k3d.io/
- Kubernetes Gateway API:
  https://kubernetes.io/docs/concepts/services-networking/gateway/
- Traefik Gateway API provider:
  https://doc.traefik.io/traefik/reference/install-configuration/providers/kubernetes/kubernetes-gateway/
- Terraform Kubernetes provider:
  https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs
