# Spring Boot Auto-Configuration

Auto-configuration explains why adding a dependency can make a feature
available without writing its setup by hand. Spring Boot loads candidate
configuration classes and registers their beans only when their conditions
match. It is a defaulting mechanism, not a promise that every dependency
creates every possible bean.

`@SpringBootApplication` enables auto-configuration. Candidate classes are
declared by Spring Boot or library jars; current Boot auto-configuration jars
list them in
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
Conditions can check the classpath, properties, and beans already registered.
See [conditional beans](./09-conditional-beans.md) for those checks.

## Diagnose a Surprising Bean

When a bean is missing or unexpected:

1. Check whether the dependency and its auto-configuration are present.
2. Check the active properties and profiles.
3. Check whether an application-defined bean made Boot back away.
4. Start with `--debug` and read the condition evaluation report for the
   configuration in question.

For example, a database dependency alone does not prove that a particular
`DataSource` will be created. A custom `DataSource` bean can replace Boot's
default. The report shows which condition matched or failed.

## Override or Exclude Deliberately

Define an application bean when only one default needs replacing. If an
entire auto-configuration is unwanted, exclude that auto-configuration class
through `@SpringBootApplication(exclude = ...)` or
`spring.autoconfigure.exclude`. The exclusion targets candidate
auto-configurations, not an arbitrary application service.

Keep the decision local: an exclusion can remove other beans supplied by the
same auto-configuration. Verify the final bean graph and the affected service
behavior. For environment-specific values, use
[profiles and external configuration](./10-profiles.md).

## Further Reading

- [Spring Boot auto-configuration](https://docs.spring.io/spring-boot/reference/using/auto-configuration.html)
- [Creating auto-configuration](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html)
