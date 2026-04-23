/**
 * Lab 4: Coroutines — Structured Concurrency
 *
 * Requires: kotlinx-coroutines-core
 *
 * Build with Gradle (recommended):
 *   dependencies { implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1") }
 *
 * Or compile manually:
 *   kotlinc 04-coroutines.kt -include-runtime \
 *     -classpath kotlinx-coroutines-core-jvm-1.8.1.jar \
 *     -d coroutines.jar
 *   java -classpath coroutines.jar:kotlinx-coroutines-core-jvm-1.8.1.jar MainKt
 *
 * Mental model:
 *   Dart async/await       ≈ Kotlin coroutines (suspend functions)
 *   Dart Isolate           ≈ Kotlin Thread (rare) or Dispatchers.Default
 *   Flutter FutureBuilder  ≈ Android collectAsState / LaunchedEffect
 */

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

// ─── Simulated async operations ───────────────────────────────────────────────

/** suspend = can be paused without blocking the thread. Like 'async' in Dart/TS. */
suspend fun fetchInventory(productId: String): Int {
    delay(100)  // simulate DB call — does NOT block the thread
    return when (productId) {
        "J001" -> 250
        "T001" -> 1500
        else   -> 0
    }
}

suspend fun fetchPrice(productId: String): Int {
    delay(80)
    return if (productId == "J001") 14900 else 2990
}

suspend fun fetchUserOrders(userId: Int): List<String> {
    delay(150)
    return listOf("ORDER-001", "ORDER-002", "ORDER-003")
}

// ─── Main ─────────────────────────────────────────────────────────────────────

fun main() = runBlocking {  // runBlocking bridges blocking (main) and coroutine world
    println("=== 1. Basic suspend function — sequential ===")
    val start = System.currentTimeMillis()
    val inventory = fetchInventory("J001")
    val price = fetchPrice("J001")
    println("inventory=$inventory, price=¥$price (took ${System.currentTimeMillis() - start}ms)")

    println("\n=== 2. async/await — parallel execution ===")
    // Like Dart's Future.wait([]), but more explicit about what runs in parallel.
    val start2 = System.currentTimeMillis()
    val inventoryDeferred = async { fetchInventory("J001") }  // starts immediately
    val priceDeferred     = async { fetchPrice("J001") }      // also starts immediately
    // Both run concurrently. We only wait here.
    println("parallel result: inventory=${inventoryDeferred.await()}, " +
            "price=¥${priceDeferred.await()} " +
            "(took ${System.currentTimeMillis() - start2}ms)")  // ~100ms, not ~180ms

    println("\n=== 3. launch — fire and forget ===")
    val job = launch {
        delay(50)
        println("  [launch] background task completed")
    }
    println("  [main] launched background task, continuing...")
    job.join()  // wait for it to finish before proceeding

    println("\n=== 4. Structured Concurrency — the key concept ===")
    // All coroutines launched inside a scope are children of that scope.
    // If the scope is cancelled, all children are cancelled too.
    // No dangling coroutines. No memory leaks. This is what Android's viewModelScope provides.
    coroutineScope {
        val orders = async { fetchUserOrders(1) }
        val items  = async { fetchInventory("T001") }
        println("Orders: ${orders.await()}, T001 stock: ${items.await()}")
        // If either child throws, the whole scope is cancelled.
    }

    println("\n=== 5. Dispatchers — which thread pool? ===")
    // Dispatchers.Main    → UI thread (Android). Don't use in CLI.
    // Dispatchers.IO      → Blocking I/O (DB, network). Backed by large thread pool.
    // Dispatchers.Default → CPU-intensive work. Uses CPU-count threads.
    // Dispatchers.Unconfined → Runs in caller's thread until first suspension.

    withContext(Dispatchers.IO) {
        // Simulate blocking JDBC call (OK here because we're on IO dispatcher)
        println("  [IO] running on ${Thread.currentThread().name}")
        delay(10)
    }
    withContext(Dispatchers.Default) {
        println("  [Default] running on ${Thread.currentThread().name}")
    }

    println("\n=== 6. Exception handling ===")
    // Unlike async in JS, unhandled exceptions in launch crash the parent scope.
    val handler = CoroutineExceptionHandler { _, throwable ->
        println("  Caught in handler: ${throwable.message}")
    }
    val failingJob = launch(handler) {
        throw RuntimeException("Simulated DB timeout")
    }
    failingJob.join()

    // With async, exceptions are deferred until .await() is called:
    val failingDeferred = async {
        delay(10)
        throw IllegalStateException("Stock sync failed")
    }
    try {
        failingDeferred.await()
    } catch (e: IllegalStateException) {
        println("  Caught from deferred.await(): ${e.message}")
    }

    println("\n=== 7. Flow — reactive streams (like RxJava, but simpler) ===")
    // Flow<T> = a cold stream of values. Like Dart's Stream<T>.
    // The pipeline is not executed until you collect it.

    fun stockUpdates(productId: String): Flow<Int> = flow {
        val levels = listOf(250, 248, 245, 240)
        for (level in levels) {
            delay(30)
            emit(level)  // produce a value
        }
    }

    print("  J001 stock updates: ")
    stockUpdates("J001")
        .filter { it < 248 }                         // only emit when stock drops
        .map { "⚠️  $it units remaining" }
        .collect { update -> print("$update  ") }    // terminal operator — starts the flow
    println()

    println("\n=== 8. Channel — communication between coroutines ===")
    // Like a concurrent queue. Producer sends, consumer receives.
    val channel = Channel<String>()

    launch {
        listOf("J001", "T001", "B001").forEach { id ->
            delay(20)
            channel.send("Restock alert: $id")
        }
        channel.close()
    }

    for (alert in channel) {  // iterates until channel is closed
        println("  $alert")
    }

    println("\nAll done!")
}
