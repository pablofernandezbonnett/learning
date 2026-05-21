# Secure Java Companion

> Primary fit: `Shared core / Spring / JVM / secure coding companion`

Use this note when you want a durable companion set for secure Java and Spring
study without depending on one vendor course staying available forever.

This is not a copy of anyone else's course.
It is a repo-local guide that maps the core secure-coding topics to:

- the mental model that matters
- the backend failure mode behind the topic
- the repo notes that already teach the topic
- the public primary references worth keeping

---

## Why This Matters

Secure-coding courses are useful, but course access can expire and course
materials are often optimized for one product, one exam, or one lab flow.

A stronger long-term setup is:

- learn the principles once
- tie them to your real backend stack
- keep your own durable notes
- improve those notes over time with code-review and production experience

This note exists to make the secure-Java topics easier to reopen later, and to
connect them directly to Spring, APIs, sessions, validation, and backend
decision-making.

## Smallest Mental Model

Secure Java backend work is usually about protecting five boundaries:

1. who can call the action
2. what input is accepted
3. how untrusted data is rendered or returned
4. how browser state and sessions are protected
5. how data, secrets, and runtime behavior stay safe behind the API

Strong default:

- validate input early
- authorize close to the business action
- encode output for the real context
- treat cookies and sessions as credentials
- keep database interaction parameterized
- keep secrets, logs, and config hygiene explicit

## How To Use This Note

For each topic below:

1. read the short explanation here
2. open the linked repo notes
3. save your own examples from your stack or project
4. improve the note with your own review findings later

That way your repo becomes the durable knowledge base, not only a course
workspace.

---

## 1. Secure Coding Principles

This is the umbrella topic.
The point is not "write defensive code" as a slogan.
The point is to keep untrusted data, state transitions, and business authority
separated clearly enough that the application stays predictable.

What this topic really means:

- trust boundaries are explicit
- safe defaults win over convenience
- the code does not let user input become control over queries, rendering, or authorization
- security checks live in the normal code path, not in scattered afterthoughts

Good repo notes:

- [03-spring-and-jvm-appsec.md](./03-spring-and-jvm-appsec.md)
- [02-web-and-api-security.md](./02-web-and-api-security.md)
- [06-threat-modeling-and-business-abuse.md](./06-threat-modeling-and-business-abuse.md)

Public references:

- [OWASP Developer Guide: Validate All Inputs](https://devguide.owasp.org/en/04-design/02-web-app-checklist/05-validate-inputs/)
- [OWASP Developer Guide PDF](https://owasp.org/www-project-developer-guide/assets/exports/OWASP_Developer_Guide.pdf)

Short rule:

> secure coding is mostly the discipline of making trust boundaries, safe
> defaults, and business checks explicit in ordinary code.

---

## 2. Error Handling and Logging with Java

Error handling is not only about nice API responses.
It is also about not leaking internals and about logging enough signal to detect
real abuse or failure.

What to learn here:

- return stable, sanitized error shapes
- do not leak stack traces, SQL details, secrets, or internal IDs
- log authentication, authorization, and workflow anomalies without logging secrets
- separate user-facing error clarity from operator-facing diagnostic detail

Good repo notes:

- [../../topics/spring-boot/02-exception-handling.md](../../topics/spring-boot/02-exception-handling.md)
- [07-secrets-logging-and-secure-sdlc.md](./07-secrets-logging-and-secure-sdlc.md)

Public references:

- [OWASP Error Handling Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Error_Handling_Cheat_Sheet.html)
- [OWASP Logging Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html)

Practical review questions:

- does the response reveal more than the client needs?
- do logs capture the event without leaking credentials or tokens?
- are authz denials and suspicious state transitions visible in structured logs?

---

## 3. Input Validation with Java

Input validation is about controlling what data enters the application boundary.
It is not a substitute for authorization, output encoding, or parameterized SQL.

What to learn here:

- validate shape, type, size, format, and allowed values
- keep validation close to request DTOs and boundary objects
- reject unknown or over-posted fields where they would be dangerous
- remember that client-side validation is convenience, not trust

Java/Spring translation:

- explicit DTOs
- Bean Validation / Jakarta Validation
- deliberate mapping from request objects into domain objects

Good repo notes:

- [03-spring-and-jvm-appsec.md](./03-spring-and-jvm-appsec.md)
- [../../topics/appsec/07-sql-injection.md](../../topics/appsec/07-sql-injection.md)
- [../../topics/spring-boot/11-web-annotations.md](../../topics/spring-boot/11-web-annotations.md)

Public references:

- [OWASP Input Validation Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html)
- [Spring Framework Validation Reference](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html)

Bad mental model vs better mental model:

- bad: "if it validates, it is safe"
- better: "validation reduces bad input, but authorization, encoding, and safe query construction still matter separately"

---

## 4. Output Encoding with Java

Output encoding is the topic that usually becomes real through `XSS`.
The key idea is not "escape everything somehow."
It is "encode for the context where the data lands."

What to learn here:

- HTML text, attributes, URLs, and JavaScript strings do not share one encoding rule
- backend code can create `XSS` risk by returning or templating unsafe data
- safe framework defaults are strong, but raw HTML or unsafe rendering paths raise the review bar

Java/Spring translation:

- template engines such as Thymeleaf help when you stay on the escaped path
- raw HTML helpers such as `th:utext` raise the review bar because they bypass escaping
- embedding untrusted data into HTML, attributes, URLs, or JavaScript needs the encoding that matches that context
- returning JSON from an API is not the same thing as safely embedding user data into an HTML page later

Good repo notes:

- [../../topics/appsec/08-cross-site-scripting-xss.md](../../topics/appsec/08-cross-site-scripting-xss.md)
- [01-auth-sessions-vs-jwt.md](./01-auth-sessions-vs-jwt.md)

Public references:

- [OWASP Cross Site Scripting Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)
- [OWASP DOM Based XSS Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/DOM_based_XSS_Prevention_Cheat_Sheet.html)
- [OWASP Java Encoder Project](https://owasp.org/www-project-java-encoder)
- [Thymeleaf Tutorial](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html)

Short rule:

> output encoding is not one global helper. It is a context-specific boundary
> that stops untrusted data from becoming executable browser behavior.

---

## 5. HTTP Cookie Security with Java

Cookies often carry browser-side credentials or state handles.
That makes cookie settings part of the auth design, not an HTTP detail.

What to learn here:

- `HttpOnly`, `Secure`, and `SameSite`
- session cookie vs persistent cookie
- session fixation, expiration, logout, and rotation
- why cookie-backed auth introduces `CSRF` considerations

Java/Spring translation:

- browser login and session flows
- secure cookie configuration
- CSRF token support in Spring Security

Good repo notes:

- [../../topics/appsec/02-cookies-and-sessions.md](../../topics/appsec/02-cookies-and-sessions.md)
- [../../topics/appsec/09-csrf.md](../../topics/appsec/09-csrf.md)
- [01-auth-sessions-vs-jwt.md](./01-auth-sessions-vs-jwt.md)
- [../../topics/spring-boot/01-spring-boot-fast-review.md](../../topics/spring-boot/01-spring-boot-fast-review.md)

Public references:

- [MDN Set-Cookie](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie/SameSite)
- [OWASP Session Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html)
- [Spring Security CSRF Reference](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)

Practical review questions:

- what cookie actually turns the browser into an authenticated session?
- is it `HttpOnly`, `Secure`, and intentionally `SameSite` configured?
- does the login flow rotate session identifiers after authentication?

---

## 6. Security Misconfigurations with Java

Misconfiguration is the topic where "the framework is secure" often becomes a
dangerous sentence.

What to learn here:

- unsafe defaults left on for convenience
- missing or weak security headers
- overly broad CORS
- disabled or misunderstood CSRF protection
- exposed actuator, admin, debug, or error surfaces
- dependency drift and stale framework versions

Good repo notes:

- [03-spring-and-jvm-appsec.md](./03-spring-and-jvm-appsec.md)
- [07-secrets-logging-and-secure-sdlc.md](./07-secrets-logging-and-secure-sdlc.md)
- [../../topics/spring-boot/10-profiles.md](../../topics/spring-boot/10-profiles.md)
- [../../topics/spring-boot/01-spring-boot-fast-review.md](../../topics/spring-boot/01-spring-boot-fast-review.md)

Public references:

- [OWASP HTTP Security Response Headers Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/HTTP_Headers_Cheat_Sheet.html)
- [Spring Security Method Security Reference](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
- [Spring Boot Security Reference](https://docs.spring.io/spring-boot/reference/web/spring-security.html)
- [Spring Security CORS Reference](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)
- [Spring Boot Actuator Endpoints Reference](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)

Bad mental model vs better mental model:

- bad: "we turned security on"
- better: "we verified what is exposed, which protections stay enabled, and what config changes would weaken those boundaries"

Practical caution:

- do not memorize one default actuator exposure story forever because it changes across versions
- safer reasoning is to verify what your exact application version exposes over HTTP and what extra endpoints were enabled intentionally

---

## 7. Web Session Management with Java

Session management overlaps with cookie security but is broader.
The key question is how the application creates, maintains, rotates, and ends
authenticated browser state safely.

What to learn here:

- opaque session identifiers
- session lifecycle and timeout
- privilege change and session rotation
- logout invalidation
- separation between session identity and server-side authorization

Good repo notes:

- [../../topics/appsec/02-cookies-and-sessions.md](../../topics/appsec/02-cookies-and-sessions.md)
- [01-auth-sessions-vs-jwt.md](./01-auth-sessions-vs-jwt.md)
- [../../topics/appsec/09-csrf.md](../../topics/appsec/09-csrf.md)

Public references:

- [OWASP Session Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html)
- [Spring Security CSRF Reference](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)

Short rule:

> treat session state like a credential with lifecycle rules, not like a
> convenience cache for login.

---

## 8. Using Databases with Java

The secure-database topic is not "know SQL."
It is "keep input from changing command structure, keep privileges narrow, and
keep data access predictable."

What to learn here:

- prepared statements and parameterization
- safe ORM and repository usage
- narrow database privileges
- sanitized database errors
- safe transaction boundaries around security-sensitive flows

Java/Spring translation:

- `PreparedStatement`
- JPA/Hibernate query discipline
- careful use of custom SQL and dynamic filters

Good repo notes:

- [../../topics/appsec/07-sql-injection.md](../../topics/appsec/07-sql-injection.md)
- [../../topics/databases/01-idempotency-and-transaction-safety.md](../../topics/databases/01-idempotency-and-transaction-safety.md)
- [../../topics/databases/02-database-locks-and-concurrency.md](../../topics/databases/02-database-locks-and-concurrency.md)
- [../../topics/spring-boot/04-jpa-hibernate-performance-traps.md](../../topics/spring-boot/04-jpa-hibernate-performance-traps.md)

Public references:

- [OWASP SQL Injection Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html)
- [Oracle PreparedStatement API](https://docs.oracle.com/en/java/javase/26/docs/api/java.sql/java/sql/PreparedStatement.html)
- [Oracle Using Prepared Statements Tutorial](https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html)

Practical review questions:

- can user input affect query structure or only parameter values?
- are dynamic filters or sort fields constrained safely?
- does the database account have more power than the application path really needs?

---

## 9. Assembling the Pieces: Java Security Essentials

This is the integration topic.
It is where the separate ideas become one backend request path.

Good repo notes:

- [03-spring-and-jvm-appsec.md](./03-spring-and-jvm-appsec.md)
- [01-auth-sessions-vs-jwt.md](./01-auth-sessions-vs-jwt.md)
- [02-web-and-api-security.md](./02-web-and-api-security.md)
- [../../topics/appsec/04-authentication-vs-authorization.md](../../topics/appsec/04-authentication-vs-authorization.md)
- [07-secrets-logging-and-secure-sdlc.md](./07-secrets-logging-and-secure-sdlc.md)

Practical rule:

> the secure backend is not one feature. It is the result of several ordinary
> engineering decisions all pointing in the same safe direction.

Use this integration check:

1. authenticate the caller
2. authorize the action and object access
3. validate the request DTO
4. parameterize database access
5. return sanitized errors
6. log the event without leaking secrets
7. protect browser session state if cookies are involved
8. keep headers, CORS, CSRF, actuator exposure, and dependency hygiene explicit

## A Simple Study Order

If you want the shortest useful order:

1. [03-spring-and-jvm-appsec.md](./03-spring-and-jvm-appsec.md)
2. [01-auth-sessions-vs-jwt.md](./01-auth-sessions-vs-jwt.md)
3. [../../topics/appsec/02-cookies-and-sessions.md](../../topics/appsec/02-cookies-and-sessions.md)
4. [../../topics/appsec/09-csrf.md](../../topics/appsec/09-csrf.md)
5. [../../topics/appsec/08-cross-site-scripting-xss.md](../../topics/appsec/08-cross-site-scripting-xss.md)
6. [../../topics/appsec/07-sql-injection.md](../../topics/appsec/07-sql-injection.md)
7. [07-secrets-logging-and-secure-sdlc.md](./07-secrets-logging-and-secure-sdlc.md)
8. [../../topics/spring-boot/02-exception-handling.md](../../topics/spring-boot/02-exception-handling.md)

## What This Companion Gives You

Compared with a one-course-only approach, this companion gives you:

- repo-local notes you control
- stronger links between secure coding and backend design
- easier transfer from Java to Kotlin and then conceptually to other backend stacks
- a place to merge future course notes into one durable system

## Coverage Check

Current repo coverage against this companion is strong for:

- sessions and cookies
- CSRF and XSS fundamentals
- SQL injection and safe database interaction
- Spring authorization, validation, serialization, and dependency hygiene
- secrets, logging, and secure SDLC habits
- Java template output-encoding basics
- Spring misconfiguration review around headers, CORS, CSRF, actuator, and exposed surfaces
- end-to-end secure request reasoning across auth, validation, database access, logging, and errors

What is still thinner than the rest:

- hands-on review exercises where the reader has to spot the flaw in a small Spring code sample
- deeper framework-specific examples beyond the common Spring and Thymeleaf path

That means the first durable version now covers the core syllabus topics well.
The main duplicate-control rule for this note is:

- practical detail should live first in the topic notes
- this companion should stay a summary map, a study order, and a cross-link layer

The best next improvement is not more topic sprawl, but more small review
exercises and repo-local examples.
