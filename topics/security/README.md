# Security in Software Systems

Use this block to design and operate trust boundaries. Start with who can act, what each request may change, and what happens under misuse or partial failure. Open the narrower notes when that decision appears.

1. [Authentication choices](./01-auth-sessions-vs-jwt.md): choose and operate a session or token model.
2. [Web and API design](./02-web-and-api-security.md): protect objects, workflows, resources, and outbound calls.
3. [Spring and JVM boundary](./03-spring-and-jvm-appsec.md): apply those rules in a Spring service.
4. [Service identity and enterprise SSO](./04-advanced-auth-and-sso.md): choose machine credentials and federated login.
5. [Payments](./05-payment-integration-patterns.md): preserve money and order state across uncertain outcomes.
6. [Threat modeling](./06-threat-modeling-and-business-abuse.md): turn abuse cases into controls and tests.
7. [Secrets and secure delivery](./07-secrets-logging-and-secure-sdlc.md): operate credentials, audit events, and dependencies.
8. [Mobile boundary](./08-mobile-appsec-basics.md): account for a public client and device-side risks.
9. [Standards map](./09-appsec-standards-and-resources.md): choose the right reference for a security question.

For browser mechanics and vulnerability-specific explanations, use [AppSec](../appsec/README.md). Its notes cover cookies, tokens, SQL injection, XSS, CSRF, and object-level authorization; this block focuses on system decisions and operation.
