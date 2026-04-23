package learning.examples.correctness

import learning.examples.common.Console
import learning.examples.common.Topic
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

object IdempotencyTopic : Topic {
    override val id: String = "correctness/idempotency"
    override val title: String = "Idempotency, request-key deduplication, and local transaction shape"
    override val sourceDocs: List<String> = listOf(
        "topics/databases/01-idempotency-and-transaction-safety.md",
    )

    override fun run() {
        naturalIdempotency()
        retrySafeRequestKey()
        localTransactionBoundary()
    }

    private fun naturalIdempotency() {
        Console.section("Natural idempotency")
        val service = SubscriptionService()
        Console.result("initial subscribed state", service.isSubscribed())
        service.subscribe()
        service.subscribe()
        Console.result("after subscribe() twice", service.isSubscribed())
        service.unsubscribe()
        service.unsubscribe()
        Console.result("after unsubscribe() twice", service.isSubscribed())
    }

    private fun retrySafeRequestKey() {
        Console.section("Request-key deduplication")
        val service = InMemoryPaymentService()
        Console.result("first request", service.charge("idem-100", 4_990))
        Console.result("duplicate request", service.charge("idem-100", 4_990))

        val controller = PaymentFlow()
        Console.result("first final response", controller.pay("idem-200", BigDecimal("1990")))
        Console.result("second retry returns stored response", controller.pay("idem-200", BigDecimal("1990")))
    }

    private fun localTransactionBoundary() {
        Console.section("Local transaction boundary")
        val accountRepository = AccountRepository()
        val orderRepository = OrderRepository()
        accountRepository.save(Account(id = 7, balance = BigDecimal("10000")))

        val service = OrderService(accountRepository, orderRepository)
        service.createOrder(accountId = 7, amount = BigDecimal("1500"), simulateFailure = false)
        Console.result("balance after successful order", accountRepository.findById(7)?.balance)
        Console.result("orders after successful order", orderRepository.count())

        try {
            service.createOrder(accountId = 7, amount = BigDecimal("2000"), simulateFailure = true)
        } catch (error: IllegalStateException) {
            Console.result("simulated failure", error.message)
        }
        Console.result("balance after failed order", accountRepository.findById(7)?.balance)
        Console.result("orders after failed order", orderRepository.count())
    }
}

private class SubscriptionService {
    private var subscribed = false

    fun subscribe() {
        subscribed = true
    }

    fun unsubscribe() {
        subscribed = false
    }

    fun isSubscribed(): Boolean = subscribed
}

private class InMemoryPaymentService {
    private val processedRequests = ConcurrentHashMap.newKeySet<String>()

    fun charge(requestId: String, amount: Int): String {
        val firstTime = processedRequests.add(requestId)
        return if (firstTime) {
            "charged $amount"
        } else {
            "duplicate request ignored"
        }
    }
}

private class PaymentFlow {
    private val responses = ConcurrentHashMap<String, String>()

    fun pay(key: String, amount: BigDecimal): String {
        responses[key]?.let { existing ->
            return "reused response -> $existing"
        }

        val previous = responses.putIfAbsent(key, "PROCESSING")
        if (previous != null) {
            return if (previous == "PROCESSING") {
                "request already in progress"
            } else {
                "reused response -> $previous"
            }
        }

        val result = "payment accepted for $amount"
        responses[key] = result
        return result
    }
}

private data class Account(
    val id: Long,
    val balance: BigDecimal,
) {
    fun debit(amount: BigDecimal): Account {
        check(balance >= amount) { "insufficient balance" }
        return copy(balance = balance - amount)
    }
}

private data class Order(
    val accountId: Long,
    val amount: BigDecimal,
)

private class AccountRepository {
    private val accounts = mutableMapOf<Long, Account>()

    fun findById(accountId: Long): Account? = accounts[accountId]

    fun save(account: Account) {
        accounts[account.id] = account
    }
}

private class OrderRepository {
    private val orders = mutableListOf<Order>()

    fun save(order: Order) {
        orders.add(order)
    }

    fun count(): Int = orders.size

    fun snapshot(): List<Order> = orders.toList()

    fun restore(snapshot: List<Order>) {
        orders.clear()
        orders.addAll(snapshot)
    }
}

private class OrderService(
    private val accountRepository: AccountRepository,
    private val orderRepository: OrderRepository,
) {
    fun createOrder(accountId: Long, amount: BigDecimal, simulateFailure: Boolean) {
        val originalAccount = accountRepository.findById(accountId) ?: error("account not found")
        val orderSnapshot = orderRepository.snapshot()

        try {
            val updatedAccount = originalAccount.debit(amount)
            accountRepository.save(updatedAccount)

            if (simulateFailure) {
                error("downstream order write failed after debit")
            }

            orderRepository.save(Order(accountId = accountId, amount = amount))
        } catch (error: RuntimeException) {
            accountRepository.save(originalAccount)
            orderRepository.restore(orderSnapshot)
            throw IllegalStateException("rolled back local transaction: ${error.message}")
        }
    }
}
