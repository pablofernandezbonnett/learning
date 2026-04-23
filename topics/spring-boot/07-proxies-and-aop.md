# 3. Proxies & Aspect-Oriented Programming (AOP) — Why @Transactional Fails on Internal Calls

> Primary fit: `Platform / Growth lane`


## The Problem (Senior Interview Trap)

```kotlin
@Service
class OrderService {

    @Transactional
    fun placeOrder(order: Order) {
        saveOrder(order)
        reserveStock(order)  // ← calls the method below via 'this'
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun reserveStock(order: Order) {
        // THIS TRANSACTION NEVER STARTS when called from placeOrder()
        // WHY? Because the call goes through 'this', not the proxy.
        stockRepository.reserve(order.items)
    }
}
```

The `REQUIRES_NEW` transaction on `reserveStock` is silently ignored. No error, no warning. This is one of the most common Spring bugs in production.

## Why This Happens: The Proxy Model

When Spring detects `@Transactional`, it doesn't add transaction logic into your class. Instead, it creates a **CGLIB proxy** (a code-generated subclass that wraps your class). The proxy intercepts method calls and adds behavior (start/commit/rollback a transaction) around the actual method call.

```
External caller → Proxy (starts TX) → Your class.placeOrder() → Your class.reserveStock()
                                                                  ↑
                                                         Direct 'this' call — bypasses proxy
```

When `placeOrder()` calls `this.reserveStock()`, it's calling the real object directly — the proxy is completely bypassed. Spring AOP only intercepts calls that go **through the proxy**.

## How AOP Actually Works

Spring AOP (Aspect-Oriented Programming) is **proxy-based**.
It exists to apply cross-cutting behavior such as transactions, logging, retry, or security around business methods without mixing that code into every method body.

There are two proxy strategies:

| Strategy            | When used                          | Limitation                            |
|---------------------|------------------------------------|---------------------------------------|
| JDK Dynamic Proxy (interface-based proxy) | Bean implements an interface | Can only proxy interface methods |
| CGLIB Proxy (subclass-based proxy) | Bean is a class (no interface) | Creates a subclass — final classes can't be proxied |

```
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)  // forces CGLIB even for interface-based beans
```

## Writing Custom Aspects

AOP lets you inject cross-cutting concerns (logging, security, metrics, retry) without polluting business code.

```kotlin
@Aspect
@Component
class AuditLogAspect {

    private val log = LoggerFactory.getLogger(javaClass)

    // Pointcut: matches any method in any class annotated with @RestController
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    fun auditLog(joinPoint: ProceedingJoinPoint): Any? {
        val method = joinPoint.signature.toShortString()
        val start = System.currentTimeMillis()
        log.info("[AUDIT] START {}", method)

        return try {
            val result = joinPoint.proceed()   // call the actual method
            log.info("[AUDIT] SUCCESS {} ({}ms)", method, System.currentTimeMillis() - start)
            result
        } catch (e: Exception) {
            log.error("[AUDIT] ERROR {} ({}ms): {}", method, System.currentTimeMillis() - start, e.message)
            throw e
        }
    }
}
```

### Pointcut Expression Syntax

```
execution(* com.learning.mastery.service.*.*(..))   // any method in service package
within(@Service *)                                    // any method in a @Service class
@annotation(Transactional)                           // methods annotated with @Transactional
bean(orderService)                                   // specific bean by name
args(java.lang.String, ..)                           // first arg is String
```

## Solutions for the Self-Call Problem

### Option A: Refactor — Extract to a separate bean (Recommended)

The clean solution. Separating responsibilities is good design anyway.

```kotlin
@Service
class OrderService(private val stockService: StockReservationService) {

    fun placeOrder(order: Order) {
        saveOrder(order)
        stockService.reserveStock(order)   // goes through StockReservationService proxy ✓
    }
}

@Service
class StockReservationService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun reserveStock(order: Order) {
        // This transaction NOW works correctly
    }
}
```

### Option B: Self-injection (Use sparingly)

Inject the proxy of the current bean into itself.

```kotlin
@Service
class OrderService {

    @Autowired
    private lateinit var self: OrderService   // Spring injects the proxy, not 'this'

    @Transactional
    fun placeOrder(order: Order) {
        saveOrder(order)
        self.reserveStock(order)   // goes through proxy — REQUIRES_NEW works ✓
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun reserveStock(order: Order) { /* ... */ }
}
```

**Caveat**: Circular dependency. Requires `@Lazy` on one end or constructor injection workarounds. Use Option A instead.

### Option C: ApplicationContext.getBean() (Don't use in production)

```kotlin
@Service
class OrderService(private val ctx: ApplicationContext) {

    fun placeOrder(order: Order) {
        val proxy = ctx.getBean(OrderService::class.java)
        proxy.reserveStock(order)   // works, but smells bad
    }
}
```

## Key Interview Points

1. **Spring AOP = proxy-based**. AspectJ (the alternative) is bytecode-weaving at compile time — no proxy, self-calls work. But Spring Boot uses Spring AOP by default.

2. **@Transactional on private methods** doesn't work — proxies can't override private methods (CGLIB creates a subclass). No error is thrown; it's silently ignored.

3. **@Async has the same problem** — self-calls bypass the executor proxy.

4. **The fix**: always apply `@Transactional`/`@Async` to public methods called from other beans.

5. **ProxyFactoryBean vs @EnableAspectJAutoProxy**: The old XML way vs the modern annotation way. Both produce the same proxy mechanism.
