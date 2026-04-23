package com.learning.mastery.ioc

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.context.annotation.SessionScope
import java.util.UUID

/**
 * ScopesDemo — illustrates Singleton, Prototype, and Web scopes in Kotlin.
 *
 * Key points:
 * 1. Singleton: one instance per ApplicationContext (default).
 * 2. Prototype: new instance every time requested from the container.
 * 3. Request: one instance per HTTP request (web apps only).
 * 4. Session: one instance per HTTP session (web apps only).
 * 5. Injecting a Prototype into a Singleton requires ObjectProvider (preferred).
 *
 * Kotlin note: plugin.spring auto-opens @Service/@Component classes so Spring can
 * create CGLIB proxies (needed for scoped proxies on @RequestScope, @SessionScope).
 */

// ── 1. Singleton (default) ────────────────────────────────────────────────────
// Spring creates ONE instance and reuses it everywhere.
// Never store mutable, user-specific state here.

@Service
// @Scope("singleton") ← implicit default, no need to annotate
class SingletonService {

    private val log = LoggerFactory.getLogger(javaClass)
    val instanceId: String = UUID.randomUUID().toString().take(8)

    @PostConstruct
    fun init() {
        log.info("[SingletonService] Created: instanceId={}", instanceId)
    }

    // WRONG pattern: storing user-specific state in a singleton
    // var currentUserId: String? = null  ← shared across all users! Race condition!
}

// ── 2. Prototype ──────────────────────────────────────────────────────────────
// Spring creates a NEW instance every time the bean is requested.
// Note: @PreDestroy is NOT called for prototype beans —
//       Spring hands them out and forgets about them.

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class PrototypeService {

    private val log = LoggerFactory.getLogger(javaClass)
    val instanceId: String = UUID.randomUUID().toString().take(8)
    var userId: String? = null

    @PostConstruct
    fun init() {
        log.info("[PrototypeService] New instance created: instanceId={}", instanceId)
    }

    fun describe(): String = "PrototypeService[id=$instanceId, userId=$userId]"
}

// ── 3. The Singleton + Prototype Problem ─────────────────────────────────────
// Problem: when a Singleton injects a Prototype via constructor, the Prototype is
// only created ONCE (at singleton initialization). All subsequent calls reuse
// the same prototype instance — defeating the purpose.
//
// Solution: Use ObjectProvider<PrototypeService> (Spring's preferred approach).

@Service
class SingletonThatNeedsPrototype(
    // ObjectProvider gives you a fresh Prototype on each getObject() call
    private val prototypeProvider: ObjectProvider<PrototypeService>,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun processForUser(userId: String) {
        // getObject() creates a NEW PrototypeService instance each time
        val prototype = prototypeProvider.getObject()
        prototype.userId = userId
        log.info("[SingletonThatNeedsPrototype] Using: {}", prototype.describe())
    }
}

// ── 4. Request Scope ─────────────────────────────────────────────────────────
// One instance per HTTP request. Commonly used to track request-specific context
// (authenticated user, correlation ID, request metadata).

@Component
@RequestScope  // = @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class RequestContext {

    private val log = LoggerFactory.getLogger(javaClass)

    val correlationId: String = UUID.randomUUID().toString()
    val requestStartTime: Long = System.currentTimeMillis()
    var userId: String? = null

    @PostConstruct
    fun init() {
        log.debug("[RequestContext] New request context: correlationId={}", correlationId)
    }

    val elapsedMs: Long get() = System.currentTimeMillis() - requestStartTime
}

// ── 5. Session Scope ──────────────────────────────────────────────────────────
// One instance per HTTP session. Use for user-specific state that persists across requests.

@Component
@SessionScope  // = @Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
class ShoppingCartSession {

    private val log = LoggerFactory.getLogger(javaClass)
    private val productIds = mutableListOf<String>()

    @PostConstruct
    fun init() {
        log.debug("[ShoppingCartSession] New cart session created")
    }

    fun addProduct(productId: String) {
        productIds.add(productId)
        log.debug("[ShoppingCartSession] Added {}. Cart size: {}", productId, productIds.size)
    }

    fun getProductIds(): List<String> = productIds.toList()

    val size: Int get() = productIds.size
}

// ── Scope Comparison Table ────────────────────────────────────────────────────
//
// | Scope      | Created when               | Destroyed when           | Thread-safe? |
// |------------|----------------------------|--------------------------|--------------|
// | Singleton  | ApplicationContext starts   | ApplicationContext closes | No (shared!) |
// | Prototype  | getBean() / constructor DI  | Never by Spring          | Yes (private)|
// | Request    | HTTP request arrives        | HTTP response sent       | Yes (per req)|
// | Session    | HTTP session starts         | HTTP session expires      | Yes (per ses)|
//
// Singleton beans: never store mutable user-specific state.
// Use ThreadLocal or Request scope for per-request data.
