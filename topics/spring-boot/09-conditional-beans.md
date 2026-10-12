# Conditional Beans

A condition decides at startup whether a configuration class or bean is
registered. Use it for application wiring that genuinely depends on a
property, a class, or another bean. It does not switch behavior per request:
changing a value after startup does not replace an already-created bean.

## Choose the Condition

| Condition | Registers when |
|---|---|
| `@ConditionalOnProperty` | A property matches the requested value |
| `@ConditionalOnClass` | A dependency class is on the classpath |
| `@ConditionalOnBean` | A matching bean has already been registered |
| `@ConditionalOnMissingBean` | No matching bean has been registered so far |

For a simple optional integration:

```java
@Configuration(proxyBeanMethods = false)
class NotificationConfig {
    @Bean
    @ConditionalOnProperty(
        prefix = "notifications",
        name = "enabled",
        havingValue = "true"
    )
    NotificationClient notificationClient() {
        return new NotificationClient();
    }
}
```

With `notifications.enabled=true`, the bean is registered. If the property
is absent, this condition does not match by default. Set `matchIfMissing`
only when absence is intentionally a safe default. Do not make a mock or
insecure implementation the silent fallback for a real integration.

## Understand Missing-Bean Defaults

`@ConditionalOnMissingBean` is useful when library auto-configuration offers
a default that an application may replace. It checks definitions processed so
far, so ordering matters; it belongs primarily in auto-configuration, which
runs after application bean definitions. In ordinary application code, prefer
explicit bean selection when the intended implementation is known.

Use `@ConditionalOnClass` to make library setup conditional on an optional
dependency. Use `@ConditionalOnBean` only when another registered bean is a
real prerequisite. If a bean is missing, inspect the condition report
described in [auto-configuration](./08-auto-configuration.md).

For environment-specific property files and `@Profile`, use
[profiles](./10-profiles.md). A profile selects an environment; a condition
expresses the prerequisite for one piece of wiring.

## Further Reading

- [Spring Boot condition annotations](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html)
- [`@ConditionalOnProperty` API](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/autoconfigure/condition/ConditionalOnProperty.html)
