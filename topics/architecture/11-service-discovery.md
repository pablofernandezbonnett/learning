# Service Discovery

You do not need to become a service-mesh specialist.

What you do need is a clean explanation of how one service finds another when
instances move, restart, and scale.

## The Problem

In a monolith, your application has one address. In microservices, you have dozens of services, each running multiple instances that scale up and down dynamically. Container orchestrators (Kubernetes, ECS) assign ephemeral IPs to each instance — the IP changes every time a container restarts.

**How does Order Service know the current address of Inventory Service when it keeps changing?**

Hardcoding `http://inventory-service:8080` in a config file breaks the moment Inventory Service scales to 3 instances or restarts on a new IP.

Service discovery solves this: services register their own address when they start, deregister when they stop, and clients look up the current healthy instances at runtime.

---

## Two Patterns

### Client-Side Discovery

The client (Order Service) queries the service registry directly, gets a list of healthy Inventory Service instances, and picks one itself (with its own load-balancing logic).

```
Order Service → queries Service Registry → gets [192.168.1.10:8080, 192.168.1.11:8080]
              → picks 192.168.1.10 (round-robin) → calls Inventory Service directly
```

**Used by:** Netflix Eureka + Spring Cloud LoadBalancer (the classic Spring Cloud microservices stack)

**Pros:** Client controls load-balancing algorithm (round-robin, weighted, zone-aware).
**Cons:** Every language/SDK needs a registry client library. Couples service code to discovery logic.

### Server-Side Discovery

The client calls a fixed address (a load balancer or API gateway). The infrastructure looks up the registry and routes to a healthy instance.

```
Order Service → calls api.internal/inventory → Load Balancer / API Gateway
             → queries Service Registry → routes to 192.168.1.10:8080
```

**Used by:** Kubernetes (kube-proxy + kube-dns), AWS ALB + ECS, Istio service mesh.

**Pros:** Client is completely unaware of service discovery. Works with any language.
**Cons:** Extra network hop through the load balancer.

---

## Tools

### Kubernetes (DNS-based — the modern default)

In Kubernetes, each Service object gets a stable DNS name. kube-dns resolves the name to the current set of healthy Pod IPs. kube-proxy handles the routing.

```yaml
# order-service calls inventory-service by stable DNS name inside the cluster
apiVersion: v1
kind: Service
metadata:
  name: inventory-service
  namespace: retail
spec:
  selector:
    app: inventory-service   # routes to all Pods with this label
  ports:
    - port: 8080
```

```kotlin
// In Order Service — just use the DNS name. Discovery is infrastructure-level.
@Configuration
class InventoryClientConfig {
    @Bean
    fun inventoryClient(): RestClient = RestClient.builder()
        .baseUrl("http://inventory-service.retail.svc.cluster.local:8080")
        .build()
}
```

`inventory-service.retail.svc.cluster.local` is the full DNS name:
- `inventory-service` — the Service name
- `retail` — the namespace
- `svc.cluster.local` — the Kubernetes cluster domain

If you're within the same namespace, `http://inventory-service:8080` works.

**Health checking:** Kubernetes removes a Pod from the Service endpoints automatically when its `readinessProbe` fails.

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

### Netflix Eureka (Spring Cloud)

The legacy Spring Cloud approach — still common in large enterprise Java shops.

```kotlin
// build.gradle.kts
implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

// application.yml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
  instance:
    prefer-ip-address: true

spring:
  application:
    name: inventory-service   # registers under this name
```

```kotlin
// Client call — @LoadBalanced RestClient resolves "INVENTORY-SERVICE" via Eureka
@Configuration
class InventoryClientConfig {
    @Bean
    @LoadBalanced
    fun restClient(): RestClient.Builder = RestClient.builder()

    @Bean
    fun inventoryClient(builder: RestClient.Builder): RestClient =
        builder.baseUrl("http://INVENTORY-SERVICE").build()
}
```

Spring Cloud LoadBalancer (replacing Ribbon) picks a healthy instance from Eureka's registry using round-robin.

### Consul (HashiCorp)

More powerful than Eureka: supports health checks, key-value config store, and multi-datacenter.

```kotlin
implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
implementation("org.springframework.cloud:spring-cloud-starter-consul-config")
```

```yaml
spring:
  cloud:
    consul:
      host: consul-server
      port: 8500
      discovery:
        health-check-path: /actuator/health
        health-check-interval: 10s
```

Consul performs active health checks (HTTP GET to your health endpoint every 10s). Failed services are immediately removed from the registry.

### Istio Service Mesh (advanced)

Istio replaces application-level service discovery with a network-level proxy (Envoy sidecar). Every Pod gets an Envoy proxy injected automatically. Service-to-service calls go through Envoy, which handles:
- Discovery (via Istio's xDS control plane)
- Load balancing
- Retries, circuit breaking, timeouts
- mTLS (mutual TLS) between services
- Distributed tracing (automatically injects trace headers)

Your application code doesn't change at all — `http://inventory-service:8080` just works.

**When Istio makes sense in practice:** "For a mature microservices platform, a service mesh like Istio can handle cross-cutting concerns — service discovery, retries, circuit breaking, and observability — at the infrastructure layer, so service code stays focused on business logic."

---

## Health Checks — The Foundation of Discovery

Service discovery only works if the registry knows which instances are healthy. All discovery tools rely on health checks.

### Spring Boot Actuator Health

```yaml
management:
  health:
    readiness-state:
      enabled: true
    liveness-state:
      enabled: true
  endpoint:
    health:
      probes:
        enabled: true
```

This exposes:
- `/actuator/health/liveness` — is the JVM alive? (fails if app is deadlocked; Kubernetes restarts it)
- `/actuator/health/readiness` — is the app ready to serve traffic? (fails during startup or if DB is unreachable; Kubernetes stops sending traffic but doesn't restart)

**Liveness vs Readiness distinction** is a common practical question:
- **Liveness** failure → restart the container (something is fundamentally broken)
- **Readiness** failure → remove from load balancer rotation (temporarily overloaded or dependency down)

---

## Practical Scenario

**Question:** "How does Order Service find Inventory Service in your microservices architecture?"

**Answer (Kubernetes context):**

> In our Kubernetes setup, we use DNS-based service discovery. Inventory Service is deployed as a Kubernetes Deployment with a corresponding Service object. When Order Service calls `http://inventory-service:8080`, Kubernetes DNS resolves it to the current set of healthy Inventory Service Pod IPs and kube-proxy load-balances across them.
>
> We configure readiness probes on all services — Spring Boot Actuator `/actuator/health/readiness`. When a new version of Inventory Service is rolling out, new Pods don't receive traffic until they pass the readiness check, giving us zero-downtime deployments. If Inventory Service is temporarily overwhelmed, its readiness probe fails (because its DB connection pool is exhausted), Kubernetes removes it from the load balancer pool, and Order Service's circuit breaker (Resilience4j) kicks in, returning cached inventory data while Inventory Service recovers.

**Answer (Spring Cloud / Eureka context, for legacy enterprise shops):**

> Each service registers itself with Eureka on startup, including its IP and port. Order Service uses Spring Cloud LoadBalancer (with `@LoadBalanced` RestClient) to resolve `INVENTORY-SERVICE` to a current healthy instance using round-robin. Eureka heartbeats every 30s — if a service stops heartbeating, Eureka evicts it within 90s. We pair this with Resilience4j circuit breakers so failed instances are removed from consideration faster than Eureka's eviction window.

---

## Quick Reference

| Tool | Pattern | Used when |
|---|---|---|
| Kubernetes kube-dns | Server-side, DNS | Modern cloud-native (most common now) |
| Netflix Eureka + Spring Cloud LB | Client-side | Legacy Spring Cloud enterprise |
| HashiCorp Consul | Client or server-side | Multi-cloud, non-Kubernetes, VM-based |
| Istio service mesh | Server-side (transparent) | Large microservices platform, need mTLS + advanced traffic management |

| Probe | What it means | Kubernetes action on failure |
|---|---|---|
| Liveness | App is alive (not deadlocked) | Restart container |
| Readiness | App is ready for traffic | Remove from Service endpoints (no restart) |
| Startup | App has finished starting | Hold liveness/readiness checks until passed |
