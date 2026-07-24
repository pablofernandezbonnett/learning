# Python for Java and Kotlin Backend Engineers

Use this folder when Python is the practical tool for a task, not when you need
another general backend stack.

Python is useful for automation, data work, small internal APIs, and AI-adjacent
helpers. The backend rules do not change: keep contracts clear, validate data,
test risky behavior, and make side effects explicit.

## Smallest Useful Mental Model

Python lets a JVM backend engineer move quickly with less ceremony and fewer
compile-time guardrails. Add structure deliberately where a script becomes a
service, a shared tool, or a business-critical job.

Good fit:

- ETL, reporting, migration, and automation scripts
- data or model-integration helpers
- small internal APIs with a clear ownership boundary

Do not use this folder as a reason to rewrite a mature JVM service only because
Python is faster to type.

## Recommended Order

1. [05-python-for-java-devs-quick-refresh.md](./05-python-for-java-devs-quick-refresh.md): the Java/Kotlin-to-Python language delta that matters in backend work
2. [04-project-shape-and-quality.md](./04-project-shape-and-quality.md): typing, testing, tooling, and the point where a script needs stronger boundaries
3. [examples/02-data-scripts.py](./examples/02-data-scripts.py): data and ETL-shaped work
4. [examples/fastapi_app.py](./examples/fastapi_app.py): a lightweight API mental bridge from Spring Boot
5. [06-integration-boundaries.md](./06-integration-boundaries.md): protocols, HTTP clients, config, errors, logging, and Python container shape
6. [07-llm-application-boundaries.md](./07-llm-application-boundaries.md): use an LLM as a bounded dependency, not as business logic

If your likely work is data or automation, run the script example next. If it is
a bounded internal API, read the project-shape note before the FastAPI example.
Read `06` when the code calls another service or starts living in production.
Read `07` only when the Python service really owns an AI-facing integration.

## JVM Mental Map

| Java or Kotlin idea | Python equivalent |
|---|---|
| `data class` | `@dataclass` or Pydantic model |
| `T?` / `Optional<T>` | `T | None` |
| streams and collection operations | comprehensions and built-ins |
| `try-with-resources` | `with` context manager |
| Spring controller | FastAPI path function |
| Gradle or Maven | `uv`, `pip`, `pyproject.toml` |

## Core Rule

> Use Python as a speed tool. When the task becomes long-lived or
> business-critical, add types, tests, boundaries, and operational ownership on
> purpose.

For selective AI support, pair this topic with [AI](../ai/README.md) and the
[senior backend roadmap](../../paths/senior-java-backend-growth-plan-roadmap.md).
