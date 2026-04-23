package learning.livecoding.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ContainerQueriesExercise implements Exercise {
    @Override
    public String id() {
        return "container-queries";
    }

    @Override
    public String title() {
        return "Container Queries with ADD / EXISTS / REMOVE";
    }

    @Override
    public String summary() {
        return "Use a hash set for presence-only queries, but switch to a frequency map once REMOVE must preserve duplicates.";
    }

    List<String> solution(List<List<String>> queries) {
        Map<String, Integer> counts = new HashMap<>();
        List<String> results = new ArrayList<>(queries.size());

        for (List<String> query : queries) {
            String operation = query.get(0);
            String value = query.get(1);

            switch (operation) {
                case "ADD" -> {
                    counts.put(value, counts.getOrDefault(value, 0) + 1);
                    results.add("");
                }
                case "EXISTS" -> results.add(counts.getOrDefault(value, 0) > 0 ? "true" : "false");
                case "REMOVE" -> {
                    int current = counts.getOrDefault(value, 0);
                    if (current == 0) {
                        results.add("false");
                    } else {
                        if (current == 1) {
                            counts.remove(value);
                        } else {
                            counts.put(value, current - 1);
                        }
                        results.add("true");
                    }
                }
                default -> throw new IllegalArgumentException("Unknown operation: " + operation);
            }
        }

        return results;
    }

    @Override
    public void run() {
        List<List<String>> queries = List.of(
            List.of("ADD", "1"),
            List.of("ADD", "2"),
            List.of("ADD", "2"),
            List.of("ADD", "3"),
            List.of("EXISTS", "1"),
            List.of("EXISTS", "2"),
            List.of("EXISTS", "3"),
            List.of("REMOVE", "2"),
            List.of("REMOVE", "1"),
            List.of("EXISTS", "2"),
            List.of("EXISTS", "1")
        );

        ExerciseSupport.expectEquals(
            "sample",
            List.of("", "", "", "", "true", "true", "true", "true", "true", "true", "false"),
            solution(queries)
        );
    }
}
