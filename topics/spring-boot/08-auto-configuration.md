# 4. Spring Boot "Magic": Auto-Configuration

> Primary fit: `Platform / Growth lane`


Why can you just add a dependency and "it works"? This is the core of Spring Boot.

### 📍 The "Core" Annotations
- **`@SpringBootApplication`**: A 3-in-1 annotation:
  1. `@Configuration`: Marks it as a source of bean definitions.
  2. `@ComponentScan`: Scans for beans in YOUR package.
  3. **`@EnableAutoConfiguration`**: This is the real magic.

### 📍 How `@EnableAutoConfiguration` works
1. **The Search**: It looks for files named `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (In newer versions) or `META-INF/spring.factories` (In older versions) in all your JARs.
2. **The "Conditionals"**: These files list hundreds of "potential" configurations (JPA, Redis, Web, etc.).
3. **The Filters**: Spring uses `@Conditional` annotations to decide which ones to load:
   - `@ConditionalOnClass(DataSource.class)`: Only load if the class is present (e.g., you added the H2 dependency).
   - `@ConditionalOnMissingBean(DataSource.class)`: Only load if YOU haven't defined your own DataSource already.
   - `@ConditionalOnProperty(name = "spring.datasource.url")`: Only load if a property is set.

### 📍 Interview Pro Tip: How to "disable" an Auto-config?
You can exclude it in the annotation:

```kotlin
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class MySimpleApp
```

<details>
<summary>Java version</summary>

```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MySimpleApp { ... }
```

</details>

Important clarification: `exclude` is for **auto-configuration classes**, not for arbitrary classes.

- **Valid**: `DataSourceAutoConfiguration::class`, `SecurityAutoConfiguration::class`
- **Not the intended use**: `MyCustomClass::class` if it is just your own `@Configuration`, `@Component`, or service class

Why: Spring Boot builds a list of candidate auto-configurations from its auto-configuration imports metadata, and `exclude` removes entries from that list. If your class is not one of those auto-configuration entries, excluding it does not mean "disable this random class".

Interview-safe answer: "I use `exclude` only for specific Spring Boot auto-configuration classes. For my own classes, I control registration with component scan boundaries, explicit imports, profiles, or conditional properties."

### 📍 When to use it?
- **Starters**: When creating a library for other teams to use.
- **Microservices**: To share common security/logging config without copy-pasting code.
