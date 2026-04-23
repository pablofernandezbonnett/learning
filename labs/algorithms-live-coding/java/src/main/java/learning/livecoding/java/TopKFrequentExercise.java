package learning.livecoding.java;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public final class TopKFrequentExercise implements Exercise {
    @Override
    public String id() {
        return "top-k-frequent";
    }

    @Override
    public String title() {
        return "Top K Frequent Elements";
    }

    @Override
    public String summary() {
        return "Count first, then keep only the k most frequent elements in a min-heap to cap memory and work.";
    }

    List<Integer> topKFrequent(int[] values, int k) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int value : values) {
            counts.put(value, counts.getOrDefault(value, 0) + 1);
        }

        PriorityQueue<Map.Entry<Integer, Integer>> heap = new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            heap.add(entry);
            if (heap.size() > k) {
                heap.remove();
            }
        }

        List<Integer> result = new ArrayList<>();
        while (!heap.isEmpty()) {
            result.add(heap.remove().getKey());
        }
        result.sort(Integer::compareTo);
        return result;
    }

    @Override
    public void run() {
        ExerciseSupport.expectEquals("top-2", List.of(1, 2), topKFrequent(new int[] {1, 1, 1, 2, 2, 3}, 2));
        ExerciseSupport.expectEquals("top-1", List.of(1), topKFrequent(new int[] {1}, 1));
    }
}
