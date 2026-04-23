package learning.examples.correctness

import learning.examples.common.Console
import learning.examples.common.Topic
import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

object LockingTopic : Topic {
    override val id: String = "correctness/locking"
    override val title: String = "Lost update, optimistic locking, and pessimistic locking"
    override val sourceDocs: List<String> = listOf(
        "topics/databases/02-database-locks-and-concurrency.md",
    )

    override fun run() {
        lostUpdate()
        optimisticLocking()
        pessimisticLocking()
    }

    private fun lostUpdate() {
        Console.section("Lost update")
        val repository = UnsafeStockRepository(10)
        val start = CountDownLatch(1)
        val done = CountDownLatch(2)

        repeat(2) { worker ->
            thread(name = "unsafe-worker-$worker") {
                start.await()
                repository.sellOne()
                done.countDown()
            }
        }

        start.countDown()
        done.await()
        Console.result("expected stock after selling 2 items from 10", 8)
        Console.result("actual unsafe stock", repository.currentStock)
    }

    private fun optimisticLocking() {
        Console.section("Optimistic locking")
        val repository = OptimisticProductRepository(stock = 10, version = 1)
        val snapshotA = repository.read()
        val snapshotB = repository.read()

        val updatedA = repository.tryUpdate(snapshotA, newStock = snapshotA.stock - 1)
        val updatedB = repository.tryUpdate(snapshotB, newStock = snapshotB.stock - 1)

        Console.result("worker A update success", updatedA)
        Console.result("worker B update success", updatedB)
        Console.result("final product state", repository.read())
    }

    private fun pessimisticLocking() {
        Console.section("Pessimistic locking")
        val repository = PessimisticStockRepository(10)
        val start = CountDownLatch(1)
        val done = CountDownLatch(2)

        repeat(2) { worker ->
            thread(name = "locked-worker-$worker") {
                start.await()
                repository.sellOne("worker-$worker")
                done.countDown()
            }
        }

        start.countDown()
        done.await()
        Console.result("final stock after serialized writes", repository.currentStock)
    }
}

private class UnsafeStockRepository(initialStock: Int) {
    @Volatile
    var currentStock: Int = initialStock
        private set

    fun sellOne() {
        val current = currentStock
        Thread.sleep(20)
        currentStock = current - 1
    }
}

private data class ProductSnapshot(
    val stock: Int,
    val version: Long,
)

private class OptimisticProductRepository(stock: Int, version: Long) {
    private var current = ProductSnapshot(stock = stock, version = version)

    @Synchronized
    fun read(): ProductSnapshot = current

    @Synchronized
    fun tryUpdate(snapshot: ProductSnapshot, newStock: Int): Boolean {
        return if (current.version == snapshot.version) {
            current = ProductSnapshot(stock = newStock, version = current.version + 1)
            true
        } else {
            false
        }
    }
}

private class PessimisticStockRepository(initialStock: Int) {
    private val lock = ReentrantLock()

    @Volatile
    var currentStock: Int = initialStock
        private set

    fun sellOne(workerName: String) {
        lock.withLock {
            val before = currentStock
            println("[lock] $workerName entered critical section with stock=$before")
            Thread.sleep(20)
            currentStock = before - 1
            println("[lock] $workerName wrote stock=$currentStock")
        }
    }
}
