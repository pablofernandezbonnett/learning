# Kotlin Basics Lab

Use these prompts after the [Kotlin topic](../../topics/kotlin/README.md). Write small programs yourself; compare each result with the guide after attempting it.

| Exercise | Task | Guide and checks |
| --- | --- | --- |
| Values and control flow | Map an order status to a user-facing message. | Use immutable values by default and an exhaustive `when` where possible. Check every status. |
| Data classes | Model an order summary and create a revised copy. | Put equality-bearing values in the primary constructor. Check `copy` and equality. |
| Null safety | Look up a customer that may be absent. | Represent absence in the type, handle it at the boundary, and avoid `!!`. Check missing and present customers. |
| Collections | Group paid orders by customer and total each group. | Filter → group → transform; keep side effects out of the chain. Check empty and repeated customers. |
| Coroutines | Run two independent remote reads under a request deadline. | Start children in one scope, await both within the deadline, and let failure cancel related work. Check timeout and cancellation. |
| Flow and channels | Stream status updates to a slow consumer. | Define who owns the scope, buffer size, and completion. Check backpressure and consumer failure. |
| Backend boundary | Wrap a blocking dependency inside a suspend-facing service. | Suspending a function does not make blocking I/O safe automatically. Choose the right dispatcher or client and keep the shared request budget. Check overload. |

One syntax reminder, where code is clearer than prose:

```kotlin
val label = status?.let { it.uppercase() } ?: "UNKNOWN"
```

For structured concurrency, dispatchers, and cancellation, use [coroutines for backend](../../topics/kotlin/03-kotlin-coroutines-for-backend.md).
