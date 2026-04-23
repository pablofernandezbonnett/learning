package learning.livecoding.java;

import java.util.HashMap;
import java.util.Map;

public final class CacheAsideExercise implements Exercise {
    @Override
    public String id() {
        return "cache-aside";
    }

    @Override
    public String title() {
        return "Cache-Aside with TTL and Invalidation";
    }

    @Override
    public String summary() {
        return "Read from cache first, load on miss, and evict on write so hot reads stay fast without serving stale data for too long.";
    }

    record Product(String id, String name, int priceCents) {
    }

    record CacheEntry(Product product, long expiresAtSeconds) {
    }

    static final class FakeClock {
        private long nowSeconds;

        FakeClock(long nowSeconds) {
            this.nowSeconds = nowSeconds;
        }

        long nowSeconds() {
            return nowSeconds;
        }

        void advanceSeconds(long seconds) {
            nowSeconds += seconds;
        }
    }

    static final class ProductStore {
        private final Map<String, Product> products = new HashMap<>();
        private int readCount;

        ProductStore() {
            products.put("sku-1", new Product("sku-1", "Ultra Light Down", 14_900));
            products.put("sku-2", new Product("sku-2", "AIRism Tee", 1_500));
        }

        Product findById(String productId) {
            readCount++;
            return products.get(productId);
        }

        void update(Product product) {
            products.put(product.id(), product);
        }

        int readCount() {
            return readCount;
        }
    }

    static final class ProductCache {
        private final ProductStore store;
        private final long ttlSeconds;
        private final FakeClock clock;
        private final Map<String, CacheEntry> entries = new HashMap<>();

        ProductCache(ProductStore store, long ttlSeconds, FakeClock clock) {
            this.store = store;
            this.ttlSeconds = ttlSeconds;
            this.clock = clock;
        }

        Product get(String productId) {
            CacheEntry cached = entries.get(productId);
            if (cached != null && cached.expiresAtSeconds() > clock.nowSeconds()) {
                return cached.product();
            }

            Product loaded = store.findById(productId);
            if (loaded == null) {
                return null;
            }

            entries.put(productId, new CacheEntry(loaded, clock.nowSeconds() + ttlSeconds));
            return loaded;
        }

        void update(Product product) {
            store.update(product);
            entries.remove(product.id());
        }
    }

    @Override
    public void run() {
        FakeClock clock = new FakeClock(1_000);
        ProductStore store = new ProductStore();
        ProductCache cache = new ProductCache(store, 30, clock);

        ExerciseSupport.expectEquals(
            "first-read-loads-store",
            new Product("sku-1", "Ultra Light Down", 14_900),
            cache.get("sku-1")
        );
        ExerciseSupport.expectEquals("store-hit-count-after-first-read", 1, store.readCount());

        ExerciseSupport.expectEquals(
            "second-read-hits-cache",
            new Product("sku-1", "Ultra Light Down", 14_900),
            cache.get("sku-1")
        );
        ExerciseSupport.expectEquals("store-hit-count-after-cache-hit", 1, store.readCount());

        clock.advanceSeconds(31);
        ExerciseSupport.expectEquals(
            "expired-entry-reloads",
            new Product("sku-1", "Ultra Light Down", 14_900),
            cache.get("sku-1")
        );
        ExerciseSupport.expectEquals("store-hit-count-after-expiry", 2, store.readCount());

        cache.update(new Product("sku-1", "Ultra Light Down Parka", 16_900));
        ExerciseSupport.expectEquals(
            "read-after-write-sees-fresh-value",
            new Product("sku-1", "Ultra Light Down Parka", 16_900),
            cache.get("sku-1")
        );
        ExerciseSupport.expectEquals("store-hit-count-after-write-eviction", 3, store.readCount());
    }
}
