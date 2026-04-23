package com.learning.mastery.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

/**
 * ConditionalConfig — illustrates @ConditionalOnProperty, @ConditionalOnMissingBean,
 * @ConditionalOnClass, and custom @Conditional implementations in Kotlin.
 *
 * application.yml to test:
 *   feature:
 *     payment-gateway: mock   # or "stripe" or "paypal"
 *     notifications:
 *       enabled: true
 *
 * Kotlin note: plugin.spring auto-opens @Service and @Configuration classes for CGLIB,
 * so conditional beans can be proxied without explicitly marking them `open`.
 */

// ═══════════════════════════════════════════════════════════════════════════════
// PAYMENT GATEWAY — multiple implementations, one active per environment
// ═══════════════════════════════════════════════════════════════════════════════

interface PaymentGateway {
    fun charge(productId: String, amountYen: Int): PaymentResult
}

// Kotlin data class replaces Java record — same conciseness, but works with copy()
data class PaymentResult(val success: Boolean, val transactionId: String, val gateway: String)

@Service
@ConditionalOnProperty(name = ["feature.payment-gateway"], havingValue = "stripe")
class StripePaymentGateway : PaymentGateway {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() { log.info("[ConditionalConfig] StripePaymentGateway ACTIVE") }

    override fun charge(productId: String, amountYen: Int): PaymentResult {
        log.info("[StripePaymentGateway] Charging ¥{} for {}", amountYen, productId)
        return PaymentResult(true, "stripe-${UUID.randomUUID()}", "stripe")
    }
}

@Service
@ConditionalOnProperty(name = ["feature.payment-gateway"], havingValue = "paypal")
class PayPalPaymentGateway : PaymentGateway {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() { log.info("[ConditionalConfig] PayPalPaymentGateway ACTIVE") }

    override fun charge(productId: String, amountYen: Int): PaymentResult {
        log.info("[PayPalPaymentGateway] Charging ¥{} for {}", amountYen, productId)
        return PaymentResult(true, "paypal-${UUID.randomUUID()}", "paypal")
    }
}

@Service
@ConditionalOnProperty(
    name = ["feature.payment-gateway"],
    havingValue = "mock",
    matchIfMissing = true,   // use mock when property is absent (tests, local dev)
)
class MockPaymentGateway : PaymentGateway {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() { log.warn("[ConditionalConfig] MockPaymentGateway ACTIVE — not a real charge!") }

    override fun charge(productId: String, amountYen: Int): PaymentResult {
        log.warn("[MockPaymentGateway] Simulating charge: ¥{} for {}", amountYen, productId)
        return PaymentResult(true, "mock-${UUID.randomUUID()}", "mock")
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// NOTIFICATION SERVICE — default provided, overridable by app
// ═══════════════════════════════════════════════════════════════════════════════

interface NotificationService {
    fun send(recipient: String, message: String)
}

@Service
@ConditionalOnProperty(name = ["feature.notifications.enabled"], havingValue = "true", matchIfMissing = false)
@ConditionalOnMissingBean(NotificationService::class)
class LogNotificationService : NotificationService {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() { log.info("[ConditionalConfig] LogNotificationService ACTIVE (default)") }

    override fun send(recipient: String, message: String) {
        log.info("[LogNotificationService] NOTIFICATION → {}: {}", recipient, message)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CUSTOM @CONDITIONAL — time-based activation (Black Friday pricing)
// ═══════════════════════════════════════════════════════════════════════════════

class BlackFridayCondition : Condition {

    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val today = LocalDate.now()
        val isBlackFriday = today.monthValue == 11 && today.dayOfMonth >= 25
        if (isBlackFriday) {
            LoggerFactory.getLogger(javaClass)
                .info("[ConditionalConfig] BlackFridayCondition: ACTIVE (it's Black Friday!)")
        }
        return isBlackFriday
    }
}

interface PricingStrategy {
    fun calculateFinalPrice(basePrice: Int): Int
}

@Service
@Conditional(BlackFridayCondition::class)
class BlackFridayPricingStrategy : PricingStrategy {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun calculateFinalPrice(basePrice: Int): Int {
        val discounted = (basePrice * 0.7).toInt()   // 30% off
        log.info("[BlackFridayPricingStrategy] ¥{} → ¥{} (30% off)", basePrice, discounted)
        return discounted
    }
}

@Service
@ConditionalOnMissingBean(PricingStrategy::class)
class StandardPricingStrategy : PricingStrategy {

    override fun calculateFinalPrice(basePrice: Int): Int = basePrice   // no discount
}

// ═══════════════════════════════════════════════════════════════════════════════
// @ConditionalOnClass — register bean only if a library is on the classpath
// ═══════════════════════════════════════════════════════════════════════════════

@Configuration
@ConditionalOnClass(name = ["io.micrometer.core.instrument.MeterRegistry"])
class MetricsConfiguration {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() { log.info("[ConditionalConfig] Micrometer found — metrics enabled") }
}

// ═══════════════════════════════════════════════════════════════════════════════
// WIRING — report which implementation is active
// ═══════════════════════════════════════════════════════════════════════════════

@Configuration
class ConditionalConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun paymentGatewayReport(gateway: PaymentGateway): String {
        val gatewayClass = gateway.javaClass.simpleName
        log.info("[ConditionalConfig] Active payment gateway: {}", gatewayClass)
        return gatewayClass
    }
}
