package learning.livecoding.java;

import java.util.Arrays;

public final class CoinChangeExercise implements Exercise {
    @Override
    public String id() {
        return "coin-change";
    }

    @Override
    public String title() {
        return "Coin Change";
    }

    @Override
    public String summary() {
        return "Bottom-up DP. Each amount reuses the best known answer for smaller amounts instead of recomputing recursively.";
    }

    int coinChange(int[] coins, int amount) {
        int impossible = amount + 1;
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, impossible);
        dp[0] = 0;

        for (int currentAmount = 1; currentAmount <= amount; currentAmount++) {
            for (int coin : coins) {
                if (coin <= currentAmount) {
                    dp[currentAmount] = Math.min(dp[currentAmount], dp[currentAmount - coin] + 1);
                }
            }
        }

        return dp[amount] == impossible ? -1 : dp[amount];
    }

    @Override
    public void run() {
        ExerciseSupport.expectEquals("11-from-1-2-5", 3, coinChange(new int[] {1, 2, 5}, 11));
        ExerciseSupport.expectEquals("3-from-2", -1, coinChange(new int[] {2}, 3));
        ExerciseSupport.expectEquals("zero-amount", 0, coinChange(new int[] {1}, 0));
    }
}
