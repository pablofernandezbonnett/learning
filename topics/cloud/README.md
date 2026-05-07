# Cloud for Backend Engineers

Use this folder to learn cloud from the runtime model first, not from provider
product names alone.
The practical question is always the same: which part of the system you own,
which part the platform owns, and what operational tradeoff comes with that
boundary.

Why this folder matters:

- cloud choices are often disguised runtime and ownership choices
- many backend discussions get vague because people name products before they
  explain responsibility or failure shape
- this folder keeps the discussion grounded in compute model, service shape, and
  operational burden

Focus:

- responsibility boundaries
- compute models
- Kubernetes and infrastructure basics
- serverless tradeoffs
- container sizing and observability

Working style:

- explain provider or platform jargon in practical terms before going deeper
- keep the focus on ownership and runtime behavior, not product-catalog memorization
- connect cloud choices back to deployment risk, operability, and service shape

Smallest mental model:

- first choose the runtime shape
- then choose how much operational responsibility the team should own
- then judge the tradeoff in cost, control, latency, and operability

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

- [05-local-kubernetes-lab.md](./05-local-kubernetes-lab.md): a hands-on local cluster setup when the basic concepts are already clear
- [06-container-sizing-cheatsheet.md](./06-container-sizing-cheatsheet.md): a compact reopen sheet for memory, CPU, and runtime sizing judgment
- [07-serverless-cheatsheet.md](./07-serverless-cheatsheet.md): a short decision sheet for function-shaped workloads, async boundaries, and serverless tradeoffs

## Core Rule

- cloud choices are usually responsibility choices
- start with the runtime model before memorizing product names
- observability and sizing belong in the same discussion as deployment
