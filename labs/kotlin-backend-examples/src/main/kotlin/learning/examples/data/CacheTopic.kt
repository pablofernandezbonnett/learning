package learning.examples.data

import learning.examples.common.Console
import learning.examples.common.Topic
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

object CacheTopic : Topic {
    override val id: String = "data/cache"
    override val title: String = "Cache-aside, TTL, and simple stampede control"
    override val sourceDocs: List<String> = listOf(
        "topics/spring-boot/12-caching-and-redis.md",
        "topics/databases/06-redis-in-depth.md",
    )

    override fun run() {
        cacheAside()
        invalidation()
        stampedeControl()
    }

    private fun cacheAside() {
        Console.section("Cache-aside")
        val productStore = ProductStore()
        val cache = ProductCache(productStore)

        Console.result("first read sku-1", cache.findById("sku-1"))
        Console.result("second read sku-1", cache.findById("sku-1"))
        Console.result("underlying store hits", productStore.readCount)
    }

    private fun invalidation() {
        Console.section("Invalidation on write")
        val productStore = ProductStore()
        val cache = ProductCache(productStore)

        cache.findById("sku-2")
        productStore.update(Product(id = "sku-2", name = "AIRism Tee", priceYen = 1_990))
        cache.evict("sku-2")

        Console.result("read after invalidation", cache.findById("sku-2"))
        Console.result("underlying store hits after invalidation", productStore.readCount)
    }

    private fun stampedeControl() {
        Console.section("Simple stampede control")
        val productStore = ProductStore()
        val cache = ProductCache(productStore)

        val resultA = cache.findByIdWithSingleFlight("sku-3")
        val resultB = cache.findByIdWithSingleFlight("sku-3")
        Console.result("first single-flight read", resultA)
        Console.result("second single-flight read", resultB)
        Console.result("underlying store hits with single-flight", productStore.readCount)
    }
}

private data class Product(
    val id: String,
    val name: String,
    val priceYen: Int,
)

private data class CacheEntry(
    val product: Product,
    val expiresAt: Instant,
)

private class ProductStore {
    private val products = mutableMapOf(
        "sku-1" to Product(id = "sku-1", name = "Ultra Light Down", priceYen = 14_900),
        "sku-2" to Product(id = "sku-2", name = "AIRism Shirt", priceYen = 1_500),
        "sku-3" to Product(id = "sku-3", name = "Smart Ankle Pants", priceYen = 4_990),
    )

    var readCount: Int = 0
        private set

    fun findById(productId: String): Product? {
        readCount++
        return products[productId]
    }

    fun update(product: Product) {
        products[product.id] = product
    }
}

private class ProductCache(
    private val productStore: ProductStore,
) {
    private val entries = ConcurrentHashMap<String, CacheEntry>()
    private val singleFlight = ConcurrentHashMap<String, Any>()

    fun findById(productId: String): Product? {
        val now = Instant.now()
        val cached = entries[productId]
        if (cached != null && cached.expiresAt.isAfter(now)) {
            return cached.product
        }

        val loaded = productStore.findById(productId) ?: return null
        entries[productId] = CacheEntry(
            product = loaded,
            expiresAt = now.plusSeconds(30),
        )
        return loaded
    }

    fun evict(productId: String) {
        entries.remove(productId)
    }

    fun findByIdWithSingleFlight(productId: String): Product? {
        val lock = singleFlight.computeIfAbsent(productId) { Any() }
        synchronized(lock) {
            return findById(productId)
        }
    }
}
