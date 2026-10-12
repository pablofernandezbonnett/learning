# Docker Compose for Local Stacks

Compose describes a set of containers, networks, volumes, and runtime values
in one file. Use it when a local application needs a database, queue, cache,
or other dependency that should start repeatably for everyone working on it.

## Smallest Useful Shape

```yaml
services:
  app:
    build: .
    depends_on:
      database:
        condition: service_healthy
  database:
    image: postgres:18
    environment:
      POSTGRES_PASSWORD: local-only
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      retries: 10
    volumes:
      - database-data:/var/lib/postgresql
volumes:
  database-data:
```

This example makes the dependency and persistent local data explicit. It is
illustrative: match image versions and volume paths to the database image
your project actually uses. Keep credentials fake and local; do not commit
real secrets.

## What to Decide

- Which dependencies change application behavior enough to include locally?
- Which data should survive a container restart or be reset between tests?
- Which ports must the host access? Containers in one Compose network can
  usually use service names directly.
- Does the application tolerate a dependency becoming unavailable after
  startup?

Plain `depends_on` controls startup order, not service readiness. A
`healthcheck` with `condition: service_healthy` can wait for an initial
ready state, but the application still needs sensible reconnect and failure
behavior later.

Keep the stack small. Compose is useful for local integration and repeatable
development; production scheduling, scaling, and traffic routing are
separate decisions.

For the image itself, see
[Docker runtime practices](./05-docker-runtime-practices.md).
For cluster mechanics, see
[Kubernetes runtime basics](./04-kubernetes-crash-course.md).

## Further Reading

- [Docker Compose startup order](https://docs.docker.com/compose/how-tos/startup-order/)
