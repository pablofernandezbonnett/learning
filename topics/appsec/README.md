# AppSec for Software Engineers

Use this folder when you want a vulnerability-first or browser-first AppSec
refresh from a developer point of view.

This is not the main security route for the senior backend roadmap.
It is a companion track for reopening the web, browser, session, token, and
common vulnerability mechanics that still matter in backend work.

The emphasis is not on memorizing vulnerability names in isolation.
It is on understanding how normal product and backend features become unsafe.

Focus:

- web and browser basics that security builds on
- authentication, sessions, tokens, and delegated access
- common web vulnerabilities and how they show up in code
- practical review habits for APIs and backend features

Working style:

- explain security jargon before relying on it
- keep examples close to browser, API, and backend implementation details
- prefer "how it breaks and how to build it safely" over glossary-style study

Decision boundary for AppSec material:

- use this folder as a companion vulnerability refresh, not as a second main backend path
- use `01` and `02` when browser, cookie, or session mechanics feel rusty
- use `03`, `05`, and `06` when auth transport and delegated-access mechanics need reopening
- use `07`, `08`, `09`, and `10` when the goal is vulnerability-class refresh for review or interview prep
- go back to `topics/security/` when the question is broader backend judgment, Spring habits, secure delivery, or workflow abuse

## Role In This Repo

Use this folder first when you need:

- a clean AppSec fundamentals pass
- browser and cookie behavior reopened before deeper backend security work
- explicit vulnerability-class refresh for interviews, review practice, or weak spots

Use [../security/README.md](../security/README.md) first when your main goal is:

- stronger backend security judgment
- secure Spring and JVM habits
- API and workflow threat thinking
- product security depth that stays close to backend design

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

- [07-sql-injection.md](./07-sql-injection.md)
- [08-cross-site-scripting-xss.md](./08-cross-site-scripting-xss.md)
- [09-csrf.md](./09-csrf.md)
- [10-access-control-and-idor.md](./10-access-control-and-idor.md)

## Growth

- [03-basic-auth-bearer-and-api-keys.md](./03-basic-auth-bearer-and-api-keys.md)
- [05-jwt.md](./05-jwt.md)
- [06-oauth2-and-openid-connect.md](./06-oauth2-and-openid-connect.md)
- extend this track later with file uploads, `SSRF`, secrets management, dependency risk, threat modeling, and secure code review checklists

## Practice Platforms

- [PortSwigger Web Security Academy](https://portswigger.net/web-security): best free guided labs for web AppSec
- [OWASP Juice Shop](https://owasp.org/www-project-juice-shop/): deliberately vulnerable modern web app
- [OWASP WebGoat](https://owasp.org/www-project-webgoat/): guided insecure training app

## Related Path

If you want a structured first-pass study order instead of a topic-only entrypoint, start with:

- [../../paths/adjacent/appsec-for-software-engineers.md](../../paths/adjacent/appsec-for-software-engineers.md)

If your goal expands into APIs, mobile clients, and AppSec work inside a
product team, continue with:

- [../../paths/adjacent/appsec-in-product-teams.md](../../paths/adjacent/appsec-in-product-teams.md)

## Related Internal Topics

- [../security/README.md](../security/README.md): canonical backend/product security route in this repo
- [../spring-boot/16-appsec-authz-lab.md](../spring-boot/16-appsec-authz-lab.md): focused Spring authorization lab
- [../architecture/04-networking-fundamentals.md](../architecture/04-networking-fundamentals.md): longer networking and HTTP refresher

## Core Rule

- do not study AppSec as a glossary
- for each topic, learn how it works, how it breaks, and how a dev should build it better
- use this folder as a companion when the main goal is backend employability, not as a second competing main path
