# System Design Decision Cheatsheet

> Primary fit: `Shared core`

Use this note when you already know the business flow and now need to choose the
main building blocks without getting lost in jargon.

This is not a deep-dive document.
It is a practical decision layer that connects the rest of the repo.

Shortest rule:

> first understand the business flow, then choose the simplest tools that
> protect correctness, keep the system understandable, and fit the load

This helps with questions like:

- should this stay a modular monolith
- should one database be enough or should each service own its own database
- do I need SQL or NoSQL here
- should this be synchronous or asynchronous
- do I need a queue or just an HTTP call
- should internal service calls use REST or gRPC
- should I add Redis
- should I use a read replica or a cache
- should I use REST or GraphQL
- do I need webhooks or polling
- is serverless actually a good fit

When terms such as `modular monolith`, `read replica`, or `eventual consistency`
appear here, the goal is not to assume the jargon is already warm.
The goal is to explain the smallest practical meaning before turning it into a choice.

---

## 1. How To Use This Cheat Sheet

For each choice, ask these questions first:

1. what must be strongly correct
2. what can be slightly delayed or stale
3. what is the busiest part of the system
4. what part is hardest to change later
5. what is the simplest option that still works

If those answers are unclear, do not choose technology yet.

---

## 2. Modular Monolith vs Microservices

### Modular monolith

A modular monolith is usually the best first serious backend shape.
You still separate responsibilities, but you avoid paying the full cost of
network calls, duplicated data, and service operations too early.

What it is:

- one application
- one deployment unit
- clear internal modules
- often one main database

Use it when:

- the product is still changing fast
- the team is still small or closely aligned
- the boundaries are not stable yet
- one local transaction still helps a lot

Pros:

- simpler deployment
- easier debugging
- easier local development
- fewer network and ownership problems

Tradeoffs:

- one release can affect many areas
- scaling one hot path may force you to scale more than you want
- code boundaries need discipline or the app becomes messy

Common mistake:

- calling something a monolith when the real problem is weak internal boundaries
- splitting too early instead of fixing module ownership first

### Microservices

Microservices help when the main pain is no longer code size alone, but shared
deployment, ownership conflicts, or very different scaling needs between domains.
The win is clearer boundaries, not just "more services".

What it is:

- several deployable services
- each service owns one business area
- each service should own its own data

Use it when:

- team boundaries are real and stable
- some parts need very different scaling
- release independence matters a lot
- one deployable app is slowing the organisation down

Pros:

- better team ownership
- independent deployment
- more targeted scaling

Tradeoffs:

- more operational work
- harder debugging
- more network failure cases
- no single transaction across the whole workflow

Common mistake:

- choosing microservices because they sound scalable
- forgetting that communication, retries, and data consistency become harder

Practical rule:

> start with a modular monolith unless there is a strong reason not to

Related reading:

- [../architecture/01-monolith-vs-microservices.md](../architecture/01-monolith-vs-microservices.md)
- [../architecture/03-distributed-transactions-and-events.md](../architecture/03-distributed-transactions-and-events.md)
- [../architecture/09-domain-driven-design.md](../architecture/09-domain-driven-design.md)

---

## 3. SQL vs NoSQL

The first important correction is simple:

- `NoSQL` is not one thing
- MongoDB and Redis are not solving the same problem

### SQL

SQL is the safe default when you are protecting business truth.
If several records are related and the relationship itself matters, relational
storage usually makes the design simpler and safer.

Use it when:

- correctness matters
- the data is related
- transactions matter
- joins and constraints matter

Good fits:

- orders
- payments
- inventory truth
- account and ledger-like data

Pros:

- strong correctness tools
- mature queries
- easy to defend for business-critical data

Tradeoffs:

- high write scale needs more care
- schema changes need more discipline

Typical design smell:

- avoiding SQL just because the system may grow later
- treating relational constraints as optional when they are really protecting the business

### Document NoSQL

Document databases are strongest when the application naturally reads and writes
one object as one object.
They help when the record shape is flexible, but they do not magically remove
the need to think about consistency.

Use it when:

- one record is usually read as one whole document
- the shape varies a lot
- denormalized reads are more important than joins

Good fits:

- product catalog
- content data
- profile-like records

Pros:

- flexible structure
- simple one-record reads

Tradeoffs:

- duplicated data can drift
- cross-record correctness is harder

Typical design smell:

- using a document store for money-sensitive or relational workflows only because schema changes feel easier

Practical rule:

> use SQL for business truth, and document databases when the read shape matters
> more than relational rules

Related reading:

- [../databases/04-sql-vs-nosql.md](../databases/04-sql-vs-nosql.md)
- [../databases/10-postgres-in-depth.md](../databases/10-postgres-in-depth.md)

---

## 4. Postgres vs MongoDB vs Redis

This choice is often clearer than the broad `SQL vs NoSQL` label.

### Postgres

Postgres is usually the strongest default for the commit path.
When an answer must be trustworthy after a failure or retry, Postgres is often
the place you want that final state to live.

Use it when:

- you need a reliable source of truth
- related data changes together
- correctness matters more than raw speed

Good fits:

- orders
- payments
- inventory

### MongoDB

MongoDB is easier to justify when the document shape maps cleanly to how the
application already thinks.
If the frontend or service usually wants the whole nested record in one go, it
can be a natural fit.

Use it when:

- the data is naturally document-shaped
- one read often needs the whole record
- schema flexibility helps the product

Good fits:

- product catalog
- content
- profile-style documents

### Redis

Redis is often added for a good reason, but it should usually stay in a support role.
It is excellent for fast temporary state, but dangerous when teams slowly let it
become the only place where important truth exists.

Use it when:

- the real problem is speed
- you need cache, counters, sessions, or short-lived shared state

Good fits:

- rate limiting
- cache
- session storage
- fast temporary coordination

Important warning:

> Redis is usually a support layer, not the final source of truth for payment
> or order state

Practical rule:

> Postgres usually owns truth, MongoDB usually owns document-shaped application
> data, and Redis usually speeds things up or coordinates short-lived state

Related reading:

- [../spring-boot/14-datastore-choice-postgres-mongo-redis.md](../spring-boot/14-datastore-choice-postgres-mongo-redis.md)
- [../databases/06-redis-in-depth.md](../databases/06-redis-in-depth.md)
- [../databases/07-mongodb-in-depth.md](../databases/07-mongodb-in-depth.md)

---

## 5. Single Database vs Database Per Service

This choice usually appears once a system starts splitting into several services.
It is not only a storage question. It is also a boundary, ownership, and change-safety question.

### Single database

Use it when:

- the system is still one application
- the boundaries are still moving
- one local transaction is very useful
- the team is still working closely together

Pros:

- simpler transactions
- easier reporting and joins
- simpler local development

Tradeoffs:

- teams can step on each other through shared tables
- one schema change can affect many parts of the system
- service boundaries stay blurry if everything shares the same data

Typical design smell:

- calling something microservices while several services still write the same tables directly

### Database per service

Use it when:

- service boundaries are stable
- teams need stronger ownership
- independent deployment matters
- you are ready to handle cross-service coordination explicitly

Pros:

- cleaner ownership
- fewer accidental cross-service couplings
- easier to evolve one service without breaking another's tables

Tradeoffs:

- no single transaction across the whole workflow
- reporting and cross-service queries get harder
- events, outbox, and idempotency become much more important

Typical design smell:

- splitting services but keeping one shared database because it feels easier, which keeps most of the coupling while adding network complexity on top

Practical rule:

> one application often means one main database; real service boundaries usually
> mean each service owns its own data

Related reading:

- [../architecture/01-monolith-vs-microservices.md](../architecture/01-monolith-vs-microservices.md)
- [../architecture/03-distributed-transactions-and-events.md](../architecture/03-distributed-transactions-and-events.md)
- [../architecture/09-domain-driven-design.md](../architecture/09-domain-driven-design.md)

---

## 6. Synchronous vs Asynchronous

### Synchronous

Synchronous work is easier to explain because the caller sees the result in one flow.
That simplicity is valuable, but it also means any slowness or failure downstream
hits the user path directly.

Meaning:

- the caller waits for the answer now

Use it when:

- the user needs the result immediately
- the next step depends on the answer
- the work is short and reliable enough

Good fits:

- login
- pricing lookup during checkout
- reading account details

Pros:

- simpler to understand
- simpler request flow

Tradeoffs:

- caller is blocked
- failures and slowness propagate quickly

Typical design smell:

- keeping too much work in the request path just because it feels simpler on day one

### Asynchronous

Asynchronous work is usually where resilience starts.
You accept that some actions finish later so the main request can stay fast and
so retries, delays, and downstream failures do not break the whole user flow.

Meaning:

- the work continues later
- the first response may be `accepted`, `pending`, or just quick acknowledgement

Use it when:

- work is slow
- retries are normal
- many consumers care about the result
- the user does not need final completion in that first request

Good fits:

- email sending
- fulfillment updates
- webhook-driven processing
- analytics and notifications

Pros:

- better resilience
- better load smoothing
- looser coupling

Tradeoffs:

- harder to trace
- state is harder to explain
- you must design retries and deduplication

Typical design smell:

- making work async without clear state transitions, so the system becomes vague instead of resilient

Practical rule:

> keep the customer-critical answer path synchronous when needed, and move slow
> or failure-prone follow-up work to asynchronous processing

Related reading:

- [system-design-guide.md](./system-design-guide.md)
- [lifecycles-and-flows-cheatsheet.md](./lifecycles-and-flows-cheatsheet.md)
- [../api/02-message-brokers-and-delivery-semantics.md](../api/02-message-brokers-and-delivery-semantics.md)

---

## 7. Direct HTTP Call vs Broker

### Direct HTTP call

Use direct calls when the caller truly needs the answer as part of the same decision.
This is the normal fit for request-response dependencies, not for every kind of
cross-system communication.

Use it when:

- the caller needs an answer now
- one service needs one response from one other service
- the operation is request-response by nature

Good fits:

- fetch customer profile
- calculate price
- validate permissions

Pros:

- simple mental model
- easy to trace request-response

Tradeoffs:

- the caller waits
- downstream slowness hurts the caller directly

Typical design smell:

- chaining many direct calls in one user request until one slow dependency drags everything down

### Broker

A broker is useful when the system should absorb delay, retries, or fan-out more gracefully.
It is not just "extra infrastructure"; it is a way to move some work out of the
critical request path.

Use it when:

- the caller should not wait
- many systems care about the same event
- traffic needs buffering
- retries should happen outside the user request

Good fits:

- order created
- payment confirmed
- send email
- warehouse notification

Pros:

- decouples systems
- handles spikes better
- supports independent consumers

Tradeoffs:

- more moving parts
- duplicates are normal
- ordering needs thought

Typical design smell:

- adding a broker for everything, even where a simple direct call would be clearer

Practical rule:

> use direct calls for immediate answers, and brokers for follow-up work or
> shared business events

Related reading:

- [../api/02-message-brokers-and-delivery-semantics.md](../api/02-message-brokers-and-delivery-semantics.md)
- [../spring-boot/20-spring-cloud-and-service-integration.md](../spring-boot/20-spring-cloud-and-service-integration.md)

---

## 8. Internal REST vs gRPC

This choice matters only when one backend talks to another backend.
Browsers and public APIs usually stay on normal HTTP and JSON, so this is mainly an internal-service decision.

### Internal REST

Use it when:

- the team wants maximum familiarity
- observability and debugging simplicity matter a lot
- the payloads are not especially performance-sensitive
- the same API may later be exposed beyond one internal client

Pros:

- easy to inspect
- widely understood
- simple fit for mixed teams and tooling

Tradeoffs:

- more payload overhead
- weaker typed contracts unless you add extra tooling
- can become noisy if one backend calls many others repeatedly

### gRPC

Use it when:

- service-to-service calls are frequent
- typed contracts are valuable
- low-latency internal communication matters
- one gateway or BFF fans out to several backend services

Pros:

- strong contracts
- compact payloads
- good fit for backend-to-backend calls

Tradeoffs:

- worse fit for browsers
- debugging by hand is less friendly
- can add complexity if the team does not really need it

Typical design smell:

- forcing gRPC in a system that is still basically one deployable app
- using REST everywhere internally even when one service is doing heavy fan-out to many typed backends

Practical rule:

> use REST by default for simpler internal calls, and use gRPC when internal
> fan-out, typed contracts, or lower-overhead backend calls make the extra
> complexity worth it

Related reading:

- [../architecture/01-monolith-vs-microservices.md](../architecture/01-monolith-vs-microservices.md)
- [../spring-boot/20-spring-cloud-and-service-integration.md](../spring-boot/20-spring-cloud-and-service-integration.md)
- [worked-diagrams.md](./worked-diagrams.md)

---

## 9. Queue vs Event Stream

This is a more specific broker choice.

### Queue

A queue is best when the message is really just a unit of work.
One side hands off a task, and another side processes it without the sender
needing to wait.

Use it when:

- the message is mainly a task
- one processing path owns the work

Good fits:

- send email
- generate report
- import file

Pros:

- simple mental model
- good for background jobs

Tradeoffs:

- weaker fit when many teams need the same event
- replay is usually not the main feature

### Event stream

An event stream fits when the event has value beyond one worker.
Several consumers may care about it now, and new consumers may need to replay it later.

Use it when:

- the event matters to several consumers
- replay is useful
- the event itself has long-term value

Good fits:

- order placed
- payment settled
- stock updated

Pros:

- several consumers can react independently
- history can be replayed

Tradeoffs:

- higher complexity
- partitioning and ordering need care

Typical design smell:

- using a durable event stream when the system really just needed one background job

Practical rule:

> queue for work items, event stream for domain events

Related reading:

- [../api/02-message-brokers-and-delivery-semantics.md](../api/02-message-brokers-and-delivery-semantics.md)

---

## 10. Cache vs Database Read

Do not ask only:

> can this be faster

Also ask:

> can this be stale

### Cache

Cache helps when the same answer is fetched repeatedly and perfect freshness is not required.
It is mainly a performance tool, so the design question is not only speed but
also how much staleness the business can safely tolerate.

Use it when:

- the same reads happen again and again
- a little staleness is acceptable
- the database is getting hammered by repeated reads

Good fits:

- product pages
- catalog search helpers
- reference data

Pros:

- lower latency
- less database pressure

Tradeoffs:

- stale data risk
- invalidation complexity

### Direct database read

Direct database reads are often the right choice on correctness-critical paths.
If the business cannot tolerate stale answers, the extra latency is often a fair price.

Use it when:

- correctness matters more than speed
- the latest state must be trusted
- the query volume is acceptable

Good fits:

- final order state
- final payment state
- inventory commit path

Typical design smell:

- caching the final truth of a payment or order just because the read path became hot

Practical rule:

> cache hot reads, but do not hide critical business truth only in cache

Related reading:

- [../architecture/07-caching-strategies.md](../architecture/07-caching-strategies.md)
- [../databases/06-redis-in-depth.md](../databases/06-redis-in-depth.md)
- [../databases/05-database-scaling.md](../databases/05-database-scaling.md)

---

## 11. Read Replica vs Cache

These two are easy to confuse because both can reduce pressure on the primary database.
The real difference is that a read replica is still a database copy, while a cache is a faster temporary layer.

### Read replica

Use it when:

- read traffic is high
- you still want normal database queries
- the application can tolerate slight replication lag

Pros:

- offloads read traffic from the primary
- keeps the same query model
- useful when the problem is broad read scale, not just a few hot keys

Tradeoffs:

- stale reads are still possible
- usually slower than cache
- still costs real database resources

### Cache

Use it when:

- a few reads are especially hot
- very low latency matters
- some staleness is acceptable

Pros:

- much faster than a replica for hot keys
- reduces repeated work for the same read

Tradeoffs:

- invalidation becomes your problem
- cache misses still fall back somewhere else
- not a good place for final truth

Typical design smell:

- adding Redis when the real problem is broad read scale across many normal queries
- adding replicas when the real problem is a small set of very hot repeated reads

Practical rule:

> use read replicas when the database query itself is fine but read volume is
> too high; use cache when the same answers are repeated so often that you want
> to avoid the database call entirely most of the time

Related reading:

- [../databases/05-database-scaling.md](../databases/05-database-scaling.md)
- [../architecture/07-caching-strategies.md](../architecture/07-caching-strategies.md)
- [../databases/06-redis-in-depth.md](../databases/06-redis-in-depth.md)

---

## 12. REST vs GraphQL

### REST

REST is usually the simplest place to start because the model is easy to explain:
resources, URLs, HTTP methods, and predictable responses.
That makes it a strong default for public APIs and straightforward backend work.

Use it when:

- the API is resource-shaped
- the data shape is stable enough
- HTTP caching matters
- you want a simple, predictable public API

Pros:

- simple
- standard
- easy edge caching

Tradeoffs:

- clients may fetch too much or too little
- complex screens may need several calls

### GraphQL

GraphQL is strongest when the frontend has several screens with very different data needs.
Its main benefit is not that it is newer, but that the client can shape the response
more precisely.

Use it when:

- the frontend needs very different data shapes
- one screen pulls data from several places
- client teams benefit from asking for exactly what they need

Pros:

- flexible responses
- fewer round trips for complex views

Tradeoffs:

- more backend complexity
- caching is harder
- query abuse needs protection

Typical design smell:

- adopting GraphQL because of hype even though the API is simple and resource-shaped

Practical rule:

> default to REST unless the frontend really benefits from flexible data shaping

Related reading:

- [../api/00-rest-vs-graphql.md](../api/00-rest-vs-graphql.md)

---

## 13. Webhook vs Polling

### Webhook

Webhooks are useful when another system should push events to you as they happen.
They reduce the need to keep asking for updates, but they also mean your system
must tolerate duplicates, retries, and untrusted traffic.

Meaning:

- another system calls you when something happens

Use it when:

- you want event-driven updates
- lower delay matters
- the provider already supports webhooks

Pros:

- near real-time updates
- less wasted traffic

Tradeoffs:

- your endpoint must be robust
- duplicate delivery is normal
- signature verification matters

### Polling

Polling is less elegant, but often easier to control and simpler to ship.
It can be a perfectly reasonable answer when updates are infrequent or when the
other side does not support push-based integration well.

Meaning:

- your system keeps asking for updates

Use it when:

- the provider has no webhook support
- occasional updates are enough
- simpler integration is more important than immediacy

Pros:

- simple client control
- easier to reason about in small systems

Tradeoffs:

- wasted requests
- slower update visibility

Typical design smell:

- building constant aggressive polling where a webhook would clearly fit better

Practical rule:

> prefer webhooks for important external events, but use polling when the update
> frequency is low or the integration is simpler that way

Related reading:

- [../api/03-webhooks-basics.md](../api/03-webhooks-basics.md)
- [../spring-boot/17-webhook-idempotency-lab.md](../spring-boot/17-webhook-idempotency-lab.md)

---

## 14. Serverless vs Long-Running Service

### Serverless

Serverless is a runtime choice, not a system-design shortcut.
It works best when the unit of work is small, stateless, and uneven in traffic,
not when you are trying to hide a large always-on service inside functions.

Use it when:

- the workload is bursty
- the work is event-driven
- you want low ops overhead
- the unit of work is small and stateless

Good fits:

- webhook intake
- scheduled jobs
- file processing

Pros:

- low idle cost
- easy scaling for uneven traffic

Tradeoffs:

- cold starts
- runtime limits
- awkward fit for large stateful applications

### Long-running service

Long-running services are often the better fit for core product backends.
If traffic is steady, latency matters, and the application keeps doing useful
work all day, an always-on service is usually simpler.

Use it when:

- traffic is steady
- latency is sensitive
- the service is stateful or complex
- you need more control over runtime behavior

Good fits:

- main user-facing APIs
- large backend services

Pros:

- predictable runtime
- easier for large always-on systems

Tradeoffs:

- more operational ownership
- pay even when traffic is quiet

Typical design smell:

- forcing serverless onto a workload that clearly behaves like a normal backend service

Practical rule:

> use serverless for small event-driven work, not as a badge of modernity

Related reading:

- [../cloud/03-serverless-for-backend-engineers.md](../cloud/03-serverless-for-backend-engineers.md)

---

## 15. The Default Decision Ladder

When you design a new backend flow, this order works well:

1. define the business goal
2. define what must not break
3. choose the source of truth
4. decide what is synchronous and what is asynchronous
5. decide whether one app is enough or real service boundaries are needed
6. add cache only where repeated reads justify it
7. add brokers only where decoupling or buffering is real
8. keep retries, timeouts, and duplicate handling explicit

This order matters because later decisions depend on earlier ones.
For example, you cannot choose cache or messaging well if you still do not know
where the source of truth lives or which part of the flow must be synchronous.

This order prevents a common mistake:

> choosing technology before understanding the write path and failure path

---

## 16. Shortest Practical Summary

If you need the shortest useful reset:

1. start simple
2. keep truth in the right store
3. prefer clear boundaries over many components
4. use synchronous calls for immediate answers
5. use asynchronous processing for slow or failure-prone follow-up work
6. add cache for hot reads, not for final truth
7. split into microservices only when boundaries and ownership are truly worth it

That is the general pattern behind most solid system design answers.
The technology choice matters, but the main skill is matching each tool to the
real business need and failure mode.
