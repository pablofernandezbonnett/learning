package com.learning.mastery.ioc

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.BeanNameAware
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

/**
 * BeanLifecycleDemo — demonstrates the full Spring bean lifecycle in Kotlin.
 *
 * Lifecycle order (verified by console output when app starts):
 *   1. Constructor
 *   2. Dependency injection (setter or field)
 *   3. BeanNameAware.setBeanName()
 *   4. ApplicationContextAware.setApplicationContext()
 *   5. BeanPostProcessor.postProcessBeforeInitialization()
 *   6. @PostConstruct / InitializingBean.afterPropertiesSet()
 *   7. BeanPostProcessor.postProcessAfterInitialization()
 *   [Bean is ready]
 *   8. @PreDestroy / DisposableBean.destroy()
 *
 * Kotlin note: plugin.spring auto-opens @Service classes for CGLIB proxying.
 * Without it, all Kotlin classes are final by default and Spring cannot subclass them.
 *
 * Run the application and look for "[LIFECYCLE]" log entries.
 */
@Service
class BeanLifecycleDemo :
    BeanNameAware,
    ApplicationContextAware,
    InitializingBean,
    DisposableBean {

    private val log = LoggerFactory.getLogger(javaClass)

    private val warmupData = mutableListOf<String>()
    private var beanName: String = ""

    // ── Phase 1: Constructor ─────────────────────────────────────────────────
    // With constructor injection, dependencies are provided here directly.
    init {
        log.info("[LIFECYCLE] 1. Constructor called — bean instance created")
    }

    // ── Phase 3: BeanNameAware ────────────────────────────────────────────────
    override fun setBeanName(name: String) {
        beanName = name
        log.info("[LIFECYCLE] 3. BeanNameAware.setBeanName() — name='{}'", name)
    }

    // ── Phase 4: ApplicationContextAware ─────────────────────────────────────
    override fun setApplicationContext(ctx: ApplicationContext) {
        log.info(
            "[LIFECYCLE] 4. ApplicationContextAware.setApplicationContext() — {} beans registered",
            ctx.beanDefinitionCount,
        )
    }

    // ── Phase 6a: @PostConstruct ──────────────────────────────────────────────
    // Called after all dependencies injected. Preferred over InitializingBean.
    @PostConstruct
    fun init() {
        log.info("[LIFECYCLE] 6a. @PostConstruct — all dependencies injected. Warming up cache...")
        warmupData.addAll(listOf("J001", "T001", "T002", "P001"))
        log.info("[LIFECYCLE] Cache warm-up complete. {} items pre-loaded.", warmupData.size)
    }

    // ── Phase 6b: InitializingBean.afterPropertiesSet() ──────────────────────
    override fun afterPropertiesSet() {
        log.info("[LIFECYCLE] 6b. InitializingBean.afterPropertiesSet() — called after @PostConstruct")
    }

    // ── Phase 8a: @PreDestroy ─────────────────────────────────────────────────
    @PreDestroy
    fun cleanup() {
        log.info("[LIFECYCLE] 8a. @PreDestroy — releasing resources, clearing cache...")
        warmupData.clear()
        log.info("[LIFECYCLE] Cache cleared. {} items remaining.", warmupData.size)
    }

    // ── Phase 8b: DisposableBean.destroy() ───────────────────────────────────
    override fun destroy() {
        log.info("[LIFECYCLE] 8b. DisposableBean.destroy() — called after @PreDestroy")
    }

    fun getWarmupData(): List<String> = warmupData.toList()

    fun getBeanName(): String = beanName
}


/**
 * Demonstrates BeanPostProcessor — intercepts ALL beans during initialization.
 *
 * This is how Spring creates CGLIB proxies for @Transactional, @Async, @Cacheable.
 * The proxy wraps the bean AFTER phase 6 (@PostConstruct). That's why calling
 * a @Transactional method from within the same class (via 'this') bypasses the proxy.
 */
@Component
class LifecycleLoggingBeanPostProcessor : BeanPostProcessor {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        // ── Phase 5: before @PostConstruct ─────────────────────────────────
        if (beanName == "beanLifecycleDemo") {
            log.info("[LIFECYCLE] 5. BPP.postProcessBeforeInitialization('{}')", beanName)
        }
        return bean   // MUST return the bean (or a replacement)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        // ── Phase 7: after @PostConstruct — proxies are created here ────────
        if (beanName == "beanLifecycleDemo") {
            log.info(
                "[LIFECYCLE] 7. BPP.postProcessAfterInitialization('{}') — actual class: {}",
                beanName,
                bean.javaClass.simpleName,
                // For a @Transactional bean, this would print: MyService$$SpringCGLIB$$0
            )
        }
        return bean
    }
}
