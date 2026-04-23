/**
 * Lab 3: Functional Programming & DSLs with Receivers
 *
 * Run: kotlinc 03-functional.kt -include-runtime -d out.jar && java -jar out.jar
 *
 * Kotlin blends OOP and FP seamlessly. The "lambdas with receivers" feature
 * enables type-safe DSLs — the same mechanism behind Jetpack Compose, Ktor routes,
 * and Gradle build scripts.
 */

// ─── Domain models ───────────────────────────────────────────────────────────

data class Product(val id: String, val name: String, val priceYen: Int, val category: String)

val catalog = listOf(
    Product("J001", "Ultra Light Down Jacket", 14900, "outerwear"),
    Product("T001", "AIRism T-Shirt",           2990,  "tops"),
    Product("T002", "Heattech Inner",            1990,  "tops"),
    Product("B001", "Oxford Shirt",              3990,  "tops"),
    Product("P001", "Slim Fit Chinos",           4990,  "bottoms"),
    Product("P002", "Jogger Pants",              3490,  "bottoms"),
)

// ─── 1. Lambdas & higher-order functions ────────────────────────────────────

fun main() {
    println("=== 1. map / filter / reduce ===")
    // map: transform every element
    val names = catalog.map { it.name }
    println("Names: $names")

    // filter: keep elements matching a predicate
    val affordable = catalog.filter { it.priceYen < 4000 }
    println("Under ¥4000: ${affordable.map { it.name }}")

    // Chained pipelines (like Java Streams, but with trailing lambda syntax)
    val topsSortedByPrice = catalog
        .filter { it.category == "tops" }
        .sortedBy { it.priceYen }
        .map { "${it.name} (¥${it.priceYen})" }
    println("Tops by price: $topsSortedByPrice")

    // fold: reduce to a single value
    val totalInventoryValue = catalog.fold(0) { acc, p -> acc + p.priceYen }
    println("Total catalog value: ¥$totalInventoryValue")

    println("\n=== 2. Functions as first-class values ===")
    // Define a function type: (Product) -> Boolean
    val isOuterwear: (Product) -> Boolean = { it.category == "outerwear" }
    val isCheap: (Product) -> Boolean = { it.priceYen < 3000 }

    // Compose predicates — AND
    fun <T> and(vararg predicates: (T) -> Boolean): (T) -> Boolean =
        { item -> predicates.all { pred -> pred(item) } }

    val cheapTops = catalog.filter(and(isCheap) { it.category == "tops" })
    println("Cheap tops: ${cheapTops.map { it.name }}")

    println("\n=== 3. Passing functions as parameters ===")
    fun applyDiscount(products: List<Product>, discountFn: (Product) -> Int): List<Pair<Product, Int>> =
        products.map { p -> p to discountFn(p) }

    val discounted = applyDiscount(catalog) { product ->
        when (product.category) {
            "outerwear" -> (product.priceYen * 0.8).toInt()    // 20% off
            "tops"      -> (product.priceYen * 0.9).toInt()    // 10% off
            else        -> product.priceYen
        }
    }
    discounted.forEach { (p, price) -> println("  ${p.name}: ¥${p.priceYen} → ¥$price") }

    println("\n=== 4. Extension functions ===")
    // Add methods to existing types without inheritance
    fun Int.toYen(): String = "¥${this.toString().reversed().chunked(3).joinToString(",").reversed()}"
    fun String.slugify(): String = this.lowercase().replace(" ", "-")

    println(14900.toYen())            // ¥14,900
    println("Ultra Light Down".slugify())  // ultra-light-down

    println("\n=== 5. Lambdas with receivers — the DSL superpower ===")
    // A lambda with receiver: the lambda body has 'this' bound to a specific type.
    // Syntax: Type.() -> Unit
    //
    // This is how Gradle, Ktor, and Compose work.

    class QueryBuilder {
        private val conditions = mutableListOf<(Product) -> Boolean>()
        private var limit = Int.MAX_VALUE

        fun category(cat: String)   { conditions.add { it.category == cat } }
        fun maxPrice(price: Int)    { conditions.add { it.priceYen <= price } }
        fun limit(n: Int)           { limit = n }

        fun build(): List<Product> = catalog
            .filter { p -> conditions.all { cond -> cond(p) } }
            .take(limit)
    }

    // The builder block: 'this' inside the lambda is a QueryBuilder
    fun query(block: QueryBuilder.() -> Unit): List<Product> =
        QueryBuilder().apply(block).build()

    // Usage reads like a DSL (no .method() chaining noise)
    val results = query {
        category("tops")
        maxPrice(3500)
        limit(2)
    }
    println("DSL query results: ${results.map { it.name }}")

    println("\n=== 6. Sequences — lazy evaluation (like Java Streams) ===")
    // Collections are eager: every intermediate step processes all elements.
    // Sequences are lazy: each element goes through the whole pipeline before the next.
    val first2Tops = catalog.asSequence()
        .filter { it.category == "tops" }   // not executed yet
        .map { it.name }                      // not executed yet
        .take(2)                              // triggers evaluation
        .toList()
    println("First 2 tops (lazy): $first2Tops")

    println("\n=== 7. groupBy & associate ===")
    val byCategory = catalog.groupBy { it.category }
    byCategory.forEach { (cat, products) ->
        println("  $cat: ${products.map { it.name }}")
    }

    val productById = catalog.associateBy { it.id }
    println("Lookup J001: ${productById["J001"]?.name}")
}
