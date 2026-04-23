package learning.livecoding.java;

public final class ExerciseSupport {
    private ExerciseSupport() {
    }

    public static void printHeading(Exercise exercise) {
        System.out.println("== " + exercise.id() + ": " + exercise.title() + " ==");
        System.out.println(exercise.summary());
    }

    public static void expectEquals(String label, Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new IllegalStateException(label + " expected <" + expected + "> but was <" + actual + ">");
        }
        System.out.println("PASS " + label + " -> " + actual);
    }

    public static void expectDouble(String label, double expected, double actual, double epsilon) {
        if (Math.abs(expected - actual) >= epsilon) {
            throw new IllegalStateException(label + " expected <" + expected + "> but was <" + actual + ">");
        }
        System.out.println("PASS " + label + " -> " + actual);
    }
}
