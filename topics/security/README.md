# Security for Backend Engineers

Use this folder for practical application security refresh in backend and
product systems.

This folder overlaps with AppSec, but the emphasis here is a little more
backend-architecture oriented.
The main concern is trust boundaries, auth models, payment-adjacent
correctness, and operational security habits inside product systems.

Focus:

- authentication patterns for web, mobile, and APIs
- web and API abuse cases
- Spring and JVM security basics

Working style:

- explain security jargon before relying on it
- keep examples close to product and backend flows
- connect auth, trust boundaries, rate limits, and workflow abuse back to real system behavior

## Recommended Order

1. [01-auth-sessions-vs-jwt.md](./01-auth-sessions-vs-jwt.md): sessions, bearer tokens, OAuth2, OpenID Connect (`OIDC`), and `PKCE`, meaning the proof step that protects public-client login flows
2. [02-web-and-api-security.md](./02-web-and-api-security.md): broken authorization, workflow abuse, `SSRF` (server-side request forgery), rate limits, and trust boundaries
3. [03-spring-and-jvm-appsec.md](./03-spring-and-jvm-appsec.md): Spring Security, validation, serialization, secrets, and dependency hygiene
4. [04-advanced-auth-and-sso.md](./04-advanced-auth-and-sso.md): API keys, client credentials, `BFF` (backend for frontend) flows, and enterprise `SSO` (single sign-on)
5. [05-payment-integration-patterns.md](./05-payment-integration-patterns.md): idempotency, webhooks, auth/capture, and payment correctness
6. [06-threat-modeling-and-business-abuse.md](./06-threat-modeling-and-business-abuse.md): workflow risk, abuse cases, and control mapping
7. [07-secrets-logging-and-secure-sdlc.md](./07-secrets-logging-and-secure-sdlc.md): secrets, logging, dependency hygiene, and secure delivery

## Refresh

- [01-auth-sessions-vs-jwt.md](./01-auth-sessions-vs-jwt.md)
- [02-web-and-api-security.md](./02-web-and-api-security.md)
- [03-spring-and-jvm-appsec.md](./03-spring-and-jvm-appsec.md)

## Required

- [04-advanced-auth-and-sso.md](./04-advanced-auth-and-sso.md)
- [05-payment-integration-patterns.md](./05-payment-integration-patterns.md)

## Growth

- [06-threat-modeling-and-business-abuse.md](./06-threat-modeling-and-business-abuse.md)
- [07-secrets-logging-and-secure-sdlc.md](./07-secrets-logging-and-secure-sdlc.md)

## Related Path

If you want a structured first-pass study order from browser and web basics up
to common vulnerability classes, start with:

- [../../paths/appsec-for-software-engineers.md](../../paths/appsec-for-software-engineers.md)

## Core Rule

- security is part of backend design, not a phase after coding
- authorization and workflow safety matter more than fashionable auth terminology
- correctness, limits, and trust boundaries are part of security
