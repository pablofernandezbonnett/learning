# AppSec for Software Engineers

Use this folder for practical application security study from a developer's point of view.

Focus:

- web and browser basics that security builds on
- authentication, sessions, tokens, and delegated access
- common web vulnerabilities and how they show up in code
- practical review habits for APIs and backend features

## Recommended Order

1. [01-http-and-https.md](./01-http-and-https.md): requests, responses, headers, cookies, and why transport security matters
2. [02-cookies-and-sessions.md](./02-cookies-and-sessions.md): session state, cookie flags, and common session failures
3. [03-basic-auth-bearer-and-api-keys.md](./03-basic-auth-bearer-and-api-keys.md): common credential transport patterns and their tradeoffs
4. [04-authentication-vs-authorization.md](./04-authentication-vs-authorization.md): who the user is versus what they can do
5. [05-jwt.md](./05-jwt.md): what JWTs are, what they are not, and how they fail in practice
6. [06-oauth2-and-openid-connect.md](./06-oauth2-and-openid-connect.md): delegated access, identity, and modern login flows
7. [07-sql-injection.md](./07-sql-injection.md): unsafe query construction and parameterization
8. [08-cross-site-scripting-xss.md](./08-cross-site-scripting-xss.md): browser-side code injection and output handling
9. [09-csrf.md](./09-csrf.md): why cookie-backed actions need request integrity defenses
10. [10-access-control-and-idor.md](./10-access-control-and-idor.md): broken authorization and object-level access mistakes

## Refresh

- [01-http-and-https.md](./01-http-and-https.md)
- [02-cookies-and-sessions.md](./02-cookies-and-sessions.md)
- [04-authentication-vs-authorization.md](./04-authentication-vs-authorization.md)
- [10-access-control-and-idor.md](./10-access-control-and-idor.md)

## Required

- [03-basic-auth-bearer-and-api-keys.md](./03-basic-auth-bearer-and-api-keys.md)
- [05-jwt.md](./05-jwt.md)
- [06-oauth2-and-openid-connect.md](./06-oauth2-and-openid-connect.md)
- [07-sql-injection.md](./07-sql-injection.md)
- [08-cross-site-scripting-xss.md](./08-cross-site-scripting-xss.md)
- [09-csrf.md](./09-csrf.md)

## Growth

- extend this track later with file uploads, SSRF, secrets management, dependency risk, threat modeling, and secure code review checklists

## Practice Platforms

- [PortSwigger Web Security Academy](https://portswigger.net/web-security): best free guided labs for web AppSec
- [OWASP Juice Shop](https://owasp.org/www-project-juice-shop/): deliberately vulnerable modern web app
- [OWASP WebGoat](https://owasp.org/www-project-webgoat/): guided insecure training app

## Core Rule

- do not study AppSec as a glossary
- for each topic, learn how it works, how it breaks, and how a dev should build it better
