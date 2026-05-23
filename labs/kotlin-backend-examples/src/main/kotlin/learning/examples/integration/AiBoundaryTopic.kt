package learning.examples.integration

import learning.examples.common.Console
import learning.examples.common.Topic
import java.io.Closeable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

object AiBoundaryTopic : Topic {
    override val id: String = "integration/ai-boundary"
    override val title: String = "AI boundary: timeout budgets, admission control, and policy gates"
    override val sourceDocs: List<String> = listOf(
        "topics/ai/04-ml-and-ai-pipelines-for-jvm-backend.md",
        "topics/ai/05-ai-serving-observability-and-rollout.md",
        "topics/security/02-web-and-api-security.md",
        "topics/python/04-project-shape-and-quality.md",
    )

    override fun run() {
        timeoutBudgetAndFallback()
        admissionControl()
        policyGate()
    }

    private fun timeoutBudgetAndFallback() {
        Console.section("Timeout budget and fallback")
        AiAssistantService(
            modelClient = FakeModelClient(
                delayMs = 180,
                response = RawModelOutput(
                    action = "ESCALATE",
                    confidence = "0.91",
                    reasoning = "slow classification path",
                ),
            ),
            permits = Semaphore(2),
            executor = Executors.newCachedThreadPool(),
        ).use { service ->
            val result = service.classifyRefund(
                request = RefundRequest(
                    caseId = "case-100",
                    amountYen = 4_800,
                    customerMessage = "customer reports duplicate charge",
                ),
                timeoutMs = 120,
            )
            Console.result("slow request result", result)
        }
    }

    private fun admissionControl() {
        Console.section("Admission control for expensive model work")
        AiAssistantService(
            modelClient = FakeModelClient(
                delayMs = 80,
                response = RawModelOutput(
                    action = "APPROVE_REFUND",
                    confidence = "0.96",
                    reasoning = "likely duplicate charge",
                ),
            ),
            permits = Semaphore(2),
            executor = Executors.newFixedThreadPool(4),
        ).use { service ->
            val start = CountDownLatch(1)
            val done = CountDownLatch(5)
            val accepted = ConcurrentLinkedQueue<String>()
            val shed = ConcurrentLinkedQueue<String>()

            repeat(5) { index ->
                thread(name = "ai-request-$index") {
                    start.await()
                    val caseId = "case-${index + 1}"
                    val result = service.classifyRefund(
                        request = RefundRequest(
                            caseId = caseId,
                            amountYen = 3_000,
                            customerMessage = "customer reports duplicate charge",
                        ),
                        timeoutMs = 200,
                    )
                    if (result.startsWith("busy-fallback")) {
                        shed.add(caseId)
                    } else {
                        accepted.add(caseId)
                    }
                    done.countDown()
                }
            }

            start.countDown()
            done.await()
            Console.result("accepted within capacity", accepted.toList())
            Console.result("shed under pressure", shed.toList())
        }
    }

    private fun policyGate() {
        Console.section("Model output stays untrusted until policy accepts it")
        val knownRaw = RawModelOutput(
            action = "APPROVE_REFUND",
            confidence = "0.99",
            reasoning = "model sounds sure",
        )
        val unknownRaw = RawModelOutput(
            action = "AUTO_SEND_GIFT_CARD",
            confidence = "0.99",
            reasoning = "invented action",
        )
        val highAmount = RefundRequest(
            caseId = "case-200",
            amountYen = 180_000,
            customerMessage = "enterprise dispute",
        )
        val normalAmount = RefundRequest(
            caseId = "case-201",
            amountYen = 3_200,
            customerMessage = "possible duplicate charge",
        )
        val gate = RefundPolicyGate()

        Console.result("bad direct action on high amount", badDirectAction(knownRaw, highAmount))
        Console.result("guarded decision on high amount", gate.decide(parseSuggestion(knownRaw), highAmount))
        Console.result("guarded decision on normal amount", gate.decide(parseSuggestion(knownRaw), normalAmount))
        Console.result("guarded decision on unknown action", gate.decide(parseSuggestion(unknownRaw), normalAmount))
    }
}

private data class RefundRequest(
    val caseId: String,
    val amountYen: Int,
    val customerMessage: String,
)

private data class RawModelOutput(
    val action: String,
    val confidence: String,
    val reasoning: String,
)

private enum class SuggestedAction {
    APPROVE_REFUND,
    REJECT_REFUND,
    ESCALATE,
}

private data class RefundSuggestion(
    val action: SuggestedAction,
    val confidence: Double,
    val reasoning: String,
)

private fun parseSuggestion(raw: RawModelOutput): RefundSuggestion? {
    val action = when (raw.action) {
        "APPROVE_REFUND" -> SuggestedAction.APPROVE_REFUND
        "REJECT_REFUND" -> SuggestedAction.REJECT_REFUND
        "ESCALATE" -> SuggestedAction.ESCALATE
        else -> return null
    }
    val confidence = raw.confidence.toDoubleOrNull() ?: return null
    return RefundSuggestion(
        action = action,
        confidence = confidence,
        reasoning = raw.reasoning,
    )
}

private class FakeModelClient(
    private val delayMs: Long,
    private val response: RawModelOutput,
) {
    fun classify(request: RefundRequest): RawModelOutput {
        Thread.sleep(delayMs)
        return response.copy(reasoning = "${response.reasoning}; case=${request.caseId}")
    }
}

private class RefundPolicyGate {
    fun decide(suggestion: RefundSuggestion?, request: RefundRequest): String {
        if (suggestion == null) {
            return "manual-review: invalid model output"
        }
        if (suggestion.confidence < 0.90) {
            return "manual-review: low model confidence"
        }
        if (request.amountYen >= 50_000) {
            return "manual-review: high-amount refund needs deterministic approval path"
        }

        return when (suggestion.action) {
            SuggestedAction.APPROVE_REFUND -> "approve refund for ${request.caseId}"
            SuggestedAction.REJECT_REFUND -> "reject refund for ${request.caseId}"
            SuggestedAction.ESCALATE -> "manual-review: model requested escalation"
        }
    }
}

private fun badDirectAction(raw: RawModelOutput, request: RefundRequest): String {
    return "refund approved for ${request.caseId} because model said ${raw.action}"
}

private class AiAssistantService(
    private val modelClient: FakeModelClient,
    private val permits: Semaphore,
    private val executor: ExecutorService,
) : Closeable {
    private val policyGate = RefundPolicyGate()

    fun classifyRefund(request: RefundRequest, timeoutMs: Long): String {
        if (!permits.tryAcquire()) {
            return "busy-fallback: skip expensive model path for ${request.caseId}"
        }

        try {
            val future = executor.submit<RefundSuggestion?> {
                parseSuggestion(modelClient.classify(request))
            }
            val suggestion = try {
                future.get(timeoutMs, TimeUnit.MILLISECONDS)
            } catch (_: TimeoutException) {
                future.cancel(true)
                return "timeout-fallback: route ${request.caseId} to manual review"
            }

            return policyGate.decide(suggestion, request)
        } finally {
            permits.release()
        }
    }

    override fun close() {
        executor.shutdownNow()
    }
}
