# AppSec for Software Engineers Path

Use this path when you want to learn application security from a developer's point of view instead of starting with general cybersecurity theory.

## Goal

Build enough practical understanding to:

- design common web features more safely
- recognize the main auth and session risks
- review APIs and backend code for obvious AppSec failures
- practice safely with real labs instead of studying security as vocabulary only

## Working Rule

For each topic:

1. learn the smallest useful mental model
2. inspect one real request, flow, or code example
3. understand the common failure mode
4. note the main defense a developer should apply
5. solve one small practice lab before moving on

## Recommended Order

### Phase 1. Web and Session Basics

1. [../topics/appsec/01-http-and-https.md](../topics/appsec/01-http-and-https.md)
2. [../topics/appsec/02-cookies-and-sessions.md](../topics/appsec/02-cookies-and-sessions.md)
3. [../topics/appsec/03-basic-auth-bearer-and-api-keys.md](../topics/appsec/03-basic-auth-bearer-and-api-keys.md)
4. [../topics/appsec/04-authentication-vs-authorization.md](../topics/appsec/04-authentication-vs-authorization.md)

Outcome:

- you understand how browsers, sessions, and credentials actually move through requests

### Phase 2. Tokens and Modern Login Flows

5. [../topics/appsec/05-jwt.md](../topics/appsec/05-jwt.md)
6. [../topics/appsec/06-oauth2-and-openid-connect.md](../topics/appsec/06-oauth2-and-openid-connect.md)

Outcome:

- you can explain the difference between sessions, bearer tokens, JWTs, OAuth2, and `OIDC` (`OpenID Connect`) without mixing them up

### Phase 3. Core Web Vulnerabilities

7. [../topics/appsec/07-sql-injection.md](../topics/appsec/07-sql-injection.md)
8. [../topics/appsec/08-cross-site-scripting-xss.md](../topics/appsec/08-cross-site-scripting-xss.md)
9. [../topics/appsec/09-csrf.md](../topics/appsec/09-csrf.md)
10. [../topics/appsec/10-access-control-and-idor.md](../topics/appsec/10-access-control-and-idor.md)

Outcome:

- you can look at a CRUD app or API and identify the first major security questions to ask

## Practice Rule

Pair the notes with:

- [PortSwigger Web Security Academy](https://portswigger.net/web-security)
- [OWASP Juice Shop](https://owasp.org/www-project-juice-shop/)
- [OWASP WebGoat](https://owasp.org/www-project-webgoat/)

Recommended habit:

- read one topic
- solve one related lab
- write 3-5 lines on how the bug happens and how you would prevent it in a real app

## Next Expansion

After this first pass, extend the path with:

1. file upload security
2. SSRF
3. secrets management
4. dependency risk and supply chain basics
5. threat modeling
6. secure code review checklists
