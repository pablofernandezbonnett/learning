package modernjava;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * VirtualThreadsRequestBudgetLab
 *
 * This lab keeps the point practical:
 *
 * - one request fans out to several blocking downstream calls
 * - virtual threads make the waiting model cheaper
 * - request budgets and downstream limits still decide the final latency
 */
public class VirtualThreadsRequestBudgetLab {

    private static final Duration REQUEST_BUDGET = Duration.ofMillis(250);

    public static void main(String[] args) throws Exception {
        System.out.println("=== Fixed pool: only two workers for three blocking calls ===");
        runScenario(Executors.newFixedThreadPool(2), "fixed-pool");

        System.out.println();
        System.out.println("=== Virtual threads: one cheap thread per blocking call ===");
        runScenario(Executors.newVirtualThreadPerTaskExecutor(), "virtual-threads");
    }

    private static void runScenario(ExecutorService executor, String label) throws Exception {
        try (executor) {
            var startedAt = Instant.now();

            Future<String> stock = executor.submit(blockingCall("stock", 120));
            Future<String> pricing = executor.submit(blockingCall("pricing", 150));
            Future<String> fraud = executor.submit(blockingCall("fraud", 80));

            String result = waitForWithinBudget(startedAt, stock, pricing, fraud);
            long elapsedMs = Duration.between(startedAt, Instant.now()).toMillis();

            System.out.println(label + " result: " + result);
            System.out.println(label + " elapsed: " + elapsedMs + "ms");
            System.out.println(label + " takeaway: cheaper waiting helps, but the request budget still decides success or timeout");
        }
    }

    private static Callable<String> blockingCall(String name, long sleepMs) {
        return () -> {
            Thread.sleep(sleepMs);
            return name + ":ok";
        };
    }

    @SafeVarargs
    private static String waitForWithinBudget(Instant startedAt, Future<String>... futures)
            throws ExecutionException, InterruptedException {
        StringBuilder result = new StringBuilder();

        for (Future<String> future : futures) {
            if (Duration.between(startedAt, Instant.now()).compareTo(REQUEST_BUDGET) > 0) {
                return "request-timeout";
            }
            result.append(future.get()).append(" ");
        }

        if (Duration.between(startedAt, Instant.now()).compareTo(REQUEST_BUDGET) > 0) {
            return "request-timeout";
        }

        return result.toString().trim();
    }
}
