# 5. Conditional Beans — @ConditionalOnProperty, @ConditionalOnMissingBean

> Primary fit: `Platform / Growth lane`


## Why Conditional Beans?

In a real application you need different behavior depending on:
- The environment (dev vs prod)
- Feature flags (A/B testing, gradual rollouts)
- Whether a dependency exists in the classpath
- Whether another bean has already been defined

Spring Boot's `@Conditional*` annotations solve this at the container level — no `if` statements in your business code.

Smallest mental model:

- Spring checks the condition at startup
- if it matches, the bean is registered
- if it does not match, that bean does not exist in the container at all

So conditional beans are about startup wiring, not per-request branching.

## @ConditionalOnProperty

The most common: activate a bean only if a property has a specific value.

```yaml
# application.yml
feature:
  payment-gateway: stripe    # "stripe" | "paypal" | "mock"
  cache:
    enabled: true
```

```kotlin
// Stripe is active when feature.payment-gateway=stripe
@Service
@ConditionalOnProperty(name = ["feature.payment-gateway"], havingValue = "stripe")
class StripePaymentService : PaymentService {

    override fun charge(request: PaymentRequest): PaymentResult {
        log.info("[StripePaymentService] Charging ¥{} via Stripe", request.amountYen)
        // real Stripe API call
        return PaymentResult.success("stripe-txn-${UUID.randomUUID()}")
    }
}

// Mock is active during tests (when property is missing — matchIfMissing=true means "use this as default")
@Service
@ConditionalOnProperty(
    name = ["feature.payment-gateway"],
    havingValue = "mock",
    matchIfMissing = true,   // use mock if property is not set at all
)
class MockPaymentService : PaymentService {

    override fun charge(request: PaymentRequest): PaymentResult {
        log.warn("[MockPaymentService] Simulating payment — NOT a real charge")
        return PaymentResult.success("mock-txn-${UUID.randomUUID()}")
    }
}
```

```kotlin
// Conditionally enable cache
@Configuration
@ConditionalOnProperty(name = ["feature.cache.enabled"], havingValue = "true")
class CacheConfiguration {

    @Bean
    fun cacheManager(factory: RedisConnectionFactory): CacheManager {
        log.info("[CacheConfiguration] Redis cache enabled")
        return RedisCacheManager.builder(factory).build()
    }
}
```

## @ConditionalOnMissingBean

Register a bean only if no other bean of that type has been registered. This is how Spring Boot's autoconfiguration works — it provides sensible defaults that you can override.

Smallest practical meaning:

- framework or library code provides the default
- your application can replace it by defining its own bean
- Spring keeps one clear bean choice instead of activating both

```kotlin
// Default notification service (sends to Slack)
@Service
@ConditionalOnMissingBean(NotificationService::class)
class SlackNotificationService : NotificationService {

    override fun send(message: String) {
        log.info("[SlackNotificationService] Sending to Slack: {}", message)
    }
}
```

```kotlin
// In your application, you define a custom one — the Slack default is NOT created
@Service
class EmailNotificationService : NotificationService {

    override fun send(message: String) {
        log.info("[EmailNotificationService] Sending email: {}", message)
    }
}
```

This is the exact pattern Spring Boot uses for `DataSource`, `ObjectMapper`, `RestTemplate`, etc.

## @ConditionalOnClass / @ConditionalOnMissingClass

Activate a bean only if a class exists in the classpath (or doesn't exist).
Here, "classpath" just means: is that dependency JAR actually present in the application?

```kotlin
@Configuration
@ConditionalOnClass(name = ["com.stripe.Stripe"])   // only if Stripe SDK is on classpath
class StripeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun stripeClient(@Value("\${stripe.api-key}") apiKey: String): StripeClient =
        StripeClient(apiKey)
}
```

## @ConditionalOnBean

Activate a bean only if another specific bean is present.

```kotlin
@Service
@ConditionalOnBean(RedisConnectionFactory::class)   // only if Redis is configured
class RedisHealthCheckService(
    private val factory: RedisConnectionFactory,
) : HealthIndicator {

    override fun health(): Health = try {
        factory.connection.ping()
        Health.up().withDetail("redis", "connected").build()
    } catch (e: Exception) {
        log.error("[RedisHealthCheck] Redis is down: {}", e.message)
        Health.down(e).build()
    }
}
```

## Custom @Conditional (Advanced)

When the built-in annotations aren't enough, implement `Condition` directly.

```kotlin
// Custom condition: activate in specific time window (e.g., Black Friday mode)
class BlackFridayCondition : Condition {

    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val today = LocalDate.now()
        return today.monthValue == 11 && today.dayOfMonth == 28
    }
}

@Service
@Conditional(BlackFridayCondition::class)
class BlackFridayPricingService : PricingService { /* ... */ }
```

## Autoconfiguration: The Full Pattern

This is how Spring Boot starters (reusable libraries that auto-register useful beans when conditions match) are built:

```kotlin
@AutoConfiguration
@ConditionalOnClass(RedisOperations::class)
@EnableConfigurationProperties(RedisProperties::class)
@Import(LettuceConnectionConfiguration::class, JedisConnectionConfiguration::class)
class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = ["redisTemplate"])
    fun redisTemplate(factory: RedisConnectionFactory): RedisTemplate<Any, Any> =
        RedisTemplate<Any, Any>().apply { connectionFactory = factory }

    @Bean
    @ConditionalOnMissingBean
    fun stringRedisTemplate(factory: RedisConnectionFactory): StringRedisTemplate =
        StringRedisTemplate(factory)
}
```

The starters declare this class in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

## Interview Summary

| Annotation                    | Activates when                                  |
|-------------------------------|-------------------------------------------------|
| `@ConditionalOnProperty`      | Property has specified value (or is missing)    |
| `@ConditionalOnMissingBean`   | No bean of this type registered yet             |
| `@ConditionalOnBean`          | A specific bean IS registered                   |
| `@ConditionalOnClass`         | Class exists in classpath                       |
| `@ConditionalOnMissingClass`  | Class does NOT exist in classpath               |
| `@ConditionalOnExpression`    | Spring Expression Language (SpEL) evaluates to true |
| `@Conditional(MyCondition)`   | Custom `Condition.matches()` returns true       |

See `src/main/kotlin/com/learning/mastery/config/ConditionalConfig.kt` for examples.
