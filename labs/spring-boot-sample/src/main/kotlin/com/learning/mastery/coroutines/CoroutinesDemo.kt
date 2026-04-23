package com.learning.mastery.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * CoroutinesDemo — coroutines with Spring Boot patterns.
 *
 * Topics covered:
 * 1. suspend functions in @Service
 * 2. @Transactional on suspend functions (Spring 6+ support)
 * 3. coroutineScope {} for parallel service calls
 * 4. Flow as a replacement for reactive streams
 * 5. withContext(Dispatchers.IO) for blocking operations
 *
 * Build requirements (build.gradle.kts):
 *   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
 *   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")  // Spring bridge
 *
 * Spring MVC note: suspend controller functions are supported via kotlinx-coroutines-reactor.
 * Spring WebFlux has deeper coroutines integration (Flow return types, etc.).
 */

// ── Stub domain types for this demo ───────────────────────────────────────────

data class ProductDetail(val id: String, val name: String, val priceYen: Int)
data class StockLevel(val productId: String, val available: Int)
data class PromotionInfo(val productId: String, val discountPct: Int)
data class ProductBundle(val detail: ProductDetail, val stock: StockLevel, val promo: PromotionInfo)

// ── 1. Suspend functions in a @Service ────────────────────────────────────────

@Service
class ProductAggregatorService(
    private val productRepo: StubProductRepo,
    private val stockService: StubStockService,
    private val promoService: StubPromoService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // ── 2. coroutineScope {} — parallel calls ─────────────────────────────────
    // All three IO calls run concurrently. Total time ≈ max(each call), not sum.
    // Without coroutineScope, they would run sequentially.

    suspend fun getBundleForProduct(productId: String): ProductBundle = coroutineScope {
        log.info("[ProductAggregatorService] Fetching bundle for {}", productId)

        // Launch all three in parallel via async {}
        val detailDeferred   = async { productRepo.findById(productId) }
        val stockDeferred    = async { stockService.getStock(productId) }
        val promoDeferred    = async { promoService.getPromotion(productId) }

        // Suspend until all three complete
        val bundle = ProductBundle(
            detail = detailDeferred.await(),
            stock  = stockDeferred.await(),
            promo  = promoDeferred.await(),
        )

        log.info("[ProductAggregatorService] Bundle ready for {}", productId)
        bundle
    }

    // ── 3. withContext(Dispatchers.IO) for blocking code ──────────────────────
    // Never call blocking code (JDBC, blocking HTTP) on the coroutine dispatcher.
    // Wrap it in withContext(Dispatchers.IO) to switch to an IO-optimised thread pool.

    suspend fun saveProductBlocking(product: ProductDetail): ProductDetail =
        withContext(Dispatchers.IO) {
            log.info("[ProductAggregatorService] Saving product (blocking IO): {}", product.id)
            // Simulate a blocking JDBC call
            Thread.sleep(10)
            product
        }

    // ── 4. Flow — lazy asynchronous stream ───────────────────────────────────
    // Flow is Kotlin's coroutine-based equivalent of RxJava Observable / Reactor Flux.
    // Useful for streaming large result sets without loading everything into memory.

    fun streamAllProducts(): Flow<ProductDetail> = flow {
        log.info("[ProductAggregatorService] Starting product stream")
        val products = withContext(Dispatchers.IO) {
            productRepo.findAll()
        }
        for (product in products) {
            log.debug("[ProductAggregatorService] Emitting product: {}", product.id)
            emit(product)
            delay(1)   // back-pressure simulation
        }
        log.info("[ProductAggregatorService] Product stream complete")
    }

    // ── 5. @Transactional on suspend functions ────────────────────────────────
    // Spring 6+ supports @Transactional on suspend functions when using
    // spring-tx + kotlinx-coroutines-reactor (TransactionCoroutineContext).
    //
    // IMPORTANT: The transaction runs on the coroutine's thread context.
    // Avoid switching dispatchers inside a transactional suspend function —
    // the transaction may not follow the coroutine to the new thread.

    @Transactional
    suspend fun transferStock(fromId: String, toId: String, quantity: Int) {
        log.info("[ProductAggregatorService] Transferring {} units from {} to {}", quantity, fromId, toId)
        // Both operations run in the same transaction
        stockService.decrement(fromId, quantity)
        stockService.increment(toId, quantity)
        log.info("[ProductAggregatorService] Transfer complete")
    }
}

// ── Stub collaborators (simulate slow IO) ─────────────────────────────────────

@Service
class StubProductRepo {
    suspend fun findById(id: String): ProductDetail {
        delay(50)   // simulate DB latency
        return ProductDetail(id, "Fleece Jacket", 5990)
    }

    suspend fun findAll(): List<ProductDetail> {
        delay(100)
        return listOf(
            ProductDetail("J001", "Fleece Jacket", 5990),
            ProductDetail("T001", "Thermal Tee", 1990),
        )
    }
}

@Service
class StubStockService {
    suspend fun getStock(productId: String): StockLevel {
        delay(30)   // simulate cache lookup
        return StockLevel(productId, 42)
    }

    suspend fun decrement(productId: String, qty: Int) {
        delay(20)
    }

    suspend fun increment(productId: String, qty: Int) {
        delay(20)
    }
}

@Service
class StubPromoService {
    suspend fun getPromotion(productId: String): PromotionInfo {
        delay(40)   // simulate promo service call
        return PromotionInfo(productId, discountPct = 10)
    }
}

// ── Comparison table ───────────────────────────────────────────────────────────
//
// | Pattern          | Kotlin Coroutines       | Project Reactor            |
// |------------------|-------------------------|----------------------------|
// | Single value     | suspend fun → T         | Mono<T>                    |
// | Stream           | Flow<T>                 | Flux<T>                    |
// | Parallel calls   | async { } + await()     | Mono.zip()                 |
// | IO offloading    | withContext(IO)          | subscribeOn(Schedulers.io) |
// | Error handling   | try/catch               | onErrorResume / onErrorMap |
// | Testing          | runTest {}              | StepVerifier               |
//
// Coroutines produce sequential-looking code while remaining non-blocking.
// Reactor uses a fluent/functional API that can be harder to read and debug.
