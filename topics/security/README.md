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
8. [08-mobile-appsec-basics.md](./08-mobile-appsec-basics.md): public-client constraints, token handling, secure storage, WebViews, SDK risk, and backend trust boundaries for mobile systems
9. [09-appsec-standards-and-resources.md](./09-appsec-standards-and-resources.md): how to use `OWASP Top 10`, `API Top 10`, `ASVS`, `MASVS`, `MASTG`, `SAMM`, and practice resources without mixing their roles
10. [10-secure-java-companion.md](./10-secure-java-companion.md): durable companion map for secure Java, Spring, cookies, sessions, validation, output handling, misconfiguration, and secure database interaction

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
- [08-mobile-appsec-basics.md](./08-mobile-appsec-basics.md)
- [09-appsec-standards-and-resources.md](./09-appsec-standards-and-resources.md)

## Related Path

If you want a structured first-pass study order from browser and web basics up
to common vulnerability classes, start with:

- [../../paths/appsec-for-software-engineers.md](../../paths/appsec-for-software-engineers.md)

If your goal is broader and includes APIs, mobile clients, standards, and
secure delivery habits inside a product team, continue with:

- [../../paths/appsec-in-product-teams.md](../../paths/appsec-in-product-teams.md)

## Core Rule

- security is part of backend design, not a phase after coding
- authorization and workflow safety matter more than fashionable auth terminology
- correctness, limits, and trust boundaries are part of security

## If You Are Studying Secure Java

This repo can work well as a companion set for a secure-Java course, but not as
a copy of that course.

Use this order:

1. [03-spring-and-jvm-appsec.md](./03-spring-and-jvm-appsec.md): Spring/JVM security surface, validation, serialization, and dependency hygiene
2. [01-auth-sessions-vs-jwt.md](./01-auth-sessions-vs-jwt.md): cookies, sessions, token flows, and browser/mobile auth tradeoffs
3. [02-web-and-api-security.md](./02-web-and-api-security.md): trust boundaries, resource limits, and abuse thinking
4. [07-secrets-logging-and-secure-sdlc.md](./07-secrets-logging-and-secure-sdlc.md): secrets, logging, and secure delivery habits
5. [../appsec/07-sql-injection.md](../appsec/07-sql-injection.md): safe database interaction patterns
6. [../appsec/08-cross-site-scripting-xss.md](../appsec/08-cross-site-scripting-xss.md): output handling and browser injection risk
7. [../appsec/09-csrf.md](../appsec/09-csrf.md): cookie-backed request integrity

What this companion set gives you that a vendor course often does not:

- stronger repo-local cross-links between Java, Spring, API, and AppSec topics
- more explicit mental models and bad-vs-better reasoning
- easier transfer from Java to Kotlin and then conceptually to other backend stacks
- durable notes you can keep improving after the course access ends

Most of the practical guidance should live in the topic notes above, not in a
separate route.
Use the companion note as a summary map when useful, not as the only place
where the "how" exists.

If you want one durable secure-Java summary entrypoint for that companion work,
start with:

- [10-secure-java-companion.md](./10-secure-java-companion.md)
