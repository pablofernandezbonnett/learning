package modernjava;

import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.time.Instant;

/**
 * Modern Java Features Lab (Java 17-21)
 * 
 * This file demonstrates a small set of modern Java features that are useful
 * in backend code.
 * 
 * Features demonstrated:
 * - Sealed interfaces and records
 * - Pattern matching for switch
 * - Virtual threads
 */
public class ModernJavaFeatures {

    // ---------------------------------------------------------
    // 1. Domain Modeling: Sealed Interfaces & Records (Java 17)
    // ---------------------------------------------------------

    // A sealed interface strictly defines allowed implementations.
    // Excellent for representing states in a finite state machine (e.g.,
    // Checkout/Order).
    public sealed interface PaymentResult permits Success, Failure, Pending {
    }

    // Records are immutable data carriers, replacing Lombok @Data for
    // DTOs/Entities.
    public record Success(String transactionId, Instant timestamp) implements PaymentResult {
    }

    public record Failure(String errorCode, String reason) implements PaymentResult {
    }

    public record Pending(String paymentUrl) implements PaymentResult {
    }

    // ---------------------------------------------------------
    // 2. Pattern Matching for switch (Java 21)
    // ---------------------------------------------------------

    /**
     * Because PaymentResult is sealed, the compiler guarantees this switch is
     * exhaustive.
     * No default clause is needed (or recommended). If a new state is added,
     * this code will fail to compile, preventing runtime bugs.
     */
    public String processPayment(PaymentResult result) {
        return switch (result) {
            // Record Patterns: We can deconstruct the record directly in the case label
            case Success(var txId, var time) -> {
                System.out.println("Processing success at " + time);
                yield String.format("Payment %s succeeded", txId);
            }
            case Failure f -> String.format("Payment failed. Code: %s, Reason: %s", f.errorCode(), f.reason());
            case Pending p -> String.format("Awaiting customer action at: %s", p.paymentUrl());
        };
    }

    // ---------------------------------------------------------
    // 3. Virtual Threads (Java 21)
    // ---------------------------------------------------------

    /**
     * Demonstrates the lightweight nature of Virtual Threads.
     * We can spawn 100,000 threads without crashing the JVM or starving the thread
     * pool.
     * Perfect for handling massive amounts of blocking I/O (Database, REST APIs).
     */
    public void demonstrateVirtualThreads() throws InterruptedException {
        System.out.println("Starting Virtual Threads demo...");
        var start = System.currentTimeMillis();

        // Use the new Executor factory method for Virtual Threads
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 100_000).forEach(i -> {
                executor.submit(() -> doSimulatedBlockingIo(i));
            });
            // The try-with-resources block automatically waits for all tasks to finish
        }

        var end = System.currentTimeMillis();
        System.out.println("Finished 100,000 blocking tasks in " + (end - start) + "ms");
    }

    private void doSimulatedBlockingIo(int taskId) {
        try {
            // This blocking call unmounts the Virtual Thread from its Carrier OS Thread.
            // The OS Thread is then free to execute other Virtual Threads.
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------------------------------------------------------
    // Entry Point
    // ---------------------------------------------------------
    public static void main(String[] args) throws InterruptedException {
        var demo = new ModernJavaFeatures();

        // Demo 1 & 2: Domain Modeling & Exhaustive Switch
        var success = new Success("TX-12345", Instant.now());
        var pending = new Pending("https://pay.stripe.com/123");

        System.out.println(demo.processPayment(success));
        System.out.println(demo.processPayment(pending));

        System.out.println("\n-----------------------------------\n");

        // Demo 3: Virtual Threads (Requires Java 21+)
        // Note: Running this with traditional Platform Threads
        // (Executors.newCachedThreadPool())
        // would likely cause an OutOfMemoryError or severe performance degradation.
        demo.demonstrateVirtualThreads();
    }
}
