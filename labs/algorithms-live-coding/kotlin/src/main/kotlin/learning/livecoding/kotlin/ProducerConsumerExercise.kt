package learning.livecoding.kotlin

import java.util.Collections
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object ProducerConsumerExercise : Exercise {
    override val id = "producer-consumer"
    override val title = "Producer / Consumer Job Queue"
    override val summary =
        "Use a blocking queue to decouple producers from consumers while preserving FIFO delivery and natural backpressure."

    private class JobQueue(capacity: Int) {
        private val queue = ArrayBlockingQueue<String>(capacity)

        fun submit(jobId: String) {
            queue.put(jobId)
        }

        fun take(): String = queue.take()
    }

    override fun run() {
        val queue = JobQueue(capacity = 2)
        val processedJobs = Collections.synchronizedList(mutableListOf<String>())
        val finished = CountDownLatch(3)
        val stopSignal = "__STOP__"

        val consumer = Thread {
            while (true) {
                when (val jobId = queue.take()) {
                    stopSignal -> return@Thread
                    else -> {
                        processedJobs.add(jobId)
                        finished.countDown()
                    }
                }
            }
        }

        consumer.start()

        queue.submit("job-1")
        queue.submit("job-2")
        queue.submit("job-3")
        queue.submit(stopSignal)

        check(finished.await(1, TimeUnit.SECONDS)) { "consumer did not process jobs in time" }
        consumer.join(1_000)

        ExerciseSupport.expectEquals("processed-jobs", listOf("job-1", "job-2", "job-3"), processedJobs.toList())
        ExerciseSupport.expectEquals("consumer-stopped", false, consumer.isAlive)
    }
}
