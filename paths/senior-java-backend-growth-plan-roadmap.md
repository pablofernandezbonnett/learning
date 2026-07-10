# Senior Java Backend Growth Plan Roadmap

Use this as the main path for this repository.

It is built for a senior Java backend engineer who wants to stay clearly inside
the backend lane, close real market gaps, and remain employable over the next
few years without drifting into pure `SRE`, pure `DevOps`, or a vague
"architect" identity.

## What Already Transfers Well

This path assumes you already bring real production value in:

- Java and Spring backend delivery
- APIs and integrations
- commerce, payments, or workflow-heavy systems
- long-lived product environments
- stakeholder communication
- practical maintainability instincts

That means this is not a restart from zero.

The goal is:

- keep the Java backend core strong
- close the runtime, cloud, architecture, and AI-adjacent gaps that now matter more
- study fewer things, but make them more reusable

## What This Path Is Not Trying To Do

Do not use this path to become:

- a pure `DevOps` engineer
- a pure `SRE`
- a `Cloud Architect`
- a `Solutions Architect`
- an `AI Engineer`

Those areas still matter, but only to the depth that helps a senior backend
engineer collaborate well and make stronger technical decisions.

## What To Skip For Now

Treat these as non-goals unless current work makes them necessary:

- deep Kubernetes ownership
- broad `Terraform` or infrastructure-as-code specialization
- service mesh and `Istio`
- advanced `CQRS` or event sourcing as a default design style
- broad Python learning that duplicates external courses
- Ruby study inside this repo unless a concrete backend gap appears
- AI agent or pipeline depth before local/private serving and eval basics are solid

## Working Rule

For each phase:

1. reopen the topic until it feels current again
2. cover the missing practical layer that improves real backend work
3. stop before the topic turns into a side career
4. reinforce with one lab, one code example, or one small design exercise

Short rule:

- fundamentals first
- production reality second
- tooling only where it raises your leverage

## Strong Default

- follow one phase at a time
- use topic `README`s as the real study order inside each phase
- use companion folders only when a concrete gap appears
- do not run adjacent paths in parallel with the main roadmap unless work clearly forces it

## Recommended Order

### Phase 1. Refresh the Java backend core

Start here:

- [../topics/java/README.md](../topics/java/README.md)
- [../topics/spring-boot/README.md](../topics/spring-boot/README.md)
- [../topics/databases/README.md](../topics/databases/README.md)
- [../topics/api/README.md](../topics/api/README.md)

Focus:

- modern Java baseline: records, sealed types, pattern matching, virtual threads
- concurrency, request budgets, and runtime behavior
- SQL judgment: indexes, joins, `EXPLAIN`, transactions, locking, isolation
- Spring Boot production basics: transactions, JPA, security, validation, config, testing, actuator
- HTTP and API behavior: status codes, headers, caching, idempotency, error shape

Runnable companions:

- [../labs/java-modern-features/README.md](../labs/java-modern-features/README.md)
- [../labs/spring-boot-sample/README.md](../labs/spring-boot-sample/README.md)
- [../labs/kotlin-backend-examples/README.md](../labs/kotlin-backend-examples/README.md)
- [../labs/sql-query-review/README.md](../labs/sql-query-review/README.md)

Outcome:

- your day-to-day Java backend judgment becomes current again instead of staying anchored in older Java and framework habits

### Phase 2. Strengthen design judgment without leaving the backend lane

Start here:

- [../topics/architecture/README.md](../topics/architecture/README.md)
- [../topics/system-design/README.md](../topics/system-design/README.md)
- [../topics/testing/README.md](../topics/testing/README.md)

Focus:

- layered, clean, and hexagonal boundaries in practical terms
- `DDD` at the level that helps service boundaries and use-case clarity
- modular monolith versus microservices tradeoffs
- API versioning and contract evolution
- Kafka, async boundaries, retries, outbox, and read-model judgment when the integration problem is real
- testing by risk instead of by ideology

Useful companion topics:

- [../topics/architecture/09-domain-driven-design.md](../topics/architecture/09-domain-driven-design.md)
- [../topics/architecture/21-hexagonal-architecture.md](../topics/architecture/21-hexagonal-architecture.md)
- [../topics/spring-boot/21-practical-ddd-in-spring.md](../topics/spring-boot/21-practical-ddd-in-spring.md)

Strong default:

- prefer modular clarity and workflow correctness before promoting a system into more distributed patterns
- do not treat `CQRS`, event sourcing, or broker-heavy designs as maturity badges

Outcome:

- you get stronger at shaping backend systems that are easier to evolve, explain, and defend technically

### Phase 3. Add developer-owned runtime and cloud depth

Start here:

- [../topics/devops/README.md](../topics/devops/README.md)
- [../topics/cloud/README.md](../topics/cloud/README.md)

Focus:

- Dockerfiles, multi-stage builds, runtime hygiene, networking, and volumes
- `Docker Compose` level local orchestration and environment parity
- observability basics: logs, metrics, traces
- `OpenTelemetry`, `Prometheus`, `Grafana`, and actuator-level operational thinking
- AWS minimums that matter most for backend work: `IAM`, `EC2`, `S3`, `RDS`, load balancers, container runtimes

Do not optimize for:

- broad cloud catalog memorization
- deep `Terraform`
- advanced Kubernetes platform work unless the job requires it
- trying to look like a platform engineer instead of a stronger backend engineer

Outcome:

- you stop depending on other teams for every runtime conversation and become much more credible in production-oriented backend work

### Phase 4. Build practical security depth for backend systems

Start here:

- [../topics/security/README.md](../topics/security/README.md)

Companion when needed:

- [../topics/appsec/README.md](../topics/appsec/README.md)

Focus:

- auth and authz boundaries
- API and workflow abuse cases
- secure Spring and JVM habits
- secrets, logging, and secure delivery basics
- payment and webhook correctness where security and backend correctness meet
- selective AppSec refresh where needed: sessions, tokens, `IDOR`, `SQL injection`, `CSRF`, and `XSS`

Strong default:

- study security as part of backend design, not as a separate identity project
- use `topics/security/` as the main route and dip into `topics/appsec/` only for concrete browser or vulnerability gaps
- use the secure-Java companion material only as a summary map after the main notes, not as a separate study lane

Outcome:

- you become the backend engineer who makes safer defaults and catches more real risks early

### Phase 5. Add AI as a backend tool and local service capability

Start here:

- [../topics/ai/README.md](../topics/ai/README.md)
- [../topics/python/README.md](../topics/python/README.md)

Use this phase in a narrow, practical way:

- local AI server setup
- Dockerized model serving or local model runtime
- network and auth boundaries around private AI services
- AI-assisted development workflows for repo Q and A, code explanation, test drafting, and incident summarization
- logging, metrics, cost, and timeout discipline
- prompt and version hygiene
- small controlled `RAG` and tool integration where it actually helps
- internal productivity and bounded AI adoption, not model research or AI engineering

Do not optimize for:

- agent frameworks before local/private serving and eval basics are clear
- ML pipeline depth before the day-to-day productivity and service-boundary use cases are proven
- broad Python learning duplicated from external courses

Strong default:

- keep deterministic backend rules in charge
- treat AI as a bounded helper or service, not as the owner of core business correctness

Outcome:

- you can run and secure useful AI capabilities locally or privately without pretending you are changing career into an AI specialist

## Optional Adjacent Paths

Use these only if a real work need appears:

- [adjacent/appsec-for-software-engineers.md](./adjacent/appsec-for-software-engineers.md)
- [adjacent/appsec-in-product-teams.md](./adjacent/appsec-in-product-teams.md)
- [adjacent/sre-from-backend-engineers.md](./adjacent/sre-from-backend-engineers.md)
- [adjacent/solutions-architect-from-backend.md](./adjacent/solutions-architect-from-backend.md)

These are not the default growth route.
They are side lanes for specific gaps or role pressure.

## Keep The Repo Sustainable

Use these maintenance rules:

- add a new note only when it closes a real gap that the current notes do not cover well
- prefer improving the canonical note before creating a parallel summary of the same topic
- keep companion material short and subordinate to the main topic lane
- if a topic has no likely near-term use, keep it in adjacent awareness instead of promoting it into the main roadmap
- review the roadmap every few months and remove or demote anything that drifted into hype, duplication, or side-career depth
- add runnable labs only when runtime behavior or debugging feedback teaches more than prose alone

## Reusable Takeaway

> The highest-return move is not to learn everything around backend. It is to become the senior Java backend engineer who is strongest at modern Java, SQL and Spring production judgment, practical architecture, developer-owned runtime depth, and narrow useful AI integration.
