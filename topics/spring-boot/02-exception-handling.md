# Spring MVC Error Responses

An error response is part of the API contract. A client needs a stable status
and machine-readable reason; operators need the internal cause in logs and
traces. Keep those two audiences separate.

For a Spring MVC API, use typed failures for expected business outcomes and
map them centrally with `@RestControllerAdvice`. `ProblemDetail` is Spring's
representation of the RFC 9457 problem format. Its `status` determines the
HTTP status; Spring can fill `instance` from the request path.

## Map One Expected Failure

```java
class OrderNotFoundException extends RuntimeException {}

@RestControllerAdvice
class ApiErrors {
    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail orderNotFound(OrderNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, "Order not found"
        );
        problem.setTitle("Order not found");
        problem.setProperty("code", "order_not_found");
        return problem;
    }
}
```

The service can throw `OrderNotFoundException`; controllers do not need the
same `try/catch` on every route. The response has a 404 status and a
`application/problem+json` body with standard fields such as `title`,
`status`, `detail`, and `instance`. The `code` extension gives clients a
stable value to branch on. Keep its name and meaning consistent across routes.

Do not put an exception message, SQL error, stack trace, or secret in
`detail`. Expected errors can explain what the caller may correct;
unexpected failures get a safe message while the server records the cause
with request or trace context.

## Decide Who Owns Each Error

| Failure | Starting response | Owner to inspect |
|---|---|---|
| Malformed or invalid request body | 400 by default | Spring MVC validation and message conversion |
| Missing resource | 404 | Application exception mapping |
| Competing state update | 409 if the conflict matters to the client | Application mapping |
| Unauthenticated or forbidden request | API-specific 401/403 behavior | Spring Security handlers |
| Unexpected failure | 500 with no internal detail | Central server-side diagnostics |

A valid request that violates a business rule may need a different status and
code from an invalid request body. Choose those meanings as part of the API
contract; do not map every business exception to 422.

`@Valid @RequestBody` commonly raises `MethodArgumentNotValidException`
and returns 400 by default. If clients need a custom field-error shape, add a
specific handler or customize `ResponseEntityExceptionHandler`; also account
for method-validation errors. If built-in MVC failures must use problem
details too, configure `spring.mvc.problemdetails.enabled=true` or use
`ResponseEntityExceptionHandler`. Review advice ordering when combining
handlers. A catch-all `@ExceptionHandler(Exception.class)` can accidentally
turn built-in MVC errors into 500s, so review its coverage before adding one.

Spring Security processes many authentication and authorization failures in
its filter chain. Configure its `AuthenticationEntryPoint` and
`AccessDeniedHandler` when their API response must match the same contract;
`@RestControllerAdvice` alone does not own every security response. See the
[Spring security baseline](./24-spring-security-actuator-and-testing-baseline.md)
and [Spring AppSec](../security/03-spring-and-jvm-appsec.md).

## Verify the Contract

For each representative failure, test the status, content type, stable code,
and client-safe detail. Include one validation error, one expected business
error, one denied request, and one unexpected failure. Check that server logs
or traces retain diagnostic context without copying it into the response.

For the endpoint's success shape and validation boundary, see
[web annotations](./11-web-annotations.md).

## Further Reading

- [Spring MVC error responses](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html)
- [Spring MVC request-body validation](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html)
- [Spring Security exception translation](https://docs.spring.io/spring-security/reference/servlet/architecture.html)
