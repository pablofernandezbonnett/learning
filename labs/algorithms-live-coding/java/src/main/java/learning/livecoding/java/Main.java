package learning.livecoding.java;

import java.util.List;

public final class Main {
    private static final List<Exercise> EXERCISES = List.of(
        new AsciiPyramidExercise(),
        new ContainerQueriesExercise(),
        new LongestSubstringExercise(),
        new TopKFrequentExercise(),
        new NumberOfIslandsExercise(),
        new LruCacheExercise(),
        new RateLimiterExercise(),
        new WalletLedgerExercise(),
        new CacheAsideExercise(),
        new EventDedupExercise(),
        new RetryBackoffExercise(),
        new PaginationMergeExercise(),
        new ProducerConsumerExercise(),
        new RotatedSearchExercise(),
        new MedianStreamExercise(),
        new CalculatorIIExercise(),
        new CoinChangeExercise(),
        new BasicCalculatorExercise(),
        new PaymentPriorityExercise()
    );

    private Main() {
    }

    public static void main(String[] args) {
        String command = args.length == 0 ? "list" : args[0];
        switch (command) {
            case "list" -> listExercises();
            case "all" -> EXERCISES.forEach(Main::runExercise);
            default -> runExercise(EXERCISES.stream()
                .filter(exercise -> exercise.id().equals(command))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown exercise: " + command)));
        }
    }

    private static void listExercises() {
        System.out.println("Available Java exercises:");
        EXERCISES.forEach(exercise -> System.out.println("- " + exercise.id() + ": " + exercise.title()));
    }

    private static void runExercise(Exercise exercise) {
        ExerciseSupport.printHeading(exercise);
        exercise.run();
        System.out.println();
    }
}
