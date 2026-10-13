# Python Integration Boundaries

A service becomes difficult to trust when routes or jobs mix configuration, HTTP calls, payload parsing, business decisions, and logging. Give each outside dependency a narrow boundary with an explicit failure policy.

## Define What the Caller Needs

A `Protocol` can describe a dependency by its methods without coupling the caller to one implementation:

```python
from typing import Protocol

class StockGateway(Protocol):
    async def available_units(self, sku: str) -> int: ...
```

Use it when a real client and a fake or alternate implementation need the same contract. A plain concrete class is simpler when substitution adds no value.

## Validate Configuration and Responses

Read settings once at startup. Check required URLs, credentials, and positive limits before serving traffic. Keep secrets outside source and logs. Parse each external response into an expected shape before domain code uses it; a successful HTTP status does not guarantee valid data.

For HTTP, reuse a client over its intended lifetime, set explicit timeouts, and close it on shutdown. HTTPX supports both synchronous and asynchronous clients; match the client to the surrounding call path. Translate expected transport and status failures into application-level outcomes at this boundary.

## Timeouts, Retries, and Logs

A timeout limits waiting but may leave a write's outcome unknown. Retry only when the operation is safe to repeat or has a server-side idempotency rule. Bound attempts by the remaining request budget and use backoff. Do not retry malformed input or authorization failures.

Log the dependency, operation, status, duration, and request identifier needed to debug a failure. Keep tokens, raw personal data, and full provider bodies out of routine logs.

## When This Becomes an API

FastAPI can validate request models and shape responses, but the route should still make authorization, service decisions, and external calls visible. Use `def` with blocking dependencies or `async def` with awaited nonblocking I/O. Decide request size, concurrency, error mapping, and shutdown policy at the application boundary.

Package one deployable artifact with configuration supplied at runtime. Run it with the permissions and resource limits appropriate to the host. The [product API exercise](./04-exercises.md) gives a small contract to apply these rules.

For model providers, treat the model as another external dependency and use the [AI block](../ai/README.md) for the extra questions about output validation, evaluation, retrieval permissions, and consequential actions.
The [Python model boundary snippets](./05-llm-application-boundaries.md) show a small implementation and test of that boundary.

## Further Reading

- [Python `Protocol`](https://docs.python.org/3/library/typing.html#typing.Protocol).
- [HTTPX async clients and lifecycle](https://www.python-httpx.org/async/).
- [FastAPI request bodies](https://fastapi.tiangolo.com/tutorial/body/) and [response models](https://fastapi.tiangolo.com/tutorial/response-model/).
