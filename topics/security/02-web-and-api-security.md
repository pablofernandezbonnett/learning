# Web and API Security for Backend Engineers

This module focuses on the AppSec topics that give the highest return for a
backend/product engineer.

If your systems expose APIs, handle user input, call third-party services, or
implement business workflows, these are the risks that matter most.

---

## Why This Matters

Most backend security bugs do not look like "advanced hacking." They look like
normal product code making the wrong trust decision: trusting the client too
much, trusting a partner payload too much, or exposing a dangerous workflow
step without enough checks.

This topic matters because these are the issues that appear in day-to-day API
work, code reviews, incidents, and strong security interview answers.

## Smallest Mental Model

Treat backend security as a set of trust and abuse questions:

- who is calling this
- what are they allowed to do
- what input or callback are we trusting
- what expensive or dangerous action could be abused

## Bad Mental Model vs Better Mental Model

Bad mental model:

- security is mostly crypto, passwords, and framework defaults
- if the endpoint requires login, it is probably safe
- partner systems and internal tools are mostly trusted

Better mental model:

- security is mostly about correct authorization, safe state transitions, and
  careful trust boundaries
- authenticated requests can still be dangerous
- third-party systems, internal tools, and expensive endpoints all need explicit
  controls

Small concrete example:

- weak approach: `POST /refunds/{id}` only checks that the user is logged in
- better approach: the backend verifies object-level access, valid workflow
  state, replay safety, and rate or abuse limits before allowing the refund

Strong default:

- start security reviews from authorization, workflow abuse, resource limits,
  and external trust boundaries before worrying about rarer edge cases

Interview-ready takeaway:

> For backend security, I start with who can do what, which state transitions
> must stay protected, and which inputs or callbacks cross a trust boundary.

---

## 1. The Core Idea

Most serious backend security issues are not "hackers breaking crypto".

They are usually one of these:

- broken authorization
- unsafe workflow/state transitions
- unsafe trust in user input or third-party APIs
- missing resource limits
- insecure defaults in public endpoints

In mature product systems, business logic abuse is often more realistic than
movie-style attacks.

---

## 2. High-Value Risks to Know

### Broken Access Control

This is the biggest category to internalize.

Examples:

- a user can read another user's order by changing an ID
- an internal support endpoint is reachable by normal users
- an "admin-only" action is protected in the UI but not enforced by the backend

Important API terms:

- BOLA: Broken Object Level Authorization
- BFLA: Broken Function Level Authorization

Backend rule:

> Never trust the client to select the right object or the right action.

### Unsafe Workflow Execution

Individual endpoints may be authenticated and still be unsafe.

Examples:

- `POST /checkout/confirm` can be called before payment succeeds
- refund endpoints can be replayed or called out of sequence
- inventory hold endpoints can be spammed to reserve stock without intent to buy

Backend rule:

> Secure the state machine, not only the endpoint.

### Resource Exhaustion

APIs fail in insecure ways when limits are missing.

Examples:

- unbounded search queries
- huge page sizes
- large file uploads
- missing rate limits on login or checkout
- webhook endpoints doing expensive work synchronously

Backend rule:

> Every expensive path needs a limit: size, time, frequency, or concurrency.

### SSRF and Unsafe Outbound Calls

If your backend fetches remote URLs, you have an SSRF risk.

Examples:

- image import by URL
- webhook forwarding
- document preview services
- integrations that fetch partner-provided URLs

Backend rule:

> Treat outbound requests as a trust boundary, not an implementation detail.

### Unsafe Consumption of Third-Party APIs

External APIs are not trusted just because they are "partner" systems.

Examples:

- trusting webhook payloads without signature verification
- assuming a third-party API always returns safe or well-formed data
- reusing partner IDs without authorization checks in your own system

Backend rule:

> Validate third-party input with the same discipline as user input.

### File Upload Risk

Uploads create both content risk and availability risk.

Examples:

- executable or active content uploaded and later served back
- oversized files or ZIP bombs
- malicious filenames or path traversal
- public retrieval of sensitive files

Backend rule:

> Validate, rename, limit, isolate, and scan uploads.

### Inventory and Endpoint Sprawl

You cannot secure endpoints you do not track.

Examples:

- old admin endpoints still deployed
- old API versions still exposed
- debug or internal routes reachable from the internet

Backend rule:

> Maintain an endpoint and version inventory, especially for APIs.

---

## 3. What This Looks Like in Commerce

For commerce and SaaS systems, the most important abuse cases are often:

- reading or modifying someone else's order
- calling payment or refund operations twice
- replaying signed webhooks
- hoarding inventory through reservation endpoints
- applying promotions outside intended business rules
- skipping checkout stages by calling later endpoints directly

This is why AppSec for product engineers must include business logic, not only
input validation and crypto.

---

## 4. Practical Checklist

Use this in code review or design review:

- Does every endpoint check authorization server-side?
- Can object IDs be guessed or tampered with?
- Can a workflow be executed out of order?
- Are expensive endpoints rate-limited and bounded?
- Are outbound requests validated or allowlisted?
- Are uploads isolated, renamed, and size-limited?
- Are old API versions and debug routes tracked?
- Are third-party responses treated as untrusted input?

---

## 5. Practical Summary

Good short answer:

> For backend and API security, I focus first on authorization, trust boundaries,
> and abuse cases. In real product systems the biggest risks are often not
> theoretical crypto failures, but broken access control, unsafe state
> transitions, missing limits, and trusting third-party systems too much.

---

## 6. Further Reading

- OWASP Top 10: https://owasp.org/www-project-top-ten/
- OWASP API Security Top 10: https://owasp.org/API-Security/
- OWASP REST Security Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html
- OWASP Authorization Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html
- OWASP SSRF Prevention Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html
- OWASP File Upload Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/File_Upload_Cheat_Sheet.html
