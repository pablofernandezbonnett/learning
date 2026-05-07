# Migration Status

This repository was created as the public learning-focused destination for reusable study material.

## Scope Rule

Include:

- topic refresh notes
- fundamentals that help with real backend work
- runnable examples and small labs
- reusable system design material

Exclude:

- job-hunting material
- company-specific interview packets
- candidate-profile material
- private workspace references
- interview-process guidance that is not useful as learning content

## Wave 1

Wave 1 is the backend core:

- API
- Architecture
- Databases
- Security
- Spring Boot
- Java
- Kotlin
- Testing
- Cloud
- DevOps
- Algorithms
- selected system design material

## Migrated So Far

The first migrated set already present in this repo is:

- Java topic notes plus a small Java `21` lab
- Kotlin topic notes plus standalone Kotlin basics labs
- selected system design material migrated out of the old interview-prep area
- Spring Boot notes plus a small Spring Boot sample
- Databases notes on idempotency, concurrency, and SQL refresh
- API notes on design, brokers, and webhooks
- Architecture notes on service boundaries, resiliency, distributed flows, networking, and tracing
- Cloud notes on runtime models, Kubernetes, serverless, and container sizing
- DevOps notes on delivery, rollout, observability, and Kubernetes basics
- Testing notes on test strategy and code review judgment
- a first migrated glossary

The expanded set now also includes:

- Cloud growth-lane material such as local Kubernetes lab setup and quick cheatsheets
- API expansion for REST vs GraphQL, GraphQL internals, and brokers quick review
- Database expansion for scaling, Redis, MongoDB, Postgres, DynamoDB, query optimization, and delivery tradeoffs
- Spring Boot expansion for container internals, bean lifecycle, proxies, auto-configuration, profiles, caching, Spring Data, Kotlin idioms, AppSec labs, Flyway, and Spring Cloud integration
- Architecture expansion for caching, concurrency models, DDD, consistent hashing, service discovery, and leader-election basics
- SRE material for backend engineers covering role boundaries, `SLO` thinking, alerting, incidents, capacity, and postmortems
- Solutions-architecture material for backend engineers covering `NFRs`, build-vs-buy, tenancy, migration strategy, and `ADR`-driven decision communication
- Secondary language refresh tracks for Go, Python, Lua, and Dart

## Coverage Rule

The goal is to preserve useful material across the old `refresh`, `required`, and `growth`
classifications whenever the topic is still valuable as public learning material.

Runnable companions are included when they help reopen the mental model instead
of forcing the repo to depend only on static notes.

## System Design Exception

Most of the old `InterviewPrep/` area stays out.

Exception:
selected material from `InterviewPrep/DesignSystems/` will be migrated because it is useful for studying backend flows, invariants, correctness, and system design tradeoffs.

## Cleanup Rules Before Material Is Copied In

- remove interview-only framing from titles and section headers
- remove company-specific overlays unless they are the actual topic
- remove links to private or local-only paths
- correct links that still point at the old repo structure
- rename interview-branded package names in runnable examples
- prefer neutral domain examples such as payment platform, checkout flow, or retail backend
