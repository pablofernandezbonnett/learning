package learning.livecoding.kotlin

object RetryBackoffExercise : Exercise {
    override val id = "retry-backoff"
    override val title = "Retry with Exponential Backoff"
    override val summary =
        "Retry only transient failures, increase the delay each time, and stop once the call succeeds or the retry budget is exhausted."

    data class RetryResult(
        val value: String?,
        val attempts: Int,
        val scheduledBackoffsMs: List<Long>,
        val status: String,
    )

    private class TransientFailure(message: String) : RuntimeException(message)

    private class PermanentFailure(message: String) : RuntimeException(message)

    private class FlakyPaymentGateway(
        private val transientFailuresBeforeSuccess: Int,
        private val permanentFailure: Boolean = false,
    ) {
        var calls: Int = 0
            private set

        fun capture(): String {
            calls++
            if (permanentFailure) throw PermanentFailure("card-declined")
            if (calls <= transientFailuresBeforeSuccess) throw TransientFailure("timeout")
            return "captured"
        }
    }

    private fun retryWithBackoff(
        maxAttempts: Int,
        initialBackoffMs: Long,
        operation: () -> String,
    ): RetryResult {
        var attempts = 0
        var nextBackoffMs = initialBackoffMs
        val scheduledBackoffs = mutableListOf<Long>()

        while (attempts < maxAttempts) {
            attempts++
            try {
                return RetryResult(
                    value = operation(),
                    attempts = attempts,
                    scheduledBackoffsMs = scheduledBackoffs,
                    status = "success",
                )
            } catch (_: PermanentFailure) {
                return RetryResult(
                    value = null,
                    attempts = attempts,
                    scheduledBackoffsMs = scheduledBackoffs,
                    status = "permanent-failure",
                )
            } catch (_: TransientFailure) {
                if (attempts == maxAttempts) {
                    return RetryResult(
                        value = null,
                        attempts = attempts,
                        scheduledBackoffsMs = scheduledBackoffs,
                        status = "exhausted",
                    )
                }
                scheduledBackoffs += nextBackoffMs
                nextBackoffMs *= 2
            }
        }

        error("Retry loop should have returned before reaching this point")
    }

    override fun run() {
        val successGateway = FlakyPaymentGateway(transientFailuresBeforeSuccess = 2)
        ExerciseSupport.expectEquals(
            "eventual-success",
            RetryResult("captured", attempts = 3, scheduledBackoffsMs = listOf(100L, 200L), status = "success"),
            retryWithBackoff(maxAttempts = 4, initialBackoffMs = 100, operation = successGateway::capture),
        )

        val permanentFailureGateway = FlakyPaymentGateway(transientFailuresBeforeSuccess = 0, permanentFailure = true)
        ExerciseSupport.expectEquals(
            "permanent-failure-stops-immediately",
            RetryResult(null, attempts = 1, scheduledBackoffsMs = emptyList(), status = "permanent-failure"),
            retryWithBackoff(maxAttempts = 4, initialBackoffMs = 100, operation = permanentFailureGateway::capture),
        )

        val exhaustedGateway = FlakyPaymentGateway(transientFailuresBeforeSuccess = 5)
        ExerciseSupport.expectEquals(
            "retry-budget-exhausted",
            RetryResult(null, attempts = 3, scheduledBackoffsMs = listOf(100L, 200L), status = "exhausted"),
            retryWithBackoff(maxAttempts = 3, initialBackoffMs = 100, operation = exhaustedGateway::capture),
        )
    }
}
