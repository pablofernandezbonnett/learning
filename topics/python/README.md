# Python for Java and Kotlin Backend Engineers

Python in this repo is not positioned as a primary backend stack.
It is also not part of the main backend refresh path.

It is positioned as a high-leverage tool for a JVM backend engineer.

That means using Python for:

- data scripts
- automation
- lightweight internal APIs
- ML or AI-adjacent integration

not as a replacement for every Spring Boot service.

---

## What Python Actually Changes For A JVM Developer

The biggest differences are:

- much less ceremony
- dynamic runtime by default
- stronger scripting and data tooling culture
- faster iteration for small tasks

The biggest carryovers are still:

- API design
- data modeling judgment
- operational caution
- testing discipline

Short rule:

> Python is most valuable here as a speed tool, not as a place to forget backend rigor

---

## The Smallest Mental Map

| Java or Kotlin idea | Python equivalent |
|---|---|
| `data class` | `@dataclass` or Pydantic model |
| `T?` / `Optional<T>` | `T | None` |
| Streams / collection ops | list comprehensions and built-ins |
| `try-with-resources` | `with` context manager |
| Spring controller | FastAPI path function |
| Gradle or Maven | `uv`, `pip`, `pyproject.toml` |

If you come from Kotlin, the easiest Python bridge is:

- simple syntax
- optional typing
- expressive small scripts

If you come from Java, the biggest adjustment is:

- fewer compile-time guarantees unless you add type discipline deliberately

---

## When To Reach For Python

Good use cases:

- ETL or migration scripts
- reporting and analysis
- one-off automation
- lightweight internal API
- model or embedding integration

Bad use cases:

- rewriting a mature Spring service just because Python feels faster to type
- using Python for critical long-lived backend services without a strong reason

Short rule:

- small task, fast feedback, lots of libraries -> Python is a good candidate
- complex long-lived enterprise service -> JVM stack may still be the better fit

---

## Files In This Folder

| File | Purpose | Run |
|---|---|---|
| `examples/01-basics.py` | syntax refresher for Java and Kotlin developers | `python examples/01-basics.py` |
| `04-project-shape-and-quality.md` | typing, tooling, testing, and project shape when Python is rusty | read as guide |
| `examples/02-data-scripts.py` | data and ETL style work | `python examples/02-data-scripts.py` |
| `examples/03-fastapi-app.py` | lightweight API mental bridge from Spring Boot | `uvicorn examples.03-fastapi-app:app --reload` |

Recommended order:

1. `examples/01-basics.py`
2. `04-project-shape-and-quality.md`
3. `examples/02-data-scripts.py`
4. `examples/03-fastapi-app.py`

---

## Setup

Python version:

- `3.12+` recommended

```bash
python3 --version

# Option A
pip install uv
uv venv .venv && source .venv/bin/activate
uv pip install pandas fastapi uvicorn

# Option B
python3 -m venv .venv
source .venv/bin/activate
pip install pandas fastapi uvicorn
```

---

## Practical Framing

Good framing:

> I use Python as a productivity language for scripts, data processing, lightweight APIs,
> and AI-adjacent work. My backend architecture instincts still come from JVM service work;
> Python just lets me move faster for the right class of problems.

Bad framing:

> Python is easy so I would just use it for everything.

---

## What To Internalize

- Python is strongest here as a fast tool, not a default replacement for the JVM stack
- dynamic languages need deliberate discipline around typing and testing
- FastAPI is a useful mental bridge from Spring controllers for small APIs
- Python pays off quickly for automation, ETL, and AI-adjacent integration
