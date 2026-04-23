# Advanced Web Annotations

> Primary fit: `Platform / Growth lane`


Quick REST refresher: `@GetMapping` reads, `@PostMapping` creates, `@PutMapping` replaces, `@PatchMapping` partially updates, and `@DeleteMapping` removes.

Beyond those common REST mappings, here are the higher-value annotations that matter in real Spring APIs.

### 📍 1. Exception Handling: `@RestControllerAdvice`
Don't use `try-catch` in controllers. Use a global handler.

```kotlin
class UserNotFoundException(message: String) : RuntimeException(message)

data class ErrorDto(val message: String)

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFound(ex: UserNotFoundException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(404).body(ErrorDto(ex.message ?: "User not found"))
    }
}

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): UserDto {
        return userService.findById(id)
            ?: throw UserNotFoundException("User $id not found")
    }
}
```

The controller does **not** call `GlobalExceptionHandler` directly. It just throws the exception. Spring sees the exception, finds the matching `@ExceptionHandler`, and builds the HTTP response for you.

<details>
<summary>Java version</summary>

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorDTO(ex.getMessage()));
    }
}
```

</details>

### 📍 2. Validation: `@Validated` & `@Valid`
Use `@Valid` on method arguments and `@NotBlank`, `@Size`, `@Email` in your DTOs.
Use `@Validated` at the class level to validate `@PathVariable` or `@RequestParam`.

```kotlin
data class CreateUserRequest(
    @field:NotBlank(message = "name is required")
    val name: String,

    @field:Email(message = "email must be valid")
    val email: String,

    @field:Size(min = 8, message = "password must have at least 8 characters")
    val password: String
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

Use `@Valid` for validating a request body object. Use `@Validated` when you also want Bean Validation on simple parameters such as `@PathVariable` or `@RequestParam`.

### 📍 3. Selective Fields: `@JsonView`
Avoid creating multiple DTOs for the same entity (e.g., `UserSummaryDTO`, `UserFullDTO`).
Use `@JsonView` to filter which fields are serialized depending on the controller method.

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
    val email: String
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

The same DTO is returned in both endpoints, but the summary endpoint serializes only `id` and `name`, while the details endpoint also includes `email`.

### 📍 4. Response Customization: `@ResponseStatus`
Instead of manually returning `ResponseEntity`, you can annotate your custom exceptions or methods directly.

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

Use this when the status is fixed and simple. If you need dynamic headers, different statuses, or more control, use `ResponseEntity`.

### 📍 5. Async Support: `@Async`
For non-blocking tasks (like sending emails) without using a manual thread pool.
Requires `@EnableAsync` in your configuration.

```kotlin
@Configuration
@EnableAsync
class AsyncConfig

@Service
class NotificationService {
    @Async
    fun sendWelcomeEmail(email: String) {
        println("Sending email to $email")
    }
}

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val notificationService: NotificationService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun createUser(@Valid @RequestBody request: CreateUserRequest) {
        userService.create(request)
        notificationService.sendWelcomeEmail(request.email)
    }
}
```

This does **not** make the whole controller reactive. It just offloads a specific method to an async executor, which is useful for fire-and-forget work such as emails, audit logs, or background notifications.
