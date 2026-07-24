# Python Integration Boundaries: Protocols, HTTP, Config, and Operations

Use this note after the Python refresh when a script becomes a service or calls
an external API. It covers the Python equivalents of the Java habits that keep
integrations understandable: a small contract, explicit configuration, bounded
waiting, useful errors, and safe logs.

## Why This Matters

Most fragile Python is not caused by an unfamiliar loop. It is caused by a
provider call, an environment variable, or a malformed response being allowed
to leak through the whole application.

The practical goal is not to recreate Spring in Python. It is to keep the
boundary around a provider as clear as the boundary around a Java `WebClient` or
repository.

## Smallest Useful Mental Model

- a `Protocol` describes what the application needs from a dependency
- Pydantic validates untrusted configuration or payloads at the edge
- `httpx` owns HTTP details; the domain service should not
- timeouts and retry decisions are policy, not accidental client defaults
- logs describe an event without copying secrets or private payloads

`Protocol` is Python's structural interface: a value fits when it has the
required methods, even if it did not declare `implements` as Java would.

## Bad Mental Model vs Better Mental Model

Bad mental model:

> A Python service is small, so a route can read environment variables, call
> HTTP, parse JSON, decide policy, and log the full response in one function.

Better mental model:

> Keep input validation, provider I/O, and deterministic business decisions in
> separate small units. The code stays light, but failures have an owner.

## 1. Use Modern Type Shapes at Important Boundaries

Use built-in generic syntax in supported modern Python:

```python
from typing import Protocol


class StockGateway(Protocol):
    async def available_units(self, sku: str) -> int: ...


def can_reserve(available_units: int, requested_units: int) -> bool:
    return requested_units > 0 and available_units >= requested_units


async def reserve_if_available(
    gateway: StockGateway,
    sku: str,
    requested_units: int,
) -> bool:
    return can_reserve(await gateway.available_units(sku), requested_units)
```

This is an interface boundary, not a reason to add abstraction everywhere.
Use it when multiple implementations are plausible: a real HTTP client, a fake
for tests, or a local-model versus hosted-model adapter.

Use `str | None` for an optional value in new code. `Optional[str]` means the
same thing and remains common in existing code, but is not a different feature.

## 2. Validate Config Once

Environment variables are strings controlled by the deployment environment.
Treat them as untrusted input. A settings model makes missing values and wrong
types fail during startup instead of during a customer request.

```python
from pydantic import AnyHttpUrl, SecretStr
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    inventory_base_url: AnyHttpUrl
    inventory_token: SecretStr
    request_timeout_seconds: float = 3.0


settings = Settings()
```

Keep `.env` for local convenience only. Production supplies the same variables
through its platform or secret manager. Do not commit real tokens, and do not
turn `SecretStr` back into text for logs.

## 3. Put HTTP Details Behind a Client

`httpx.AsyncClient` is a suitable async HTTP client when the surrounding code
is async. Set a timeout explicitly and reuse the client for its intended
lifetime rather than creating one per request.

```python
import httpx
from pydantic import BaseModel, Field, ValidationError


class InventoryUnavailable(Exception):
    pass


class StockAvailability(BaseModel):
    available_units: int = Field(ge=0)


class InventoryHttpClient:
    def __init__(self, base_url: str, token: str, timeout_seconds: float) -> None:
        self._client = httpx.AsyncClient(
            base_url=base_url,
            headers={"Authorization": f"Bearer {token}"},
            timeout=httpx.Timeout(timeout_seconds),
        )

    async def available_units(self, sku: str) -> int:
        response = await self._client.get(f"/v1/stock/{sku}")
        response.raise_for_status()
        try:
            payload = StockAvailability.model_validate(response.json())
        except (ValueError, ValidationError) as error:
            raise InventoryUnavailable("inventory response was invalid") from error
        return payload.available_units

    async def close(self) -> None:
        await self._client.aclose()
```

In FastAPI, construct and close the client through application lifespan, then
inject the narrow gateway into routes or services. Do not place it as a global
that tests cannot replace.

## 4. Errors, Timeouts, and Retries Need a Decision

Translate expected provider failures at the integration boundary. The rest of
the application should not need to know `httpx` exception names.

```python
async def availability_for_checkout(client: StockGateway, sku: str) -> int:
    try:
        return await client.available_units(sku)
    except httpx.TimeoutException as error:
        raise InventoryUnavailable("inventory lookup timed out") from error
    except httpx.HTTPStatusError as error:
        if error.response.status_code >= 500:
            raise InventoryUnavailable("inventory service failed") from error
        raise
```

Strong default:

- always set a timeout
- retry only failures that are probably transient
- bound attempts and add exponential backoff with jitter
- retry a write only when it is naturally idempotent or protected by an
  idempotency key

Do not retry bad input, authentication failures, or every `4xx` response. A
retry without an idempotency decision can duplicate a payment, order, or tool
call.

## 5. Log Events, Not Secrets

Python's standard `logging` module is enough for a small service. Give useful
events stable names and fields, while excluding authorization headers, tokens,
raw customer text, and complete provider responses.

```python
import logging

logger = logging.getLogger(__name__)


def log_inventory_failure(sku: str, status_code: int | None) -> None:
    logger.warning(
        "inventory_lookup_failed",
        extra={"sku": sku, "status_code": status_code},
    )
```

Use a request or trace identifier from your web framework or platform to join
logs across services. A log is evidence for debugging, not an archive of user
data.

## 6. Containerize the Same Boundary

The container should package code, not configuration. A minimal production
shape still needs a small runtime image, a non-root process, and configuration
injected at runtime.

```dockerfile
FROM python:3.12-slim
WORKDIR /app
RUN useradd --create-home app
COPY --chown=app:app . /app
RUN pip install --no-cache-dir .
USER app
CMD ["python", "-m", "app.main"]
```

The exact dependency command depends on the project tool (`uv` or `pip`), but
the operating rule does not: build one image, pass configuration from outside,
and never bake a `.env` file or secret into it.

## Practical Rule

> In Python, keep the interface light but real: validate at the edge, put
> provider details behind a narrow client, bound waiting and retries, and keep
> secrets out of logs and images.

## Further Reading

- [Python `Protocol`](https://docs.python.org/3/library/typing.html#typing.Protocol): structural contracts
- [Pydantic settings](https://docs.pydantic.dev/latest/concepts/pydantic_settings/): typed environment configuration
- [HTTPX async support](https://www.python-httpx.org/async/) and [timeouts](https://www.python-httpx.org/advanced/timeouts/)
- [Python logging](https://docs.python.org/3/library/logging.html)
