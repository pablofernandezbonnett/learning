# Advanced Web Annotations

> Primary fit: `Platform / Growth lane`

Quick REST refresher: `@GetMapping` reads, `@PostMapping` creates,
`@PutMapping` replaces, `@PatchMapping` partially updates, and
`@DeleteMapping` removes.

This note stays focused on higher-value Spring MVC annotations that shape the
HTTP boundary directly.

Use the dedicated notes for adjacent concerns:

- [02-exception-handling.md](./02-exception-handling.md): central error mapping with `@RestControllerAdvice` and `ProblemDetail`
- [24-spring-security-actuator-and-testing-baseline.md](./24-spring-security-actuator-and-testing-baseline.md): route policy, actuator exposure, and service-boundary verification

This note uses Java and Kotlin side by side only where controller, validation,
and response-shape wiring are genuinely easier to compare directly.

## Controller Boundary Rule

Good controller responsibilities:

- parse HTTP input
- validate request shape
- call the service or use-case boundary
- return a stable response shape and status

Bad controller responsibilities:

- business rules
- transaction orchestration
- direct entity exposure
- persistence decisions
- ad hoc retry or cache logic

Short rule:

> a Spring controller is an HTTP boundary, not the place where business or
> persistence policy should accumulate

## 1. Validation: `@Validated` and `@Valid`

Use `@Valid` on request body DTOs.
Use `@Validated` on the controller when you also want Bean Validation on
`@PathVariable` or `@RequestParam`.

```kotlin
data class CreateUserRequest(
    @field:NotBlank(message = "name is required")
    val name: String,

    @field:Email(message = "email must be valid")
    val email: String,

    @field:Size(min = 8, message = "password must have at least 8 characters")
    val password: String,
)

@RestController
@Validated
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@Valid @RequestBody request: CreateUserRequest): UserDto {
        return userService.create(request)
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable @Min(1) id: Long): UserDto {
        return userService.getById(id)
    }
}
```

<details>
<summary>Java version</summary>

```java
public record CreateUserRequest(
    @NotBlank(message = "name is required")
    String name,

    @Email(message = "email must be valid")
    String email,

    @Size(min = 8, message = "password must have at least 8 characters")
    String password
) {
}

@RestController
@Validated
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable @Min(1) Long id) {
        return userService.getById(id);
    }
}
```

</details>

Strong default:

- validate request DTOs explicitly
- validate simple route and query parameters when they carry real constraints
- keep validation failures part of the API contract, not only an internal concern

## 2. Selective Serialization: `@JsonView`

`@JsonView` can help when one DTO needs a small and a detailed response shape
without multiplying types too early.

```kotlin
object Views {
    interface Summary
    interface Details : Summary
}

data class UserDto(
    @field:JsonView(Views.Summary::class)
    val id: Long,

    @field:JsonView(Views.Summary::class)
    val name: String,

    @field:JsonView(Views.Details::class)
    val email: String,
)

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @JsonView(Views.Summary::class)
    @GetMapping("/{id}/summary")
    fun getSummary(@PathVariable id: Long): UserDto = userService.getById(id)

    @JsonView(Views.Details::class)
    @GetMapping("/{id}")
    fun getDetails(@PathVariable id: Long): UserDto = userService.getById(id)
}
```

<details>
<summary>Java version</summary>

```java
public final class Views {
    public interface Summary {
    }

    public interface Details extends Summary {
    }
}

public record UserDto(
    @JsonView(Views.Summary.class) Long id,
    @JsonView(Views.Summary.class) String name,
    @JsonView(Views.Details.class) String email
) {
}

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @JsonView(Views.Summary.class)
    @GetMapping("/{id}/summary")
    public UserDto getSummary(@PathVariable Long id) {
        return userService.getById(id);
    }

    @JsonView(Views.Details.class)
    @GetMapping("/{id}")
    public UserDto getDetails(@PathVariable Long id) {
        return userService.getById(id);
    }
}
```

</details>

Use this with restraint.
If the read shapes keep diverging, separate DTOs are often clearer.

## 3. Fixed Status Mapping: `@ResponseStatus`

Use `@ResponseStatus` when the status is simple and stable.
If you need dynamic headers or dynamic status selection, use `ResponseEntity`
instead.

```kotlin
@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFoundException(message: String) : RuntimeException(message)

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@Valid @RequestBody request: CreateUserRequest): UserDto {
        return userService.create(request)
    }
}
```

<details>
<summary>Java version</summary>

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }
}
```

</details>

## 4. Boundary Reminder About `@Async`

`@Async` is useful, but it is not mainly a web-annotation topic.
Treat it as a service-execution concern that a controller may trigger, not as
part of the core HTTP boundary model.

Strong default:

- keep controllers synchronous in structure unless the real workflow needs more
- use `@Async` for bounded fire-and-forget work only when the delivery
  consequences are acceptable
- do not confuse `@Async` with reactive end-to-end request handling

## Reusable Takeaway

> The useful Spring MVC annotations are the ones that keep the HTTP boundary
> explicit: validate input clearly, control the response shape deliberately, and
> keep business and persistence policy outside the controller.
