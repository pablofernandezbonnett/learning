# Learning Repository

Practical backend learning and refresh materials in one place.

This repo exists for one main reason:

- turn scattered backend, AppSec, architecture, and reliability study into one reusable knowledge base

It is built for practical reopening, not for textbook-style completeness.
The goal is to help you answer questions such as:

- what is this topic really about?
- why does it matter in real systems?
- what is the strong default?
- what usually goes wrong?
- how would I explain or defend this in code review, design review, or interview?

What you will find here:

- topic-first notes for fast reopening
- practical docs that explain failure modes, tradeoffs, and better defaults
- small runnable labs in Java and Kotlin where code helps more than prose
- paths that give you a study order instead of a pile of disconnected notes

The repo is optimized for practical understanding, tradeoffs, failure modes,
and reusable mental models, not encyclopedic coverage.

## Who This Is For

Best fit:

- backend engineers who want a durable refresh repo
- engineers moving toward stronger system design, AppSec, SRE, or solutions-architecture judgment
- people who prefer plain-language notes with practical examples over abstract theory first

Less ideal fit:

- someone looking for full certification prep copied from one vendor course
- someone who wants theory completeness before practical use

## Start Here

Pick the entry point that matches your goal:

- `paths/general-refresh.md`: broad backend refresh
- `paths/java-spring-dart-flutter-refresh.md`: deep refresh across Java, Spring Boot, and Dart/Flutter-oriented work
- `paths/appsec-for-software-engineers.md`: AppSec from a developer point of view
- `paths/sre-from-backend-engineers.md`: backend to reliability ownership
- `paths/solutions-architect-from-backend.md`: backend to end-to-end solution design
- `topics/`: topic-by-topic entrypoints when you already know what you want to study
- `labs/`: runnable companion material

## How To Use This Repo

Strong default:

1. start with a path if your goal is broad or role-based
2. start with a topic folder if you already know the concept you want to reopen
3. use labs when the topic benefits from code and runtime behavior

Practical rule:

- use `paths/` for study order
- use `topics/` for the durable source notes
- use `labs/` for hands-on reinforcement

## Main Topics

- [Java](./topics/java/README.md)
- [Kotlin](./topics/kotlin/README.md)
- [Spring Boot](./topics/spring-boot/README.md)
- [Databases](./topics/databases/README.md)
- [API Design](./topics/api/README.md)
- [Architecture](./topics/architecture/README.md)
- [System Design](./topics/system-design/README.md)
- [Security](./topics/security/README.md)
- [AppSec](./topics/appsec/README.md)
- [SRE](./topics/sre/README.md)
- [Solutions Architecture](./topics/solutions-architecture/README.md)
- [Cloud](./topics/cloud/README.md)
- [DevOps](./topics/devops/README.md)
- [Testing](./topics/testing/README.md)
- [Algorithms](./topics/algorithms/README.md)
- [Go](./topics/go/README.md)
- [Python](./topics/python/README.md)
- [Dart](./topics/dart/README.md)
- [Lua](./topics/lua/README.md)

## How The Repo Is Shaped

Most topic folders use the same order:

- `Refresh`: shortest useful reopen
- `Required`: day-to-day material
- `Growth`: deeper follow-up study

When a topic has runnable examples, its `README` should point to the matching
lab.

Common note shape:

- why this matters
- smallest useful mental model
- bad vs better reasoning when the decision boundary matters
- a small code example or a small word example when helpful
- strong default
- main tradeoff or failure mode

## Core Rule

- the repo should help you understand, reopen, and defend ideas
- practical topic notes are the main source of truth
- paths help with order, but should not become the only place where meaning lives
- examples matter: use code when code teaches best, and word examples when code would only add noise

See `CONTRIBUTING.md` and `LICENSE.md` for contribution and usage details.
