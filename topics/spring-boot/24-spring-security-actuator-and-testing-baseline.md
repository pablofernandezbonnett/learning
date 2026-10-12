# Spring Security, Actuator, and Tests

A Spring Boot service needs an explicit route policy, a deliberate
operational surface, and tests that prove both. These are separate
configuration points: a route may be exposed by Actuator and still denied by
Security, or allowed by Security but absent because Actuator did not expose it.

For authentication models and business authorization, use
[Spring AppSec](../security/03-spring-and-jvm-appsec.md). For metrics, traces,
and logs, use [observability](../devops/03-observability-and-monitoring.md).

## Define the HTTP Route Policy

For an API using HTTP Basic in this example:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers("/actuator/**").hasRole("OPS")
            .requestMatchers("/api/**").authenticated()
            .anyRequest().denyAll()
        )
        .httpBasic(Customizer.withDefaults())
        .build();
}
```

Order the specific rules before the broad ones. This example assumes the
default Actuator path and that the application and management routes share a
security setup; review the matchers if the base path or management port
changes. Once an application defines a `SecurityFilterChain`, Spring Boot's
default Actuator security backs away, so the application owns those rules.

The example keeps CSRF protection enabled. Do not disable it merely because
a route is called an API: browsers can send cookies or HTTP Basic credentials
automatically. Choose CSRF behavior from the actual authentication and client
model, then test state-changing requests. Route authentication also does not
prove a user may access a particular order; enforce that rule in the
application operation.

## Expose Only Useful Actuator Endpoints

Spring Boot exposes `health` over HTTP by default. Add another endpoint only
when an operator or platform uses it, and set its access policy separately.
For example, `management.endpoints.web.exposure.include=health,info` makes
both available over HTTP; the security chain above still restricts
`/actuator/info` to `OPS`.

Use liveness to detect a process that cannot recover, and readiness to decide
whether it should receive traffic. Probe paths and their availability depend
on the deployment setup. Avoid making liveness depend on a shared downstream
service: an outage there could restart every instance. If a separate
management port is used, check that probes reflect the application's real
traffic path. A shared dependency in readiness also needs a deliberate choice:
its outage could remove every instance from service. Spring Boot does not add
external health checks to its readiness probe by default. For the wider
operational decision, see
[observability](../devops/03-observability-and-monitoring.md).

## Test the Boundary That Matters

- Use a focused MVC test with the intended security configuration for
  anonymous access, an allowed user, a wrong role, validation, and the error
  body. `@WithMockUser` supplies a test identity; it does not test the real
  login mechanism.
- Test object-level authorization separately: two authenticated users must
  not gain the same access merely because both match `/api/**`.
- Use a small full-application test or smoke check for the actual Actuator
  exposure and access rules. A controller slice alone does not prove those
  endpoints exist or that the management port is configured correctly.
- Include a state-changing request with the CSRF behavior expected by the
  chosen browser or API client.

For HTTP validation and response shape, see
[web boundaries](./11-web-annotations.md). For error bodies, see
[error responses](./02-exception-handling.md).

## Further Reading

- [Spring Boot Actuator endpoints](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)
- [Spring Security route authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html)
- [Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [Spring Boot application tests](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html)
