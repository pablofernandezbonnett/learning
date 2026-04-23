package learning.livecoding.java;

import java.util.ArrayList;
import java.util.List;

public final class PaymentPriorityExercise implements Exercise {
    @Override
    public String id() {
        return "payment-priority";
    }

    @Override
    public String title() {
        return "Payment Source Prioritizer";
    }

    @Override
    public String summary() {
        return "Consume sources in priority order, take only what you need from each one, and fail cleanly if total funds are insufficient.";
    }

    static final class FundingSource {
        private final String name;
        private int availableCents;

        FundingSource(String name, int availableCents) {
            this.name = name;
            this.availableCents = availableCents;
        }
    }

    record Allocation(String sourceName, int usedCents) {
    }

    List<Allocation> allocate(int amountCents, List<FundingSource> sources) {
        int remaining = amountCents;
        List<Allocation> allocations = new ArrayList<>();

        for (FundingSource source : sources) {
            if (remaining == 0) {
                break;
            }

            int used = Math.min(source.availableCents, remaining);
            if (used == 0) {
                continue;
            }

            source.availableCents -= used;
            remaining -= used;
            allocations.add(new Allocation(source.name, used));
        }

        return remaining == 0 ? allocations : null;
    }

    @Override
    public void run() {
        List<FundingSource> successSources = List.of(
            new FundingSource("gift-wallet", 1_000),
            new FundingSource("cash-balance", 900),
            new FundingSource("credit-card", 10_000)
        );
        List<Allocation> success = allocate(2_500, successSources);
        ExerciseSupport.expectEquals(
            "priority-success",
            List.of(
                new Allocation("gift-wallet", 1_000),
                new Allocation("cash-balance", 900),
                new Allocation("credit-card", 600)
            ),
            success
        );

        List<FundingSource> failureSources = List.of(
            new FundingSource("gift-wallet", 400),
            new FundingSource("cash-balance", 300)
        );
        ExerciseSupport.expectEquals("priority-failure", null, allocate(1_000, failureSources));
    }
}
