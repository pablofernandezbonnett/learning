# Docker Runtime Practices for Backend Engineers

> Primary fit: `Shared core`


This is the "beyond Compose" Docker refresher.

The goal is not to memorize every Docker flag.

The goal is to understand the practices that matter when packaging and running
backend services in a way that scales cleanly into Kubernetes and cloud
environments.

---

## 1. Multi-Stage Builds

This is one of the highest-value Docker skills for Java and Spring Boot.

The problem:

- build tools and source code are needed during image build
- they are not needed in the final runtime image
- keeping them in the final image makes the image larger and noisier

The solution:

- use one stage to build the application
- use another, smaller stage to run it

Example shape:

```dockerfile
FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Why it matters:

- smaller images
- faster pushes and pulls
- lower attack surface
- cleaner separation between build and runtime concerns

For Spring Boot specifically, this is often the difference between a reasonable
image and a bloated one.

---

## 2. Layering and Caching

Docker builds in layers.

If a layer does not change, Docker reuses it from cache.

That is why Dockerfiles should be ordered carefully.

Good idea:

- copy dependency descriptors first
- resolve dependencies
- copy application code later

This reduces rebuild time when only application code changes.

Related practices:

- use `.dockerignore`
- avoid copying the whole repository if not needed
- prefer slim runtime images

---

## 3. Do Not Run as Root

Running the application as `root` inside the container is a bad default.

It increases the blast radius if the process is compromised.

Prefer:

- create a dedicated application user
- switch with `USER`
- ensure the app only has the permissions it needs

Example:

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r -u 10001 appuser
COPY app.jar app.jar
RUN chown appuser:appuser /app/app.jar
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
```

This is a simple but valuable production habit.

---

## 4. Networks

Containers do not magically know how to talk to each other.

Docker networks provide that communication boundary.

Important practical ideas:

- containers on the same network can resolve each other by service/container name
- ports exposed to the host are different from ports used inside the network
- internal service-to-service calls should usually use the internal network

Example mental model:

- app container talks to `postgres:5432`
- your laptop talks to `localhost:8080`

Those are different paths.

In Compose, service names become a simple DNS layer.

That is why `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/app` works
inside the network even when the host uses another port mapping.

---

## 5. Volumes

Containers are ephemeral.

If the container dies, its writable layer dies with it.

Volumes exist so data can outlive the container.

Use cases:

- Postgres data persistence
- local development files
- mounted config or certificates

Important distinction:

- bind mount: uses a path from your machine
- named volume: managed by Docker

Rule of thumb:

- bind mounts are convenient in development
- named volumes are cleaner for container-managed state like databases

---

## 6. Environment and Config Discipline

Do not bake environment-specific values into the image.

The same image should run in different environments with different config.

Use:

- environment variables
- mounted config files
- secret injection

This is one of the big bridges from Docker to Kubernetes and cloud platforms.

---

## 7. Health and Lifecycle

A container being "running" does not always mean the application is actually
ready.

For backend services, you should think about:

- startup time
- health endpoints
- graceful shutdown

This matters even more later in Kubernetes where readiness and liveness probes
depend on it.

---

## 8. Practical Minimums For You

If you are building a cloud lab, make sure your Docker side includes:

1. a multi-stage Dockerfile
2. a non-root runtime user
3. sane environment/config handling
4. at least one named volume for persistent data
5. clear network understanding between services

That is already a strong foundation.

---

## Interview Framing

You can say something like:

> For me, Docker is not just a way to start a service locally. I care about
> image size, build/runtime separation, running as non-root, and understanding
> how networking and persistence behave. Those practices make the move from local
> Compose to Kubernetes much cleaner.

---

## Further Reading

- Docker overview:
  https://docs.docker.com/get-started/docker-overview/
- Multi-stage builds:
  https://docs.docker.com/build/building/multi-stage/
- Docker volumes:
  https://docs.docker.com/engine/storage/volumes/
- Docker networking:
  https://docs.docker.com/engine/network/
- Spring Boot container images:
  https://docs.spring.io/spring-boot/reference/packaging/container-images/
