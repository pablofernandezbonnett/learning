# IoC and the Bean Lifecycle

> Primary fit: `Platform / Growth lane`


Inversion of Control (IoC) means the Framework controls the flow, not you. Dependency Injection (DI) is just the **way** we implement it.

## The Lifecycle
1.  **Instantiation**: Spring finds the class and creates the bean instance. How many instances it creates depends on the bean scope: with the default `singleton` scope, Spring creates one shared instance per `ApplicationContext`; with `prototype`, Spring creates a new instance each time that bean is requested.
2.  **Populate Properties**: Dependencies injected (via constructor, setter, or field).
3.  **BeanName/Factory Aware**: Spring tells the bean its name or gives it access to the BeanFactory.
4.  **Pre-Initialization (BeanPostProcessor)**: `postProcessBeforeInitialization` is called.
5.  **Initialization**: `@PostConstruct` and `afterPropertiesSet`.
6.  **Post-Initialization (BeanPostProcessor)**: `postProcessAfterInitialization` is called (This is where proxies like `@Transactional` or `@Async` are created!).
7.  **Destruction**: `@PreDestroy` or `destroy()`.

## Why Constructor Injection Is Usually Better
- **Immutability**: You can use `final`.
- **Easy Testing**: No need for Reflection or `@MockBean` just to set a field.
- **Fail-Fast**: It won't even compile or start if a dependency is missing.

## Scopes
- **Singleton**: One shared instance per `ApplicationContext` (default).
- **Prototype**: A new instance every time the container is asked for that bean.
- **Request/Session**: Web-aware scopes.
