# Bean Lifecycle: `@PostConstruct`, `BeanPostProcessor`, `@PreDestroy`

> Primary fit: `Platform / Growth lane`


## The Full Lifecycle (expanded from doc 01)

Spring manages beans through a precise lifecycle. Understanding each phase is useful when debugging startup behavior, proxies, and shutdown issues.

Smallest mental model:

- Spring creates the object
- injects dependencies
- runs initialization hooks
- may wrap the bean with proxy behavior
- destroys it on shutdown

```
Class scanning / @Bean factory method
         ↓
1. Instantiation (constructor)
         ↓
2. Populate Properties (dependency injection)
         ↓
3. Aware interfaces (BeanNameAware, ApplicationContextAware, etc.)
         ↓
4. BeanPostProcessor.postProcessBeforeInitialization()
         ↓
5. @PostConstruct / InitializingBean.afterPropertiesSet()
         ↓
6. BeanPostProcessor.postProcessAfterInitialization()   ← proxies created HERE
         ↓
   [Bean is ready — in use]
         ↓
7. @PreDestroy / DisposableBean.destroy()
```

## @PostConstruct — Initialization Hook

Run code once all dependencies have been injected and bean initialization is finishing.
Typical use cases: warm up caches, validate config, or start resources that should begin only after bean wiring is complete.

```kotlin
@Service
class ProductCacheService(private val repository: ProductRepository) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val cache = mutableMapOf<String, Product>()

    // Constructor runs first — repository is already injected here
    // (Kotlin constructor injection is always safe, unlike Java field injection)

    @PostConstruct
    fun init() {
        // At this point, ALL dependencies are injected
        log.info("[ProductCacheService] Warming up product cache...")
        repository.findAll().forEach { cache[it.id] = it }
        log.info("[ProductCacheService] Cache warm-up complete. {} products loaded.", cache.size)
    }
}
```

**Why not do this work in the constructor?**
With constructor injection, dependencies are already available in the constructor.
So the real issue is not always "the dependency is null".

The real issue is lifecycle timing:

- the bean is not fully initialized yet
- `@PostConstruct` has not run
- post-processors and proxy wrappers may not have finished

Practical rule:

- constructor: cheap setup and fail-fast validation
- `@PostConstruct`: startup work that touches other beans, external resources, or warm-up logic

## @PreDestroy — Cleanup Hook

Run code when the application is shutting down (graceful shutdown). Close connections, flush buffers, release resources.

```kotlin
@Service
class WebSocketSessionManager {

    private val log = LoggerFactory.getLogger(javaClass)
    private val sessions: MutableSet<WebSocketSession> = ConcurrentHashMap.newKeySet()

    fun register(session: WebSocketSession) { sessions.add(session) }
    fun unregister(session: WebSocketSession) { sessions.remove(session) }

    @PreDestroy
    fun shutdown() {
        log.info("[WebSocketSessionManager] Closing {} active sessions...", sessions.size)
        sessions.forEach { session ->
            try {
                session.close(CloseStatus.SERVICE_RESTARTED)
            } catch (e: IOException) {
                log.warn("Failed to close session {}", session.id, e)
            }
        }
        log.info("[WebSocketSessionManager] All sessions closed.")
    }
}
```

## BeanPostProcessor — The Extension Point

`BeanPostProcessor` is a container hook that can inspect or wrap every bean before and after initialization.
This is how Spring implements proxies for `@Transactional`, `@Async`, `@Cacheable`, etc.

```kotlin
@Component
class TimingBeanPostProcessor : BeanPostProcessor {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        // Called before @PostConstruct
        if (beanName.endsWith("Service")) {
            log.debug("[BPP] Before init: {}", beanName)
        }
        return bean  // IMPORTANT: always return the bean (or a wrapper)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        // Called after @PostConstruct — this is where Spring creates CGLIB proxies
        // for @Transactional, @Async, @Cacheable, @Retryable, etc.
        if (beanName.endsWith("Service")) {
            log.debug("[BPP] After init: {} (class: {})", beanName, bean.javaClass.simpleName)
            // If @Transactional, bean.javaClass.simpleName shows "ProductService$$SpringCGLIB$$0"
        }
        return bean
    }
}
```

## Key Interview Questions

**Q: Why does @Transactional not work when calling a method from within the same class?**

Because `@Transactional` works via a CGLIB proxy. When you call `this.someMethod()`, you bypass the proxy — the transaction interceptor never runs. See doc 03 for the full explanation.

**Q: What happens if @PostConstruct throws?**

The ApplicationContext fails to start. The exception propagates up and Spring aborts startup.

**Q: What's the difference between @PostConstruct and InitializingBean.afterPropertiesSet()?**

Functionally identical. `@PostConstruct` is preferred because it's a standard Java annotation, not a Spring interface, so your code is less coupled to the framework.

**Q: Are @PreDestroy hooks guaranteed to run?**

Only on graceful shutdown (`context.close()` or SIGTERM handled by Spring Boot). A `kill -9` or JVM crash skips them.

## Scope Interaction with Lifecycle

| Scope        | @PostConstruct | @PreDestroy         |
|--------------|----------------|---------------------|
| Singleton    | Once on startup | Once on shutdown    |
| Prototype    | Once per new instance | NOT called (Spring doesn't track prototypes after giving them out) |
| Request      | Once per HTTP request | End of request |

See `src/main/kotlin/com/learning/mastery/ioc/BeanLifecycleDemo.kt` for runnable code.
