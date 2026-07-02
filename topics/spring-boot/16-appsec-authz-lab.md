# 13. AppSec Lab — Authentication and Authorization in a Spring API

This lab is about the most important AppSec (application security) skill for backend engineers:

> enforcing authorization correctly in business code, not only at the route level.

The scenario is intentionally simple:

- users can read their own orders
- support agents can read any order
- normal users must not read someone else's order

This lab is not about building a full auth system.
It is about enforcing the right access rule in the backend.

This lab uses Java and Kotlin side by side in the main authorization examples
because service-layer authorization rules are a common comparison point in
mixed Spring teams.

---

## Goal

Build a Spring API endpoint:

`GET /api/orders/{orderId}`

Security rules:

- the order owner can read it
- a support/admin role can read it
- all other authenticated users get `403`

This is a classic broken object level authorization (BOLA) problem if implemented badly.

---

## Step 1. Model the Principal Clearly

Do not spread raw Spring Security details everywhere.

Create an application-level principal:

```kotlin
data class AppPrincipal(
    val userId: Long,
    val email: String,
    val roles: Set<String>,
)
```

<details>
<summary>Java version</summary>

```java
public record AppPrincipal(
    long userId,
    String email,
    Set<String> roles
) {
}
```

</details>

In a real app, this could be mapped from:

- a session-backed user
- a JWT (JSON Web Token)
- an OAuth2 login

The important part is that business code can answer:

- who is the user
- what roles do they have

---

## Step 2. Avoid UI-Only Security

This is not enough:

```kotlin
@GetMapping("/api/orders/{orderId}")
fun getOrder(@PathVariable orderId: Long): OrderDto =
    orderService.getOrder(orderId)
```

<details>
<summary>Java version</summary>

```java
@GetMapping("/api/orders/{orderId}")
public OrderDto getOrder(@PathVariable Long orderId) {
    return orderService.getOrder(orderId);
}
```

</details>

Even if the frontend hides the button, a caller can still invoke the endpoint.

Server-side authorization must validate the object and the actor together.

---

## Step 3. Enforce Object-Level Authorization

One clean option is a service-layer guard:

```kotlin
@Service
class OrderService(
    private val orderRepository: OrderRepository,
) {
    fun getOrder(orderId: Long, principal: AppPrincipal): OrderDto {
        val order = orderRepository.findById(orderId)
            ?: throw OrderNotFoundException(orderId)

        val isOwner = order.userId == principal.userId
        val isSupport = "SUPPORT" in principal.roles || "ADMIN" in principal.roles

        if (!isOwner && !isSupport) {
            throw AccessDeniedException("Not allowed to read this order")
        }

        return order.toDto()
    }
}
```

<details>
<summary>Java version</summary>

```java
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderDto getOrder(long orderId, AppPrincipal principal) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }

        boolean isOwner = order.getUserId() == principal.userId();
        boolean isSupport =
            principal.roles().contains("SUPPORT") ||
            principal.roles().contains("ADMIN");

        if (!isOwner && !isSupport) {
            throw new AccessDeniedException("Not allowed to read this order");
        }

        return order.toDto();
    }
}
```

</details>

This is explicit and easy to reason about.

---

## Step 4. If You Use Method Security, Keep It Honest

Method security can help, but do not use it as magic decoration.

```kotlin
@PreAuthorize("@orderAuthorization.canRead(#orderId, authentication)")
fun getOrder(orderId: Long): OrderDto { ... }
```

<details>
<summary>Java version</summary>

```java
@PreAuthorize("@orderAuthorization.canRead(#orderId, authentication)")
public OrderDto getOrder(Long orderId) { ... }
```

</details>

That is fine if the referenced logic is real and testable.

Do not hide fragile business rules inside unreadable SpEL.

---

## Step 5. Test the Real Security Cases

Your minimum cases should be:

- owner reads own order -> `200`
- different user reads order -> `403`
- support reads any order -> `200`
- unknown order -> `404`

Example direction with MockMvc:

```kotlin
@Test
fun `normal user cannot read another user's order`() {
    mockMvc.get("/api/orders/42") {
        header("X-Demo-User-Id", "100")
        header("X-Demo-Roles", "USER")
    }.andExpect {
        status { isForbidden() }
    }
}
```

<details>
<summary>Java version</summary>

```java
@Test
void normalUserCannotReadAnotherUsersOrder() throws Exception {
    mockMvc.perform(get("/api/orders/42")
            .header("X-Demo-User-Id", "100")
            .header("X-Demo-Roles", "USER"))
        .andExpect(status().isForbidden());
}
```

</details>

The exact auth setup does not matter for the lab.
The authorization outcome does.

---

## Step 6. Stretch the Lab

Add one mutating action:

`POST /api/orders/{orderId}/refund`

Rules:

- only support/admin can trigger it
- refund can happen only if order state allows it

This teaches the next AppSec lesson:

> authorization and workflow validation are separate controls.

---

## Common Mistakes

- checking only that the user is authenticated
- trusting the frontend to filter visible objects
- fetching by order ID without checking owner
- mixing up `404` and `403` inconsistently
- putting the entire rule in SpEL and never testing it

---

## Completion Checklist

- Can a normal user fetch only their own order?
- Can support read any order?
- Is the object-level rule enforced server-side?
- Are the tests written for the failure path, not only the success path?
- Could you explain why this is BOLA prevention in one sentence?

---

## What You Should Be Able to Say After This Lab

> In API security, route protection is not enough. The important part is
> object-level authorization: making sure the authenticated user is actually
> allowed to access that specific resource or action.

---

## Related Reading

- [../../topics/security/02-web-and-api-security.md](../../topics/security/02-web-and-api-security.md)
- [../../topics/security/03-spring-and-jvm-appsec.md](../../topics/security/03-spring-and-jvm-appsec.md)
