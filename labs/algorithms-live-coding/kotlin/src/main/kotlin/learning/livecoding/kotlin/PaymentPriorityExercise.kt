package learning.livecoding.kotlin

object PaymentPriorityExercise : Exercise {
    override val id = "payment-priority"
    override val title = "Payment Source Prioritizer"
    override val summary = "Consume sources in priority order, take only what you need from each one, and fail cleanly if total funds are insufficient."

    data class FundingSource(val name: String, var availableCents: Int)
    data class Allocation(val sourceName: String, val usedCents: Int)

    fun allocate(amountCents: Int, sources: List<FundingSource>): List<Allocation>? {
        var remaining = amountCents
        val allocations = mutableListOf<Allocation>()

        for (source in sources) {
            if (remaining == 0) break

            val used = minOf(source.availableCents, remaining)
            if (used == 0) continue

            source.availableCents -= used
            remaining -= used
            allocations += Allocation(source.name, used)
        }

        return if (remaining == 0) allocations else null
    }

    override fun run() {
        val successSources = listOf(
            FundingSource("gift-wallet", 1_000),
            FundingSource("cash-balance", 900),
            FundingSource("credit-card", 10_000),
        )
        val success = allocate(2_500, successSources)
        ExerciseSupport.expectEquals(
            "priority-success",
            listOf(
                Allocation("gift-wallet", 1_000),
                Allocation("cash-balance", 900),
                Allocation("credit-card", 600),
            ),
            success,
        )

        val failureSources = listOf(
            FundingSource("gift-wallet", 400),
            FundingSource("cash-balance", 300),
        )
        ExerciseSupport.expectEquals("priority-failure", null, allocate(1_000, failureSources))
    }
}
