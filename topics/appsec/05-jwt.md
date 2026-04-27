# JSON Web Tokens (JWT)

JWT is one of the most overused and most misunderstood topics in modern app security.
It is useful, but only if you stop treating it like a magic security upgrade.

---

## 1. What This Topic Is

A JWT is a token format for sending JSON claims between systems.
In practice it often contains user or session-related data and is signed so the receiver can detect tampering.

That does not mean the contents are secret.
Most JWTs you see in apps are only encoded, not encrypted, so anyone holding the token can often read the claims.
This is one of the first misconceptions worth killing early.

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
Teams also tend to adopt JWT for architectural reasons they cannot clearly explain, which makes misuse even more likely.

---

## 3. What You Should Understand

The important thing is not just naming the parts.
You should understand what the server must validate before trusting the token and what information is still unsafe to trust too eagerly even after validation.

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

The repeated failure pattern is confusing "I can read this token" with "I can trust this token".
Parsing is easy.
Trust requires correct verification and a sound design around what the token is allowed to decide.

---

## 5. How To Build It Better

Keep the trust model narrow and explicit.
Use the token to carry identity or session-related claims, but keep sensitive authorization decisions anchored in server-side rules and current application state where needed.

- verify signature with the expected algorithm
- validate `iss`, `aud`, and `exp`
- keep token lifetime short
- treat token claims as input that still needs a sound trust model
- do not use JWT just because it feels modern; use it when the architecture benefits from it

---

## 6. What To Look For In Code

Many JWT bugs are visible in just a few lines of code.
Look closely at whether the library call actually verifies the token or merely decodes it, and whether claim validation is configured or assumed.

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

If you can explain that scenario clearly, you already understand more than many teams who "use JWT in production".

---

## 8. Resources

- formal reference: [RFC 7519](https://www.rfc-editor.org/rfc/rfc7519)
- defense: [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- practice: [PortSwigger JWT Attacks](https://portswigger.net/web-security/jwt)

---

## 9. Internal Repo Links

- [../security/01-auth-sessions-vs-jwt.md](../security/01-auth-sessions-vs-jwt.md): longer repo note comparing session auth and JWT-based auth with web and mobile tradeoffs
- [../security/04-advanced-auth-and-sso.md](../security/04-advanced-auth-and-sso.md): extends JWT usage into OIDC, BFF flows, and service-to-service auth
