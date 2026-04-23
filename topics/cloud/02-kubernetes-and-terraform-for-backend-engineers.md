# Kubernetes and Terraform for Backend Engineers

> Primary fit: `Platform / Growth lane`


This document is about practical minimums.

You do not need to become a platform engineer overnight.

You do need to understand what these tools are, why teams use them, and what a
backend engineer should be able to discuss in real projects.

---

## First: Docker vs Kubernetes vs Terraform

These tools solve different problems.

### Docker

Docker packages an application and its runtime into an image, then runs it as a
container.

Use it for:

- local reproducibility
- packaging services for CI/CD
- running the same artifact in dev, staging, and production

### Kubernetes

Kubernetes is a container orchestrator.

It does not replace Docker's packaging role. It solves the next problem:

- where containers run
- how many replicas run
- how traffic reaches them
- how unhealthy instances are replaced
- how services scale

### Terraform

Terraform is Infrastructure as Code (IaC).

It defines infrastructure in versioned files instead of manual console clicks.

Use it for:

- creating and updating cloud resources repeatably
- reviewing infrastructure changes in pull requests
- keeping environments consistent
- reducing drift between what should exist and what actually exists

In one line:

- Docker packages the app
- Kubernetes runs and manages the app
- Terraform provisions the infrastructure around the app

---

## What Kubernetes Is For

Kubernetes manages containerized applications across a cluster of machines.

It matters when you need:

- multiple replicas
- rolling deployments
- service discovery
- internal load balancing
- auto-healing
- autoscaling

If all you have is one container on one VM, Kubernetes may be unnecessary.

If you have several services, different environments, and production traffic,
Kubernetes becomes much more relevant.

---

## Kubernetes Minimums You Should Learn

### 1. Pod

The smallest deployable unit.

Usually one app container, sometimes with a sidecar.

Important idea:

- Pods are ephemeral
- Pod IPs change
- never treat a Pod like a pet server

### 2. Deployment

Defines the desired number of replicas and rollout behavior.

Know what it gives you:

- declarative desired state
- rolling updates
- self-healing replacement of failed Pods

### 3. Service

A stable internal address in front of Pods.

Know why it exists:

- Pods change
- callers need stable DNS and load balancing

### 4. Ingress and Gateway API

This is the public entry point for HTTP traffic into the cluster.

Historically, teams modeled this with `Ingress`.

Today, the more modern direction is `Gateway API`, which gives clearer roles and
more expressive routing resources such as:

- `Gateway`
- `HTTPRoute`

For backend engineers, the important thing is understanding the edge role:

- host/path routing
- TLS termination
- public access boundary

For local labs, a practical default is:

- `Traefik` as the controller
- `Gateway API` as the routing model

### 5. ConfigMaps and Secrets

How applications receive configuration without baking it into the image.

For backend engineers, this connects directly to:

- Spring profiles
- environment variables
- externalized config
- secret injection

### 6. Liveness and Readiness Probes

This is one of the most important practical topics.

Know the difference:

- liveness: restart the app if it is dead
- readiness: stop sending traffic if it is temporarily not ready

### 7. Requests and Limits

These define resource expectations and safety boundaries.

Know the practical effect:

- requests affect scheduling
- limits prevent noisy-neighbor problems
- bad memory limits cause OOMKills

### 8. Horizontal Pod Autoscaler (HPA)

Know what it does:

- scales replica count based on metrics

Know its limitation:

- it reacts, it does not predict
- sudden traffic spikes still need sane defaults or pre-scaling

---

## Terraform Minimums You Should Learn

### 1. What It Manages

Terraform manages infrastructure resources such as:

- networks
- databases
- caches
- load balancers
- container services
- buckets
- IAM roles and policies

### 2. Desired State

You declare what you want.

Terraform compares declared state to real state and computes a plan.

That is why the usual workflow is:

- `terraform plan`
- review
- `terraform apply`

### 3. State File

Terraform keeps a state file that records what it manages.

This is a core concept.

You do not need to master remote backends immediately, but you should know:

- state matters
- teams store it centrally
- state corruption or drift is a real operational concern

### 4. Variables, Outputs, and Modules

Know these conceptually:

- variables parameterize infrastructure
- outputs expose useful values
- modules package reusable infrastructure building blocks

### 5. Resources vs Data Sources

This is a common basic distinction:

- resource: Terraform creates or manages it
- data source: Terraform reads something that already exists

### 6. Why Teams Use It

The value is not "writing infra in code because code is cool."

The value is:

- repeatability
- reviewability
- environment consistency
- safer change management

---

## How These Tools Fit Together

A simple mental model:

1. You build your Spring Boot service into a Docker image.
2. CI pushes the image to a registry.
3. Kubernetes runs that image with replicas, probes, services, and edge routing.
4. Terraform provisions the infrastructure Kubernetes or the service needs:
   cluster, load balancer, database, cache, secrets integration, IAM, networking.

That is the modern backend runtime chain.

---

## What You Do Not Need Yet

Do not go deep on these immediately:

- advanced Kubernetes networking
- custom resource definitions
- operators
- Helm internals
- advanced Terraform module design
- large multi-account cloud governance

That is not the right starting point for your current goal.

---

## Suggested Learning Order

For your profile, the best order is:

1. Docker fundamentals and local runtime discipline
2. Kubernetes basics: Pod, Deployment, Service, edge routing, probes
3. requests/limits and autoscaling basics
4. Terraform core concepts: state, plan/apply, modules, variables
5. map all of that to AWS later

The point is to understand the runtime model first, then the provider-specific
implementation.

---

## Practical Summary

You can say something like:

> I am strongest on backend application design, but I make sure I understand how
> services actually run in production. For me, Docker is the packaging layer,
> Kubernetes is the orchestration layer, and Terraform is the infrastructure
> definition layer. I do not need to be a platform specialist to reason clearly
> about probes, scaling, config, secrets, and the deploy path from code to
> production.

---

## Further Reading

- Kubernetes concepts:
  https://kubernetes.io/docs/concepts/
- Kubernetes workload basics:
  https://kubernetes.io/docs/tutorials/kubernetes-basics/
- Terraform intro:
  https://developer.hashicorp.com/terraform/intro
- Terraform language overview:
  https://developer.hashicorp.com/terraform/language
- Kubernetes Gateway API:
  https://kubernetes.io/docs/concepts/services-networking/gateway/
- Traefik Gateway API provider:
  https://doc.traefik.io/traefik/reference/install-configuration/providers/kubernetes/kubernetes-gateway/
- Docker overview:
  https://docs.docker.com/get-started/docker-overview/
