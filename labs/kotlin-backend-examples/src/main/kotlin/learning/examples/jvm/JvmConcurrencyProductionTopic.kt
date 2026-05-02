package learning.examples.jvm

import learning.examples.common.Console
import learning.examples.common.Topic
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.math.max

object JvmConcurrencyProductionTopic : Topic {
    override val id: String = "jvm/concurrency-production"
    override val title: String = "Pool saturation, request budgets, admission control, and shared truth"
    override val sourceDocs: List<String> = listOf(
        "topics/java/05-concurrency-in-production.md",
        "topics/java/02-java-concurrency-and-jmm.md",
        "topics/databases/02-database-locks-and-concurrency.md",
    )

    override fun run() {
        poolSaturation()
        admissionControl()
        requestBudget()
        localLockVsSharedTruth()
    }

    private fun poolSaturation() {
        Console.section("Fixed pool saturation")
        val start = System.currentTimeMillis()

        Executors.newFixedThreadPool(2).use { executor ->
            val running = AtomicInteger()
            val peak = AtomicInteger()

            val futures = (1..6).map { jobId ->
                CompletableFuture.supplyAsync(
                    {
                        val currentRunning = running.incrementAndGet()
                        peak.updateAndGet { previous -> max(previous, currentRunning) }
                        Thread.sleep(80)
                        running.decrementAndGet()
                        "job-$jobId"
                    },
                    executor,
                )
            }

            CompletableFuture.allOf(*futures.toTypedArray()).join()
            Console.result("peak parallelism on a 2-thread pool", peak.get())
            Console.result("elapsed ms for 6 blocking jobs", System.currentTimeMillis() - start)
        }
    }

    private fun admissionControl() {
        Console.section("Admission control with Semaphore")
        val permits = Semaphore(2)
        val start = CountDownLatch(1)
        val done = CountDownLatch(5)
        val accepted = ConcurrentLinkedQueue<String>()
        val rejected = ConcurrentLinkedQueue<String>()

        repeat(5) { index ->
            thread(name = "request-$index") {
                start.await()
                val requestId = "request-${index + 1}"
                if (!permits.tryAcquire()) {
                    rejected.add(requestId)
                    done.countDown()
                    return@thread
                }

                try {
                    accepted.add(requestId)
                    Thread.sleep(60)
                } finally {
                    permits.release()
                    done.countDown()
                }
            }
        }

        start.countDown()
        done.await()
        Console.result("accepted requests", accepted.toList())
        Console.result("rejected requests", rejected.toList())
    }

    private fun requestBudget() {
        Console.section("Request budget over fan-out")
        val start = System.currentTimeMillis()

        Executors.newVirtualThreadPerTaskExecutor().use { executor ->
            val cart = CompletableFuture.supplyAsync(
                {
                    Thread.sleep(70)
                    "cart-ready"
                },
                executor,
            )
            val pricing = CompletableFuture.supplyAsync(
                {
                    Thread.sleep(170)
                    "pricing-ready"
                },
                executor,
            )

            try {
                cart.thenCombine(pricing) { cartResult, pricingResult ->
                    "$cartResult + $pricingResult"
                }.orTimeout(120, TimeUnit.MILLISECONDS).join()
            } catch (_: CompletionException) {
                Console.result("request budget respected", true)
                Console.result("elapsed ms", System.currentTimeMillis() - start)
                return
            }
        }

        Console.result("request budget respected", false)
    }

    private fun localLockVsSharedTruth() {
        Console.section("Local lock vs shared source of truth")
        val brokenRepository = BrokenStockRepository(initialStock = 1)
        val serviceA = LocalMutexReservationService(brokenRepository)
        val serviceB = LocalMutexReservationService(brokenRepository)

        val brokenSuccesses = runConcurrentReservations(
            serviceA::reserveOne,
            serviceB::reserveOne,
        )
        Console.result("broken successes for 1 item", brokenSuccesses)
        Console.result("broken final stock", brokenRepository.read())

        val safeRepository = AtomicReservationRepository(initialStock = 1)
        val safeSuccesses = runConcurrentReservations(
            safeRepository::tryReserve,
            safeRepository::tryReserve,
        )
        Console.result("safe successes for 1 item", safeSuccesses)
        Console.result("safe final stock", safeRepository.read())
    }
}

private class BrokenStockRepository(
    initialStock: Int,
) {
    private var stock: Int = initialStock

    fun read(): Int = stock

    fun write(newStock: Int) {
        stock = newStock
    }
}

private class LocalMutexReservationService(
    private val repository: BrokenStockRepository,
) {
    private val lock = Any()

    fun reserveOne(): Boolean = synchronized(lock) {
        val current = repository.read()
        if (current <= 0) {
            return false
        }

        Thread.sleep(25)
        repository.write(current - 1)
        true
    }
}

private class AtomicReservationRepository(
    initialStock: Int,
) {
    private var stock: Int = initialStock

    @Synchronized
    fun tryReserve(): Boolean {
        if (stock <= 0) {
            return false
        }

        stock -= 1
        return true
    }

    @Synchronized
    fun read(): Int = stock
}

private fun runConcurrentReservations(
    first: () -> Boolean,
    second: () -> Boolean,
): Int {
    val start = CountDownLatch(1)
    val done = CountDownLatch(2)
    val successes = AtomicInteger()

    listOf(first, second).forEach { action ->
        thread(start = true) {
            start.await()
            if (action()) {
                successes.incrementAndGet()
            }
            done.countDown()
        }
    }

    start.countDown()
    done.await()
    return successes.get()
}
