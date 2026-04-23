package learning.examples.common

import learning.examples.algorithms.AlgorithmsDrillsTopic
import learning.examples.algorithms.AlgorithmsPatternsTopic
import learning.examples.correctness.IdempotencyTopic
import learning.examples.correctness.LockingTopic
import learning.examples.data.CacheTopic
import learning.examples.integration.AsyncBoundariesTopic
import learning.examples.jvm.JvmConcurrencyTopic
import learning.examples.jvm.JvmModelingTopic
import learning.examples.quality.CleanCodeTopic

object TopicRegistry {
    val all: List<Topic> = listOf(
        AlgorithmsPatternsTopic,
        AlgorithmsDrillsTopic,
        IdempotencyTopic,
        LockingTopic,
        AsyncBoundariesTopic,
        CacheTopic,
        JvmConcurrencyTopic,
        JvmModelingTopic,
        CleanCodeTopic,
    )

    fun find(id: String): Topic? = all.find { it.id == id }
}
