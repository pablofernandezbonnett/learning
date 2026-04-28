# Cloud for Backend Engineers

Use this folder to learn cloud from the runtime model first, not from provider
product names alone.
The practical question is always the same: which part of the system you own,
which part the platform owns, and what operational tradeoff comes with that
boundary.

Focus:

- responsibility boundaries
- compute models
- Kubernetes and infrastructure basics
- serverless tradeoffs
- container sizing and observability

## Recommended Order

1. [01-cloud-basics.md](./01-cloud-basics.md): the shared mental model behind regions, networks, managed services, and responsibility boundaries
2. [02-kubernetes-and-terraform-for-backend-engineers.md](./02-kubernetes-and-terraform-for-backend-engineers.md): when container orchestration and infrastructure-as-code help, and what they cost
3. [03-serverless-for-backend-engineers.md](./03-serverless-for-backend-engineers.md): when functions and managed runtimes speed you up and when they become awkward
4. [04-container-sizing-and-observability.md](./04-container-sizing-and-observability.md): how memory, CPU, request load, and visibility interact once the service is running

## Refresh

- [01-cloud-basics.md](./01-cloud-basics.md)
- [03-serverless-for-backend-engineers.md](./03-serverless-for-backend-engineers.md)
- [04-container-sizing-and-observability.md](./04-container-sizing-and-observability.md)

## Required

- [02-kubernetes-and-terraform-for-backend-engineers.md](./02-kubernetes-and-terraform-for-backend-engineers.md)

## Growth

- [05-local-kubernetes-lab.md](./05-local-kubernetes-lab.md)
- [06-container-sizing-cheatsheet.md](./06-container-sizing-cheatsheet.md)
- [07-serverless-cheatsheet.md](./07-serverless-cheatsheet.md)

## Core Rule

- cloud choices are usually responsibility choices
- start with the runtime model before memorizing product names
- observability and sizing belong in the same discussion as deployment
