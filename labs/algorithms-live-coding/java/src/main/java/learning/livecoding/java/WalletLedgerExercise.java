package learning.livecoding.java;

import java.util.HashMap;
import java.util.Map;

public final class WalletLedgerExercise implements Exercise {
    @Override
    public String id() {
        return "wallet-ledger";
    }

    @Override
    public String title() {
        return "Idempotent Wallet Ledger";
    }

    @Override
    public String summary() {
        return "Store the first result per request ID so retries return the same outcome without mutating balance twice.";
    }

    record LedgerResult(boolean applied, int balanceAfterCents, String message) {
    }

    static final class WalletLedger {
        private int balanceCents;
        private final Map<String, LedgerResult> processedRequests = new HashMap<>();

        LedgerResult apply(String requestId, int deltaCents) {
            LedgerResult existing = processedRequests.get(requestId);
            if (existing != null) {
                return existing;
            }

            LedgerResult result;
            if (deltaCents < 0 && balanceCents + deltaCents < 0) {
                result = new LedgerResult(false, balanceCents, "insufficient-funds");
            } else {
                balanceCents += deltaCents;
                result = new LedgerResult(true, balanceCents, "applied");
            }

            processedRequests.put(requestId, result);
            return result;
        }
    }

    @Override
    public void run() {
        WalletLedger ledger = new WalletLedger();
        LedgerResult deposit = ledger.apply("r1", 1_000);
        ExerciseSupport.expectEquals("deposit", new LedgerResult(true, 1_000, "applied"), deposit);

        LedgerResult depositRetry = ledger.apply("r1", 1_000);
        ExerciseSupport.expectEquals("deposit-retry", deposit, depositRetry);

        LedgerResult debit = ledger.apply("r2", -700);
        ExerciseSupport.expectEquals("debit", new LedgerResult(true, 300, "applied"), debit);

        LedgerResult rejected = ledger.apply("r3", -500);
        ExerciseSupport.expectEquals("rejected", new LedgerResult(false, 300, "insufficient-funds"), rejected);

        LedgerResult rejectedRetry = ledger.apply("r3", -500);
        ExerciseSupport.expectEquals("rejected-retry", rejected, rejectedRetry);
    }
}
