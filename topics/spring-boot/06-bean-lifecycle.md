# Bean Initialization and Shutdown

A bean has work around its useful lifetime: Spring creates it, injects
dependencies, runs initialization callbacks, makes it available, and later
runs destruction callbacks when the managed scope ends. This matters when
startup blocks, a resource leaks, or cleanup never runs.

For object creation, injection, and scope selection, start with
[IoC and dependency injection](./05-ioc-deep-dive.md).

## Keep Initialization Small

`@PostConstruct` runs after dependencies have been injected, during bean
initialization. Use it for quick local validation or preparation that needs
the injected configuration. A failure can prevent the application context
from starting.

Avoid slow database calls, remote requests, or broad cache warm-up in this
hook. The bean is still being initialized, and Spring warns that external
bean access here can cause startup lock problems. Work that must begin after
regular singleton initialization needs a later lifecycle hook, such as
`SmartInitializingSingleton` or a context-refresh listener; its failure and
readiness behavior still need an explicit design.

## Clean Up Owned Resources

`@PreDestroy` runs as a managed bean is destroyed during orderly shutdown.
It is useful for closing a resource the bean owns. Cleanup must be bounded:
a crashed process or forced termination can skip the callback.

For a `prototype` bean, Spring runs initialization callbacks but does not
track the instance for destruction. The caller that receives it must manage
its later cleanup. A singleton has one managed instance per container;
request-scoped beans end with their request in a web-aware context.

## Know Where Framework Hooks Fit

A `BeanPostProcessor` can inspect or wrap beans before and after their
initialization callbacks. Spring uses post-processors for several container
features, including some proxy creation. Application code rarely needs to
implement one just to observe startup. If a transaction or cache annotation
does not run, inspect the call path in [proxies and AOP](./07-proxies-and-aop.md)
before adding lifecycle code.

Ask three questions when a lifecycle issue appears:

1. Who creates this object, and what scope owns it?
2. Does initialization do work that should happen after startup instead?
3. Who closes each resource if shutdown is incomplete or the bean is a prototype?

## Further Reading

- [Spring lifecycle callbacks](https://docs.spring.io/spring-framework/reference/core/beans/factory-nature.html)
- [`@PostConstruct` and `@PreDestroy`](https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/postconstruct-and-predestroy-annotations.html)
- [Prototype lifecycle](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html)
