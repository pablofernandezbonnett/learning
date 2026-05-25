# Python for Java and Kotlin Backend Engineers

Use this folder when you want to use Python as a practical tool from a JVM
backend background.

Python in this repo is not positioned as a primary backend stack.
It is positioned as a high-leverage language for tasks where speed,
data tooling, or library reach matter more than the stronger compile-time
guardrails of Java or Kotlin.

Focus:

- data scripts
- automation
- lightweight internal APIs
- ML or AI-adjacent integration
- secure boundary handling when Python exposes an API or model helper
- not as a replacement for every Spring Boot service

Working style:

- keep the Python discussion pragmatic rather than ideological
- explain the tradeoff between speed and discipline instead of treating Python as "easy by default"
- connect Python back to the same backend quality standards used elsewhere in the repo

## Smallest Mental Model

Python changes the shape of day-to-day work more than it changes the backend
principles underneath it.
You still need clear contracts, safe data handling, and testing discipline.
What changes is that the language lets you move faster, but it also asks you to
add more discipline on purpose.

The biggest differences for a JVM developer are:

- much less ceremony
- dynamic runtime by default
- stronger scripting and data tooling culture
- faster iteration for small tasks
- Python uses lighter structure than Java, so you need to be deliberate about where you add types, models, and tests

The biggest carryovers are still:

- API design
- data modeling judgment
- operational caution
- testing discipline

Short rule:

> Python is most valuable here as a speed tool, not as a place to forget backend rigor

## Mental Map

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

## When To Reach For Python

Good use cases:

- ETL or migration scripts
- reporting and analysis
- one-off automation
- lightweight internal API
- model or embedding integration
- small model-serving or eval-support tools around a larger JVM system

Bad use cases:

- rewriting a mature Spring service just because Python feels faster to type
- using Python for critical long-lived backend services without a strong reason

Short rule:

- small task, fast feedback, lots of libraries -> Python is a good candidate
- complex long-lived enterprise service -> JVM stack may still be the better fit

## Recommended Order

| File | Why start here | Run |
|---|---|---|
| [05-python-for-java-devs-quick-refresh.md](./05-python-for-java-devs-quick-refresh.md) | minimum modern Python mental model from a Java backend starting point | read as guide |
| [04-project-shape-and-quality.md](./04-project-shape-and-quality.md) | typing, tooling, testing, and project shape when Python is rusty | read as guide |
| [examples/01-basics.py](./examples/01-basics.py) | syntax refresher for Java and Kotlin developers after the mental model is clear | `python examples/01-basics.py` |
| [examples/02-data-scripts.py](./examples/02-data-scripts.py) | data and ETL style work | `python examples/02-data-scripts.py` |
| [examples/fastapi_app.py](./examples/fastapi_app.py) | lightweight API mental bridge from Spring Boot | `uvicorn fastapi_app:app --app-dir topics/python/examples --reload` |

## If You Want To Get Productive Fast

1. read [05-python-for-java-devs-quick-refresh.md](./05-python-for-java-devs-quick-refresh.md)
2. read [04-project-shape-and-quality.md](./04-project-shape-and-quality.md)
3. run [examples/01-basics.py](./examples/01-basics.py) for syntax refresh only where needed
4. run [examples/02-data-scripts.py](./examples/02-data-scripts.py) if your likely use case is ETL, reporting, or automation
5. run [examples/fastapi_app.py](./examples/fastapi_app.py) if your likely use case is a small internal API or AI-adjacent helper

This order keeps Python in its most useful role for a JVM backend engineer:
mental model first, quality bar second, examples third.

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

## Practical Framing

Good framing:

> I use Python as a productivity language for scripts, data processing, lightweight APIs,
> and AI-adjacent work. My backend architecture instincts still come from JVM service work;
> Python just lets me move faster for the right class of problems.

Bad framing:

> Python is easy so I would just use it for everything.

## What To Internalize

- Python is strongest here as a fast tool, not a default replacement for the JVM stack
- dynamic languages need deliberate discipline around typing and testing
- FastAPI is a useful mental bridge from Spring controllers for small APIs
- Python pays off quickly for automation, ETL, and AI-adjacent integration
- if Python serves AI or ML traffic, the same backend rules still apply: typed boundaries, auth, limits, and explicit side effects
- if you want the shortest Java-to-Python bridge, read [05-python-for-java-devs-quick-refresh.md](./05-python-for-java-devs-quick-refresh.md) early

If your current goal is market-oriented backend growth, pair this folder with:

- [../ai/README.md](../ai/README.md)
- [../../paths/market-oriented-backend-growth.md](../../paths/market-oriented-backend-growth.md)
