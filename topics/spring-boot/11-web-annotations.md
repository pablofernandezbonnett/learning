# Spring MVC HTTP Boundaries

A controller translates between HTTP and an application operation. It reads
route and body input, validates request shape, calls the service, and returns
a stable response. Keep business rules, persistence entities, and retry
policy outside that boundary.

## Validate Input at the Edge

```java
record CreateOrderRequest(
    @NotNull Long productId,
    @Min(1) int quantity
) {}

@RestController
@RequestMapping("/orders")
class OrderController {
    private final OrderService orders;

    OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orders.create(request.productId(), request.quantity());
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable @Min(1) long id) {
        return orders.get(id);
    }
}
```

`@RequestBody` reads the JSON body; with Bean Validation configured,
`@Valid` checks constraints on its DTO. A direct constraint such as `@Min`
on a route parameter invokes method validation. In Spring Framework 6.1 and
later, MVC supports that directly.
Adding class-level `@Validated` instead routes method validation through an
AOP proxy, so use it only when that is the intended setup.

Request validation catches malformed or out-of-range input. The service
still owns business invariants such as stock availability or an allowed state
transition. Depending on the method signature, Spring MVC can raise
`MethodArgumentNotValidException` or `HandlerMethodValidationException`;
map those consistently in [error responses](./02-exception-handling.md).

## Choose the Response Contract

`@ResponseStatus(HttpStatus.CREATED)` sets a fixed success status. Return a
`ResponseEntity` when the response needs headers or a status chosen from the
result, for example a `Location` header for a newly created resource.
Keep API response DTOs separate from persistence entities so internal mapping
changes do not silently change the JSON contract.

`@JsonView` can select a small serialization variant of one response DTO.
It does not change what the database fetched. If two endpoints need materially
different fields or queries, separate response shapes and choose each read
explicitly; see [JPA and Hibernate](./04-jpa-hibernate-performance-traps.md).

## Check One Endpoint

Verify the accepted input, validation failure, success status, response
fields, and any headers clients rely on. Include the case where the request
is valid but the business operation fails; its status and body belong to the
[error contract](./02-exception-handling.md). For route security and service
tests, use the [Spring baseline](./24-spring-security-actuator-and-testing-baseline.md).

## Further Reading

- [Spring MVC validation](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html)
- [Spring MVC response entities](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responseentity.html)
