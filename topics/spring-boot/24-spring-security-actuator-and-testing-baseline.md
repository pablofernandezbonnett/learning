# Spring Security, Actuator, and Testing Baseline

Use this note when you want the smallest practical baseline for a Spring Boot
service that should be safe to operate, not only easy to start.

For Spring-specific examples, this note uses both Java and Kotlin because the
framework wiring is often easier to compare directly across both styles.

Why this matters:

- many Spring services look complete because the endpoint works, but still fail
  on auth, observability, or verification
- security, actuator exposure, and tests are often configured in different
  places even though they protect the same service boundary
- a senior backend engineer should be able to explain the default service
  posture before discussing deeper framework features

## Smallest Useful Mental Model

A production-minded Spring Boot service needs three baseline boundaries:

- request boundary: who can call which route
- runtime boundary: what the platform and operators can observe safely
- verification boundary: what tests prove before the change is trusted

In Spring terms, that usually means:

- one explicit `SecurityFilterChain`
- one deliberate actuator exposure policy
- one testing shape that covers unit, HTTP, and a few full integration paths

These are separate features, but they should tell one coherent story.

## Bad Mental Model vs Better Mental Model

Bad mental model:

- add Spring Security defaults
- expose actuator because operations asked for it
- rely on a few happy-path integration tests

Better mental model:

- define the public and internal routes explicitly
- expose only the actuator endpoints that have a real operational use
- test both allowed and denied behavior, not only business success

Weak baseline symptoms:

- every actuator endpoint exposed because "it is internal"
- no explicit test that an anonymous caller is rejected
- health is open, but readiness is not understood
- one `@SpringBootTest` suite doing everything slowly and still missing edge
  cases

## Small Concrete Example

Imagine a checkout service with:

- public API routes under `/api/**`
- actuator enabled for health and metrics
- authenticated access for normal API calls

A sensible first baseline could look like this:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/actuator/health",
                "/actuator/health/liveness",
                "/actuator/health/readiness"
            ).permitAll()
            .requestMatchers("/actuator/**").hasRole("OPS")
            .requestMatchers("/api/**").authenticated()
            .anyRequest().denyAll()
        )
        .httpBasic(Customizer.withDefaults());

    return http.build();
}
```

```kotlin
@Bean
fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http
        .csrf { it.disable() }
        .authorizeHttpRequests { auth ->
            auth.requestMatchers(
                "/actuator/health",
                "/actuator/health/liveness",
                "/actuator/health/readiness",
            ).permitAll()
            auth.requestMatchers("/actuator/**").hasRole("OPS")
            auth.requestMatchers("/api/**").authenticated()
            auth.anyRequest().denyAll()
        }
        .httpBasic(Customizer.withDefaults())

    return http.build()
}
```

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "health,info,prometheus"
```

```java
@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void anonymousCallerIsRejected() throws Exception {
        mockMvc.perform(get("/api/orders/42"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void authenticatedCallerCanReadOrder() throws Exception {
        mockMvc.perform(get("/api/orders/42"))
            .andExpect(status().isOk());
    }
}
```

```kotlin
@WebMvcTest(OrderController::class)
@Import(SecurityConfig::class)
class OrderControllerSecurityTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun anonymousCallerIsRejected() {
        mockMvc.get("/api/orders/42").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    @WithMockUser
    fun authenticatedCallerCanReadOrder() {
        mockMvc.get("/api/orders/42").andExpect {
            status { isOk() }
        }
    }
}
```

This is not the only possible setup.
It is a useful first shape because:

- API access is explicit
- actuator exposure is small
- denied access is tested
- operational and business paths are not mixed casually

Actuator access itself is often best covered with one small integration or
smoke test because those endpoints are framework-provided rather than normal
application controllers.

## Best Approach or Strong Default

Strong defaults:

- define route policy explicitly in one security config
- expose only `health` first, then add `info`, `prometheus`, or others for a
  concrete reason
- keep actuator endpoints behind a narrower boundary than business APIs when
  they expose operational details
- use slice tests for controller and security behavior
- use a smaller number of full integration tests for real wiring, persistence,
  and migrations
- test failure paths that matter: anonymous access, wrong role, invalid input,
  and readiness under missing dependencies

A good practical testing split is:

- unit tests for business rules
- `@WebMvcTest` for HTTP contract, validation, and security behavior
- `@DataJpaTest` or repository-focused tests for persistence behavior
- a few `@SpringBootTest` flows for real wiring and startup confidence

The main point is not annotation coverage.
It is proving the service boundary from more than one angle.

## Actuator Posture

Spring Boot Actuator provides built-in endpoints such as `health`, `metrics`,
`mappings`, and `prometheus`.

The useful baseline is not "turn actuator on."
It is:

- decide which endpoints are operationally necessary
- expose the minimum set
- secure what is sensitive
- know which health endpoint the platform should call

For many services, that means:

- `/actuator/health` for basic status
- `/actuator/health/liveness` and `/actuator/health/readiness` when the
  platform needs separate probe semantics
- metrics only when something is actually scraping or using them

## Security Testing Posture

A common mistake is testing only the happy business path with an authenticated
mock user.

Stronger defaults:

- test unauthenticated access
- test wrong-role access
- test validation failures
- test error shape, not only status code
- test one or two actuator access rules explicitly

If authorization is important, the denied test is often more valuable than the
happy-path test.

## Main Tradeoff or Failure Mode

The tradeoff is speed versus clarity.

It is faster to:

- leave security mostly implicit
- expose extra actuator endpoints "just in case"
- put all verification into a few broad integration tests

That usually creates slower debugging later.

The common failure is having:

- security config no one can explain
- actuator exposure no one has reviewed
- tests that prove business logic but not service posture

## Practical Rule

Before calling a Spring Boot service production-ready, be able to answer:

- which routes are public
- which routes require auth
- which actuator endpoints are exposed and why
- how readiness differs from basic health
- which tests prove denied access and error behavior

If those answers are vague, the service baseline is still weak.

## Reusable Takeaway

> A trustworthy Spring Boot baseline is not only "the API works." It is "the
> route policy is explicit, the actuator surface is deliberate, and the tests
> prove both allowed and denied behavior."

## Further Reading

- Spring Boot Actuator endpoints: https://docs.spring.io/spring-boot/reference/actuator/endpoints.html
- Spring Boot testing reference: https://docs.spring.io/spring-boot/reference/testing/index.html
- Spring Security testing reference: https://docs.spring.io/spring-security/reference/servlet/test/index.html
