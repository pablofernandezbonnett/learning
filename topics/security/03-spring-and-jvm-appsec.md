# Security Boundaries in Spring Services

Spring Security can enforce request and method rules, but the application must still decide whether a caller may access a particular business object and state transition.

## Put Each Check at the Right Boundary

- Configure the request security chain so every endpoint has an intentional rule; review multiple chains and matcher order.
- Use method or service authorization for business actions that can be reached through more than one route.
- Check object ownership and tenant scope against server-side data, not an ID or role supplied by the client.
- Map identity-provider scopes or claims to application authorities explicitly; do not assume a JWT claim automatically expresses a domain permission.
- Keep cookie-backed browser flows' CSRF protection aligned with their actual authentication mechanism.

For `GET /orders/{id}`, a route rule such as “authenticated” is insufficient. The service must load the order within the caller's permitted scope or reject it. Test both the allowed and denied cases with an authenticated principal.

## Validate and Limit Data

Bind requests to specific input models. Validate domain rules after shape validation, allow only intended fields to change, and avoid returning persistence models directly. Use parameterized database access. Encode output for its destination and keep internal exceptions out of public responses.

Load secrets from the runtime's secret source, review dependency updates, and exclude credentials or personal data from logs. These are application responsibilities even when a framework supplies helpers.

Use [Spring Security and testing](../spring-boot/24-spring-security-actuator-and-testing-baseline.md) for framework setup, [web and API decisions](./02-web-and-api-security.md) for the general threat model, and [AppSec](../appsec/README.md) for SQL injection, XSS, and CSRF mechanics.

## Further Reading

- [Spring Security reference](https://docs.spring.io/spring-security/reference/) for current request, method, OAuth resource-server, and CSRF configuration.
