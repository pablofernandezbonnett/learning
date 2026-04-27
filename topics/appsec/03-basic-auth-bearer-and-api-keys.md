# Basic Auth, Bearer Tokens, and API Keys

Developers often lump these together as "ways to authenticate requests".
That is close enough to start, but the security tradeoffs are different and worth understanding clearly.

---

## 1. What This Topic Is

These are three common patterns for sending credentials with a request:

- `Basic`: usually username and password
- `Bearer`: a token that grants access if the server accepts it
- API key: an application credential, often used for service-to-service or developer APIs

Short rule:

> whoever possesses the credential can usually act with it, so storage and transport matter as much as syntax

---

## 2. Why It Matters

You will see these patterns in:

- internal tools
- third-party integrations
- service APIs
- legacy systems
- OAuth-protected resource requests

The mistake is assuming the header format alone makes one option secure.

---

## 3. What You Should Understand

- `Authorization` header basics
- why base64 in `Basic` is encoding, not protection
- what "bearer" means in practice
- where API keys fit and where they do not
- why HTTPS is mandatory for all three
- difference between user credentials and app credentials

---

## 4. How It Breaks In Real Apps

Common failures:

- using `Basic` over unsafe transport
- treating long-lived bearer tokens as harmless
- hardcoding API keys in source code
- logging tokens or keys by accident
- giving API keys broad permissions with no rotation plan

---

## 5. How To Build It Better

- use HTTPS everywhere
- prefer short-lived tokens when possible
- keep API keys scoped, revocable, and rotated
- avoid `Basic` for user-facing modern auth unless the case is tightly controlled
- never treat bearer tokens as safe to expose just because they are not passwords

---

## 6. What To Look For In Code and Config

- where credentials are read from headers
- whether auth secrets appear in logs
- token lifetime and revocation behavior
- key storage in environment variables or secret managers
- access scope attached to keys or tokens

---

## 7. Practical Exercise

Compare three requests:

- one with `Authorization: Basic ...`
- one with `Authorization: Bearer ...`
- one with an API key header

For each, answer:

- what exactly is being sent?
- what happens if it leaks?
- how would you revoke it?

---

## 8. Resources

- base: [MDN HTTP Authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Authentication)
- header reference: [MDN Authorization Header](https://developer.mozilla.org/docs/Web/HTTP/Reference/Headers/Authorization)
- bearer specification: [RFC 6750](https://www.rfc-editor.org/rfc/rfc6750)
