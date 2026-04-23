# Python Project Shape and Quality for JVM Backend Engineers

If your Python is rusty, the biggest risk is not syntax.

It is writing code that works once, but gets hard to trust as soon as the script
grows, a second engineer touches it, or the input becomes messy.

You do not need to become a Python specialist for the current repo.
You do need a small quality bar that keeps Python useful instead of fragile.

---

## 1. Why This Matters

Python removes ceremony fast.
That is useful for:

- automation
- ETL
- lightweight APIs
- AI-adjacent glue code

But the tradeoff is immediate:

- fewer compile-time guarantees
- easier accidental type drift
- easier "just one more script" sprawl

Short rule:

> Python is most productive when you add back a little structure on purpose.

---

## 2. The Smallest Useful Project Shape

For anything beyond a tiny one-file script, use a shape like this:

```text
python-tool/
  pyproject.toml
  app/
    __init__.py
    main.py
    models.py
    service.py
  tests/
    test_service.py
```

Why this shape matters:

- `pyproject.toml` gives one place for dependencies and tool config
- `app/` separates code from scripts and temp files
- `tests/` makes it normal to add checks before the tool becomes risky

You do not need a big framework.
You do need a place where the code, inputs, and tests stop bleeding together.

---

## 3. The Tooling That Carries Most Of The Value

Use a small set of tools consistently:

- `uv`: environment and dependency management
- `ruff`: linting and formatting
- `pytest`: tests
- `pyright` or `mypy`: static type checking on the code that matters

Minimal mental model:

- `uv` replaces ad hoc `pip install` habits
- `ruff` replaces style arguments and catches obvious mistakes fast
- `pytest` keeps scripts from rotting silently
- `pyright` or `mypy` restores some of the safety you miss from Java/Kotlin

Good default:

```bash
uv init
uv add fastapi pydantic
uv add --dev pytest ruff pyright
ruff check .
ruff format .
pytest
pyright
```

---

## 4. The Rules Worth Keeping

For Python code that you want to trust:

- add type hints on public functions
- model important input with `dataclass` or Pydantic instead of raw nested dicts
- validate JSON, env vars, and external API payloads at the boundary
- keep side effects obvious
- isolate parsing from business decisions
- test empty, duplicate, missing, and malformed input

This is the same backend logic you already use elsewhere in the repo.
Python just gives you less protection if you skip it.

---

## 5. Minimal Example

Weak shape:

```python
def process(payload):
    return payload["price"] * payload["qty"]
```

Why it is weak:

- no type signal
- no validation
- fails late and unclearly

Better shape:

```python
from dataclasses import dataclass


@dataclass(frozen=True)
class LineItem:
    price_jpy: int
    quantity: int


def line_total(item: LineItem) -> int:
    if item.quantity <= 0:
        raise ValueError("quantity must be positive")
    return item.price_jpy * item.quantity
```

Why it is better:

- data shape is explicit
- failure rule is explicit
- the function is easy to test

---

## 6. FastAPI Boundary Rule

When Python becomes a small API, keep this order:

1. Pydantic model validates input
2. route stays thin
3. service function holds the decision
4. external calls stay obvious

That is the same mental model as:

- controller
- DTO
- service
- integration boundary

Do not turn a small FastAPI tool into a pseudo-Spring clone.
But do keep the path from input to decision to side effect obvious.

---

## 7. Honest Positioning

Good sentence:

> I use Python as a fast tool, but I still keep type hints, boundary validation,
> tests, and explicit side effects so the code stays trustworthy.

Bad sentence:

> Python is only for quick scripts, so quality does not matter much.

---

## 8. What To Internalize

- Python speed comes from less ceremony, not from skipping engineering judgment
- a small toolchain gives back a lot of the safety you miss from the JVM
- typed boundaries matter more than internal cleverness
- once Python is more than a throwaway script, project shape matters quickly
