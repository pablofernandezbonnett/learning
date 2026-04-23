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

---

## 5. Write Paths Under Retry And Async Work

Many API problems are not about list reads.
They are about user-critical writes that can be retried, delayed, or confirmed later.

### 5.1 Retry-safe `POST`

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

Example:

- malformed JSON -> `400`
- amount is negative -> `422`
- same idempotency key is already processing -> `409`

---

## 6. BFF: When A Shared API Stops Fitting The Clients

The BFF pattern exists because different clients want different payloads and latency tradeoffs.

Smallest example:

- mobile app wants one compact checkout summary call
- admin web wants large tables, filters, and detailed audit fields
- third-party API wants a stable public contract and stricter rate controls

If one backend endpoint tries to serve all three well, it usually becomes awkward for everyone.

What a BFF gives you:

- client-specific aggregation
- smaller mobile payloads
- less frontend orchestration
- a place to shape data without polluting core domain services

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
- pagination is a correctness and scale topic, not only a UX topic
- clients need actionable error shapes, not vague strings
- version only for breaking change when additive evolution is not enough
- retry-prone writes need explicit contracts too
- a BFF is useful when clients genuinely diverge, not because the pattern sounds modern
