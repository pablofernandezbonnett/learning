package learning.examples.quality

import learning.examples.common.Console
import learning.examples.common.Topic

object CleanCodeTopic : Topic {
    override val id: String = "quality/clean-code"
    override val title: String = "Bad flow vs better flow, with visible side effects"
    override val sourceDocs: List<String> = listOf(
        "topics/testing/02-clean-code-and-code-review.md",
    )

    override fun run() {
        badShape()
        betterShape()
    }

    private fun badShape() {
        Console.section("Bad shape")
        val order = Order(id = "order-1", items = listOf("sku-1"), isPaid = false)
        val sideEffects = mutableListOf<String>()
        process(order, sideEffects)
        Console.result("side effects", sideEffects)
        Console.result("final order", order)
    }

    private fun betterShape() {
        Console.section("Better shape")
        val order = Order(id = "order-2", items = listOf("sku-2"), isPaid = false)
        val paymentGateway = PaymentGateway()
        val orderRepository = OrderRepository()
        val notificationService = NotificationService()
        val service = CheckoutService(paymentGateway, orderRepository, notificationService)

        service.processOrderPayment(order)
        Console.result("stored order", orderRepository.savedOrders.single())
        Console.result("sent notifications", notificationService.sentMessages)
    }
}

private data class Order(
    val id: String,
    val items: List<String>?,
    var isPaid: Boolean,
    var transactionId: String? = null,
) {
    fun markPaid(transactionId: String) {
        isPaid = true
        this.transactionId = transactionId
    }
}

private fun process(order: Order?, sideEffects: MutableList<String>) {
    if (order != null && !order.items.isNullOrEmpty()) {
        if (!order.isPaid) {
            sideEffects += "charge(${order.id})"
            order.isPaid = true
            sideEffects += "save(${order.id})"
            sideEffects += "mail(${order.id})"
        }
    }
}

private data class PaymentResult(val transactionId: String)

private class PaymentGateway {
    fun charge(order: Order): PaymentResult {
        println("[gateway] charge(${order.id})")
        return PaymentResult(transactionId = "txn-${order.id}")
    }
}

private class OrderRepository {
    val savedOrders = mutableListOf<Order>()

    fun save(order: Order) {
        println("[repo] save(${order.id})")
        savedOrders += order.copy()
    }
}

private class NotificationService {
    val sentMessages = mutableListOf<String>()

    fun sendPaymentConfirmation(orderId: String) {
        println("[notification] sendPaymentConfirmation($orderId)")
        sentMessages += orderId
    }
}

private class CheckoutService(
    private val paymentGateway: PaymentGateway,
    private val orderRepository: OrderRepository,
    private val notificationService: NotificationService,
) {
    fun processOrderPayment(order: Order) {
        validatePayableOrder(order)
        val payment = paymentGateway.charge(order)
        order.markPaid(payment.transactionId)
        orderRepository.save(order)
        notificationService.sendPaymentConfirmation(order.id)
    }

    private fun validatePayableOrder(order: Order) {
        check(!order.isPaid) { "order is already paid" }
        check(!order.items.isNullOrEmpty()) { "order has no items" }
    }
}
