# Learning Repository

Practical software engineering notes in one place, with many backend examples.

This repo exists for one main reason:

- make design, implementation, security, and reliability ideas easy to reopen and apply

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
- small labs with prompts, solution guides, and cases to check
- topic entrypoints that give each area a clear study order

The repo is optimized for practical understanding, tradeoffs, failure modes,
and reusable mental models, not encyclopedic coverage.
It should not grow into a duplicate of broad courses or vendor certification
tracks. Small, clearly separated personal-learning companions are acceptable
when they remain practical and deliberately scoped.

## Who This Is For

Best fit:

- software engineers who want to design, build, secure, and operate systems more deliberately
- readers who prefer plain-language notes and practical examples over abstract theory first
- engineers using a specific language or framework who want to connect it to general decisions

Less ideal fit:

- someone looking for full certification prep copied from one vendor course
- someone looking for beginner language material reproduced from broad web courses
- someone who wants theory completeness before practical use

## Start Here

Choose the entrypoint that matches the problem:

- [System design](./topics/system-design/README.md): a general method for shaping a system
- [Solution decisions](./topics/solutions-architecture/README.md): requirements, build-vs-buy, data boundaries, migration, and decision records
- [Safe production change](./paths/safe-production-change-loop.md): connect a code change to review, rollout, monitoring, and recovery
- [Topics](./topics/README.md): focused notes by subject
- [Labs](./labs/README.md): practice prompts and reasoning guides

## How To Use This Repo

Strong default:

1. start with the general system design method if the problem is broad
2. use a topic folder when you know which decision or concept to reopen
3. use a lab to practise a decision or implementation yourself

Practical rule:

- use `topics/` for the durable source notes
- use `labs/` for hands-on reinforcement
- use `paths/` only for a focused sequence that adds value beyond a topic index

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
- [Reliability and Operations](./topics/sre/README.md)
- [Solution Decisions](./topics/solutions-architecture/README.md)
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

When a topic has practice exercises, its `README` should point to the matching
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
- entrypoints help with order, but should not become the only place where meaning lives
- examples matter: use code when code teaches best, and word examples when code would only add noise
- for JVM topics, prefer Java as the default example language and add Kotlin when the comparison teaches something useful

See `CONTRIBUTING.md` and `LICENSE` for contribution and usage details.
