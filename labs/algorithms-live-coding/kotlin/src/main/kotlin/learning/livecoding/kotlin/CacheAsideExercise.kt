package learning.livecoding.kotlin

object CacheAsideExercise : Exercise {
    override val id = "cache-aside"
    override val title = "Cache-Aside with TTL and Invalidation"
    override val summary =
        "Read from cache first, load on miss, and evict on write so hot reads stay fast without serving stale data for too long."

    data class Product(val id: String, val name: String, val priceCents: Int)

    private data class CacheEntry(val product: Product, val expiresAtSeconds: Long)

    private class FakeClock(var nowSeconds: Long)

    private class ProductStore {
        private val products = mutableMapOf(
            "sku-1" to Product("sku-1", "Ultra Light Down", 14_900),
            "sku-2" to Product("sku-2", "AIRism Tee", 1_500),
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
        private val store: ProductStore,
        private val ttlSeconds: Long,
        private val clock: FakeClock,
    ) {
        private val entries = mutableMapOf<String, CacheEntry>()

        fun get(productId: String): Product? {
            val cached = entries[productId]
            if (cached != null && cached.expiresAtSeconds > clock.nowSeconds) {
                return cached.product
            }

            val loaded = store.findById(productId) ?: return null
            entries[productId] = CacheEntry(
                product = loaded,
                expiresAtSeconds = clock.nowSeconds + ttlSeconds,
            )
            return loaded
        }

        fun update(product: Product) {
            store.update(product)
            entries.remove(product.id)
        }
    }

    override fun run() {
        val clock = FakeClock(nowSeconds = 1_000)
        val store = ProductStore()
        val cache = ProductCache(store, ttlSeconds = 30, clock = clock)

        ExerciseSupport.expectEquals(
            "first-read-loads-store",
            Product("sku-1", "Ultra Light Down", 14_900),
            cache.get("sku-1"),
        )
        ExerciseSupport.expectEquals("store-hit-count-after-first-read", 1, store.readCount)

        ExerciseSupport.expectEquals(
            "second-read-hits-cache",
            Product("sku-1", "Ultra Light Down", 14_900),
            cache.get("sku-1"),
        )
        ExerciseSupport.expectEquals("store-hit-count-after-cache-hit", 1, store.readCount)

        clock.nowSeconds += 31
        ExerciseSupport.expectEquals(
            "expired-entry-reloads",
            Product("sku-1", "Ultra Light Down", 14_900),
            cache.get("sku-1"),
        )
        ExerciseSupport.expectEquals("store-hit-count-after-expiry", 2, store.readCount)

        cache.update(Product("sku-1", "Ultra Light Down Parka", 16_900))
        ExerciseSupport.expectEquals(
            "read-after-write-sees-fresh-value",
            Product("sku-1", "Ultra Light Down Parka", 16_900),
            cache.get("sku-1"),
        )
        ExerciseSupport.expectEquals("store-hit-count-after-write-eviction", 3, store.readCount)
    }
}
