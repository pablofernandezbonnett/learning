package learning.livecoding.kotlin

object LongestSubstringExercise : Exercise {
    override val id = "longest-substring"
    override val title = "Longest Substring Without Repeating Characters"
    override val summary = "Sliding window with a last-seen map. The invariant is that the active window never contains duplicates."

    fun lengthOfLongestSubstring(text: String): Int {
        var left = 0
        var best = 0
        val lastSeen = mutableMapOf<Char, Int>()

        for ((right, char) in text.withIndex()) {
            val previousIndex = lastSeen[char]
            if (previousIndex != null && previousIndex >= left) {
                // Move the left boundary past the duplicate inside the current window.
                left = previousIndex + 1
            }

            lastSeen[char] = right
            best = maxOf(best, right - left + 1)
        }

        return best
    }

    override fun run() {
        ExerciseSupport.expectEquals("abcabcbb", 3, lengthOfLongestSubstring("abcabcbb"))
        ExerciseSupport.expectEquals("bbbbb", 1, lengthOfLongestSubstring("bbbbb"))
        ExerciseSupport.expectEquals("pwwkew", 3, lengthOfLongestSubstring("pwwkew"))
        ExerciseSupport.expectEquals("empty", 0, lengthOfLongestSubstring(""))
    }
}
