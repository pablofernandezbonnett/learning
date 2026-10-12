# IoC and Dependency Injection

**Inversion of control (IoC)** means Spring creates and connects application
objects instead of each object finding or constructing its collaborators.
**Dependency injection (DI)** is how a bean receives those collaborators.
The result is an object graph whose wiring can be inspected and changed in
one place.

## Declare Required Dependencies

```java
@Service
class OrderService {
    private final PaymentGateway paymentGateway;

    OrderService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
}
```

Spring creates `OrderService` and supplies a `PaymentGateway` bean through
its constructor. The dependency is visible, required, and easy to replace in
a focused test. If several beans match the type, choose one explicitly with
a qualifier or configuration instead of depending on an accidental choice.

Spring can discover annotated components such as `@Service`, or register an
object returned by an `@Bean` method. Both become managed beans. Use
constructor injection for required dependencies; setter injection fits an
optional dependency that has a sensible default. A long constructor may
signal that one class owns too many jobs.

## Know the Scope

A `singleton` bean is one shared instance per Spring container; it is the
default. Keep request-specific mutable data out of its fields. A `prototype`
bean is created when the container is asked for it. Injecting one prototype
into a singleton at construction does not automatically create a fresh
prototype on every later method call. Request-scoped beans follow an HTTP
request when a web-aware context is present.

Choose a scope from the state and lifetime the object actually needs.
Spring's handling of initialization and cleanup is described in
[bean lifecycle](./06-bean-lifecycle.md). Interception of calls to managed
beans is described in [proxies and AOP](./07-proxies-and-aop.md).

## Further Reading

- [Spring dependency injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html)
- [Bean scopes](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html)
