/**
 * Lab 0: Kotlin Basics — A Quick Cheat Sheet for Java Developers
 *
 * Run: kotlinc 00-basics.kt -include-runtime -d out.jar && java -jar out.jar
 */

// 1. Variables: val (final) vs var (mutable)
val company = "Retail Platform"  // Immutable (preferred, like Java's final)
var stockCount = 100             // Mutable

// 2. Functions: concise syntax
fun sum(a: Int, b: Int): Int = a + b  // Expression body — no need for return

fun getMemberInfo(name: String, role: String = "Engineer"): String {
    return "$name works as $role at $company"  // String templates
}

// 3. Control Flow: 'when' is the more powerful 'switch'
fun describeScore(score: Int): String = when (score) {
    in 0..49  -> "Poor"
    in 50..89 -> "Good"
    90, 100   -> "Excellent"
    else      -> "Invalid"
}

// 4. If as an expression (replaces Java's ternary operator a ? b : c)
val stockStatus = if (stockCount > 0) "AVAILABLE" else "OUT_OF_STOCK"

// ── Data Classes ──────────────────────────────────────────────────────────────
// Auto-generates equals(), hashCode(), toString(), copy()
// Java equivalent: POJO + Lombok @Data @Builder

data class Product(
    val id: String,
    val name: String,
    val priceYen: Int,
    val category: String,
    val active: Boolean = true,
)

// ── Companion Objects (replaces Java's static methods/fields) ─────────────────
class ProductService {
    companion object {
        const val MAX_RESULTS = 100
        const val DEFAULT_CATEGORY = "all"

        // Factory method — common pattern replacing static factories in Java
        fun createDefault(): ProductService = ProductService()
    }

    fun findAll(): String = "Finding up to $MAX_RESULTS products"
}

// ── Scope Functions ───────────────────────────────────────────────────────────
// These are Kotlin's most idiomatic feature. Each differs in:
// - What 'this' or 'it' refers to inside the lambda
// - What the block returns

// let  → receiver as 'it', returns lambda result. Use for null-checks and transformations.
// apply → receiver as 'this', returns receiver. Use for object initialization.
// run   → receiver as 'this', returns lambda result. Use for grouping operations.
// also  → receiver as 'it', returns receiver. Use for side-effects (logging).
// with  → not extension, receiver as 'this', returns lambda result. Use for operating on an object.

fun demoscopeFunctions() {
    // apply: build/configure an object and return it.
    // Equivalent Java: new Product(); product.setName(...); return product;
    val jacket = Product("J001", "KANDO Jacket", 9900, "outerwear").let { product ->
        println("[let] Product received: ${product.name}")
        product.copy(priceYen = 8900)  // returns the transformed product
    }
    println("[let result] New price: ¥${jacket.priceYen}")

    // also: run a side-effect (logging) and return the original object unchanged
    val shirt = Product("S001", "HEATTECH Shirt", 1500, "tops")
        .also { println("[also] Created product: ${it.name}") }
        .also { println("[also] Category: ${it.category}") }
    // shirt is still the original Product — also does not transform

    // run: operate on an object and compute a result
    val summary = shirt.run {
        // 'this' is shirt inside run
        "Product $id: $name at ¥$priceYen (${if (active) "active" else "inactive"})"
    }
    println("[run] $summary")

    // with: same as run but not an extension function — useful when the object is already named
    val report = with(jacket) {
        "Report — $name | Category: $category | Price: ¥$priceYen"
    }
    println("[with] $report")
}

// ── Collections: map, filter, groupBy, associateBy ───────────────────────────

fun demoCollections() {
    val products = listOf(
        Product("J001", "KANDO Jacket",    9900,  "outerwear"),
        Product("J002", "Ultra Light Down", 7900,  "outerwear"),
        Product("S001", "HEATTECH Shirt",   1500,  "tops"),
        Product("S002", "Dry Stretch Polo", 2500,  "tops"),
        Product("P001", "Smart Ankle Pants", 4990, "bottoms", active = false),
    )

    // map: transform each element
    val names = products.map { it.name }
    println("[map] Names: $names")

    // filter: keep elements matching a predicate
    val activeOnes = products.filter { it.active }
    println("[filter] Active: ${activeOnes.size} of ${products.size}")

    // map + filter chained
    val cheapActiveNames = products
        .filter { it.active && it.priceYen < 5000 }
        .map { "${it.name} (¥${it.priceYen})" }
    println("[filter+map] Affordable: $cheapActiveNames")

    // groupBy: partition into a Map<K, List<V>>
    val byCategory = products.groupBy { it.category }
    byCategory.forEach { (cat, items) ->
        println("[groupBy] $cat: ${items.map { it.name }}")
    }

    // associateBy: build a Map<K, V> — keys must be unique, last one wins on collision
    val byId: Map<String, Product> = products.associateBy { it.id }
    println("[associateBy] Lookup J001: ${byId["J001"]?.name}")

    // sumOf, maxByOrNull, minByOrNull
    val totalValue = products.filter { it.active }.sumOf { it.priceYen }
    val mostExpensive = products.maxByOrNull { it.priceYen }
    println("[sumOf] Total active stock value: ¥$totalValue")
    println("[maxByOrNull] Most expensive: ${mostExpensive?.name}")

    // any / all / none — short-circuit predicates
    println("[any] Any over ¥5000: ${products.any { it.priceYen > 5000 }}")
    println("[all] All active: ${products.all { it.active }}")
    println("[none] None free: ${products.none { it.priceYen == 0 }}")
}

// ── Destructuring ─────────────────────────────────────────────────────────────
// Data classes automatically support destructuring via componentN() functions.

fun demoDestructuring() {
    val product = Product("J001", "KANDO Jacket", 9900, "outerwear")
    val (id, name, price) = product  // componentN() generated by data class
    println("[destructuring] id=$id, name=$name, price=¥$price")

    // Destructuring in for loops over maps
    val stockMap = mapOf("J001" to 42, "S001" to 100, "P001" to 0)
    for ((sku, qty) in stockMap) {
        println("[destructuring map] $sku: $qty units")
    }
}

fun main() {
    println("=== 1. Basic Variables & Functions ===")
    println(getMemberInfo("Pablo"))
    println(getMemberInfo("Taro", "Architect"))
    println("Stock status: $stockStatus")

    println("\n=== 2. Control Flow ===")
    println("Score 85: ${describeScore(85)}")
    for (i in 1..3) {
        println("  Counting... $i")
    }

    // Null Safety
    var nullableName: String? = null
    println("Name length: ${nullableName?.length ?: 0}")  // safe call + Elvis

    // Smart Casts
    val obj: Any = "I am a String"
    if (obj is String) {
        println("String length: ${obj.length}")  // auto-cast inside the if block
    }

    println("\n=== 3. Companion Objects ===")
    println("Max results: ${ProductService.MAX_RESULTS}")
    val svc = ProductService.createDefault()
    println(svc.findAll())

    println("\n=== 4. Scope Functions ===")
    demoscopeFunctions()

    println("\n=== 5. Collections ===")
    demoCollections()

    println("\n=== 6. Destructuring ===")
    demoDestructuring()
}
