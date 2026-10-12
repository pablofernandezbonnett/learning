# Spring Proxies and AOP

Spring AOP adds behavior around method calls on a managed bean. A **proxy**
receives the call, runs the matching behavior, and delegates to the target
object. Spring uses this mechanism for features such as declarative
transactions. Knowing where the proxy sits explains why an annotation can
appear correct but have no effect at runtime.

```text
another bean -> proxy -> target.method()
                        target.otherMethod() -> direct self-call
```

Spring AOP intercepts the first call. The target's call to its own method
uses `this` and does not pass through the proxy.

## See the Self-Call Failure

```kotlin
@Service
class ImportService(
    private val records: RecordRepository,
    private val batches: BatchRepository,
) {
    fun runImport(batch: Batch) {
        saveBatch(batch) // direct call; the annotation below is not applied
    }

    @Transactional
    fun saveBatch(batch: Batch) {
        records.saveAll(batch.records)
        batches.markImported(batch.id)
    }
}
```

If a caller invokes `runImport` through Spring, the self-call to `saveBatch`
still bypasses transaction advice. The repository may have its own
transaction behavior, but this annotation does not create the intended
service-level boundary.

Move the independently advised operation to another bean and call that bean:

```kotlin
@Service
class ImportCoordinator(private val writer: ImportWriter) {
    fun runImport(batch: Batch) = writer.saveBatch(batch)
}

@Service
class ImportWriter(
    private val records: RecordRepository,
    private val batches: BatchRepository,
) {
    @Transactional
    fun saveBatch(batch: Batch) {
        records.saveAll(batch.records)
        batches.markImported(batch.id)
    }
}
```

The coordinator calls the injected `ImportWriter` proxy, so its transaction
advice can run. Check that the chosen transaction scope matches the business
operation; [Transactions in Spring](./03-transactions-and-isolation.md) owns
propagation, rollback, and database boundaries.

## Know What a Proxy Can Intercept

| Proxy style | How it is made | Important limit |
|---|---|---|
| JDK interface proxy | Implements the bean's interfaces | Exposes and advises methods through those interfaces |
| Class-based proxy | Subclasses the target class | Cannot override final or private methods; the class must be proxyable |

Which style Spring uses depends on the interfaces and proxy configuration.
Even a public method is not advised on a direct self-call. For class-based
proxying in Kotlin, ensure the relevant class and method are open to proxying;
the Kotlin Spring compiler plugin can open Spring-annotated classes.

## Use AOP for Repeated Cross-Cutting Behavior

An **aspect** groups behavior that applies across several methods, such as a
custom timing rule. A **pointcut** selects the method executions to match;
**advice** is the action run before, after, or around them. An `@Around`
advice calls `proceed()` to run the target method and can observe its result or
failure. Spring AOP matches method executions on beans, subject to the same
proxy boundary.

Keep business steps in ordinary service methods. Add a custom aspect when one
consistent technical behavior really belongs around many bean calls. Verify
that the pointcut matches the intended methods and that an external bean call
actually triggers the advice.

## Spring References

- [Proxying mechanisms and self-invocation](https://docs.spring.io/spring-framework/reference/core/aop/proxying.html)
- [AOP concepts](https://docs.spring.io/spring-framework/reference/core/aop/introduction-defn.html)
- [Advice and `proceed()`](https://docs.spring.io/spring-framework/reference/core/aop/ataspectj/advice.html)
- [Kotlin classes and Spring proxies](https://docs.spring.io/spring-framework/reference/languages/kotlin/spring-projects-in.html)
