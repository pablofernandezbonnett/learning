# Learning Repository

A curated repository for practical backend learning and refresh.

The goal is to keep useful technical notes, examples, and small runnable labs in one
place so you can reopen a topic quickly without depending on scattered articles,
random bookmarks, or ten different explanations of the same thing.

This repo is meant to be useful when you want to revisit topics such as:

- Spring Boot exception handling
- transactions, isolation, and idempotency
- JVM memory, concurrency, and modern Java
- Kotlin backend idioms
- API design and messaging semantics
- system design tradeoffs
- container sizing, observability, and deployment basics

## What You Will Find

- topic-first notes written for fast reopening
- practical examples in Kotlin and Java
- small runnable labs and companion code
- curated explanations focused on tradeoffs, failure modes, and day-to-day backend decisions

The general writing style is intentionally practical:

- keep the first useful meaning of jargon visible
- prefer short explanations over shorthand that assumes too much context
- connect theory back to real backend flows such as checkout, payments, inventory, and integrations

## Usage

This repository is shared as a curated learning and reference base.

- read it, reuse it for study, and adapt it for your own learning
- if you want your own version, fork it and customize it around your own topics and priorities
- commercial use is not allowed

For contribution and usage details, see `CONTRIBUTING.md`.
Licensing details are in `LICENSE`.

## Start Here

Use one of these entry points depending on what you need:

- `paths/general-refresh.md`: a broad backend refresh path
- `paths/appsec-for-software-engineers.md`: a practical AppSec path for developers
- `topics/`: topic-by-topic notes, usually split into `Refresh`, `Required`, and `Growth`
- `labs/`: runnable companion material

## Topic Index

Jump directly into a topic entry point:

- [Java](./topics/java/README.md)
- [Kotlin](./topics/kotlin/README.md)
- [Spring Boot](./topics/spring-boot/README.md)
- [Databases](./topics/databases/README.md)
- [API Design](./topics/api/README.md)
- [Architecture](./topics/architecture/README.md)
- [System Design](./topics/system-design/README.md)
- [Security](./topics/security/README.md)
- [AppSec](./topics/appsec/README.md)
- [Cloud](./topics/cloud/README.md)
- [DevOps](./topics/devops/README.md)
- [Testing](./topics/testing/README.md)
- [Algorithms](./topics/algorithms/README.md)
- [Go](./topics/go/README.md)
- [Python](./topics/python/README.md)
- [Dart](./topics/dart/README.md)
- [Lua](./topics/lua/README.md)

## Current Focus

The first migration wave is centered on:

- [Java](./topics/java/README.md)
- [Kotlin](./topics/kotlin/README.md)
- [Spring Boot](./topics/spring-boot/README.md)
- [Databases](./topics/databases/README.md)
- [API design](./topics/api/README.md)
- [Architecture](./topics/architecture/README.md)
- [System design](./topics/system-design/README.md)
- [Security](./topics/security/README.md)
- [Cloud](./topics/cloud/README.md)
- [DevOps](./topics/devops/README.md)
- [Testing](./topics/testing/README.md)
- [Algorithms](./topics/algorithms/README.md)

See `docs/migration-status.md` for the current migration scope and cleanup rules.

Most topic folders use the same simple layering:

- `Refresh`: the shortest useful reopen order when you are rusty
- `Required`: the topics that show up often in real backend work
- `Growth`: deeper material worth studying once the core feels stable

When a topic has runnable companion material, the matching `README` should also
point you to the relevant lab instead of leaving the concept as static notes only.

## Contributions

This repository is intentionally kept read-only by default.

If you want to improve or adapt something:

- fork it and customize it for your own interests
- if you want to contribute back directly, contact me and I can add you as a contributor

The goal is to keep the material curated, consistent, and easy to trust as a practical reference.
