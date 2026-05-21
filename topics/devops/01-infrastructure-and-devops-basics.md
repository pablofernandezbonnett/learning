# Infrastructure & DevOps Basics

You are not expected to be a Site Reliability Engineer (SRE), but you do need
to know how your code gets from your laptop to the cloud, and how it runs once
it is there.

Here are the bare minimum concepts you need to understand to comfortably discuss deployment and operations.

---

## Why This Matters

This topic matters because backend work does not stop at "the code compiles."

If you cannot explain how code is packaged, tested, released, and kept running,
you usually miss the practical costs of your own design choices:

- deploy risk
- rollback difficulty
- runtime drift between environments
- weak failure recovery after a node or pod dies

Good infrastructure basics are really about understanding the execution path
from commit to running service.

## Smallest Mental Model

The smallest useful production path is:

1. code is validated automatically
2. code is packaged with a predictable runtime
3. the artifact is promoted through environments
4. the platform keeps the desired number of instances alive
5. traffic is routed to healthy instances

That maps cleanly to:

- `CI` for validation
- container image for packaging
- registry for artifact storage
- orchestrator for runtime control
- gateway or load balancer for traffic entry

## Bad Mental Model vs Better Mental Model

Bad mental model:

- DevOps means "someone else runs the servers"
- Docker is just a command-line tool for local development
- Kubernetes is the main story, so the rest is less important

Better mental model:

- DevOps basics are about release safety, runtime consistency, and recovery
- containers solve packaging and environment parity, not deployment on their own
- orchestration only helps after CI, artifacts, health checks, and traffic flow are clear

Small concrete example:

- weak approach: build on a laptop, copy a `.jar` to a server, restart manually, and hope it works there too
- stronger approach: CI validates the change, builds one immutable image, stores it in a registry, and the orchestrator rolls it out to healthy instances

Interview-ready takeaway:

> I think of infrastructure basics as the path from commit to running service:
> validate automatically, package predictably, promote one artifact, keep
> healthy instances alive, and route traffic safely.

---

## 1. Containerization (Docker)

**The Problem:** "It works on my machine!" (But fails on the server because the server has Java 11 and you built it with Java 17).

**The Solution:** Docker packages your application code *along with* its runtime environment (Java, OS libraries, configurations) into a single, immutable artifact called an **Image**.

*   **Dockerfile:** The recipe. It says "Start with Ubuntu, install Java 17, copy my `.jar` file, and run it."
*   **Image:** The compiled result of the Dockerfile. It's read-only.
*   **Container:** A running instance of an Image. You can run 5 identical containers from 1 image.

**Practical point:** "We use Docker to ensure parity across environments. The exact same image tested in staging is promoted to production."

### Container vs Virtual Machine — How They Actually Work

This is one of the most common senior backend questions. The answer requires understanding
what is happening at the OS level.

**Virtual Machine (VM):**
```
┌─────────────────────────────────────────────────┐
│   App A   │   App B   │   App C                 │
├───────────┼───────────┼──────────               │
│  Guest OS │  Guest OS │  Guest OS   (full Linux) │
├─────────────────────────────────────────────────┤
│              Hypervisor (VMware, KVM)            │
├─────────────────────────────────────────────────┤
│              Host OS                            │
├─────────────────────────────────────────────────┤
│              Physical Hardware                  │
└─────────────────────────────────────────────────┘
```
Each VM runs a complete, separate OS kernel. Strong isolation. But each VM costs
~500MB–1GB of RAM just for the OS, and takes minutes to boot.

**Container:**
```
┌─────────────────────────────────────────────────┐
│   App A   │   App B   │   App C                 │
│ (own view │ (own view │ (own view               │
│  of FS,   │  of FS,   │  of FS,                 │
│  network) │  network) │  network)               │
├─────────────────────────────────────────────────┤
│              Docker Engine / containerd         │
├─────────────────────────────────────────────────┤
│              Host OS Kernel (shared!)           │
├─────────────────────────────────────────────────┤
│              Physical Hardware                  │
└─────────────────────────────────────────────────┘
```
Containers share the host OS kernel. Isolation comes from two Linux kernel features:

- **Namespaces:** Give each container its own isolated *view* of system resources.
  Each container sees its own process list (PID namespace), its own network interfaces
  (net namespace), its own filesystem root (mnt namespace). They are not actually
  separate — it is the same kernel, just with a restricted view.

- **cgroups (Control Groups):** Enforce *resource limits*. The kernel uses cgroups to
  say "container A gets at most 512MB RAM and 0.5 CPU cores." Without cgroups, one
  container could starve all others on the same host. This is exactly what Kubernetes
  `resources.limits` configures under the hood.

**The key difference in one sentence:**
A VM virtualizes the *hardware* (each VM has its own OS). A container virtualizes the
*OS* (each container has its own isolated view, but shares the kernel).

| | VM | Container |
|---|---|---|
| Startup time | Minutes | Milliseconds |
| Memory overhead | ~500MB per OS | ~10MB per container |
| Isolation | Full kernel separation | Namespace/cgroup isolation |
| Security boundary | Strong (separate kernel) | Weaker (shared kernel, escapes possible) |
| Portability | Tied to hypervisor | Runs anywhere Docker/containerd runs |

**Practical framing:** "Containers are more lightweight than VMs because they share the
host kernel — isolation is provided by Linux namespaces (process/network/filesystem view)
and cgroups (resource enforcement). The trade-off is a weaker security boundary: a
kernel exploit can theoretically break container isolation. For multi-tenant SaaS where
tenants run arbitrary code, we use VMs or firecracker-style microVMs. For our internal
Spring Boot services, containers are the right trade-off."

---

## 2. CI/CD (Continuous Integration / Continuous Deployment)

**The Problem:** Manually testing code and FTP-ing `.jar` files to servers is slow and error-prone.

**The Solution:** Automation pipelines (like GitHub Actions, GitLab CI, or Jenkins) that trigger every time you push code.

### The Pipeline Stages:

1.  **Continuous Integration (CI):**
    *   *Trigger:* You push a PR to GitHub.
    *   *Action:* A server pulls your code, compiles it, runs the Unit Tests, and runs the Linter.
    *   *Result:* If tests fail, the PR is blocked. This ensures the `main` branch is always green.
2.  **Continuous Delivery (CD):**
    *   *Trigger:* The PR is merged into `main`.
    *   *Action:* The pipeline builds the Docker Image, tags it with a version (e.g., `v1.2.0`), and pushes it to an Image Registry (like AWS ECR or Docker Hub).
3.  **Continuous Deployment (CD):**
    *   *Action:* The pipeline automatically tells the production servers to pull the new `v1.2.0` image and restart.

**Practical point:** "A solid CI/CD pipeline gives developers the confidence to deploy multiple times a day because we rely on automated tests to catch regressions before they hit production."

---

## 3. Orchestration (Kubernetes / K8s Basics)

**The Problem:** You have 50 microservices, each running in 3 Docker containers. How do you monitor 150 containers? How do you route traffic? What happens if Server A crashes?

**The Solution:** Kubernetes is an "orchestrator." You give it your Docker images and say, "I always want exactly 3 instances of the Payment Service running. Figure it out."

### The Key Concepts (Simplified):

*   **Pod:** The smallest unit in K8s. It usually contains one Docker container (e.g., your Spring Boot app).
*   **Deployment:** A configuration file stating *how many* Pods you want. (e.g., "Keep 3 replicas of the Payment Pod running"). If a node crashes and a Pod dies, K8s automatically spins up a new one somewhere else to maintain the target of 3.
*   **Service:** An internal load balancer. Because Pods are constantly dying and restarting (changing IP addresses), a Service provides a single, stable internal IP address that other microservices can use to talk to the Payment Pods.
*   **Ingress / Gateway:** The front door. Historically this was modeled with `Ingress`; newer platforms often use `Gateway API`. The job is the same: route external traffic into the cluster and direct it to the correct internal Services based on URL paths.

**Practical point:** "Using Kubernetes Deployments allows us to horizontally scale stateless services during peak traffic, while Services handle the internal load balancing across the pods. At the edge, we use an Ingress or Gateway controller to expose the right routes externally."

---

## Summary of the Journey

If someone asks, "How does code reach production?", this is your answer:

> "I commit my code to a feature branch, and open a PR. The **CI pipeline** automatically runs tests and linters. Once approved and merged to `main`, the **CD pipeline** builds a **Docker image** containing my application and its dependencies, and pushes it to a registry. Finally, our orchestrator, like **Kubernetes**, pulls that new image and performs a rolling update to the cluster, replacing old Pods with the new ones without dropping traffic. External traffic enters through an Ingress or Gateway controller."

---

## Practical Rule

Do not memorize tooling first.
Follow the artifact and the traffic.

Ask:

- where is the code validated
- what exact artifact gets deployed
- how do we know the runtime matches what we tested
- who replaces failed instances
- how does traffic reach only healthy instances

If you can answer those five questions, your infrastructure explanation is
already useful.

## 20-Second Answer

> DevOps basics are the mechanics of getting one tested artifact from source
> control into a reliable runtime. CI validates the change, Docker packages the
> app with its runtime, a registry stores the image, and Kubernetes or another
> orchestrator keeps the right number of healthy instances running while routing
> traffic safely through a gateway or load balancer.

## What To Internalize

- containers solve packaging and environment parity
- CI reduces release risk before deployment starts
- a registry gives you one deployable artifact instead of rebuild-per-environment drift
- orchestration keeps desired instances alive and replaces failed ones
- traffic management and health checks are part of delivery safety, not an afterthought
