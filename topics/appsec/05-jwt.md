# JSON Web Tokens (JWT)

JWT is one of the most overused and most misunderstood topics in modern app security.
It is useful, but only if you stop treating it like a magic security upgrade.

---

## 1. What This Topic Is

A JWT is a token format for sending JSON claims between systems.
In practice it often contains user or session-related data and is signed so the receiver can detect tampering.

Short rule:

> JWT is a format, not a security strategy

---

## 2. Why It Matters

JWTs show up in:

- SPA and mobile backends
- SSO and identity providers
- API gateways
- microservice communication

Because they are common, bad JWT handling creates serious failures fast.

---

## 3. What You Should Understand

- header, payload, signature
- claims such as `iss`, `sub`, `aud`, `exp`, `iat`
- signed does not mean encrypted
- JWT versus JWS versus JWE at a practical level
- why JWT does not replace authorization checks

---

## 4. How It Breaks In Real Apps

Common failures:

- decoding a token without actually verifying it
- accepting the wrong algorithm
- weak shared secret for HMAC-signed tokens
- trusting claims without validating issuer, audience, and expiry
- putting too much authority in client-held claims

---

## 5. How To Build It Better

- verify signature with the expected algorithm
- validate `iss`, `aud`, and `exp`
- keep token lifetime short
- treat token claims as input that still needs a sound trust model
- do not use JWT just because it feels modern; use it when the architecture benefits from it

---

## 6. What To Look For In Code

- token parsing versus token verification APIs
- accepted algorithms
- hardcoded secrets
- missing issuer or audience validation
- business logic that trusts `role` or `isAdmin` claims too easily

---

## 7. Practical Exercise

Take a sample JWT and:

- decode it
- list its claims
- decide which claims the server must validate
- explain what would happen if an attacker changed the payload and the app forgot signature verification

---

## 8. Resources

- formal reference: [RFC 7519](https://www.rfc-editor.org/rfc/rfc7519)
- defense: [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- practice: [PortSwigger JWT Attacks](https://portswigger.net/web-security/jwt)
