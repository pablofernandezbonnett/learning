package learning.livecoding.java;

import java.util.HashMap;
import java.util.Map;

public final class LongestSubstringExercise implements Exercise {
    @Override
    public String id() {
        return "longest-substring";
    }

    @Override
    public String title() {
        return "Longest Substring Without Repeating Characters";
    }

    @Override
    public String summary() {
        return "Sliding window with a last-seen map. The invariant is that the active window never contains duplicates.";
    }

    int lengthOfLongestSubstring(String text) {
        int left = 0;
        int best = 0;
        Map<Character, Integer> lastSeen = new HashMap<>();

        for (int right = 0; right < text.length(); right++) {
            char current = text.charAt(right);
            Integer previousIndex = lastSeen.get(current);
            if (previousIndex != null && previousIndex >= left) {
                // Move the left boundary past the duplicate inside the current window.
                left = previousIndex + 1;
            }

            lastSeen.put(current, right);
            best = Math.max(best, right - left + 1);
        }

        return best;
    }

    @Override
    public void run() {
        ExerciseSupport.expectEquals("abcabcbb", 3, lengthOfLongestSubstring("abcabcbb"));
        ExerciseSupport.expectEquals("bbbbb", 1, lengthOfLongestSubstring("bbbbb"));
        ExerciseSupport.expectEquals("pwwkew", 3, lengthOfLongestSubstring("pwwkew"));
        ExerciseSupport.expectEquals("empty", 0, lengthOfLongestSubstring(""));
    }
}
