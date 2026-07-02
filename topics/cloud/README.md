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
- AWS minimums and managed runtime choices
- Kubernetes and infrastructure basics as awareness, not as the default endpoint
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

Decision boundary for cloud material:

- use `01` for the runtime and responsibility mental model
- use `08` for the AWS minimums that matter most for a senior backend engineer
- use `04` when the service already runs in containers and you need sizing or observability judgment
- use `03` only when the workload is truly trigger-shaped or bursty enough for serverless tradeoffs to matter
- use `02` as awareness-only material for collaboration with platform teams, not as a primary target right now
- use `06` and `07` only as short reopen companions after the full notes

## Recommended Order

1. [01-cloud-basics.md](./01-cloud-basics.md): the shared mental model behind regions, networks, managed services, and responsibility boundaries
2. [08-aws-for-backend-engineers.md](./08-aws-for-backend-engineers.md): the AWS minimums that matter most for backend work
3. [04-container-sizing-and-observability.md](./04-container-sizing-and-observability.md): how memory, CPU, request load, and visibility interact once the service is running
4. [03-serverless-for-backend-engineers.md](./03-serverless-for-backend-engineers.md): when functions and managed runtimes speed you up and when they become awkward
5. [02-kubernetes-and-terraform-for-backend-engineers.md](./02-kubernetes-and-terraform-for-backend-engineers.md): when container orchestration and infrastructure-as-code help, and what they cost

## Refresh

- [01-cloud-basics.md](./01-cloud-basics.md)
- [04-container-sizing-and-observability.md](./04-container-sizing-and-observability.md)
- [08-aws-for-backend-engineers.md](./08-aws-for-backend-engineers.md)

## Required

- [08-aws-for-backend-engineers.md](./08-aws-for-backend-engineers.md)
- [04-container-sizing-and-observability.md](./04-container-sizing-and-observability.md)

## Growth

- [02-kubernetes-and-terraform-for-backend-engineers.md](./02-kubernetes-and-terraform-for-backend-engineers.md): awareness-only for collaboration with platform or infra-heavy teams
- [03-serverless-for-backend-engineers.md](./03-serverless-for-backend-engineers.md)
- [05-local-kubernetes-lab.md](./05-local-kubernetes-lab.md): a hands-on local cluster setup only if the basic concepts are already relevant to current work
- [06-container-sizing-cheatsheet.md](./06-container-sizing-cheatsheet.md): reopen-only companion for `04`
- [07-serverless-cheatsheet.md](./07-serverless-cheatsheet.md): reopen-only companion for `03`

## Core Rule

- cloud choices are usually responsibility choices
- start with the runtime model before memorizing product names
- observability and sizing belong in the same discussion as deployment
