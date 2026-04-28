# Glossary — Abbreviations & Terms

This file explains every abbreviation used across these materials.
For each term: what it stands for, what it means in plain language, and
why it matters in real backend work.

---

## Performance Metrics

### RPS / QPS / TPS
**Requests Per Second / Queries Per Second / Transactions Per Second.**
All measure throughput — how much work the system handles in one second.
- RPS: total HTTP requests hitting your API.
- QPS: usually refers to database query load specifically.
- TPS: usually refers to write/commit operations (transactional systems).

**How to reason:** "Our checkout endpoint handles 500 RPS at peak. Each checkout fires
3 DB queries, so that is 1,500 QPS to Postgres." Throughput and latency are inversely
linked — as RPS approaches a system's ceiling, latency climbs sharply.

### p95 / p99 (Percentile Latency)
**The 95th and 99th percentile of response time**, measured across all requests in a window.

If p95 = 200ms, it means 95% of requests finished in ≤ 200ms, but 5% took longer.
If p99 = 800ms, 1% of users experienced 800ms+ waits.

**Why percentiles, not averages?**
Averages hide tail latency. If 99 requests take 10ms and 1 takes 10,000ms, the average
is ~110ms — looks fine. The p99 is 10,000ms — one user in 100 had a terrible experience.
Senior engineers always cite p95/p99, never averages.

**Rule of thumb:** Set SLOs on p99, not averages. Design for the worst case.

### Latency vs Throughput
- **Latency:** How long a single request takes (milliseconds). User-facing metric.
- **Throughput:** How many requests the system handles per unit time (RPS). Capacity metric.

They pull in opposite directions at scale: increasing throughput (via batching, caching)
often increases individual request latency slightly. Optimizing purely for latency
(synchronous, single-threaded) reduces throughput.

---

## Reliability / SRE

### SLI / SLO / SLA
- **SLI (Service Level Indicator):** The raw measured metric. e.g., "availability = 99.85%
  over the last 30 days."
- **SLO (Service Level Objective):** The internal target you set. e.g., "we aim for 99.9%
  availability." This is what your engineering team owns.
- **SLA (Service Level Agreement):** The contractual commitment to customers, with financial
  consequences for breach. Usually looser than the SLO (e.g., SLA = 99.5%, SLO = 99.9%).
  The gap between SLO and SLA is your safety buffer.

**How to reason:** "Our SLO is 99.9% availability. That allows ~43 minutes of downtime per
month. We set our internal alerts to fire at 99.95% so we catch problems before we breach
the SLO."

### Error Budget
Derived from the SLO: `error_budget = 1 - SLO`. At 99.9% SLO, you have 0.1% = ~43
minutes/month of allowed downtime. Error budget is a team's licence to take risk —
deploy often while budget is healthy, freeze releases when it is nearly exhausted.

### MTTR / MTBF
- **MTTR (Mean Time to Repair/Recovery):** Average time from incident start to resolution.
  Low MTTR = fast recovery. Improved by runbooks, observability, on-call processes.
- **MTBF (Mean Time Between Failures):** Average time between incidents.
  High MTBF = high reliability. Improved by testing, redundancy, chaos engineering.

---

## Infrastructure & Kubernetes

### K8s
Short for **Kubernetes** (8 letters between K and s). Container orchestration platform.
See `topics/devops/04-kubernetes-crash-course.md`.

### HPA
**Horizontal Pod Autoscaler.** A Kubernetes controller that automatically adds or removes
Pod replicas based on a metric (CPU utilization by default, or custom metrics like RPS).
"Horizontal" = more copies of the same Pod. "Vertical" (VPA) = bigger Pod (more CPU/RAM).

**Why horizontal is preferred:** Adding replicas is non-disruptive and fast. Resizing a
running Pod requires a restart. Horizontal scaling also gives you fault tolerance.

### VPC
**Virtual Private Cloud.** A logically isolated network segment in a cloud provider
(AWS/GCP/Azure). Your services run inside a VPC and are not reachable from the public
internet by default. An Ingress, Load Balancer, or NAT Gateway is what opens a controlled
door to the outside.

**Practical use:** "All microservices communicate inside the VPC over plain HTTP.
TLS terminates at the ALB at the VPC boundary — internal traffic doesn't need the overhead
of TLS re-encryption."

### ALB / NLB / LB
- **LB (Load Balancer):** Generic term — distributes incoming traffic across backend servers.
- **ALB (Application Load Balancer):** Layer 7. Reads HTTP headers and URL paths.
  Supports path-based routing, sticky sessions, WebSocket upgrades.
- **NLB (Network Load Balancer):** Layer 4. Only sees IP + port. Extremely fast (hardware-
  level), used for high-throughput non-HTTP traffic (raw TCP, UDP, gRPC with TLS passthrough).

### CDN
**Content Delivery Network.** A globally distributed network of edge servers (PoPs —
Points of Presence) that cache static assets (images, JS, CSS) close to users.
Reduces latency by shortening the physical distance a packet must travel.

**Practical use:** "Static product images are served from our CDN. A user in Tokyo gets
the image from a Tokyo edge node, not our origin server in Frankfurt. Origin is only hit
on a cache miss."

### AZ
**Availability Zone.** A physically separate data center within a cloud region.
AWS `eu-west-1` has three AZs: `eu-west-1a`, `eu-west-1b`, `eu-west-1c`. Deploy
replicas across AZs — if one data center loses power, the others continue serving traffic.

### ECS / EKS
- **ECS (Elastic Container Service):** AWS-managed container orchestration. Simpler than K8s.
- **EKS (Elastic Kubernetes Service):** AWS-managed Kubernetes. More portable, more complex.

### OOMKilled
**Out Of Memory Killed.** When a Kubernetes Pod's memory usage exceeds its `limits.memory`,
the Linux kernel's OOM Killer terminates the process. K8s then restarts the Pod.
Frequent OOMKills indicate either a memory leak or limits set too low.

---

## Protocols & Networking

### DNS
**Domain Name System.** Translates human-readable domain names (`api.mystore.com`) into
IP addresses (`52.18.4.101`). Every HTTP request starts with a DNS lookup. CDNs, geo-routing,
and blue/green switches all manipulate DNS records.

### TCP / UDP
- **TCP (Transmission Control Protocol):** Reliable, ordered, connection-based. Sender and
  receiver do a 3-way handshake before data flows. Lost packets are retransmitted.
  Used for HTTP, DB connections, file transfers.
- **UDP (User Datagram Protocol):** Unreliable, connectionless, fast. No handshake,
  no retransmission. Used for video streaming, DNS, online gaming, and HTTP/3 (QUIC).

**Practical use:** "Kafka brokers communicate over TCP. We chose gRPC (HTTP/2 over TCP)
for internal services because we need reliability and ordering guarantees."

### TLS / SSL
- **SSL (Secure Sockets Layer):** The original encryption protocol. Now deprecated.
- **TLS (Transport Layer Security):** The modern replacement for SSL. When people say
  "SSL certificate," they usually mean a TLS certificate.

TLS uses asymmetric encryption (public/private key pair) to negotiate a symmetric
session key. All subsequent traffic is encrypted with the fast symmetric key.

### HPACK
**Header Compression for HTTP/2.** HTTP headers like `Authorization` and `Content-Type`
repeat identically across hundreds of requests. HPACK compresses them by encoding deltas
from a shared header table maintained by client and server. Reduces wire size significantly
for header-heavy APIs.

### gRPC
**Google Remote Procedure Call.** A framework for calling functions on a remote service
as if they were local. Built on HTTP/2 + Protocol Buffers. Not an abbreviation of
something meaningful — "g" stands for different things in each release (gRPC 1.0: "gRPC").

**Practical distinction:** REST = resource-based (nouns, URLs). gRPC = function-based
(verbs, service methods). See `topics/architecture/04-networking-fundamentals.md`.

### SSE
**Server-Sent Events.** A one-directional push protocol over HTTP where the server streams
events to the client. Simpler than WebSockets (no upgrade handshake, works over HTTP/1.1).
Used for live dashboards, notifications, subscription feeds.

### mTLS
**Mutual TLS.** In standard TLS, only the server presents a certificate (client verifies
the server is legitimate). In mTLS, both sides present certificates. Used in zero-trust
service meshes (Istio, Linkerd) where every service-to-service call is authenticated.

---

## Security

### XSS
**Cross-Site Scripting.** An attacker injects malicious JavaScript into a page that other
users load. The script runs in the victim's browser with their session. Mitigation:
- Never put tokens in `localStorage` (JS-readable).
- Use `HttpOnly` cookies (not accessible to JS).
- Apply Content Security Policy (CSP) headers to restrict which scripts can run.

### CSRF
**Cross-Site Request Forgery.** An attacker tricks a logged-in user's browser into
submitting a request to your API. Because the browser automatically sends cookies, the
request arrives with valid session credentials. Mitigation: `SameSite=Strict` cookies,
double-submit CSRF token, or custom request headers (browsers block cross-origin headers).

### PKCE
**Proof Key for Code Exchange** (pronounced "pixie"). An OAuth2 extension for public clients
(SPAs, mobile apps) that cannot securely store a `client_secret`. PKCE replaces the static
secret with a per-request cryptographic challenge. See `topics/security/01-auth-sessions-vs-jwt.md`.

### JWT / JTI
- **JWT (JSON Web Token):** A base64-encoded, cryptographically signed token carrying a
  JSON payload (userId, roles, expiry). Self-contained — verified by signature alone,
  no DB lookup needed.
- **JTI (JWT ID):** A unique identifier claim inside a JWT (`"jti": "abc123"`). Used to
  uniquely identify a token — for example, to put it on a deny-list for forced revocation.

### OIDC
**OpenID Connect.** A thin identity layer on top of OAuth2. OAuth2 answers "can this user
access this resource?" OIDC answers "who is this user?" — delivering an `id_token` (a JWT)
containing the user's profile. Used by "Sign in with Google/Apple".

### OAuth2
**Open Authorization 2.0.** A framework for delegating access: "I (the user) authorize
this app to access my data on this other service, without giving the app my password."
Four flows: Authorization Code (for web/mobile), Client Credentials (service-to-service),
Implicit (deprecated), Device Code (TV/CLI apps).

### RBAC
**Role-Based Access Control.** Users are assigned roles (ADMIN, USER, READ_ONLY).
Permissions are attached to roles, not individual users. Simpler than ABAC (Attribute-Based)
but less granular. In JWTs, roles are typically embedded as a `roles` claim in the payload.

### WAF
**Web Application Firewall.** Sits in front of your API and inspects HTTP traffic for
known attack patterns: SQL injection, XSS payloads, suspicious User-Agent strings.
AWS WAF, Cloudflare WAF. First line of defence — not a replacement for application-level
input validation.

---

## Databases

### ACID
**Atomicity, Consistency, Isolation, Durability.** The four properties of a reliable
database transaction.
- **Atomicity:** All operations in a transaction succeed, or none do. No partial writes.
- **Consistency:** A transaction brings the DB from one valid state to another valid state.
  Constraints (FK, UNIQUE, CHECK) are always enforced.
- **Isolation:** Concurrent transactions don't see each other's in-progress changes.
  Levels: READ UNCOMMITTED → READ COMMITTED → REPEATABLE READ → SERIALIZABLE.
- **Durability:** Once committed, data survives crashes (written to disk, WAL).

**Practical use:** Postgres is ACID-compliant. MongoDB is ACID per single-document by
default; multi-document ACID requires explicit transactions (higher cost).

### DDL / DML
- **DDL (Data Definition Language):** SQL that changes the schema structure.
  `CREATE TABLE`, `ALTER TABLE`, `DROP COLUMN`. These are the dangerous migrations.
- **DML (Data Manipulation Language):** SQL that changes the data.
  `INSERT`, `UPDATE`, `DELETE`, `SELECT`. Generally safe to run live.

**Practical use:** DDL changes can lock tables and break running Pods. DML changes (like
backfilling a new column) can be done in background batches without downtime.

### ORM / JPA
- **ORM (Object-Relational Mapper):** A library that maps database rows to in-memory objects
  so you write Java/Kotlin code instead of SQL. Examples: Hibernate, Exposed.
- **JPA (Jakarta Persistence API, formerly Java Persistence API):** The Java standard
  specification for ORM. Hibernate is the most common JPA implementation. Spring Data JPA
  wraps Hibernate with repositories and query derivation.

### MVCC
**Multi-Version Concurrency Control.** How Postgres (and most modern DBs) handle concurrent
reads and writes without locking. Each transaction sees a consistent snapshot of the data
as it existed when the transaction started. Writers create new row versions; readers read
the old version. This allows reads and writes to proceed concurrently without blocking.

### WAL
**Write-Ahead Log.** A durability mechanism in Postgres. Before a data page is modified,
the change is first written to an append-only log file (the WAL). If the server crashes,
Postgres replays the WAL to recover committed transactions. Also the foundation of
Postgres replication (streaming WAL to replicas) and CDC tools like Debezium.

### N+1 (Query Problem)
Not an abbreviation, but a named anti-pattern. If you load a list of N orders, and then
for each order execute 1 more query to fetch its user, you run N+1 total queries.
At N=1000, that is 1001 DB round-trips for what should be 1-2 queries with a JOIN or
batch load. See `topics/databases/03-sql-refresh-for-backend-engineers.md`.

### LRU / LFU (Cache Eviction Policies)
- **LRU (Least Recently Used):** When the cache is full, evict the item that was accessed
  longest ago. Good for recency-based access patterns (recently viewed products).
- **LFU (Least Frequently Used):** Evict the item with the fewest total accesses.
  Good for frequency-based patterns (popular product catalogue items).

Redis default is LRU (with `maxmemory-policy allkeys-lru`).

### DLQ / DLT
- **DLQ (Dead Letter Queue):** A secondary queue in RabbitMQ/SQS where messages are routed
  after failing processing N times. Prevents a bad message from blocking the whole queue.
- **DLT (Dead Letter Topic):** The Kafka equivalent. See `topics/api/02-message-brokers-and-delivery-semantics.md`.

### TTL
**Time To Live.** How long a record lives before it is automatically deleted or considered
stale. Used in Redis (expire keys after N seconds), DNS (how long to cache an IP), JWT
(token expiry), and CDN (cache-control max-age).

### ACK
**Acknowledgement.** A signal from the consumer back to the broker confirming a message was
processed successfully. In Kafka: the consumer commits its offset (effectively ACKing).
In RabbitMQ: the consumer sends an explicit ACK. If the consumer crashes before ACKing,
the broker redelivers the message (at-least-once delivery).

---

## Application Patterns & Architecture

### Invariant
**A business rule that must always stay true.** Not a math word for its own
sake, but a way to name the rule your system is protecting.

**Practical use:** "The checkout invariant is that one purchase intent must not
create two final orders. The payment invariant is that we must not capture the
same payment twice."

### Idempotency
**A property where repeating the same request does not create a second business
result by accident.** If a client retries `POST /payments` with the same request
key, the system should return the same outcome instead of charging again.

**Practical use:** "Webhook handlers must be idempotent because providers can
redeliver the same event several times."

### Source of Truth
**The durable system whose final state you trust when several layers disagree.**
Caches, search indexes, and read models can be useful, but they are often not
the final authority for business correctness.

**Practical use:** "Postgres is the source of truth for order state. Redis helps
with speed, but if Redis and Postgres disagree, Postgres wins."

### Delivery Semantics
**The actual guarantee a queue or broker gives you about message delivery.**
This is where terms like `at-most-once`, `at-least-once`, ordering, and duplicate
delivery matter.

**Practical use:** "Kafka gives us at-least-once delivery by default in this
flow, so consumers must handle duplicates safely."

### Backpressure
**A form of natural slowing or throttling when a producer is faster than a
consumer.** Without backpressure, queues grow without bound and latency or
memory usage can explode.

**Practical use:** "A bounded queue gives the worker pool backpressure: once the
queue is full, producers slow down instead of flooding the system forever."

### Graceful Degradation
**A fallback mode where the system still gives a reduced but useful response
instead of failing completely.** This is different from pretending there is no
problem.

**Practical use:** "If the recommendation service is down, the product page can
still load without recommendations. That is graceful degradation."

### CQRS
**Command Query Responsibility Segregation.** Separating the write model (Commands — CREATE,
UPDATE, DELETE) from the read model (Queries — SELECT). The read model can be a denormalized
read replica, an Elasticsearch index, or a Redis cache — optimized purely for query speed,
without the constraints of the normalized write schema.

**Practical use:** "Our product catalogue writes go to Postgres (normalized, ACID). The
read side is a pre-built Elasticsearch index — search and filter at millisecond speed
regardless of how the relational schema is structured."

### CDC
**Change Data Capture.** A technique for detecting and capturing row-level changes in a
database (INSERT/UPDATE/DELETE) and streaming them to consumers. Implemented by reading
the DB's WAL (Postgres) or oplog (MongoDB). Tools: Debezium, AWS DMS.

**Practical use:** Used to implement the Outbox Pattern without polling: Debezium reads
committed Outbox rows from the Postgres WAL and publishes them to Kafka — no application
polling loop needed.

### Debezium
**A popular CDC platform.** It reads the database transaction log (for example the Postgres
WAL) and turns committed row changes into Kafka events or other downstream records.

**Practical use:** "Instead of writing my own polling outbox relay, I can use Debezium to
stream committed outbox rows from Postgres into Kafka."

### Saga
**A pattern for multi-step business workflows across services.** Each service commits its
own local transaction. If something later fails, the system runs compensating actions such
as `ReleaseReservation` or `RefundPayment` instead of one global rollback.

**Practical use:** "I use a Saga when one business operation crosses several services and I
need explicit failure handling after local commits."

### Outbox Pattern
**A reliability pattern for the dual-write problem.** The service saves its business change
and an outbox row in the same local transaction, then a relay or CDC process publishes the
outbox row to the broker.

**Practical use:** "Outbox lets me avoid the broken 'save to DB and then publish' sequence,
because the event is staged in the same database transaction as the business write."

### BFF
**Backend For Frontend.** An API layer purpose-built for a specific client type
(mobile BFF, web BFF). Aggregates multiple microservice responses into one response shaped
exactly for what the UI needs. Avoids over-fetching and multiple round-trips from the client.

### SOA
**Service-Oriented Architecture.** The predecessor to microservices. Large services
(not necessarily small/micro) communicating over a shared enterprise service bus (ESB).
Heavier and more centralized than microservices. Useful as context for
why microservices emerged.

### AOP
**Aspect-Oriented Programming.** A programming paradigm for separating cross-cutting
concerns (logging, security, transaction management) from business logic. In Spring,
`@Transactional`, `@Cacheable`, and `@PreAuthorize` are all AOP-based — Spring wraps your
class in a proxy that intercepts method calls to apply the behaviour.

**Critical Kotlin gotcha:** Spring's AOP uses CGLIB to subclass your beans. In Kotlin, all
classes are `final` by default — CGLIB cannot subclass them. The `kotlin("plugin.spring")`
Gradle plugin auto-opens `@Service`, `@Component`, etc. to make this work.

### CGLIB
**Code Generation Library.** A Java bytecode generation library that Spring uses to create
dynamic subclass proxies for AOP. When you mark a method `@Transactional`, Spring generates
a CGLIB subclass of your service that wraps the method in transaction management code.

---

## Spring / JVM

### HikariCP
**Hikari Connection Pool.** The default JDBC connection pool in Spring Boot. Maintains a
pool of pre-opened DB connections that application threads borrow and return — avoids the
overhead of opening a new TCP connection to Postgres on every request.

**Sizing formula:** `pool_size = (num_cpu_cores × 2) + num_disk_spindles`.
For a 4-core server with SSDs (1 spindle): 4×2 + 1 = 9 connections.
Over-sizing the pool wastes Postgres memory; under-sizing causes request queuing.

### MDC
**Mapped Diagnostic Context.** A thread-local map in SLF4J/Logback where you store
contextual values (requestId, userId, traceId) that are automatically appended to every
log line on that thread. Without MDC, you cannot correlate logs from a single request
across thousands of interleaved log lines.

---

## Testing

### E2E
**End-to-End Test.** A test that exercises the entire system from the user's perspective —
real browser or HTTP client, real database, all services running. Highest confidence,
slowest, most brittle. Reserve for critical user journeys only (checkout, login, payment).
See `topics/testing/01-testing-strategies.md`.

### TDD
**Test-Driven Development.** Write the failing test first, then write the minimum code to
make it pass, then refactor. Disciplines you to write testable code by design. Not always
practical for exploratory or prototype work, but strongly valued for core business logic.

### SUT
**System Under Test.** The class or component you are actually testing in a given test.
Used to distinguish from mocks/stubs/fakes: "The SUT is `OrderService`. Its dependencies
(`UserRepository`, `PaymentClient`) are mocked."

---

## Other

### SPA
**Single Page Application.** A web app architecture where the browser loads one HTML page
and JavaScript handles all navigation, updating the DOM without full page reloads.
Examples: React, Vue, Angular apps. Contrast with MPA (Multi-Page Application) where
each navigation loads a new HTML page from the server.

### ELK Stack
**Elasticsearch + Logstash + Kibana.** A popular log aggregation and search platform.
Logstash (or Fluentd/Vector) ships logs to Elasticsearch; Kibana provides a UI to search
and visualize them. Often replaced today with cloud-native solutions (AWS OpenSearch,
Datadog, Grafana Loki).

### ETL
**Extract, Transform, Load.** A data pipeline pattern: extract data from a source
(Postgres, Kafka), transform it (clean, aggregate, join), load it into a destination
(data warehouse, analytics DB). Used in reporting, BI, and ML pipelines.

### UUID
**Universally Unique Identifier.** A 128-bit random identifier, typically formatted as
`550e8400-e29b-41d4-a716-446655440000`. Statistically guaranteed to be unique without
central coordination. Used as primary keys, idempotency keys, trace IDs. Prefer UUIDv7
(time-ordered) over UUIDv4 (pure random) for DB primary keys — time-ordering reduces
B-tree index fragmentation.

### DAU / MAU
- **DAU (Daily Active Users):** Number of unique users who use the product in a given day.
- **MAU (Monthly Active Users):** Same, per month.

Used in capacity planning: "Our checkout service must support 50,000 DAU, with peak
concurrency of ~5% at any moment = 2,500 concurrent users."
