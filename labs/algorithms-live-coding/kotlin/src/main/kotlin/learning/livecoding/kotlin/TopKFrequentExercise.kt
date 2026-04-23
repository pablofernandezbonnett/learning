package learning.livecoding.kotlin

import java.util.PriorityQueue

object TopKFrequentExercise : Exercise {
    override val id = "top-k-frequent"
    override val title = "Top K Frequent Elements"
    override val summary = "Count first, then keep only the k most frequent elements in a min-heap to cap memory and work."

    fun topKFrequent(values: IntArray, k: Int): List<Int> {
        val counts = mutableMapOf<Int, Int>()
        for (value in values) {
            counts[value] = (counts[value] ?: 0) + 1
        }
        val heap = PriorityQueue<Map.Entry<Int, Int>>(compareBy { it.value })

        for (entry in counts.entries) {
            heap.add(entry)
            if (heap.size > k) heap.remove()
        }

        return buildList {
            while (heap.isNotEmpty()) add(heap.remove().key)
        }.sorted()
    }

    override fun run() {
        ExerciseSupport.expectEquals(
            "top-2",
            listOf(1, 2),
            topKFrequent(intArrayOf(1, 1, 1, 2, 2, 3), 2),
        )
        ExerciseSupport.expectEquals(
            "top-1",
            listOf(1),
            topKFrequent(intArrayOf(1), 1),
        )
    }
}
