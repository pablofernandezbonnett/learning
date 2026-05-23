# Spring and JVM AppSec

Spring gives you strong security building blocks, but it does not remove the
need for judgment.

For a backend engineer, Spring AppSec is mostly about using the framework
correctly and not leaving dangerous gaps around authorization, configuration,
serialization, and dependencies.

---

## Why This Matters

Spring security discussions go wrong when people assume the framework default is
the same as a safe application design.

This topic matters because many real AppSec bugs in Spring services are not
crypto mistakes.
They are normal engineering mistakes such as:

- request-level auth without object-level authorization
- accepting too much input through binding
- leaking internals through serialization or error handling
- storing secrets badly
- trusting dependency hygiene to happen automatically

If you can explain where authorization, validation, serialization, secrets, and
dependency controls live, you already have a much stronger AppSec answer.

## Smallest Mental Model

For Spring and JVM applications, the practical AppSec surface is usually:

- who can call this action
- which object or workflow state they can affect
- what input is accepted and validated
- what output or error detail is exposed
- where secrets come from
- how dependency risk is controlled

Strong default:

- explicit security config
- method-level authorization for business actions
- explicit DTOs and validation
- sanitized errors
- secrets from a proper secret source
- dependency scanning in `CI` (`continuous integration`)

## Bad Mental Model vs Better Mental Model

Bad mental model:

- Spring Security is enabled, so authorization is mostly solved
- if the endpoint is authenticated, the main security work is done
- serialization and binding are convenience topics, not security topics

Better mental model:

- framework defaults reduce boilerplate, but business authorization still needs explicit design
- authenticated users can still abuse object access and workflow transitions
- binding, serialization, secret handling, and dependency hygiene are part of the attack surface

Small concrete example:

- weak approach: `/api/orders/**` requires login, but any logged-in user can fetch or refund any order ID
- stronger approach: request security is combined with method-level authorization, object-level ownership checks, DTO validation, and sanitized error handling

Reusable takeaway:

> In Spring apps I do not stop at "security enabled." I check where business
> authorization lives, what binding and serialization expose, how secrets are
> loaded, and whether dependency risk is controlled in CI.

---

## How To Use This As A Secure-Coding Companion

If you are studying a secure-Java course such as `SJD-100`, use this note as
the Spring and JVM anchor, not as the whole study set.

This note is strongest for:

- request and method authorization
- DTO validation and binding risk
- serialization and deserialization boundaries
- secret handling and dependency hygiene

Pair it with these repo notes for the rest of the secure-coding picture:

- [`01-auth-sessions-vs-jwt.md`](./01-auth-sessions-vs-jwt.md): cookies, sessions, token tradeoffs, and browser security constraints
- [`02-web-and-api-security.md`](./02-web-and-api-security.md): trust boundaries, abuse cases, rate limits, and `SSRF`
- [`07-secrets-logging-and-secure-sdlc.md`](./07-secrets-logging-and-secure-sdlc.md): secret handling, logging, and delivery hygiene
- [`../../topics/appsec/07-sql-injection.md`](../../topics/appsec/07-sql-injection.md): SQL injection and safe database query habits
- [`../../topics/appsec/08-cross-site-scripting-xss.md`](../../topics/appsec/08-cross-site-scripting-xss.md): output handling and browser injection risk
- [`../../topics/appsec/09-csrf.md`](../../topics/appsec/09-csrf.md): cookie-backed request integrity and CSRF
- [`../../topics/spring-boot/02-exception-handling.md`](../../topics/spring-boot/02-exception-handling.md): sanitized error handling for APIs

Plain-English version:

> use this note for the framework-specific secure-coding decisions, and use the
> neighboring security and AppSec notes to fill in cookies, sessions, XSS, CSRF,
> SQL injection, and secure operational habits.

---

## 1. The Main Principle

Secure frameworks reduce boilerplate.

They do not remove the need to answer:

- who can call this action
- which object can they access
- what data is accepted
- what data is exposed
- how secrets are loaded
- how vulnerable dependencies are handled

---

## 2. High-Value Areas in Spring Applications

### Request Security and Method Security

URL rules alone are rarely enough.

Use request-level protection for broad boundaries and method-level protection for
business actions.

Typical failure:

- `/api/orders/**` is authenticated
- but any authenticated user can load or mutate any order ID

Useful pattern:

```kotlin
@EnableMethodSecurity
```

<details>
<summary>Java version</summary>

```java
@EnableMethodSecurity
```

</details>

```kotlin
@PreAuthorize("hasAuthority('ORDER_READ')")
fun getOrder(orderId: Long): OrderDto = orderService.getVisibleOrder(orderId)
```

<details>
<summary>Java version</summary>

```java
@PreAuthorize("hasAuthority('ORDER_READ')")
public OrderDto getOrder(Long orderId) { ... }
```

</details>

The point is not the annotation itself.
The point is that business actions need server-side authorization close to the
business code.

### Roles, Authorities, And Scopes In Plain Language

This is one of the most common Spring Security confusion points.

Smallest practical model:

- **authority**: the generic permission string Spring checks
- **role**: usually a coarse-grained business grouping such as `ADMIN` or `SUPPORT`
- **scope**: an OAuth2 permission attached to a token, often describing what the client is allowed to do with an API

Short rule:

> in Spring Security, roles and OAuth scopes usually end up as authorities

That is why explanations often sound cleaner when you start from `authority` as the
generic concept.

#### 1. Authorities

An authority is just a permission Spring can check.

Examples:

- `ORDER_READ`
- `ORDER_REFUND`
- `SCOPE_orders.read`
- `ROLE_ADMIN`

That is why `hasAuthority(...)` is the most explicit check:

```kotlin
@PreAuthorize("hasAuthority('ORDER_READ')")
fun getOrder(orderId: Long): OrderDto = orderService.getVisibleOrder(orderId)
```

Use this when you want the rule to match one exact permission string.

#### 2. Roles

A role is usually a broader grouping meant for people or operator types.

Examples:

- `ADMIN`
- `SUPPORT`
- `CUSTOMER`

In Spring Security, `hasRole('ADMIN')` usually means:

- Spring will actually look for the authority `ROLE_ADMIN`

That implicit prefix is the main source of confusion.

So these are conceptually related:

```kotlin
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
```

Practical rule:

- use `role` when the rule is truly about a coarse user type
- use `authority` when you want exact permission names and less hidden magic

#### 3. OAuth2 scopes

A scope is usually a token-level permission granted by the authorization server.

Examples:

- `orders.read`
- `orders.write`
- `profile`

In Spring Security resource-server setups, scopes from a JWT are often mapped into authorities with a `SCOPE_` prefix.

That means:

- token scope `orders.read`
- becomes authority `SCOPE_orders.read`

So a typical check becomes:

```kotlin
@PreAuthorize("hasAuthority('SCOPE_orders.read')")
fun getOrder(orderId: Long): OrderDto = orderService.getVisibleOrder(orderId)
```

This is very common in OAuth2 bearer-token APIs.

#### 4. The easiest mental map to remember

Use this translation:

- user type -> role
- precise permission Spring checks -> authority
- token permission from OAuth2 -> scope, often mapped to authority

One safe line:

> In Spring Security I treat authority as the generic checkable permission. Roles are coarse groups, and OAuth2 scopes usually arrive from the token and get mapped into authorities such as `SCOPE_orders.read`.

#### 5. Typical Spring examples

Request-level rule:

```kotlin
http.authorizeHttpRequests {
    it.requestMatchers(HttpMethod.GET, "/api/orders/**").hasAuthority("SCOPE_orders.read")
      .requestMatchers(HttpMethod.POST, "/api/orders/*/refund").hasRole("SUPPORT")
      .anyRequest().authenticated()
}
```

Method-level rule:

```kotlin
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ORDER_REFUND', 'SCOPE_orders.write')")
fun refundOrder(orderId: Long) { }
```

These examples are intentionally mixed to show the difference:

- `ROLE_ADMIN` is a coarse admin-style authority
- `ORDER_REFUND` is an application permission
- `SCOPE_orders.write` comes from an OAuth2 token permission

#### 6. What usually happens with JWT bearer auth

In a bearer-token API, the normal flow is:

1. authorization server issues JWT with scopes or claims
2. Spring Security resource server validates the JWT
3. claims are mapped into `GrantedAuthority`
4. request and method security checks those authorities

If you need a deeper implementation hook, the class name worth recognizing is:

- `JwtAuthenticationConverter`

You do not need to memorize the whole setup.
You just need to know that Spring can map token claims into authorities before your business code checks them.

#### 7. The common mistakes

- using `hasRole('ADMIN')` while the actual authority names do not use the `ROLE_` convention
- mixing business ownership checks with coarse role checks and thinking the problem is solved
- assuming token scope alone is enough for object-level authorization
- hiding all authorization logic in unreadable SpEL

Important rule:

> role, authority, and scope checks decide broad permission. They do not replace object-level checks like "is this really your order?"

### Password Storage

Never store raw passwords.

Use modern password hashing and framework-supported encoders.

Good rule:

- prefer modern, adaptive password hashing
- avoid custom crypto
- plan migrations for legacy hashes

### Validation and Binding

Unsafe binding is one of the easiest ways to create silent bugs.

Watch for:

- DTOs that accept more fields than intended
- missing validation annotations
- trusting client-supplied enums, IDs, or role-like fields
- mass assignment style problems

Good rule:

> Accept explicit DTOs, validate them, and map them deliberately.

Small Spring shape:

```kotlin
data class UpdateEmailRequest(
    @field:NotBlank
    @field:Email
    val email: String,
)

@PostMapping("/users/{id}/email")
fun updateEmail(
    @PathVariable id: Long,
    @Valid @RequestBody request: UpdateEmailRequest,
) {
    userService.updateEmail(id, request.email)
}
```

Why this is better:

- the request shape is explicit
- validation happens at the HTTP boundary
- the caller cannot over-post unrelated fields such as `role` or `enabled`

Practical rule:

- validate boundary DTOs
- map them deliberately into domain commands or service inputs
- do not bind broad entity-like objects straight from the request if the caller should only control a subset of fields

### Serialization and Deserialization

Serialization is part of your attack surface.

Watch for:

- overly permissive Jackson configuration
- polymorphic deserialization without strong control
- returning domain objects directly instead of response DTOs

Good rule:

> Be explicit about what enters and what leaves the API.

Small review example:

- weak approach: controller returns a JPA entity directly and trusts default Jackson behavior
- stronger approach: controller returns a response DTO with only the fields the API intends to expose

Practical caution:

- polymorphic deserialization should be treated carefully because it expands what untrusted input may instantiate
- entity graphs and internal persistence objects are often too broad for stable API output contracts

### Headers, CORS, and Operational Exposure

Some of the highest-value Spring misconfiguration questions are not inside one
controller method.
They are at the web edge and runtime edge.

Watch for:

- CORS allowlists broader than the real frontend origins
- CSRF disabled without understanding the auth model
- missing or weak security headers
- actuator, error, or admin endpoints exposed too broadly

Small mental model:

- CORS controls which browser origins can call you
- CSRF matters when the browser sends authenticated cookies automatically
- headers such as `Content-Security-Policy` or `X-Frame-Options` add browser-side protection
- actuator exposure is an operational boundary, not a convenience toggle

Good rule:

> Treat CORS, CSRF, headers, and actuator exposure as part of the public attack
> surface, not as deployment trivia.

### Error Handling

Good error handling is also a security control.

Avoid:

- stack traces in responses
- leaking internal IDs, SQL details, or secrets
- inconsistent status codes that reveal too much

### Secrets and Configuration

Do not treat `application.yml` as a secret store.

Watch for:

- secrets hardcoded in properties
- long-lived credentials in local config
- secrets copied into logs or exception messages

### Dependency Hygiene

Your code can be safe while your dependencies are not.

For JVM projects, keep an eye on:

- Spring ecosystem version drift
- transitive vulnerable libraries
- old JSON, XML, templating, or logging libraries

---

## 3. What Good Engineering Looks Like

In Spring systems, strong AppSec habits look like this:

- explicit security configuration
- method-level authorization where business actions matter
- DTO validation instead of trusting bound objects
- careful outbound and inbound serialization
- secrets loaded from a proper secrets system
- dependency scanning in CI

---

## 4. Code Review Checklist

- Is authorization enforced server-side for the business action?
- Are request DTOs explicit and validated?
- Could a caller over-post fields they should not control?
- Are CORS, CSRF, and exposed operational endpoints aligned with the real auth model?
- Are errors sanitized?
- Are secrets absent from source and logs?
- Is method security used where request-level auth is too coarse?
- Are dependencies scanned and updated with intent?

---

## 5. Practical Summary

Practical summary:

> In Spring applications, security is mostly about putting the right controls in
> the right place. I want authentication, authorization, validation, safe error
> handling, and dependency hygiene to be explicit. The framework helps, but the
> important part is still designing the boundaries correctly.

## What To Internalize

- framework help is not the same thing as safe business authorization
- method-level checks matter when URL rules are too coarse
- DTO validation and deliberate mapping reduce binding and over-posting risk
- serialization and error handling are part of the attack surface
- secrets and dependency hygiene are normal backend security work, not cleanup tasks

---

## 6. Further Reading

- Spring Security Reference: https://docs.spring.io/spring-security/reference/index.html
- Spring Security Method Security: https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html
- Spring Authorization Server Reference: https://docs.spring.io/spring-authorization-server/reference/index.html
- OWASP Password Storage Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
- OWASP Dependency-Check: https://owasp.org/www-project-dependency-check/
- OWASP ASVS: https://owasp.org/www-project-application-security-verification-standard/
