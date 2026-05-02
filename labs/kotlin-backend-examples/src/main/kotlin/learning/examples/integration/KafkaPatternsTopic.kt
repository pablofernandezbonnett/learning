package learning.examples.integration

import learning.examples.common.Console
import learning.examples.common.Topic
import kotlin.math.absoluteValue

object KafkaPatternsTopic : Topic {
    override val id: String = "integration/kafka-patterns"
    override val title: String = "Partitions, consumer groups, duplicates, replay, and DLT"
    override val sourceDocs: List<String> = listOf(
        "topics/api/06-kafka-practical-foundations.md",
        "topics/api/02-message-brokers-and-delivery-semantics.md",
        "topics/architecture/03-distributed-transactions-and-events.md",
    )

    override fun run() {
        partitioning()
        consumerGroupAssignment()
        duplicateDelivery()
        deadLetterFlow()
    }

    private fun partitioning() {
        Console.section("Partitioning by key")
        val topic = DemoTopic(partitions = 3)
        val records = listOf(
            DemoRecord(eventId = "evt-1", key = "order-10", payload = "created"),
            DemoRecord(eventId = "evt-2", key = "order-10", payload = "paid"),
            DemoRecord(eventId = "evt-3", key = "order-22", payload = "created"),
        )

        Console.result(
            "partition assignment",
            records.map { record -> "${record.key}:${record.payload}->p${topic.partitionFor(record.key)}" },
        )
    }

    private fun consumerGroupAssignment() {
        Console.section("Consumer group assignment")
        val assignment = assignPartitions(
            partitions = listOf(0, 1, 2),
            consumers = listOf("inventory-1", "inventory-2"),
        )
        Console.result("assignment", assignment)
    }

    private fun duplicateDelivery() {
        Console.section("Duplicate delivery after crash before commit")
        val record = DemoRecord(eventId = "evt-900", key = "order-77", payload = "reserve-stock")

        val naive = NaiveConsumer()
        naive.handle(record, commitOffset = false)
        naive.handle(record, commitOffset = true)
        Console.result("naive business actions", naive.businessActions)

        val idempotent = IdempotentConsumer()
        idempotent.handle(record, commitOffset = false)
        idempotent.handle(record, commitOffset = true)
        Console.result("idempotent business actions", idempotent.businessActions)
        Console.result("idempotent committed offset", idempotent.committedOffset)
    }

    private fun deadLetterFlow() {
        Console.section("Retry then DLT")
        val processor = RetryThenDltProcessor(maxAttempts = 3)
        val goodRecord = DemoRecord(eventId = "evt-1000", key = "order-88", payload = "ship-order")
        val poisonRecord = DemoRecord(eventId = "evt-1001", key = "order-89", payload = "bad-state")

        processor.handle(goodRecord) { "processed ${it.payload}" }
        processor.handle(poisonRecord) { record ->
            if (record.payload == "bad-state") {
                throw IllegalStateException("invalid downstream state")
            }
            "processed ${record.payload}"
        }

        Console.result("successful actions", processor.successfulActions)
        Console.result("dead-lettered events", processor.deadLettered.map { it.eventId })
    }
}

private data class DemoRecord(
    val eventId: String,
    val key: String,
    val payload: String,
)

private class DemoTopic(
    private val partitions: Int,
) {
    fun partitionFor(key: String): Int = key.hashCode().absoluteValue % partitions
}

private fun assignPartitions(
    partitions: List<Int>,
    consumers: List<String>,
): Map<String, List<Int>> {
    if (consumers.isEmpty()) {
        return emptyMap()
    }

    val assignment = consumers.associateWith { mutableListOf<Int>() }
    partitions.forEachIndexed { index, partition ->
        assignment.getValue(consumers[index % consumers.size]).add(partition)
    }
    return assignment.mapValues { (_, ownedPartitions) -> ownedPartitions.toList() }
}

private class NaiveConsumer {
    val businessActions = mutableListOf<String>()
    var committedOffset: Long = -1
        private set

    fun handle(record: DemoRecord, commitOffset: Boolean) {
        businessActions += "processed ${record.payload}"
        if (commitOffset) {
            committedOffset += 1
        }
    }
}

private class IdempotentConsumer {
    private val processedEventIds = linkedSetOf<String>()
    val businessActions = mutableListOf<String>()
    var committedOffset: Long = -1
        private set

    fun handle(record: DemoRecord, commitOffset: Boolean) {
        if (processedEventIds.add(record.eventId)) {
            businessActions += "processed ${record.payload}"
        }

        if (commitOffset) {
            committedOffset += 1
        }
    }
}

private class RetryThenDltProcessor(
    private val maxAttempts: Int,
) {
    val successfulActions = mutableListOf<String>()
    val deadLettered = mutableListOf<DemoRecord>()

    fun handle(
        record: DemoRecord,
        action: (DemoRecord) -> String,
    ) {
        repeat(maxAttempts) { attempt ->
            try {
                successfulActions += action(record)
                return
            } catch (_: IllegalStateException) {
                if (attempt == maxAttempts - 1) {
                    deadLettered += record
                }
            }
        }
    }
}
