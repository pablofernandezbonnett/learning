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
- how does this show up in code, design, or production decisions?

What you will find here:

- topic-first notes for fast reopening
- practical docs that explain failure modes, tradeoffs, and better defaults
- small runnable labs, with Java as the main backend baseline and Kotlin added where comparison or Kotlin-specific learning helps
- paths that give you a study order instead of a pile of disconnected notes

The repo is optimized for practical understanding, tradeoffs, failure modes,
and reusable mental models, not encyclopedic coverage.
It should not grow into a duplicate of broad courses or vendor certification
tracks. Small, clearly separated personal-learning companions are acceptable
when they remain practical and deliberately scoped.

## Who This Is For

Best fit:

- backend engineers who want a durable refresh repo
- engineers moving toward stronger system design, AppSec, SRE, or solutions-architecture judgment
- people who prefer plain-language notes with practical examples over abstract theory first

Less ideal fit:

- someone looking for full certification prep copied from one vendor course
- someone looking for beginner language material reproduced from broad web courses
- someone who wants theory completeness before practical use

## Start Here

Use one main entrypoint, then branch out only if a real work need appears:

- `paths/senior-java-backend-growth-plan-roadmap.md`: the canonical path for a senior Java backend engineer closing practical gaps without leaving the backend lane
- `paths/safe-production-change-loop.md`: one practical loop that connects a code change to review, rollout, monitoring, rollback, and follow-up
- `paths/adjacent/README.md`: optional side paths for AppSec, SRE, or wider architecture pressure
- `topics/`: topic-by-topic entrypoints when you already know what you want to study
- `labs/`: runnable companion material

## How To Use This Repo

Strong default:

1. start with `paths/senior-java-backend-growth-plan-roadmap.md` if you want the main backend path
2. start with a topic folder if you already know the concept you want to reopen
3. use labs when the topic benefits from code and runtime behavior

Practical rule:

- use `paths/` for study order
- use `topics/` for the durable source notes
- use `labs/` for hands-on reinforcement
- use `paths/adjacent/` only when a focused side lane becomes relevant

## Main Topics

- [Git](./topics/git/README.md)
- [Java](./topics/java/README.md)
- [Kotlin](./topics/kotlin/README.md)
- [Spring Boot](./topics/spring-boot/README.md)
- [Databases](./topics/databases/README.md)
- [API Design](./topics/api/README.md)
- [Architecture](./topics/architecture/README.md)
- [System Design](./topics/system-design/README.md)
- [Security](./topics/security/README.md)
- [AppSec Foundations](./topics/appsec/README.md)
- [SRE](./topics/sre/README.md)
- [Solutions Architecture](./topics/solutions-architecture/README.md)
- [Cloud](./topics/cloud/README.md)
- [AI](./topics/ai/README.md)
- [DevOps](./topics/devops/README.md)
- [Testing](./topics/testing/README.md)
- [Algorithms](./topics/algorithms/README.md)
- [Go](./topics/go/README.md)
- [Python](./topics/python/README.md)
- [TypeScript](./topics/typescript/README.md)
- [Dart](./topics/dart/README.md)
- [Flutter](./topics/flutter/README.md)
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

- the repo should help you understand, reopen, and reuse ideas
- practical topic notes are the main source of truth
- paths help with order, but should not become the only place where meaning lives
- examples matter: use code when code teaches best, and word examples when code would only add noise
- for JVM topics, prefer Java as the default example language and add Kotlin when the comparison teaches something useful

See `CONTRIBUTING.md` and `LICENSE` for contribution and usage details.
