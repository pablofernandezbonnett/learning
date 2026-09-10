# Advanced API Design

> Primary fit: `Shared core`


Good API design is not mainly about endpoint names.
It is about contracts that stay understandable under scale, change, and failure.

This note follows the same reusable pattern as the other topic notes:

- what the concept is
- the smallest example that shows the idea
- how it looks in real systems
- how to explain it clearly

---

## 1. What Advanced API Design Actually Means

The "advanced" part is usually four things:

- list endpoints that still work at scale
- errors that clients can act on
- contracts that can evolve without breaking old clients
- client-specific aggregation only when it is justified

If someone asks about API design, they usually care less about framework syntax and
more about whether you can protect:

- performance
- backwards compatibility
- client usability
- clarity when the API grows and fails

## 1.1 What Makes An API Good In Practice

A good API is not just one that "works" in happy-path demos.
In practice, a good API is usually:

- clear for the client to understand and use correctly
- explicit about validation errors, conflicts, and retryable failure
- bounded in payload size, pagination, and request cost
- safe under retries and duplicate delivery
- protected by authorization, rate limits, and trust boundaries
- stable enough to evolve without surprising old clients
- observable enough that you can debug real failures in production

Small practical example:

- weak API: returns huge unbounded lists, vague errors, and duplicate charges on retry
- stronger API: paginates, gives actionable errors, uses idempotency for sensitive writes, and rejects abusive request shapes early

Short rule:

> A good API is one the client can use safely under scale, failure, and change,
> not just one with neat endpoint names.

---

## 1.2 What Good Looks Like In Practice

Strong default:

- bounded reads
- actionable errors
- retry-safe writes where duplicate side effects would hurt
- explicit authorization and request-cost limits
- additive contract evolution by default

Bad vs better:

- bad: "the endpoint works if the client behaves well"
- better: "the contract is still safe when the client retries, paginates deeply, sends bad input, or asks for too much data"

- bad: neat URLs but vague runtime behavior
- better: clear limits, stable error shapes, and predictable contract behavior under failure

Small review loop:

1. can the client understand success and failure clearly?
2. can this request become too expensive or too large?
3. what happens if the client retries?
4. what breaks if we add or change fields later?
5. is authz and abuse control explicit, not assumed?

---

## 2. Pagination: The Smallest Useful Example

When a list endpoint can grow, pagination is not optional.

### 2.1 Offset pagination

```http
GET /users?limit=50&offset=100
```

Typical SQL:

```sql
SELECT * FROM users
ORDER BY id
LIMIT 50 OFFSET 100;
```

Why people use it:

- easy to understand
- easy to test
- good enough for admin tools and modest datasets

Why it breaks down:

- deep pages get slower
- inserts and deletes can shift the window
- clients can see duplicates or miss records

### 2.2 Cursor or keyset pagination

```http
GET /users?limit=50&after=eyJpZCI6MTAwfQ==
```

Typical SQL:

```sql
SELECT * FROM users
WHERE id > 100
ORDER BY id
LIMIT 50;
```

Why it scales better:

- the database can jump from the index instead of scanning skipped rows
- stable ordering avoids most duplicate or missing item problems
- it is the normal choice for feeds, histories, and large lists

Practical rule:

- use `offset` for simple backoffice screens or small datasets
- use `cursor` for user-facing infinite scroll, event history, and large tables

Important nuance:

If the sort key is not unique, use a composite cursor such as `created_at + id`.
Otherwise two rows with the same timestamp can break ordering.

Bad vs better:

- bad: one unbounded list endpoint because "clients can filter on their side"
- better: explicit page limits and a pagination model that fits the real dataset and ordering needs

### 2.3 A Search Endpoint Is Also A Cost Contract

Before choosing an endpoint, clarify what is being searched. A list of records
already owned by the caller and a live availability search may both look like
`GET`, but they have different freshness, authorization, and cost rules.

For a live search, a bounded shape might be:

```http
GET /v1/hotel-offers?destinationId=TYO&checkIn=2026-10-12&checkOut=2026-10-15&adults=2&pageSize=25&cursor=opaque-value
```

Strong defaults:

- derive the agency or tenant from authentication, not a caller-controlled
  query parameter
- validate dates, occupancy, filters, sort values, and a small maximum page
  size before starting expensive work
- make a cursor a server-controlled marker for the last item in the page; the
  client should pass it back unchanged, and the server rejects it if filters or
  ordering differ from the original search
- return `nextCursor` rather than an exact `total` unless the product really
  needs that count and can afford it
- return only the fields needed for the result list, not every detail of every
  result

If the filter object is too large or structured for a query string, a
side-effect-free `POST /v1/.../searches` can be a clearer input contract. The
HTTP method does not remove the need to bound the query.

For search data that can change quickly, state the freshness rule explicitly. A
displayed price or availability result is not a hold. The later booking path
must check the current database value again before confirming.

Practical rule:

> Pagination protects response size. Validation, authorization, and request
> cost limits protect the work required to produce that page.

### 2.4 A Generic Way To Design A Costly Read Operation

This works for hotel offers, a streaming catalogue, product search, account
history, or any API operation that returns a potentially large or expensive
list. Start with the contract, not a technology.

1. **Name the result.** What is the caller trying to see or decide? Is it a
   catalogue, live availability, history, recommendation list, or status?
2. **Name the caller and scope.** Who is allowed to see it, and which tenant,
   subscription, region, or permissions change the result?
3. **Define small valid input.** Required filters, allowed optional filters,
   allowed sort orders, and validation rules. Reject a vague or unlimited query
   before it starts expensive work.
4. **Choose the data freshness rule.** Is a slightly old answer acceptable, or
   must it reflect the latest state? This decides whether caching is safe and
   whether a displayed result can be trusted for a later write.
5. **Bound the response and work.** Return only needed fields, cap page size,
   use a stable order and suitable pagination, and set a deadline for slow
   dependencies.
6. **State failure behaviour.** Say which input errors are `400`, which access
   failures are `401` or `403`, when a caller should back off, and whether a
   partial answer is useful when a data source fails.
7. **Protect and observe it.** Reuse identical safe reads where possible, but
   protect the public entry point from abusive traffic separately. Measure
   latency, errors, dependency time, and rejected work.

For example, a streaming catalogue search and hotel-offer search differ in
their fields, but both need caller scope, bounded filters, a freshness rule,
pagination, cost limits, failure behaviour, and observability.

Generic interview opening:

> I first define what the caller is allowed to see, the smallest valid input,
> how fresh the answer must be, and how much work one request may cause. Then
> I choose the endpoint shape, pagination, error contract, and protection
> limits. I keep a displayed read separate from a later correctness-critical
> write unless the product explicitly creates a hold.

### 2.5 How To Start The Hotel Search Design

Do not begin by naming a framework, database, cache, or endpoint. Here the
business task is: an agency searches live hotel offers for the traveller it
represents, then may reserve one. First make that contract concrete. A useful
interview order is:

1. **Where does availability come from?** Is room stock owned in our database,
   supplied live by partners, or both? This decides how fresh the answer can be
   and how we handle a partner timing out.
2. **Who is acting and for whom?** The authenticated agency determines
   permissions, contracted prices, and supplier access. The traveller's search
   needs may affect occupancy, country, or room rules, but the agency identity
   comes from authentication, never a normal `agencyId` query parameter.
3. **What is the smallest valid search?** Destination or hotel, check-in and
   check-out dates, rooms and guests per room, and possibly currency or
   traveller country. Decide which filters and sort values are allowed; do not
   accept arbitrary fields or unlimited date ranges.
4. **What must one offer contain?** Return a hotel and room type, rate plan,
   price and currency, cancellation conditions, and an `offerId` the agency can
   later select. An `offerId` identifies what was shown; it is not a promise
   that the room is held.
5. **How does the list stay bounded?** Pick a small maximum page size and a
   stable order. Use a server-made cursor for a large or changing result set;
   omit an expensive exact total unless it is truly needed.
6. **What can change after search?** Prices and availability can change. State
   the freshness rule or an offer expiry time, then check and claim inventory
   again when the agency creates a reservation.
7. **What happens under load or failure?** Validate before costly work, use a
   brief safe cache for repeated searches, set timeouts, and protect the public
   API separately against excessive traffic. If hotel partners are queried,
   give each partner a short deadline and a bounded number of parallel calls;
   decide explicitly whether a partial answer is useful or whether failure of a
   required partner fails the search.

Only after these answers, choose a boring first contract such as:

```http
GET /v1/hotel-offers?destinationId=TYO&checkIn=2026-10-12&checkOut=2026-10-15&adults=2&pageSize=25&cursor=next-page-marker
Authorization: Bearer <agency-token>
```

```json
{
  "items": [
    {
      "offerId": "offer_abc",
      "hotelId": "h_123",
      "roomTypeId": "standard",
      "name": "Example Hotel",
      "ratePlan": "refundable",
      "price": { "amount": "180.00", "currency": "EUR" },
      "cancellation": "Free cancellation until 2026-10-10",
      "expiresAt": "2026-10-01T10:05:00Z"
    }
  ],
  "nextCursor": "next-page-marker"
}
```

The search endpoint is a read: it returns `200` or a clear input or permission
error. It does not create a hold. A separate `POST /v1/reservations` accepts the
selected `offerId`, traveller details, and agency reference. That write uses an
`Idempotency-Key` for retry safety, rechecks price and availability, and returns
`201` or `409` when another client took the last room first.

Good interview opening:

> First I clarify whether availability comes from our inventory, hotel partners,
> or both; then I identify the authenticated agency and the traveller search
> data it needs. I design an authenticated, validated, paginated endpoint that
> returns available offers, not reservations. A later booking endpoint receives
> the selected offer and traveller details, then rechecks and claims
> availability in a database transaction.

### 2.6 Decide What “Next Page” Means When Offers Change

Hotel availability is live data. An offer can disappear or change price between
page 1 and page 2, even with correct cursor pagination. Decide the product rule
instead of promising accidental consistency.

Two reasonable choices are:

- **Live pages:** each request returns the best offers available at that moment.
  This is simpler and needs no saved search state, but page 2 may differ from
  what page 1 implied.
- **Short-lived search session:** the first request saves the ordered set of
  offers or IDs and returns a search marker. Later pages use that saved set
  until it expires. Pages are more stable, but the service must store and
  expire that state. Prices and availability still need checking at booking.

Strong default: start with live pages and tell clients that offers can change.
Add a short-lived search session only when stable pagination is a real product
requirement, not merely because cursor pagination exists.

Before calling the design complete, measure search latency, database time,
partner timeouts, cache-hit rate, rejected requests, and `409` reservation
conflicts. These numbers show whether the search is slow, being abused, or
losing availability too often; they prevent tuning from becoming guesswork.

### 2.7 Simple Default Before Extra Components

For a modest, stable reservation-history list, start with an authenticated
`GET`, validated filters, a response containing only the fields needed in the
list, and a capped page size.
Offset pagination is still the simpler fit when the data set and page depth are
bounded. Do not add a search service, cache, or asynchronous workflow merely
because the endpoint is named "search".

Move to cursor pagination when deep pages, a large changing result set, or a
user-facing feed make offset's work and shifting windows a real problem. Move
to a separate data copy designed for fast reads only when measurement shows
that the simple query cannot meet the required response time or load.

---

## 3. Error Shape: Make Failures Actionable

The smallest good rule:

> A client should be able to tell what failed, why it failed, and whether retrying makes
> sense without reverse-engineering your backend logs.

Bad pattern:

```json
{ "error": "Bad request" }
```

Better pattern: RFC 7807 style problem details.

```json
{
  "type": "https://api.example.com/problems/out-of-credit",
  "title": "Not enough credit",
  "status": 409,
  "detail": "Balance is 30 but the request requires 50",
  "instance": "/payments/req-123"
}
```

Why this works:

- `type` identifies the category
- `title` and `detail` are readable
- `status` matches the HTTP semantics
- `instance` can point to the exact request or trace

Status code rules worth saying cleanly:

- `400`: malformed request
- `404`: resource not found
- `409`: state conflict, duplicate, concurrent operation
- `422`: valid JSON, invalid business content
- `429`: caller should back off
- `503`: service is overloaded or unavailable

Do not hide everything behind `200 OK` with an `"error"` field in the payload.

Bad vs better:

- bad: every failure becomes a generic payload the client cannot act on
- better: the response makes clear whether the caller should fix input, stop retrying, back off, or handle a state conflict

---

## 4. Versioning: Change The Contract Without Breaking Clients

Versioning matters when the change is breaking.

Common breaking changes:

- removing a field clients use
- changing field meaning
- changing enum values
- changing pagination or error shape

### 4.1 URI versioning

```http
GET /api/v1/users
```

Best default because it is:

- explicit
- easy to route
- easy to test
- easy to reason about in production

### 4.2 Header versioning

```http
GET /api/users
Accept: application/vnd.example.v2+json
```

Useful when you want cleaner resource URIs, but operationally harder:

- less obvious in browsers
- trickier caching
- easier to forget in debugging

### 4.3 Query parameter versioning

```http
GET /api/users?version=2
```

Usually acceptable but less clean than URI versioning.

Practical rule:

- pick URI versioning unless you have a strong reason not to
- do not create a new version for every additive field
- prefer additive change when clients can ignore new fields safely

Bad vs better:

- bad: version every tiny change or break old clients casually
- better: prefer additive evolution and reserve versioning for real contract breaks

If you want the broader compatibility and test strategy behind that rule, see
[`08-contract-testing-and-api-evolution.md`](./08-contract-testing-and-api-evolution.md).

---

## 5. Write Paths Under Retry And Async Work

Many API problems are not about list reads.
They are about user-critical writes that can be retried, delayed, or confirmed later.

### 5.1 Retry-safe `POST`

Smallest mental model first:

```kotlin
var active = false
var attempts = 0

fun activate() { active = true }      // idempotent
fun recordAttempt() { attempts += 1 } // not idempotent
```

If the write sets the final state, repeating it is often harmless.
If the write creates a new effect each time, the HTTP contract needs a stable
request identity before retries become safe.

Good minimal shape:

```http
POST /payments
Idempotency-Key: 8f7a-123
```

Why it matters:

- the client may retry after a timeout
- without a stable request identity, the same write can run twice

Short rule:

> if a write can be retried and a duplicate side effect would hurt, give the request a stable identity

If you want the longer Java and Kotlin teaching examples behind that rule, see
[`../databases/01-idempotency-and-transaction-safety.md`](../databases/01-idempotency-and-transaction-safety.md).

### 5.2 `202 Accepted` for async completion

Good minimal shape:

```http
POST /payments
Idempotency-Key: 8f7a-123

HTTP/1.1 202 Accepted
Location: /payments/requests/8f7a-123
```

Why this is useful:

- the request was accepted
- the final result is not ready yet
- the client has a clear place to check status

Good fit:

- payment confirmation
- long-running import
- workflows that depend on later provider or broker confirmation

### 5.3 Validation vs conflict

This distinction matters a lot:

- `400` when the request shape is malformed
- `422` when the JSON is valid but the business content is not
- `409` when the request is valid but conflicts with current state or duplicate processing

Concrete example:

- malformed JSON -> `400`
- amount is negative -> `422`
- same idempotency key is already processing -> `409`

Bad vs better:

- bad: timeout plus blind retry can create a second charge or second order path
- better: idempotency keys and explicit async status make retries part of the contract, not accidental behavior

---

## 6. BFF: When A Shared API Stops Fitting The Clients

The BFF pattern exists because different clients want different payloads and latency tradeoffs.

If you want the fuller edge-boundary discussion, including gateway vs BFF, see
[`../architecture/17-gateway-vs-bff-vs-edge-patterns.md`](../architecture/17-gateway-vs-bff-vs-edge-patterns.md).

Small concrete example:

- mobile app wants one compact checkout summary call
- admin web wants large tables, filters, and detailed audit fields
- third-party API wants a stable public contract and stricter rate controls

If one backend endpoint tries to serve all three well, it usually becomes awkward for everyone.

What a BFF gives you:

- client-specific aggregation
- smaller mobile payloads
- less frontend orchestration
- a place to shape data without polluting core domain services

Bad vs better:

- bad: one shared endpoint stretched awkwardly across mobile, admin, and public third-party use cases
- better: separate client-facing shaping when the payload, latency, or composition needs genuinely diverge

When to use it:

- multiple clients have meaningfully different data needs
- composition logic would otherwise leak into every frontend
- you want frontend-facing contract changes without constant core-service changes

When not to use it:

- one client only
- thin CRUD domain where aggregation is trivial
- you are using a BFF only to hide weak service boundaries

---

## 7. Choice By Use Case

### Backoffice table with modest size

- offset pagination: yes
- cursor pagination: maybe, but often unnecessary
- BFF: usually no
  Why: the simplest thing is often good enough here.

### User-facing feed or order history

- cursor pagination: yes
- offset pagination: usually no for deep pages
- structured error payload: yes
  Why: large lists and stable ordering matter more here.

### Breaking contract change

- versioning: yes
- additive change without version bump: only if the old clients stay safe
  Why: versioning is for real breaking change, not for every field you add.

### Retry-prone user-critical write

- idempotency key: yes
- `202 Accepted`: maybe, when final confirmation is later
- vague success response with hidden async uncertainty: no
  Why: the client needs a clear contract under timeout and retry.

### Web, mobile, and third-party clients all want different payloads

- BFF: maybe yes
- one shared backend contract for all: maybe not
  Why: when client needs really diverge, a BFF can keep contracts cleaner.

---

## 8. The Big Traps

1. **Using offset pagination for huge user-facing feeds**
   Example: page 10,000 becomes slow and unstable under inserts or deletes.

2. **Returning vague errors that clients cannot act on**
   Example: every failure becomes `{ \"error\": \"Bad request\" }`.

3. **Versioning every additive change**
   Example: a harmless new field creates `/v7` for no good reason.

4. **Adding a BFF by default**
   Example: one thin CRUD app now has an extra layer with no real client-specific need.

5. **Treating API design as just endpoint naming**
   Example: URI names are neat, but pagination, errors, and contract evolution are weak.

6. **Treating a retry-prone write like a simple synchronous create**
   Example: `POST /payments` times out, the client retries, and the system creates a second charge path.

7. **Ignoring request cost and abuse shape**
   Example: one endpoint allows huge page sizes, expensive filters, or fan-out behavior with no limits.

8. **Treating authentication as if it automatically solved authorization**
   Example: the caller is logged in, but can still access another user's order or trigger an unsafe workflow step.

---

## 9. Decision Rules

These are the shortest clean answers worth memorizing:

- "Offset pagination is acceptable for small backoffice lists, but for large or user-facing feeds I prefer cursor pagination because it scales better and avoids shifting windows."
- "I want error responses that clients can act on, so I prefer structured problem details instead of vague generic payloads."
- "I version only when the contract change is breaking. URI versioning is usually the most pragmatic default."
- "If a user-critical write can be retried, I prefer a stable request identity and sometimes `202 Accepted` when the final result is asynchronous."
- "I use a BFF when multiple clients have genuinely different composition and latency needs, not as a default architecture layer."

---

## 10. 20-Second Answer

> Advanced API design means protecting the contract under growth and failure. I think
> about scalable pagination, structured errors, clear versioning, retry-safe writes,
> and whether different clients need their own aggregation layer. I prefer cursor
> pagination for large lists, structured problem details for failures, URI versioning
> for breaking changes, and explicit write contracts when retries or async completion
> matter.

---

## 11. 1-Minute Answer

> When I design APIs at a senior level, I focus less on controller syntax and more on
> contract durability. For list endpoints I choose offset or cursor pagination based on
> scale and ordering requirements; cursor pagination is my default for large or user-facing
> feeds. For failures I want structured problem details so the client can tell whether the
> issue is malformed input, a business conflict, rate limiting, or a transient outage. I
> version only when the contract change is breaking, and URI versioning is usually the most
> pragmatic production choice. On the write side, if a request can be retried and duplicates
> would hurt, I want a stable request identity such as an idempotency key, and I use `202 Accepted`
> when the final result will arrive later. If web, mobile, and third-party clients need very
> different aggregation and latency behavior, I consider a BFF, but I do not add it by default.

---

## 12. What To Internalize

- API design is contract design, not just endpoint naming
- a good API is clear, bounded, retry-safe, and operable
- pagination is a correctness and scale topic, not only a UX topic
- clients need actionable error shapes, not vague strings
- version only for breaking change when additive evolution is not enough
- retry-prone writes need explicit contracts too
- a BFF is useful when clients genuinely diverge, not because the pattern sounds modern
