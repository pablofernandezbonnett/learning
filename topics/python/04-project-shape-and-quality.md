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

Minimal `pyproject.toml` example:

```toml
[project]
name = "python-tool"
version = "0.1.0"
requires-python = ">=3.12"
dependencies = [
  "fastapi>=0.122.0",
  "pydantic>=2.0",
]

[dependency-groups]
dev = [
  "pytest>=8.0",
  "ruff>=0.5",
  "pyright>=1.1",
]
```

Why this matters:

- one file owns dependency intent
- the Python version is explicit
- the quality tools stop being optional memory

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

Tiny `pytest` example:

```python
import pytest


def test_line_total_rejects_non_positive_quantity() -> None:
    with pytest.raises(ValueError):
        line_total(LineItem(price_jpy=2990, quantity=0))
```

Why this matters:

- the rule is executable, not just written in prose
- Python quality improves fast once tests become a normal habit

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

Practical rule:

- use `def` for ordinary blocking code paths
- use `async def` when the route really awaits non-blocking I/O
- do not mark a route `async` if the important work is still blocking JDBC, blocking file I/O, or a blocking HTTP client under the hood

---

## 7. AI-Adjacent Python Shape

For your current career direction, one of the most useful Python roles is not
"new backend stack".

It is:

- eval scripts
- retrieval or embedding helpers
- lightweight model-serving tools
- data prep around AI or ML workflows

Good default:

- let Python own the thin AI-adjacent helper where the ecosystem advantage is real
- let the main JVM backend keep owning identity, business workflows, and stronger product-serving boundaries unless there is a clear reason to move them

Small word example:

- weak approach: "the whole support platform is now in Python because the model code started there"
- better approach: "Python owns the eval and model-helper layer, while the main Java service still owns auth, case workflow, audit trail, and final action control"

---

## 8. Security Baseline For Small Python APIs

If a small Python service starts serving AI or ML traffic, the baseline should
still look like backend engineering, not notebook culture.

Keep these defaults:

- validate request bodies with typed models
- use explicit auth dependencies instead of ad hoc header parsing
- return typed responses where possible
- bound request size, concurrency, and timeout
- treat model output as untrusted before side effects
- avoid leaking prompts, secrets, or private source documents in logs

Plain-English version:

- Python can be quick without being sloppy

Small code example:

```python
from fastapi import Depends, FastAPI
from fastapi.security import OAuth2PasswordBearer
from pydantic import BaseModel

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")
app = FastAPI()


class ClassificationRequest(BaseModel):
    text: str


@app.post("/classify")
async def classify(
    payload: ClassificationRequest,
    token: str = Depends(oauth2_scheme),
) -> dict[str, str]:
    return {"status": "accepted"}
```

Why it is better:

- request shape is explicit
- auth boundary is explicit
- the route can stay thin while service logic remains testable elsewhere

---

## 9. Honest Positioning

Good sentence:

> I use Python as a fast tool, but I still keep type hints, boundary validation,
> tests, and explicit side effects so the code stays trustworthy.

Bad sentence:

> Python is only for quick scripts, so quality does not matter much.

---

## 10. What To Internalize

- Python speed comes from less ceremony, not from skipping engineering judgment
- a small toolchain gives back a lot of the safety you miss from the JVM
- typed boundaries matter more than internal cleverness
- once Python is more than a throwaway script, project shape matters quickly
- AI-adjacent Python still needs normal backend controls around auth, limits, and side effects
