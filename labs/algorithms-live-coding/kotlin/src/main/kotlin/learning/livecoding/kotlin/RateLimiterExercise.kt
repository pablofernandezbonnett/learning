package learning.livecoding.kotlin

import java.util.ArrayDeque

object RateLimiterExercise : Exercise {
    override val id = "rate-limiter"
    override val title = "Sliding Window Rate Limiter"
    override val summary = "Keep only the accepted timestamps that still belong to the active window, then reject once capacity is full."

    class SlidingWindowRateLimiter(
        private val maxRequests: Int,
        private val windowSeconds: Int,
    ) {
        private val acceptedTimestamps = ArrayDeque<Int>()

        fun allow(timestampSeconds: Int): Boolean {
            while (acceptedTimestamps.isNotEmpty() && acceptedTimestamps.first() <= timestampSeconds - windowSeconds) {
                acceptedTimestamps.removeFirst()
            }

            if (acceptedTimestamps.size >= maxRequests) return false

            acceptedTimestamps.addLast(timestampSeconds)
            return true
        }
    }

    override fun run() {
        val limiter = SlidingWindowRateLimiter(maxRequests = 3, windowSeconds = 10)
        ExerciseSupport.expectEquals("t=1", true, limiter.allow(1))
        ExerciseSupport.expectEquals("t=2", true, limiter.allow(2))
        ExerciseSupport.expectEquals("t=3", true, limiter.allow(3))
        ExerciseSupport.expectEquals("t=4-blocked", false, limiter.allow(4))
        ExerciseSupport.expectEquals("t=12-allowed", true, limiter.allow(12))
    }
}
