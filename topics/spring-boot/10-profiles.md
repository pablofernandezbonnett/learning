# 6. Profiles — @Profile, application-{env}.yml, Cloud-Native Config

> Primary fit: `Platform / Growth lane`


## What Are Profiles?

A profile is a named set of beans and configuration that is only activated when that profile is active. Spring Boot maps profiles to environments: `local`, `dev`, `staging`, `prod`.

Smallest mental model:

- one codebase
- different runtime behavior per environment
- profiles decide which config and which beans are active

```bash
# Activate a profile
java -jar app.jar --spring.profiles.active=prod

# Via environment variable (cloud-native deployment style)
SPRING_PROFILES_ACTIVE=prod java -jar app.jar

# In application.yml
spring:
  profiles:
    active: dev   # default for local development
```

## Configuration Files Per Profile

Spring Boot automatically loads `application-{profile}.yml` when a profile is active. Properties in the profile file **override** `application.yml`.

```
src/main/resources/
  application.yml              ← base config (always loaded)
  application-local.yml        ← overrides for local dev
  application-dev.yml          ← overrides for dev server
  application-staging.yml      ← overrides for staging
  application-prod.yml         ← overrides for production
```

### application.yml (base)
```yaml
spring:
  application:
    name: uniqlo-product-service

server:
  port: 8080

logging:
  level:
    root: INFO
    com.learning: INFO

feature:
  payment-gateway: mock
  cache:
    enabled: false
```

### application-prod.yml
```yaml
spring:
  datasource:
    url: ${DB_URL}               # always use env vars for secrets in prod
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    show-sql: false              # never log SQL in prod

logging:
  level:
    root: WARN
    com.learning: INFO           # keep app logs, suppress framework noise

feature:
  payment-gateway: stripe
  cache:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info    # restrict actuator in prod
```

### application-local.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb     # H2 in-memory DB for local
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true             # H2 web console at /h2-console
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    com.learning: DEBUG

feature:
  payment-gateway: mock         # never hit real Stripe locally
```

## @Profile on Beans

Use `@Profile` to register beans only for specific environments.

```kotlin
@Service
@Profile("prod")
class CloudStorageService(
    private val s3Client: AmazonS3,
) : StorageService {

    override fun upload(data: ByteArray, filename: String): String {
        log.info("[CloudStorageService] Uploading {} to S3", filename)
        val request = PutObjectRequest(
            "uniqlo-media",
            filename,
            ByteArrayInputStream(data),
            ObjectMetadata(),
        )
        s3Client.putObject(request)
        return "https://cdn.uniqlo.com/$filename"
    }
}

@Service
@Profile("local", "dev")  // active for multiple profiles
class LocalFileStorageService : StorageService {

    override fun upload(data: ByteArray, filename: String): String {
        log.warn("[LocalFileStorageService] Writing to local tmp (not S3): {}", filename)
        return "http://localhost:8080/files/$filename"
    }
}
```

<details>
<summary>Java version</summary>

```java
@Service
@Profile("prod")
public class CloudStorageService implements StorageService {

    private final AmazonS3 s3Client;

    public CloudStorageService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String upload(byte[] data, String filename) {
        log.info("[CloudStorageService] Uploading {} to S3", filename);
        PutObjectRequest request = new PutObjectRequest("uniqlo-media", filename,
            new ByteArrayInputStream(data), new ObjectMetadata());
        s3Client.putObject(request);
        return "https://cdn.uniqlo.com/" + filename;
    }
}

@Service
@Profile({"local", "dev"})  // active for multiple profiles
public class LocalFileStorageService implements StorageService {

    @Override
    public String upload(byte[] data, String filename) {
        log.warn("[LocalFileStorageService] Writing to local tmp (not S3): {}", filename);
        // write to /tmp for local testing
        return "http://localhost:8080/files/" + filename;
    }
}
```

</details>

## Profile Groups (Spring Boot 2.4+)

Define a group of profiles that are activated together.

```yaml
spring:
  profiles:
    group:
      production:
        - prod
        - cloud-logging    # enables structured JSON logging
        - metrics          # enables Micrometer metrics export to Datadog
      development:
        - local
        - debug-queries
```

```bash
java -jar app.jar --spring.profiles.active=production
# → activates prod, cloud-logging, and metrics simultaneously
```

## Profile-Specific @Configuration

```kotlin
@Configuration
@Profile("prod")
class ProductionSecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .requiresChannel { it.anyRequest().requiresSecure() }  // HTTPS required
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // no server-side session state
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .build()
}

@Configuration
@Profile("local", "dev", "test")
class DevSecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }  // convenience example for local/dev only, not a production pattern
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .build()
}
```

<details>
<summary>Java version</summary>

```java
@Configuration
@Profile("prod")
public class ProductionSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .requiresChannel(c -> c.anyRequest().requiresSecure())  // HTTPS required
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a.anyRequest().authenticated())
            .build();
    }
}

@Configuration
@Profile({"local", "dev", "test"})
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Disable security for local/dev (swagger, h2 console, etc.)
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(a -> a.anyRequest().permitAll())
            .build();
    }
}
```

</details>

## Cloud-Native Configuration: Externalizing Secrets

Here, "cloud-native" just means the deployment platform provides config and secrets from outside the application package.
In Kubernetes/ECS, secrets are often injected as environment variables. Spring Boot binds them automatically.

```yaml
# application-prod.yml — references env vars, never hardcodes secrets
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
  redis:
    host: ${REDIS_HOST:redis}       # default "redis" if not set
    port: ${REDIS_PORT:6379}

stripe:
  api-key: ${STRIPE_API_KEY}
```

```bash
# Kubernetes Secret → environment variable → Spring property
kubectl create secret generic app-secrets \
  --from-literal=DATABASE_PASSWORD=s3cr3t \
  --from-literal=STRIPE_API_KEY=sk_live_xyz
```

### Spring Cloud Config Server (Enterprise)

For centralized config across many services:

```yaml
# bootstrap.yml
spring:
  cloud:
    config:
      uri: http://config-server:8888
      label: main           # git branch
      profile: prod
```

This is useful mainly when many services share the same operational config process.
If your platform already manages env vars, secrets, and config files cleanly, you may not need a Config Server.

## @TestPropertySource & @ActiveProfiles for Tests

```kotlin
@SpringBootTest
@ActiveProfiles("test")  // loads application-test.yml
class OrderServiceIntegrationTest {
    // ...
}

@WebMvcTest(ProductController::class)
@TestPropertySource(
    properties = [
        "feature.payment-gateway=mock",
        "feature.cache.enabled=false",
    ],
)
class ProductControllerTest {
    // ...
}
```

<details>
<summary>Java version</summary>

```java
@SpringBootTest
@ActiveProfiles("test")  // loads application-test.yml
class OrderServiceIntegrationTest {
    // ...
}

@WebMvcTest(ProductController.class)
@TestPropertySource(properties = {
    "feature.payment-gateway=mock",
    "feature.cache.enabled=false"
})
class ProductControllerTest {
    // ...
}
```

</details>

## Interview Summary

| Question                                          | Answer                                                       |
|---------------------------------------------------|--------------------------------------------------------------|
| How do you prevent prod secrets in source code?   | Env vars → Spring properties (${MY_SECRET})                 |
| How do you run different code per environment?    | @Profile("prod") on beans or @Configuration classes         |
| How does Spring Boot pick the right .yml?         | application-{active-profile}.yml overrides application.yml  |
| What if I have 10 services that share config?     | Spring Cloud Config Server (git-backed centralized config)   |
| How do you test with specific properties?         | @ActiveProfiles("test") + application-test.yml              |
