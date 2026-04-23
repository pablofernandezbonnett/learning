package learning.livecoding.kotlin

object WalletLedgerExercise : Exercise {
    override val id = "wallet-ledger"
    override val title = "Idempotent Wallet Ledger"
    override val summary = "Store the first result per request ID so retries return the same outcome without mutating balance twice."

    data class LedgerResult(val applied: Boolean, val balanceAfterCents: Int, val message: String)

    class WalletLedger(initialBalanceCents: Int = 0) {
        private var balanceCents = initialBalanceCents
        private val processedRequests = mutableMapOf<String, LedgerResult>()

        fun apply(requestId: String, deltaCents: Int): LedgerResult {
            val existing = processedRequests[requestId]
            if (existing != null) return existing

            val result = if (deltaCents < 0 && balanceCents + deltaCents < 0) {
                LedgerResult(applied = false, balanceAfterCents = balanceCents, message = "insufficient-funds")
            } else {
                balanceCents += deltaCents
                LedgerResult(applied = true, balanceAfterCents = balanceCents, message = "applied")
            }

            processedRequests[requestId] = result
            return result
        }
    }

    override fun run() {
        val ledger = WalletLedger()
        val deposit = ledger.apply("r1", 1_000)
        ExerciseSupport.expectEquals("deposit", LedgerResult(true, 1_000, "applied"), deposit)

        val depositRetry = ledger.apply("r1", 1_000)
        ExerciseSupport.expectEquals("deposit-retry", deposit, depositRetry)

        val debit = ledger.apply("r2", -700)
        ExerciseSupport.expectEquals("debit", LedgerResult(true, 300, "applied"), debit)

        val rejected = ledger.apply("r3", -500)
        ExerciseSupport.expectEquals("rejected", LedgerResult(false, 300, "insufficient-funds"), rejected)

        val rejectedRetry = ledger.apply("r3", -500)
        ExerciseSupport.expectEquals("rejected-retry", rejected, rejectedRetry)
    }
}
