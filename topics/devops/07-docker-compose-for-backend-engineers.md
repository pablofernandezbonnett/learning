# Docker Compose for Backend Engineers

Use this note when one service is no longer enough for a useful local backend
environment.

This note stays tool- and YAML-first on purpose. Java or Kotlin examples would
not explain the core decision any better here.

Why this matters:

- many backend features need a database, cache, queue, or telemetry sidecar to
  feel realistic
- ad hoc `docker run` commands become hard to repeat, share, and review
- `Docker Compose` gives you one local stack definition instead of a pile of
  shell history

## Smallest Useful Mental Model

`Docker Compose` is a local stack contract.

It lets you define:

- which services exist
- which networks they share
- which volumes keep state
- which environment values they need

Then one command starts the whole shape.

Docker's own docs describe Compose as a tool for defining and running
multi-container applications in one YAML file.

## Bad Mental Model vs Better Mental Model

Bad mental model:

- Compose is just a shortcut for starting several containers

Better mental model:

- Compose is the reproducible local environment contract for one backend stack

Why the better model matters:

- service names become stable
- networking becomes visible
- volumes become explicit
- onboarding gets easier
- runtime dependencies stop living only in one engineer's terminal history

## Small Concrete Example

Imagine a Spring Boot service that needs:

- Postgres for transactional truth
- Redis for cache
- an OpenTelemetry collector for local traces

That stack is annoying to recreate by hand every day.

With Compose, the shape becomes explicit:

```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/learning
      SPRING_DATA_REDIS_HOST: redis
      OTEL_EXPORTER_OTLP_ENDPOINT: http://otel-collector:4318
    depends_on:
      - postgres
      - redis
      - otel-collector

  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: learning
      POSTGRES_USER: learning
      POSTGRES_PASSWORD: learning
    volumes:
      - postgres-data:/var/lib/postgresql/data

  redis:
    image: redis:7

  otel-collector:
    image: otel/opentelemetry-collector:latest

volumes:
  postgres-data:
```

The main learning value is not YAML syntax.
It is seeing the local dependency graph clearly.

## Best Approach or Strong Default

Strong defaults for backend use:

- keep the stack small and explicit
- include only dependencies that change backend behavior meaningfully
- prefer named volumes for durable local state such as Postgres data
- keep service names aligned with what the app actually calls
- make ports explicit instead of relying on defaults you will forget later
- keep secrets fake in local Compose and avoid copying real credentials into the file

A good first Compose stack for this repo's style is:

- app
- database
- cache or queue when relevant
- one observability helper only when it teaches something real

## What Compose Solves Well

- local environment parity
- repeatable startup for app plus dependencies
- clearer networking than ad hoc container commands
- easier team onboarding
- faster switching between projects or branches

## What Compose Does Not Solve

- production orchestration
- real autoscaling
- cluster scheduling
- advanced secret governance
- full service-mesh or platform concerns

That is the key boundary.

Compose is a strong local tool.
It is not a small Kubernetes replacement.

## Main Tradeoff or Failure Mode

The common failure is turning local Compose into fake production.

Example:

- dozens of services
- many overrides
- fragile startup ordering
- hidden scripts
- copied production-like credentials

That shape usually teaches less, not more.

A second failure is assuming `depends_on` means "my dependency is fully ready".
It mainly controls startup order.
The stronger mental model is still:

- your app needs retry tolerance
- your app needs health awareness
- your app should fail clearly when a dependency is not actually usable yet

## Practical Rule

Use Compose when the local backend needs several moving parts to be credible.

Do not use Compose to simulate an entire platform when three or four services
already teach the point.

## Reusable Takeaway

> Docker Compose is the local stack contract for a backend service and its real
> dependencies. Its value is reproducibility and clarity, not fake production
> complexity.

## Further Reading

- Docker Compose docs: https://docs.docker.com/compose/
- Compose file reference: https://docs.docker.com/reference/compose-file/
- Docker volumes: https://docs.docker.com/engine/storage/volumes/
