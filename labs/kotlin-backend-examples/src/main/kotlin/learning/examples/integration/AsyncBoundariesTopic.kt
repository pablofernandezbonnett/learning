package learning.examples.integration

import learning.examples.common.Console
import learning.examples.common.Topic
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object AsyncBoundariesTopic : Topic {
    override val id: String = "integration/async-boundaries"
    override val title: String = "Webhooks, outbox, retries, and at-least-once handling"
    override val sourceDocs: List<String> = listOf(
        "topics/api/03-webhooks-basics.md",
        "topics/api/02-message-brokers-and-delivery-semantics.md",
        "topics/architecture/03-distributed-transactions-and-events.md",
        "topics/architecture/06-reactive-and-event-driven-basics.md",
        "topics/architecture/02-resiliency-patterns.md",
    )

    override fun run() {
        webhookDedup()
        outboxPattern()
        retryPattern()
    }

    private fun webhookDedup() {
        Console.section("Webhook deduplication")
        val handler = WebhookHandler()
        val event = WebhookEvent(id = "evt-100", type = "payment.succeeded", payload = "order-123")
        Console.result("first delivery", handler.handle(event))
        Console.result("duplicate delivery", handler.handle(event))
    }

    private fun outboxPattern() {
        Console.section("Outbox-style flow")
        val orderRepository = MutableOrderRepository()
        val outbox = OutboxRepository()
        val service = CheckoutPublisher(orderRepository, outbox)

        service.placeOrder(orderId = "order-200", amountYen = 4_990)
        Console.result("saved order IDs", orderRepository.savedOrderIds())
        Console.result("outbox size after local write", outbox.pendingCount())

        val publisher = OutboxPublisher(outbox)
        val firstBatch = publisher.publishPending()
        val secondBatch = publisher.publishPending()
        Console.result("first publication batch", firstBatch)
        Console.result("second publication batch", secondBatch)
    }

    private fun retryPattern() {
        Console.section("Retry with bounded attempts")
        val unstable = UnstableDependency(failuresBeforeSuccess = 2)
        val result = retry(
            maxAttempts = 3,
            action = { unstable.call() },
        )
        Console.result("retry result", result)
        Console.result("total attempts", unstable.totalAttempts)
    }
}

private data class WebhookEvent(
    val id: String,
    val type: String,
    val payload: String,
)

private class WebhookHandler {
    private val processedEventIds = ConcurrentHashMap.newKeySet<String>()

    fun handle(event: WebhookEvent): String {
        val firstDelivery = processedEventIds.add(event.id)
        return if (firstDelivery) {
            "acknowledged ${event.type} for ${event.payload}"
        } else {
            "duplicate ignored for ${event.id}"
        }
    }
}

private data class OrderRecord(
    val orderId: String,
    val amountYen: Int,
)

private data class OutboxRecord(
    val eventId: String,
    val type: String,
    val payload: String,
)

private class MutableOrderRepository {
    private val orders = mutableListOf<OrderRecord>()

    fun save(order: OrderRecord) {
        orders += order
    }

    fun savedOrderIds(): List<String> = orders.map { it.orderId }
}

private class OutboxRepository {
    private val pending = ConcurrentLinkedQueue<OutboxRecord>()

    fun save(record: OutboxRecord) {
        pending.add(record)
    }

    fun drainAll(): List<OutboxRecord> {
        val drained = mutableListOf<OutboxRecord>()
        while (true) {
            val record = pending.poll() ?: break
            drained += record
        }
        return drained
    }

    fun pendingCount(): Int = pending.size
}

private class CheckoutPublisher(
    private val orderRepository: MutableOrderRepository,
    private val outboxRepository: OutboxRepository,
) {
    fun placeOrder(orderId: String, amountYen: Int) {
        orderRepository.save(OrderRecord(orderId = orderId, amountYen = amountYen))
        outboxRepository.save(
            OutboxRecord(
                eventId = "outbox-$orderId",
                type = "order.created",
                payload = orderId,
            ),
        )
    }
}

private class OutboxPublisher(
    private val outboxRepository: OutboxRepository,
) {
    private val publishedEventIds = ConcurrentHashMap.newKeySet<String>()

    fun publishPending(): List<String> {
        return outboxRepository
            .drainAll()
            .mapNotNull { record ->
                if (publishedEventIds.add(record.eventId)) {
                    "published ${record.type} for ${record.payload}"
                } else {
                    null
                }
            }
    }
}

private class UnstableDependency(
    private val failuresBeforeSuccess: Int,
) {
    var totalAttempts: Int = 0
        private set

    fun call(): String {
        totalAttempts++
        if (totalAttempts <= failuresBeforeSuccess) {
            throw IllegalStateException("temporary upstream timeout")
        }
        return "success on attempt $totalAttempts"
    }
}

private fun retry(maxAttempts: Int, action: () -> String): String {
    var lastError: Exception? = null
    repeat(maxAttempts) { attempt ->
        try {
            return action()
        } catch (error: Exception) {
            lastError = error
            println("[retry] attempt ${attempt + 1} failed: ${error.message}")
        }
    }
    throw IllegalStateException("exhausted retries: ${lastError?.message}")
}
