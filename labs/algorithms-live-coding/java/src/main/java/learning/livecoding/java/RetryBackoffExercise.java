package learning.livecoding.java;

import java.util.ArrayList;
import java.util.List;

public final class RetryBackoffExercise implements Exercise {
    @Override
    public String id() {
        return "retry-backoff";
    }

    @Override
    public String title() {
        return "Retry with Exponential Backoff";
    }

    @Override
    public String summary() {
        return "Retry only transient failures, increase the delay each time, and stop once the call succeeds or the retry budget is exhausted.";
    }

    record RetryResult(String value, int attempts, List<Long> scheduledBackoffsMs, String status) {
    }

    static final class TransientFailure extends RuntimeException {
        TransientFailure(String message) {
            super(message);
        }
    }

    static final class PermanentFailure extends RuntimeException {
        PermanentFailure(String message) {
            super(message);
        }
    }

    static final class FlakyPaymentGateway {
        private final int transientFailuresBeforeSuccess;
        private final boolean permanentFailure;
        private int calls;

        FlakyPaymentGateway(int transientFailuresBeforeSuccess, boolean permanentFailure) {
            this.transientFailuresBeforeSuccess = transientFailuresBeforeSuccess;
            this.permanentFailure = permanentFailure;
        }

        String capture() {
            calls++;
            if (permanentFailure) {
                throw new PermanentFailure("card-declined");
            }
            if (calls <= transientFailuresBeforeSuccess) {
                throw new TransientFailure("timeout");
            }
            return "captured";
        }
    }

    static RetryResult retryWithBackoff(int maxAttempts, long initialBackoffMs, ThrowingSupplier<String> operation) {
        int attempts = 0;
        long nextBackoffMs = initialBackoffMs;
        List<Long> scheduledBackoffs = new ArrayList<>();

        while (attempts < maxAttempts) {
            attempts++;
            try {
                return new RetryResult(operation.get(), attempts, List.copyOf(scheduledBackoffs), "success");
            } catch (PermanentFailure ignored) {
                return new RetryResult(null, attempts, List.copyOf(scheduledBackoffs), "permanent-failure");
            } catch (TransientFailure ignored) {
                if (attempts == maxAttempts) {
                    return new RetryResult(null, attempts, List.copyOf(scheduledBackoffs), "exhausted");
                }
                scheduledBackoffs.add(nextBackoffMs);
                nextBackoffMs *= 2;
            }
        }

        throw new IllegalStateException("Retry loop should have returned before reaching this point");
    }

    @Override
    public void run() {
        FlakyPaymentGateway successGateway = new FlakyPaymentGateway(2, false);
        ExerciseSupport.expectEquals(
            "eventual-success",
            new RetryResult("captured", 3, List.of(100L, 200L), "success"),
            retryWithBackoff(4, 100, successGateway::capture)
        );

        FlakyPaymentGateway permanentFailureGateway = new FlakyPaymentGateway(0, true);
        ExerciseSupport.expectEquals(
            "permanent-failure-stops-immediately",
            new RetryResult(null, 1, List.of(), "permanent-failure"),
            retryWithBackoff(4, 100, permanentFailureGateway::capture)
        );

        FlakyPaymentGateway exhaustedGateway = new FlakyPaymentGateway(5, false);
        ExerciseSupport.expectEquals(
            "retry-budget-exhausted",
            new RetryResult(null, 3, List.of(100L, 200L), "exhausted"),
            retryWithBackoff(3, 100, exhaustedGateway::capture)
        );
    }

    @FunctionalInterface
    interface ThrowingSupplier<T> {
        T get();
    }
}
