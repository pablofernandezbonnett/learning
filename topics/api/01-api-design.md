# API Design: From Use Case to Contract

An API is a promise to a caller: which operation it may request, what result it
receives, and what happens when the call fails, repeats, or changes over time.
Design that promise before choosing endpoints or framework classes. For the
surrounding system, start with the [general system design method](../system-design/00-general-system-design-method.md).

## 1. Define the Caller and the Job

- Who calls: a browser, mobile app, partner, or another service?
- What task must the caller complete, and which data does it need?
- How long must existing clients keep working?
- What traffic, response time, payload size, and availability matter?

Choose an immediate request and response when the caller needs the final answer
now. If work can finish later, define an accepted operation and a way to check
its state. Consider [REST vs GraphQL](./00-rest-vs-graphql.md) when the shape of
client data makes that choice relevant.

## 2. Define Operations and States

Name the resources, valid operations, and business states. Specify which calls
only read, which change state, and which can remain `PENDING`. Keep internal
implementation fields out of the public contract.

For each operation, answer:

| Part | Decision |
| --- | --- |
| Request | Required fields, allowed values, size limits, and validation? |
| Response | Fields and state the caller needs on success or while pending? |
| Errors | Invalid input, denied access, conflict, limit, and temporary failure? |
| Retry | Can the caller repeat the request after losing the response? |
| Change | What can evolve without breaking an existing client? |

HTTP methods and status codes should describe the operation's real behavior.
A pleasant URL cannot compensate for an unclear state transition.

## 3. Bound Reads and Their Cost

For a list or search, define allowed filters and sort orders, maximum page size,
stable ordering, and how fresh the result must be. A page limit bounds the
response; validation and query limits must also bound the work needed to make
it. Derive tenant scope from authenticated identity instead of trusting a
caller-supplied tenant ID.

### Offset or cursor pagination

```http
GET /orders?limit=50&offset=100
GET /orders?limit=50&after=opaque-cursor
```

- **Offset:** simple for modest lists and shallow pages. Deep offsets can cost
  more, and inserts or deletes may shift items between pages.
- **Cursor/keyset:** resume after a stable sort key. It usually handles deep,
  changing lists better, but the cursor must match the original filters and
  ordering. Use a unique tie-breaker such as `created_at + id` when timestamps
  can repeat.

A cursor does not freeze live data. Decide whether later pages show the latest
items or a saved snapshot. A snapshot gives more stable pages but needs stored
state and expiry.

Cache only when a slightly old answer is acceptable and the cache cannot mix
data between callers. Use conditional requests or compression when transfer
cost warrants them; see [HTTP caching](./09-http-caching-etag-and-compression.md).

### Example: hotel offers

An agency searches offers and may later reserve one. The search needs the
authenticated agency's scope, validated dates and occupancy, a capped page,
and an explicit freshness rule. It returns only fields needed to choose an
offer, plus a `nextCursor` if more results exist. A displayed offer is not a
hold: the reservation write must check current price and availability again.
If availability comes from partners, bound their response time and concurrent
calls; decide whether a partial result is useful.

Start with a simple query and measure its cost before adding a separate search
service or cache.

## 4. Make Failures Actionable

The client should know whether to fix input, handle a conflict, back off, or
retry later. Use consistent HTTP semantics and a stable error body. The
[RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457.html) format
is one option:

```json
{
  "type": "https://api.example.com/problems/slot-taken",
  "title": "Slot already taken",
  "status": 409,
  "detail": "Choose another appointment slot"
}
```

Common distinctions:

- `400`: malformed request; `422`: understood request with invalid content.
- `401`: authentication is needed; `403`: caller lacks permission.
- `404`: resource is not available to this caller.
- `409`: request conflicts with current state, such as an already claimed slot.
- `429`: caller is rate limited; `503`: service is temporarily unavailable.

Do not return `200 OK` with a hidden error field for a failed operation. Avoid
putting private details in error messages.

## 5. Make Writes Safe Under Retries

A client may time out after the server has already committed a write. If a
second attempt could create another charge or booking, give the operation a
stable business identity or an idempotency key backed by durable state:

```http
POST /payments
Idempotency-Key: payment-attempt-123
```

Define the key's scope and lifetime, what happens when the same key is reused
with different input, and how a repeat gets the original result or current
status. A repeated request is not automatically a `409`; use `409` for a real
conflict, such as incompatible input for an existing operation. See
[idempotency and transaction safety](../databases/01-idempotency-and-transaction-safety.md).

When completion is later, `202 Accepted` can point to a status resource:

```http
HTTP/1.1 202 Accepted
Location: /payments/requests/payment-attempt-123
```

The response means the work was accepted, not that the business action has
finished. Define the possible states and how the caller learns the final
outcome. For an appointment booking, return confirmed success only after the
guarded write claims the slot; send reminders later.

## 6. Protect Each Operation

Authenticate the caller, then authorize the specific action on the specific
resource and workflow state. Check tenant boundaries on the server. Treat
input, object IDs, uploads, and external callbacks as untrusted. Cap payloads,
expensive filters, request rate, and concurrent work where each can exhaust a
shared resource. Keep secrets and private fields out of responses and logs.

See [web and API security](../security/02-web-and-api-security.md) and
[authentication models](../security/01-auth-sessions-vs-jwt.md).

## 7. Define Failure and Capacity Behavior

Set deadlines for dependencies. Retry transient failures only when repetition
is safe. Decide what the caller sees if a dependency is slow or unavailable.
Bound connection pools, in-flight work, and queue size; a rate limit at the
entrance does not protect every internal bottleneck. Measure latency, error
types, rejected work, and dependency pressure.

See [resilience patterns](../architecture/02-resiliency-patterns.md) and
[capacity and load shedding](../sre/05-capacity-planning-and-load-shedding.md).

## 8. Evolve and Verify the Contract

Prefer additive changes when old clients can safely ignore new fields. A
breaking change, such as changing a field's meaning or removing a required
field, needs a migration plan. If parallel versions are necessary, choose a
versioning mechanism that clients and operators can use consistently; a URI
version is visible and simple to route, but it is not required for every API.

Test observable responses, authorization across users and tenants, duplicate
and concurrent writes, timeout paths, and query limits. Use contract tests
when clients and providers release independently. See
[contract testing and API evolution](./08-contract-testing-and-api-evolution.md).

## 9. Add Client-Specific Composition When Needed

A backend-for-frontend (BFF) can combine data and tailor payloads when mobile,
admin, and partner clients genuinely need different shapes or latency. It adds
another service boundary, so first check whether the shared API is sufficient.
See [gateway vs BFF vs edge patterns](../architecture/17-gateway-vs-bff-vs-edge-patterns.md).

## Review Questions

1. Can the caller understand success, failure, and pending work?
2. Can one request read, return, or change too much?
3. Can a retry create a duplicate effect?
4. Is access checked on the target resource and workflow state?
5. What happens when a dependency slows down or fails?
6. How will existing clients survive a contract change?
