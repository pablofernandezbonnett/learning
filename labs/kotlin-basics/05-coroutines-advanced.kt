import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * Lab 5: Advanced Coroutines — Structured Concurrency & Streams
 * 
 * Key Concept: Coroutines are NOT threads. They are "suspendable computations".
 * 100,000 coroutines can run on 4 threads easily.
 */

// 1. Structured Concurrency: Coroutines must run in a Scope
class InventorySyncService {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun syncAll() {
        scope.launch {
            val jobs = listOf(
                async { fetchStockFromSAP() },
                async { fetchStockFromWarehouse() }
            )
            val results = jobs.awaitAll()
            println("Sync complete. Combined total: ${results.sum()}")
        }
    }

    private suspend fun fetchStockFromSAP(): Int {
        delay(1000) // Non-blocking sleep
        return 500
    }

    private suspend fun fetchStockFromWarehouse(): Int {
        delay(800)
        return 300
    }
    
    fun shutdown() = scope.cancel()
}

// 2. Flows: Asynchronous Streams (The equivalent of Flux/Reactive)
fun getOrderStream(): Flow<String> = flow {
    for (i in 1..5) {
        delay(500)
        emit("Order #$i received") // Pushing data
    }
}

suspend fun runFlowExample() {
    println("\n=== 2. Reactive Flow Example ===")
    getOrderStream()
        .filter { it.contains("2") || it.contains("4") }
        .map { it.uppercase() }
        .collect { order ->
            println("Processing in Analytics: $order")
        }
}

// 3. Channels: CSP pattern in Kotlin (similar to Go channels)
suspend fun runChannelExample() {
    println("\n=== 3. Channel Example (Producer-Consumer) ===")
    val channel = Channel<Int>()
    
    coroutineScope {
        launch { // Producer
            for (x in 1..3) {
                println("Sending to warehouse robot: $x")
                channel.send(x)
            }
            channel.close()
        }

        launch { // Consumer
            for (y in channel) {
                println("Robot processed item: $y")
            }
        }
    }
}

fun main() = runBlocking {
    println("=== 1. Structured Concurrency ===")
    val service = InventorySyncService()
    service.syncAll()
    delay(2000) // Wait for async tasks to finish
    
    runFlowExample()
    runChannelExample()
    
    service.shutdown()
}

/*
 * PRACTICAL NOTE: Integration with Spring Boot
 * - Spring Boot 3 + Coroutines is the best of both worlds.
 * - You can define: suspend fun findUser(id: String): User
 * - Spring Framework handles the suspension automatically, allowing your
 *   server to handle thousands of requests per thread (non-blocking I/O).
 */
