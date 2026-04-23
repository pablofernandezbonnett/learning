package learning.examples.jvm

import learning.examples.common.Console
import learning.examples.common.Topic

object JvmModelingTopic : Topic {
    override val id: String = "jvm/modeling"
    override val title: String = "Modern JVM modeling through Kotlin equivalents"
    override val sourceDocs: List<String> = listOf(
        "topics/java/03-modern-java-for-backend-engineers.md",
    )

    override fun run() {
        dataCarrier()
        sealedResults()
    }

    private fun dataCarrier() {
        Console.section("Data carrier")
        val product = ProductView(id = "sku-1", name = "Ultra Light Down", priceYen = 14_900)
        val discounted = product.copy(priceYen = 12_900)
        Console.result("original product", product)
        Console.result("discounted copy", discounted)
    }

    private fun sealedResults() {
        Console.section("Sealed workflow result")
        val results = listOf<PaymentResult>(
            Success(transactionId = "txn-100"),
            Declined(reason = "insufficient_funds"),
            Error(message = "psp timeout"),
        )
        results.forEach { result ->
            Console.result("described result", describe(result))
        }
    }
}

private data class ProductView(
    val id: String,
    val name: String,
    val priceYen: Int,
)

private sealed interface PaymentResult

private data class Success(val transactionId: String) : PaymentResult
private data class Declined(val reason: String) : PaymentResult
private data class Error(val message: String) : PaymentResult

private fun describe(result: PaymentResult): String =
    when (result) {
        is Success -> "charged ${result.transactionId}"
        is Declined -> "declined: ${result.reason}"
        is Error -> "error: ${result.message}"
    }
