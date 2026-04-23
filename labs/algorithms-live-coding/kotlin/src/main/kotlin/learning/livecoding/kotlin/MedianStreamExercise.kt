package learning.livecoding.kotlin

import java.util.PriorityQueue

object MedianStreamExercise : Exercise {
    override val id = "median-stream"
    override val title = "Find Median From Data Stream"
    override val summary = "Maintain the lower half in a max-heap and the upper half in a min-heap, then keep them balanced."

    class RunningMedian {
        private val lower = PriorityQueue<Int>(compareByDescending { it })
        private val upper = PriorityQueue<Int>()

        fun add(value: Int) {
            if (lower.isEmpty() || value <= lower.peek()) {
                lower.add(value)
            } else {
                upper.add(value)
            }

            rebalance()
        }

        fun median(): Double {
            return when {
                lower.size == upper.size -> (lower.peek() + upper.peek()) / 2.0
                else -> lower.peek().toDouble()
            }
        }

        private fun rebalance() {
            if (lower.size < upper.size) {
                lower.add(upper.remove())
            } else if (lower.size - upper.size > 1) {
                upper.add(lower.remove())
            }
        }
    }

    override fun run() {
        val stream = RunningMedian()
        stream.add(5)
        ExerciseSupport.expectDouble("median-after-5", 5.0, stream.median())
        stream.add(15)
        ExerciseSupport.expectDouble("median-after-15", 10.0, stream.median())
        stream.add(1)
        ExerciseSupport.expectDouble("median-after-1", 5.0, stream.median())
        stream.add(3)
        ExerciseSupport.expectDouble("median-after-3", 4.0, stream.median())
    }
}
