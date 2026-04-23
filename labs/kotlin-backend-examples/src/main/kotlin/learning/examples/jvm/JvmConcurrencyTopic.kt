package learning.examples.jvm

import learning.examples.common.Console
import learning.examples.common.Topic
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

object JvmConcurrencyTopic : Topic {
    override val id: String = "jvm/concurrency"
    override val title: String = "Shared state, visibility, atomics, futures, and virtual threads"
    override val sourceDocs: List<String> = listOf(
        "topics/java/02-java-concurrency-and-jmm.md",
    )

    override fun run() {
        brokenCounter()
        synchronizedCounter()
        atomicCounter()
        volatileFlag()
        concurrentCollection()
        completableFutureFanOut()
        virtualThreads()
    }

    private fun brokenCounter() {
        Console.section("Broken shared counter")
        val service = BrokenCounterService()
        repeatConcurrentIncrements(times = 2_000) { service.increment() }
        Console.result("expected", 2_000)
        Console.result("actual", service.get())
    }

    private fun synchronizedCounter() {
        Console.section("Synchronized counter")
        val service = SynchronizedCounterService()
        repeatConcurrentIncrements(times = 2_000) { service.increment() }
        Console.result("expected", 2_000)
        Console.result("actual", service.get())
    }

    private fun atomicCounter() {
        Console.section("AtomicInteger counter")
        val service = AtomicCounterService()
        repeatConcurrentIncrements(times = 2_000) { service.incrementAndGet() }
        Console.result("expected", 2_000)
        Console.result("actual", service.get())
    }

    private fun volatileFlag() {
        Console.section("Volatile stop flag")
        val worker = Worker()
        val done = CountDownLatch(1)
        val runningThread = thread(name = "volatile-worker") {
            worker.runLoop()
            done.countDown()
        }

        Thread.sleep(50)
        worker.stop()
        val stopped = done.await(500, TimeUnit.MILLISECONDS)
        Console.result("worker observed stop flag", stopped)
        runningThread.join()
    }

    private fun concurrentCollection() {
        Console.section("ConcurrentHashMap")
        val store = SessionStore()
        store.put("token-1", "user-1")
        store.put("token-2", "user-2")
        Console.result("token-1 owner", store.findUser("token-1"))
        Console.result("token-2 owner", store.findUser("token-2"))
    }

    private fun completableFutureFanOut() {
        Console.section("CompletableFuture fan-out")
        val start = System.currentTimeMillis()
        val checkout = buildCheckout("user-42").join()
        Console.result("checkout view", checkout)
        Console.result("elapsed ms", System.currentTimeMillis() - start)
    }

    private fun virtualThreads() {
        Console.section("Virtual threads")
        Executors.newVirtualThreadPerTaskExecutor().use { executor ->
            val payment = executor.submit<String> {
                Thread.sleep(60)
                "payment-authorized"
            }
            val inventory = executor.submit<String> {
                Thread.sleep(40)
                "inventory-reserved"
            }
            Console.result("payment result", payment.get())
            Console.result("inventory result", inventory.get())
        }
    }
}

private class BrokenCounterService {
    private var counter = 0

    fun increment() {
        counter++
    }

    fun get(): Int = counter
}

private class SynchronizedCounterService {
    private var counter = 0

    @Synchronized
    fun increment() {
        counter++
    }

    @Synchronized
    fun get(): Int = counter
}

private class AtomicCounterService {
    private val counter = AtomicInteger()

    fun incrementAndGet(): Int = counter.incrementAndGet()

    fun get(): Int = counter.get()
}

private class Worker {
    @Volatile
    private var stopped = false

    fun stop() {
        stopped = true
    }

    fun runLoop() {
        while (!stopped) {
            Thread.sleep(5)
        }
    }
}

private class SessionStore {
    private val sessions = ConcurrentHashMap<String, String>()

    fun put(token: String, userId: String) {
        sessions[token] = userId
    }

    fun findUser(token: String): String? = sessions[token]
}

private data class Cart(val userId: String, val items: List<String>)
private data class Pricing(val userId: String, val totalYen: Int)
private data class CheckoutView(val cart: Cart, val pricing: Pricing)

private fun buildCheckout(userId: String): CompletableFuture<CheckoutView> {
    val cartFuture = CompletableFuture.supplyAsync {
        Thread.sleep(80)
        Cart(userId = userId, items = listOf("jacket", "shirt"))
    }
    val pricingFuture = CompletableFuture.supplyAsync {
        Thread.sleep(70)
        Pricing(userId = userId, totalYen = 17_890)
    }

    return cartFuture.thenCombine(pricingFuture) { cart, pricing ->
        CheckoutView(cart = cart, pricing = pricing)
    }
}

private fun repeatConcurrentIncrements(times: Int, action: () -> Unit) {
    val start = CountDownLatch(1)
    val done = CountDownLatch(times)

    repeat(times) {
        thread(start = true) {
            start.await()
            action()
            done.countDown()
        }
    }

    start.countDown()
    done.await()
}
