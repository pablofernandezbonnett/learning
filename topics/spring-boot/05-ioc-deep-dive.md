# IoC and the Bean Lifecycle

> Primary fit: `Platform / Growth lane`


This topic matters because a lot of Spring behavior only makes sense once you
understand what the container creates, wraps, and destroys for you.

If you keep one line warm, keep this:

> `IoC` (`Inversion of Control`) means Spring owns object creation and wiring. `DI` (`dependency injection`) is the main mechanism it uses to give objects the collaborators they need.

---

## Smallest Useful Mental Model

The Spring container does three high-value jobs:

- creates beans
- wires dependencies
- wraps some beans with extra runtime behavior such as proxies

That is why `@Transactional`, `@Async`, and some other features do not feel
like plain method calls.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- Spring just finds classes and injects them
- bean lifecycle is trivia unless I write framework code

Better mental model:

- the container decides creation order, scope, lifecycle hooks, and proxy wrapping
- many real Spring surprises come from misunderstanding those boundaries

Small concrete example:

- weak approach: call a `@Transactional` method inside the same class and expect proxy behavior
- better approach: remember that proxy-based behavior usually appears only when the call crosses the bean boundary Spring manages

---

## The Lifecycle

1. **Instantiation**:
   Spring finds the class and creates the bean instance.
   How many instances it creates depends on scope:
   with the default `singleton` scope, Spring creates one shared instance per
   `ApplicationContext`; with `prototype`, Spring creates a new instance each
   time that bean is requested.
2. **Populate properties**:
   dependencies are injected by constructor, setter, or field
3. **Aware callbacks**:
   Spring can tell the bean its name or give it access to the `BeanFactory`
4. **Pre-initialization**:
   `BeanPostProcessor.postProcessBeforeInitialization(...)`
5. **Initialization**:
   `@PostConstruct` and `afterPropertiesSet()`
6. **Post-initialization**:
   `BeanPostProcessor.postProcessAfterInitialization(...)`
   This is where proxies such as `@Transactional` or `@Async` are often created.
7. **Destruction**:
   `@PreDestroy` or `destroy()`

Short rule:

> if a Spring behavior feels "magical," check whether it is really a lifecycle or proxy effect

---

## Why Constructor Injection Is Usually Better

- **Immutability**:
  you can use `final` fields
- **Easy testing**:
  dependencies are visible and easy to pass directly in tests
- **Fail-fast wiring**:
  missing dependencies are exposed early

Small code example:

```java
@Service
public class OrderService {
    private final PaymentGateway paymentGateway;

    public OrderService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
}
```

Why this is stronger:

- the dependency is explicit
- the bean is easier to test
- the object shape is harder to misuse accidentally

---

## Scopes

- **singleton**:
  one shared instance per `ApplicationContext`
- **prototype**:
  a new instance each time the container is asked for that bean
- **request / session**:
  web-aware scopes tied to the current HTTP lifecycle

Practical rule:

- default to `singleton` unless the real lifecycle demands something else
