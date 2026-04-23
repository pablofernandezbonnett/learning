/**
 * Lab 2: Null Safety — No More NullPointerException
 *
 * Run: kotlinc 02-null-safety.kt -include-runtime -d out.jar && java -jar out.jar
 *
 * Kotlin splits the type system: String (never null) vs String? (nullable).
 * This is the same philosophy as Dart (null safety) and TypeScript (strictNullChecks).
 * The compiler FORCES you to handle the null case before compilation.
 */

data class User(val id: Int, val name: String, val address: Address?)
data class Address(val city: String, val postalCode: String?)

// Simulates a DB lookup that may or may not find a user
fun findUser(id: Int): User? {
    return when (id) {
        1 -> User(1, "Taro", Address("Tokyo", "100-0001"))
        2 -> User(2, "Hanako", Address("Osaka", null))        // no postal code
        3 -> User(3, "Jiro", null)                             // no address
        else -> null                                            // not found
    }
}

fun main() {
    println("=== 1. Non-nullable vs Nullable ===")
    val name: String = "Taro"       // Can NEVER be null — compiler guarantees it
    val nickname: String? = null    // Can be null — must be checked before use

    // name = null  // ← compile error
    // println(nickname.length)  // ← compile error: unsafe dereference

    println("name=$name, nickname=$nickname")

    println("\n=== 2. Safe call operator (?.) ===")
    // Chain of calls that short-circuits to null if any link is null
    val user1 = findUser(1)
    val user3 = findUser(3)  // has null address
    val ghost = findUser(99) // not found

    // Java equivalent: user != null ? user.getAddress() != null ? user.getAddress().getCity() : null : null
    println("User 1 city : ${user1?.address?.city}")    // "Tokyo"
    println("User 3 city : ${user3?.address?.city}")    // null (address is null)
    println("Ghost city  : ${ghost?.address?.city}")    // null (user is null)

    println("\n=== 3. Elvis operator (?:) — provide a default ===")
    val city1 = user1?.address?.city ?: "Unknown City"
    val city3 = user3?.address?.city ?: "Unknown City"
    val cityGhost = ghost?.address?.city ?: "Unknown City"

    println("User 1 city : $city1")     // "Tokyo"
    println("User 3 city : $city3")     // "Unknown City"
    println("Ghost city  : $cityGhost") // "Unknown City"

    // Elvis with throw (great for early return / guard clauses)
    fun getCity(id: Int): String {
        val user = findUser(id) ?: throw IllegalArgumentException("User $id not found")
        return user.address?.city ?: "No address on file"
    }
    println("getCity(2) : ${getCity(2)}")

    println("\n=== 4. let — run a block only if non-null ===")
    // Dart equivalent: if (value != null) { ... }
    val user2 = findUser(2)

    user2?.let { u ->
        println("Found user: ${u.name}")
        println("  postal: ${u.address?.postalCode ?: "N/A"}")
    }

    ghost?.let {
        println("This won't print — ghost is null")
    } ?: println("Ghost user not found — skipping block")

    println("\n=== 5. Non-null assertion (!!) — use sparingly ===")
    // Like Dart's ! operator. Throws KotlinNullPointerException if null.
    // Only use when you're 100% sure the value is non-null and the type system can't prove it.
    val trustedUser = findUser(1)!!   // we KNOW id=1 exists
    println("Trusted user name: ${trustedUser.name}")

    println("\n=== 6. Smart casts — after a null check, type is narrowed ===")
    fun describeUser(id: Int) {
        val user = findUser(id)
        if (user == null) {
            println("User $id: not found")
            return
        }
        // After the null check above, 'user' is automatically User (non-nullable) here
        println("User $id: ${user.name}, city=${user.address?.city ?: "no address"}")
    }

    describeUser(1)
    describeUser(3)
    describeUser(99)

    println("\n=== 7. lateinit — for DI-style fields (Spring beans) ===")
    // For non-null properties that are initialized after construction (by a DI container)
    class ServiceA {
        lateinit var repository: String   // will be injected later

        fun init(repo: String) { repository = repo }

        fun query(): String {
            // if (!::repository.isInitialized) throw UninitializedPropertyAccessException
            return "Result from $repository"
        }
    }
    val svc = ServiceA().apply { init("UserRepository") }
    println(svc.query())
}
