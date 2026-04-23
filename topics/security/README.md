# Security for Backend Engineers

Use this folder for practical application security refresh in backend and
product systems.

Focus:

- authentication patterns for web, mobile, and APIs
- web and API abuse cases
- Spring and JVM security basics

## Recommended Order

1. [01-auth-sessions-vs-jwt.md](./01-auth-sessions-vs-jwt.md): sessions, bearer tokens, OAuth2, OIDC, and PKCE
2. [02-web-and-api-security.md](./02-web-and-api-security.md): broken authorization, workflow abuse, SSRF, rate limits, and trust boundaries
3. [03-spring-and-jvm-appsec.md](./03-spring-and-jvm-appsec.md): Spring Security, validation, serialization, secrets, and dependency hygiene
4. [04-advanced-auth-and-sso.md](./04-advanced-auth-and-sso.md): API keys, client credentials, BFF flows, and enterprise SSO
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

## Core Rule

- security is part of backend design, not a phase after coding
- authorization and workflow safety matter more than fashionable auth terminology
- correctness, limits, and trust boundaries are part of security
