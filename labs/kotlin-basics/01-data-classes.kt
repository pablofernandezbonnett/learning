/**
 * Lab 1: Data Classes — Goodbye Lombok!
 *
 * Run: kotlinc 01-data-classes.kt -include-runtime -d out.jar && java -jar out.jar
 *
 * In Java you need Lombok (@Data) or 200 lines of boilerplate.
 * In Kotlin, a single line gives you: equals, hashCode, toString, copy(), and componentN().
 */

// 1. Basic data class — primary constructor in the header
data class User(val id: Int, val name: String, val email: String)

// 2. Nested data classes
data class Address(val street: String, val city: String, val country: String = "Japan")

data class Customer(
    val id: Int,
    val name: String,
    val address: Address,
    val tags: List<String> = emptyList()
)

// 3. Data class with computed property (NOT part of equals/hashCode)
data class Product(val id: String, val name: String, val priceYen: Int) {
    val priceUsd: Double get() = priceYen / 150.0   // computed, excluded from generated methods
}

fun main() {
    println("=== 1. Basic data class ===")
    val user1 = User(1, "Taro", "taro@example.com")
    val user2 = User(1, "Taro", "taro@example.com")
    val user3 = User(2, "Hanako", "hanako@example.com")

    println(user1)                          // toString() — auto-generated
    println("user1 == user2: ${user1 == user2}")   // true — structural equality
    println("user1 == user3: ${user1 == user3}")   // false

    println("\n=== 2. copy() — Immutable updates (like Dart copyWith) ===")
    val promoted = user1.copy(email = "taro@platform.example")
    println("Original : $user1")
    println("Promoted : $promoted")

    println("\n=== 3. Destructuring (componentN) ===")
    val (id, name, email) = user1
    println("id=$id, name=$name, email=$email")

    // Works great in loops
    val users = listOf(user1, user3)
    for ((uid, uname) in users) {
        println("  User $uid → $uname")
    }

    println("\n=== 4. Nested data class + default parameters ===")
    val customer = Customer(
        id = 101,
        name = "Retail HQ",
        address = Address("717-1 Sayama", "Yamaguchi"),  // country defaults to "Japan"
        tags = listOf("vip", "wholesale")
    )
    println(customer)

    println("\n=== 5. Computed property excluded from equals ===")
    val jacket = Product("J001", "Ultra Light Down", 14900)
    println("${jacket.name}: ¥${jacket.priceYen} / \${"%.2f".format(jacket.priceUsd)}")

    println("\n=== 6. data class in a Map (hashCode works) ===")
    val inventory = mapOf(
        Product("J001", "Ultra Light Down", 14900) to 250,
        Product("T001", "AIRism T-Shirt", 2990) to 1500
    )
    inventory.forEach { (product, stock) -> println("  ${product.name} → $stock units") }
}
