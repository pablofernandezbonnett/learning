# Security for Backend Engineers

Use this folder as the main security entrypoint for this repository when your
goal is stronger backend and product security judgment.

This is the canonical security track for the senior backend route.
The focus is not vulnerability memorization.
The focus is trust boundaries, auth models, payment-adjacent correctness,
secure delivery habits, and the places where backend design and security meet.

Use `topics/appsec/` as a companion set when you need a more browser-first or
vulnerability-first refresh.

Focus:

- authentication and authorization models for web, mobile, and APIs
- trust boundaries, workflow abuse, and backend threat thinking
- Spring and JVM security basics
- secrets, logging, delivery hygiene, and payment-adjacent correctness

Working style:

- explain security jargon before relying on it
- keep examples close to product and backend flows
- connect auth, trust boundaries, rate limits, and workflow abuse back to real system behavior

Decision boundary for security material:

- use this folder as the canonical security lane for the main senior backend path
- use `topics/appsec/` only when you need browser, cookie, or vulnerability-class mechanics reopened explicitly
- use `10-secure-java-companion.md` as a summary map after the core notes, not as a competing path
- use `09-appsec-standards-and-resources.md` as a reference map when you need standards context, not as a study starting point

## Role In This Repo

Use this folder first when you want to improve:

- backend security judgment
- secure Spring and JVM habits
- API and workflow safety
- product-team security collaboration

Use [../appsec/README.md](../appsec/README.md) selectively when you need:

- browser and cookie basics reopened quickly
- vulnerability-class refresh such as `XSS`, `CSRF`, `SQL injection`, or `IDOR`
- a more classical AppSec-first study order

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
- [09-appsec-standards-and-resources.md](./09-appsec-standards-and-resources.md): reference map, not a primary study note
- [10-secure-java-companion.md](./10-secure-java-companion.md): summary companion after the main notes above

## Related Path

If you want a structured first-pass study order from browser and web basics up
to common vulnerability classes, start with:

- [../../paths/adjacent/appsec-for-software-engineers.md](../../paths/adjacent/appsec-for-software-engineers.md)

If your goal is broader and includes APIs, mobile clients, standards, and
secure delivery habits inside a product team, continue with:

- [../../paths/adjacent/appsec-in-product-teams.md](../../paths/adjacent/appsec-in-product-teams.md)

## Core Rule

- security is part of backend design, not a phase after coding
- authorization and workflow safety matter more than fashionable auth terminology
- correctness, limits, and trust boundaries are part of security
- this folder is the main security route; AppSec is a companion when you need deeper vulnerability mechanics

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
