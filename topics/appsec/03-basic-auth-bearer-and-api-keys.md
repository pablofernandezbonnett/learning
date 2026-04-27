# Basic Auth, Bearer Tokens, and API Keys

Developers often lump these together as "ways to authenticate requests".
That is close enough to start, but the security tradeoffs are different and worth understanding clearly.

---

## 1. What This Topic Is

These are three common patterns for sending credentials with a request:

- `Basic`: usually username and password
- `Bearer`: a token that grants access if the server accepts it
- API key: an application credential, often used for service-to-service or developer APIs

They may all look like "some string in a header", but they represent different trust decisions.
`Basic` usually proves user credentials directly.
`Bearer` proves possession of a token.
An API key usually identifies and authorizes an application or integration, not a human user.

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
The real questions are who the credential represents, how long it lives, where it is stored, and what happens if it leaks.

---

## 3. What You Should Understand

At this stage, you do not need every edge case of every standard.
You do need to be able to explain the difference between sending a password, sending a user token, and sending an app credential.

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

These failures matter because leaked credentials often behave like silent remote control.
The app may keep working normally while an attacker reuses the same secret elsewhere.

---

## 5. How To Build It Better

Treat every credential as something that can leak eventually.
That mindset pushes you toward shorter lifetimes, narrower scopes, better logging hygiene, and revocation paths that actually work.

- use HTTPS everywhere
- prefer short-lived tokens when possible
- keep API keys scoped, revocable, and rotated
- avoid `Basic` for user-facing modern auth unless the case is tightly controlled
- never treat bearer tokens as safe to expose just because they are not passwords

---

## 6. What To Look For In Code and Config

When reviewing an implementation, look beyond the authentication library.
A safe format can still become unsafe if tokens are stored badly, logged, or granted more power than they need.

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

That comparison forces you to think in terms of blast radius, not just syntax.

---

## 8. Resources

- base: [MDN HTTP Authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Authentication)
- header reference: [MDN Authorization Header](https://developer.mozilla.org/docs/Web/HTTP/Reference/Headers/Authorization)
- bearer specification: [RFC 6750](https://www.rfc-editor.org/rfc/rfc6750)

---

## 9. Internal Repo Links

- [../security/01-auth-sessions-vs-jwt.md](../security/01-auth-sessions-vs-jwt.md): broader repo note on common auth shapes, bearer semantics, and session versus token tradeoffs
- [../security/04-advanced-auth-and-sso.md](../security/04-advanced-auth-and-sso.md): deeper explanation of API keys, machine-to-machine auth, and advanced auth patterns
