# Docker Image and Runtime Practices

An image packages an application with the files it needs to run. A container
is a running instance of that image. Packaging makes an artifact repeatable,
but the process still needs correct configuration, permissions, resource
behavior, and lifecycle handling.

## Build a Small, Reviewable Image

- Separate build tools from runtime files with a multi-stage build when the
  application needs compilation or asset generation.
- Copy only required files; use a `.dockerignore` so source secrets, caches,
  and local artifacts do not enter the build context.
- Pin or deliberately update base images, then rebuild to receive security
  fixes. An old image does not update itself.
- Run the process as a non-root user when it does not need elevated access.

Example: a service needs a compiler to produce a binary, but production only
needs that binary and its runtime libraries. The final stage copies those
artifacts and omits the compiler and source tree. The gain is a smaller
runtime surface; the cost is maintaining the build stages.

## Keep Runtime Inputs Explicit

Use the same image in each environment and supply environment-specific
configuration at runtime. Do not bake production credentials into the image
or log them at startup. Decide which writable data is temporary and which
must persist outside the container. A container filesystem is not a durable
database.

Publish only the ports that need external access. Containers on one network
can use service names rather than hard-coded instance addresses. In a local
stack, [Docker Compose](./07-docker-compose-local-stacks.md) makes those
dependencies visible.

## Handle the Process Lifecycle

The main process should receive shutdown signals, stop taking new work, and
finish or safely abandon in-flight work within a known period. Health checks
should answer a useful operational question; a running process can still be
unable to serve traffic.

Set resource expectations from the workload. A container can be terminated
for memory pressure or become slow under CPU limits even if the application
code itself has not changed. See
[container sizing](../cloud/04-container-sizing.md) and
[Kubernetes runtime basics](./04-kubernetes-crash-course.md).

## Review Questions

- Is the image reproducible and limited to files needed at runtime?
- Can it run without root privileges?
- Are secrets and environment settings supplied safely at runtime?
- What happens to writable data when the container is replaced?
- Does shutdown preserve or safely retry in-flight work?

## Further Reading

- [Docker multi-stage builds](https://docs.docker.com/build/building/multi-stage/)
- [Docker build best practices](https://docs.docker.com/build/building/best-practices/)
