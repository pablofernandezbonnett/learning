import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.delay

/**
 * Lab 6: Backend Coroutines — Timeout, Cancellation, and Blocking Boundaries
 *
 * This file focuses on the backend questions that matter most:
 *
 * - when async fan-out is actually useful
 * - where the request timeout should live
 * - why blocking work is still blocking work
 */

suspend fun fetchCart(userId: String): String {
    delay(80)
    return "cart:$userId"
}

suspend fun fetchPricing(userId: String): String {
    delay(110)
    return "pricing:$userId"
}

fun blockingWarehouseCall(userId: String): String {
    Thread.sleep(140)
    return "warehouse:$userId"
}

suspend fun buildCheckoutView(userId: String): String =
    withTimeout(250) {
        coroutineScope {
            val cart = async { fetchCart(userId) }
            val pricing = async { fetchPricing(userId) }
            val warehouse =
                async {
                    withContext(Dispatchers.IO) {
                        blockingWarehouseCall(userId)
                    }
                }

            listOf(cart.await(), pricing.await(), warehouse.await()).joinToString(" | ")
        }
    }

fun main() = runBlocking {
    println("=== Backend coroutine boundary demo ===")

    val startedAt = System.currentTimeMillis()
    try {
        val result = buildCheckoutView("user-1")
        println(result)
        println("completed in ${System.currentTimeMillis() - startedAt}ms")
    } catch (e: Exception) {
        println("request failed: ${e::class.simpleName}: ${e.message}")
    }

    println()
    println("What to notice:")
    println("- one timeout owns the whole request")
    println("- async is used only for independent child work")
    println("- the blocking warehouse call still needs Dispatchers.IO")
    println("- coroutine syntax improved orchestration, not the downstream latency itself")
}
