package learning.livecoding.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class ProducerConsumerExercise implements Exercise {
    @Override
    public String id() {
        return "producer-consumer";
    }

    @Override
    public String title() {
        return "Producer / Consumer Job Queue";
    }

    @Override
    public String summary() {
        return "Use a blocking queue to decouple producers from consumers while preserving FIFO delivery and natural backpressure.";
    }

    static final class JobQueue {
        private final ArrayBlockingQueue<String> queue;

        JobQueue(int capacity) {
            this.queue = new ArrayBlockingQueue<>(capacity);
        }

        void submit(String jobId) throws InterruptedException {
            queue.put(jobId);
        }

        String take() throws InterruptedException {
            return queue.take();
        }
    }

    @Override
    public void run() {
        JobQueue queue = new JobQueue(2);
        List<String> processedJobs = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch finished = new CountDownLatch(3);
        String stopSignal = "__STOP__";

        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    String jobId = queue.take();
                    if (stopSignal.equals(jobId)) {
                        return;
                    }
                    processedJobs.add(jobId);
                    finished.countDown();
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("consumer interrupted", error);
            }
        });

        consumer.start();

        try {
            queue.submit("job-1");
            queue.submit("job-2");
            queue.submit("job-3");
            queue.submit(stopSignal);
            if (!finished.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("consumer did not process jobs in time");
            }
            consumer.join(1_000);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("producer interrupted", error);
        }

        ExerciseSupport.expectEquals("processed-jobs", List.of("job-1", "job-2", "job-3"), List.copyOf(processedJobs));
        ExerciseSupport.expectEquals("consumer-stopped", false, consumer.isAlive());
    }
}
