# Topics

This directory is the main reference surface of the repo.
Each topic folder should work as a practical re-entry point, not as a pile of
links or shorthand that only makes sense if the topic is already fresh in your
head.

Why this matters:

- the topic folders are the durable source notes of the repo
- if a path gives order, the topic notes should still carry the meaning
- reopening should be possible from the topic itself without hunting for context elsewhere

Use this directory when:

- you already know the topic you want to reopen
- you want the topic-level `Refresh`, `Required`, and `Growth` order
- you want a topic README to point you to any matching companion lab

If you are not sure where to start at all, use the repo root [README.md](../README.md).
If the problem is broad, start with the [general system design method](./system-design/00-general-system-design-method.md).

Most topic folders follow the same shape:

- `Refresh`: the shortest path back into the topic when you need a quick reopen
- `Required`: the material that matters often in real engineering work
- `Growth`: deeper material worth adding once the core feels stable

A good topic README should tell you three things quickly:

- what problem space the folder covers
- which documents to read first and why
- any term that could otherwise feel like unexplained jargon on first contact

When a topic has a companion exercise, the topic README should link to the
matching lab in `labs/`.

Strong default:

- keep topic READMEs short enough to guide study order without replacing the topic notes
- for JVM code examples, prefer Java first and add Kotlin when the comparison clarifies the tradeoff or idiom

Current topic areas include:

- `git`
- `java`, `kotlin`, `spring-boot`, `databases`, `api`, `architecture`
- `system-design`, `security`, `appsec`, `sre`, `solutions-architecture`
- `cloud`, `devops`, `testing`, `algorithms`
- `python`, `typescript`, `go`, `dart`, `flutter`, `lua`
- `ai`
