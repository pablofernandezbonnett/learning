package learning.livecoding.kotlin

object CoinChangeExercise : Exercise {
    override val id = "coin-change"
    override val title = "Coin Change"
    override val summary = "Bottom-up DP. Each amount reuses the best known answer for smaller amounts instead of recomputing recursively."

    fun coinChange(coins: IntArray, amount: Int): Int {
        val impossible = amount + 1
        val dp = IntArray(amount + 1) { impossible }
        dp[0] = 0

        for (currentAmount in 1..amount) {
            for (coin in coins) {
                if (coin <= currentAmount) {
                    dp[currentAmount] = minOf(dp[currentAmount], dp[currentAmount - coin] + 1)
                }
            }
        }

        return if (dp[amount] == impossible) -1 else dp[amount]
    }

    override fun run() {
        ExerciseSupport.expectEquals("11-from-1-2-5", 3, coinChange(intArrayOf(1, 2, 5), 11))
        ExerciseSupport.expectEquals("3-from-2", -1, coinChange(intArrayOf(2), 3))
        ExerciseSupport.expectEquals("zero-amount", 0, coinChange(intArrayOf(1), 0))
    }
}
