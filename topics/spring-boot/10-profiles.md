# Profiles and External Configuration

A profile selects configuration for a runtime context. It can activate
profile-specific files and beans marked with `@Profile`. Use profiles for
stable environment differences, such as a local adapter or test settings.
Keep deploy-time values and secrets external to the application package.

## Activate and Inspect a Profile

```bash
SPRING_PROFILES_ACTIVE=prod java -jar app.jar
```

When `prod` is active, Spring Boot considers both `application.yml` and
`application-prod.yml`; the profile-specific file can override the base
file. External configuration sources, such as environment variables and
command-line arguments, also participate in property precedence. If a value
is surprising, inspect its source rather than assuming a profile file won.

A small profile-specific file might contain a non-secret behavior choice:

```yaml
# application-local.yml
notifications:
  enabled: false
```

Supply production credentials from an external secret source. A profile name
does not make values inside `application-prod.yml` secret or safe to commit.

## Use `@Profile` for a Real Bean Difference

```java
@Configuration(proxyBeanMethods = false)
@Profile("local")
class LocalStorageConfig {
    @Bean
    StorageClient storageClient() {
        return new LocalStorageClient();
    }
}
```

This bean exists only with the `local` profile. Make sure another
configuration supplies the required client in other environments. Avoid a
profile that silently removes authentication or authorization; security
policy needs deliberate testing in every environment.

Profile groups can activate several related profiles under one name. Define
the group in a non-profile-specific document. For tests, `@ActiveProfiles`
selects the profile used by the test context; override individual properties
when that is simpler than creating another profile.

For bean prerequisites independent of environment names, use
[conditional beans](./09-conditional-beans.md). For centrally managed
configuration across services, see
[service integration](./20-spring-cloud-and-service-integration.md).

## Further Reading

- [Spring Boot profiles](https://docs.spring.io/spring-boot/reference/features/profiles.html)
- [Externalized configuration and precedence](https://docs.spring.io/spring-boot/reference/features/external-config.html)
