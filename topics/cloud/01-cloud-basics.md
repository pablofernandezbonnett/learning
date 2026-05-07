# Cloud Basics for Backend Engineers

> Primary fit: `Shared core`


You do not need to become a cloud specialist.
You do need a simple mental model for where backend code runs, where data lives,
and why teams prefer managed services over building everything themselves.

This is the shortest useful cloud refresher for the current repo.

---

## Why This Matters

Cloud basics matter because many backend design decisions are really decisions
about who owns operational responsibility, not just where the code is hosted.

This matters in work and interviews because weak cloud answers often list AWS
products, while stronger answers explain what the team is choosing to manage
themselves and what they are delegating to the platform.

## Smallest Mental Model

Cloud is a responsibility-allocation model.

The practical first question is:

- what does the platform manage for us
- what does the application team still have to own and operate

## Bad Mental Model vs Better Mental Model

Bad mental model:

- cloud knowledge means memorizing vendor services
- moving to the cloud automatically removes operational complexity
- managed service choice is mostly a pricing or convenience detail

Better mental model:

- cloud design is mainly about responsibility boundaries
- managed services remove some work but also change control, coupling, and
  failure modes
- the useful comparison is who owns servers, runtime, scaling, backups, and
  failover

Small concrete example:

- weak approach: "we run Postgres in AWS, so we are cloud-native"
- better approach: "we use a managed relational database because backups,
  failover, and patching are not something the product team should hand-operate"

Strong default:

- explain cloud choices in terms of responsibility, operational burden, and fit
  for the workload, not just provider names

Interview-ready takeaway:

> I treat cloud basics as responsibility choices: who manages compute, data,
> scaling, and failover, and which parts still belong to the application team.

---

## 1. What Cloud Basics Actually Mean

Cloud is not mainly about memorizing provider product names.
It is about choosing the right level of responsibility.

Typical questions behind cloud design are:

- who manages the server?
- who manages the database engine?
- who manages scaling and failover?
- what do we still own as the application team?

Short rule:

> cloud decisions are usually decisions about responsibility boundaries

---

## 2. The Service Spectrum: IaaS, PaaS, SaaS

### IaaS

You get infrastructure primitives such as virtual machines and networking.
You manage more of the runtime yourself.

Example:

- AWS EC2

Pros:

- more control
- useful when the runtime needs special tuning

Tradeoffs / Cons:

- more operational burden
- patching, scaling, and runtime management stay closer to your team

### PaaS

You bring the application code and the platform manages more of the runtime.

Examples:

- Heroku
- Elastic Beanstalk
- App Runner

Pros:

- faster to start
- less platform work for the app team

Tradeoffs / Cons:

- less control
- some runtime or networking constraints

### SaaS

You consume a finished product instead of building the capability yourself.

Examples:

- Auth0
- Stripe

Pros:

- fastest path to capability
- reduced maintenance

Tradeoffs / Cons:

- external dependency and vendor coupling
- less control over internals and roadmap

---

## 3. Compute: Where The Code Runs

### Virtual machines

This is the "host the server yourself" cloud shape.

Use when:

- you need strong control
- you run legacy systems
- the team already owns server-level operations

### Managed containers

You package the app as a container and let the platform run it.

Examples:

- ECS
- EKS
- Fargate

Use when:

- you want predictable packaging
- you need replicas and service-style deployments
- you want a strong default for backend APIs

Pros:

- good fit for typical backend services
- cleaner scaling and deployment model

Tradeoffs / Cons:

- still need to think about memory, CPU, health, and runtime behaviour

### Serverless functions

Code runs in response to events and the platform scales it for you.

Example:

- Lambda

Use when:

- traffic is bursty or event-driven
- the unit of work is small and isolated
- scaling to zero is useful

Pros:

- low idle cost
- good fit for sporadic event handlers

Tradeoffs / Cons:

- cold starts
- execution limits
- database connection and idempotency issues become important

---

## 4. Managed Databases And Caches

For most backend teams, the practical default is:

- use managed databases
- avoid self-hosting unless you have a strong reason

### Managed relational database

Examples:

- RDS
- Aurora

Why teams choose them:

- backups
- failover options
- patching support
- read replicas

### Managed NoSQL database

Example:

- DynamoDB

Why teams choose it:

- high scale
- low operational burden
- good fit for key-driven access patterns

### Managed cache

Example:

- ElastiCache / Redis

Why teams choose it:

- hot read performance
- shared low-latency cache or coordination state

Practical rule:

> the cloud value is often not "the database exists in the cloud" but "the team is not
> hand-operating backups, replicas, and failover on raw VMs"

---

## 5. Object Storage And CDN

### Object storage

Use object storage for:

- images
- PDFs
- CSV files
- uploads

Example:

- S3

Good mental model:

- store large unstructured files outside the relational database

### CDN

A CDN caches content closer to users and reduces origin load.

Use when:

- you serve images, JS, CSS, or cacheable public content globally

Examples:

- CloudFront
- Cloudflare

Pros:

- lower user latency
- reduced origin load

Tradeoffs / Cons:

- cache invalidation and freshness still need thought

---

## 6. The Smallest Practical Architecture

A clean cloud baseline is:

- backend service runs in managed containers
- relational data lives in a managed Postgres-compatible service
- files live in object storage
- cache lives in managed Redis if needed
- public traffic enters through a load balancer or gateway
- static assets go through a CDN

That is already a credible cloud story for many backend systems.

---

## 7. Real Use Cases

### Retail system

- managed containers for backend services
- managed Postgres-compatible database for orders and core transactional data
- CDN and object storage for catalog assets
- cache for hot read paths

### Payment backend

- managed compute for APIs and workers
- managed relational store for money-sensitive data
- queues or events for async boundaries
- observability and scaling matter more than memorizing every AWS service name

---

## 8. The Big Traps

1. **Choosing technology by provider popularity instead of workload**
   Example: picking Lambda for every JVM service regardless of cold-start cost.

2. **Self-hosting databases without a strong reason**
   Example: running Postgres on raw EC2 when the team does not want that operational load.

3. **Treating cloud as only compute**
   Example: forgetting storage, observability, config, and network boundaries.

4. **Memorizing product names without understanding the runtime model**
   Example: naming ECS, EKS, and Lambda without knowing why one fits better.

---

## 9. Practical Summary

Good short answer:

> I think about cloud first in terms of responsibility boundaries. For a typical backend
> service I prefer managed compute, managed databases, object storage for files, and a
> CDN for static or cacheable content. The goal is not to memorize every provider product,
> but to choose the right level of operational ownership.
