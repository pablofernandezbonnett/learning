package learning.livecoding.java;

import java.util.ArrayDeque;
import java.util.Deque;

public final class RateLimiterExercise implements Exercise {
    @Override
    public String id() {
        return "rate-limiter";
    }

    @Override
    public String title() {
        return "Sliding Window Rate Limiter";
    }

    @Override
    public String summary() {
        return "Keep only the accepted timestamps that still belong to the active window, then reject once capacity is full.";
    }

    static final class SlidingWindowRateLimiter {
        private final int maxRequests;
        private final int windowSeconds;
        private final Deque<Integer> acceptedTimestamps = new ArrayDeque<>();

        SlidingWindowRateLimiter(int maxRequests, int windowSeconds) {
            this.maxRequests = maxRequests;
            this.windowSeconds = windowSeconds;
        }

        boolean allow(int timestampSeconds) {
            while (!acceptedTimestamps.isEmpty() && acceptedTimestamps.peekFirst() <= timestampSeconds - windowSeconds) {
                acceptedTimestamps.removeFirst();
            }

            if (acceptedTimestamps.size() >= maxRequests) {
                return false;
            }

            acceptedTimestamps.addLast(timestampSeconds);
            return true;
        }
    }

    @Override
    public void run() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(3, 10);
        ExerciseSupport.expectEquals("t=1", true, limiter.allow(1));
        ExerciseSupport.expectEquals("t=2", true, limiter.allow(2));
        ExerciseSupport.expectEquals("t=3", true, limiter.allow(3));
        ExerciseSupport.expectEquals("t=4-blocked", false, limiter.allow(4));
        ExerciseSupport.expectEquals("t=12-allowed", true, limiter.allow(12));
    }
}
