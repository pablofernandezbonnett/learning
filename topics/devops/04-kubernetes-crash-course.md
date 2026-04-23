# Kubernetes Basics for Backend Engineers

> Primary fit: `Platform / Growth lane`


You do not need to be a Kubernetes operator.
You do need a simple mental model for what Kubernetes solves once "run one Docker
container" is no longer enough.

This is mainly useful when the conversation pushes on deployment, availability, scaling,
and traffic routing for containerized services.

---

## What is Kubernetes?
Docker lets you run one application container.
Kubernetes helps when you need many copies, health management, service discovery, and
controlled rollout across a fleet.

That is **Container Orchestration**. Kubernetes (K8s) is the tool that automates the deployment, scaling, and management of containerized applications.

---

## The 4 Core Concepts

### 1. Pods (The smallest unit of compute)
*   Kubernetes doesn't run Docker containers directly. It runs **Pods**.
*   A Pod is slightly larger than a container. It usually contains *one* Docker container (e.g., your Spring Boot app), but it can contain multiple tightly coupled containers that share the same network IP and storage volume (e.g., your app + a logging agent "sidecar").
*   *Key trait:* Pods are ephemeral. They die, they get recreated, and their IP addresses change constantly. **Never rely on a Pod's IP address.**

### 2. Deployments (The Blueprint)
*   You almost never create a Pod manually. You create a **Deployment**.
*   A Deployment is a YAML file where you declare your desired state: *"I want 3 replicas of my User Service container running at all times, using version 2.0 of my image."*
*   Kubernetes constantly monitors this. If a physical server crashes and takes 1 Pod with it, the Deployment Controller notices the count dropped to 2, and instantly spins up a new Pod on a healthy server to maintain the desired state of 3.
*   *Zero-Downtime:* Deployments handle Rolling Updates automatically. When you upgrade to version 2.1, it spins up a new 2.1 Pod, kills an old 2.0 Pod, and repeats until the rollout is complete.

### 3. Services (The Internal Address)
*   Since Pods die and their IP addresses change constantly, how does the `Order Service` talk to the `User Service`?
*   It talks to a **Service**.
*   A Service is a stable, permanent IP address and DNS name (e.g., `http://user-service:8080`) that acts as an internal Load Balancer. It receives traffic and forwards it to the healthy Pods that back it.
*   Even if all the User Service Pods are destroyed and recreated, the `Service` IP/DNS remains completely unchanged.

### 4. Ingress / Gateway (The Public Door)
*   By default, everything in Kubernetes is private and completely inaccessible from the public internet.
*   Historically, teams used **Ingress** for this. The newer Kubernetes direction is **Gateway API**.
*   In both cases, the job is the same: an intelligent Layer 7 edge that routes outside traffic to the correct internal services.
*   *Example:*
    *   Traffic to `api.myapp.com/users` -> `User Service`
    *   Traffic to `api.myapp.com/orders` -> `Order Service`
*   For local labs, `Traefik` is a practical modern controller choice.

---

## Practical Summary

Good short answer:

> If we are already containerized and need replica management, rolling updates,
> internal service discovery, and health-aware traffic routing, Kubernetes is a strong fit.
> I think first in terms of Deployments, Services, probes, and controlled rollout,
> not in terms of raw cluster internals.

---

## 5. Health Probes — Liveness vs Readiness

Without probes, Kubernetes sends traffic to Pods before they finish startup (Spring Boot
takes 10–30 seconds to load context) and never restarts them when they get stuck.

**Liveness Probe:** "Is this Pod alive, or should K8s restart it?"
- If it fails, K8s kills the Pod and starts a fresh one.
- Use case: detect infinite loops, deadlocks, memory leaks that cause the app to hang.
- Use a **shallow** check (just HTTP 200 from the app itself — no DB or Redis calls).

**Readiness Probe:** "Is this Pod ready to receive traffic?"
- If it fails, K8s removes the Pod from the Service load balancer — traffic stops.
- Use case: startup not complete, downstream dependency unavailable, overloaded.
- Use a **deep** check (DB connection, Redis, required external dependencies).

```yaml
# Spring Boot Actuator exposes these endpoints automatically
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30   # Spring Boot startup buffer
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
  failureThreshold: 3       # removed from LB after 3 consecutive failures
```

**Practical tip:** "We never use the same endpoint for liveness and readiness. A liveness
failure should restart the Pod. A readiness failure should only take it out of rotation —
for example during a temporary DB blip, we don't want to restart all Pods, just stop
sending them traffic until the connection recovers."

---

## 6. Resource Requests and Limits

Without these, one misbehaving Pod can starve neighbors of CPU or memory on the same node.

- **requests:** Guaranteed minimum. K8s uses this to decide which node to schedule the Pod on.
- **limits:** Hard ceiling. Exceed memory limit → Pod is OOMKilled. Exceed CPU limit → throttled.

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"      # 250 millicores = 0.25 CPU core
  limits:
    memory: "1Gi"
    cpu: "1000m"     # 1 full CPU core
```

**Rule:** Always set both. Start with `requests.memory = 512Mi`, `limits.memory = 1Gi` for
a Spring Boot service. Monitor actual usage with `kubectl top pods` and tune. Limits set
too low cause OOMKills on normal traffic spikes.

---

## 7. Horizontal Pod Autoscaler (HPA)

HPA automatically scales replicas based on a metric — CPU utilization by default, or custom
metrics (RPS, queue depth).

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  scaleTargetRef:
    kind: Deployment
    name: checkout-service
  minReplicas: 2
  maxReplicas: 20
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70    # scale up when average CPU > 70%
```

HPA checks metrics every 15 seconds. Scale-up takes 30–60 seconds (Pod startup +
readiness probe passing). **You cannot rely on HPA alone for instant traffic spikes.**

**Flash sale pattern:**
Pre-scale manually the night before (`kubectl scale deployment checkout --replicas=15`).
HPA then handles real-time adjustments during the event. The `minReplicas` acts as the
floor — HPA will never scale below it.

**Practical framing:**
"We define resource requests on every Deployment so the scheduler can place Pods correctly,
and limits to prevent noisy-neighbor problems. HPA manages auto-scaling on CPU — but for
predictable spikes like a product launch, we pre-scale manually and rely on readiness probes
to ensure traffic only reaches Pods that are genuinely ready."
