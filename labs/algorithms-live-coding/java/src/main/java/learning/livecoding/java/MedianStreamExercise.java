package learning.livecoding.java;

import java.util.Comparator;
import java.util.PriorityQueue;

public final class MedianStreamExercise implements Exercise {
    @Override
    public String id() {
        return "median-stream";
    }

    @Override
    public String title() {
        return "Find Median From Data Stream";
    }

    @Override
    public String summary() {
        return "Maintain the lower half in a max-heap and the upper half in a min-heap, then keep them balanced.";
    }

    static final class RunningMedian {
        private final PriorityQueue<Integer> lower = new PriorityQueue<>(Comparator.reverseOrder());
        private final PriorityQueue<Integer> upper = new PriorityQueue<>();

        void add(int value) {
            if (lower.isEmpty() || value <= lower.peek()) {
                lower.add(value);
            } else {
                upper.add(value);
            }

            rebalance();
        }

        double median() {
            if (lower.size() == upper.size()) {
                return (lower.peek() + upper.peek()) / 2.0;
            }
            return lower.peek();
        }

        private void rebalance() {
            if (lower.size() < upper.size()) {
                lower.add(upper.remove());
            } else if (lower.size() - upper.size() > 1) {
                upper.add(lower.remove());
            }
        }
    }

    @Override
    public void run() {
        RunningMedian stream = new RunningMedian();
        stream.add(5);
        ExerciseSupport.expectDouble("median-after-5", 5.0, stream.median(), 0.000001);
        stream.add(15);
        ExerciseSupport.expectDouble("median-after-15", 10.0, stream.median(), 0.000001);
        stream.add(1);
        ExerciseSupport.expectDouble("median-after-1", 5.0, stream.median(), 0.000001);
        stream.add(3);
        ExerciseSupport.expectDouble("median-after-3", 4.0, stream.median(), 0.000001);
    }
}
