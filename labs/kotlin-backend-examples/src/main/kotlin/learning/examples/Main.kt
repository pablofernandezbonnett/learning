package learning.examples

import learning.examples.common.Console
import learning.examples.common.TopicRegistry

fun main(args: Array<String>) {
    when (val command = args.firstOrNull() ?: "list") {
        "list", "help", "--help", "-h" -> {
            Console.banner("Kotlin Backend Examples")
            println("Usage:")
            println("  ./run-topic.sh list")
            println("  ./run-topic.sh <topic-id>")
            println("  ./run-topic.sh all")
            println()
            println("Available topics:")
            TopicRegistry.all.forEach { topic ->
                println("  - ${topic.id}: ${topic.title}")
            }
        }

        "all" -> {
            TopicRegistry.all.forEach { topic ->
                Console.banner("${topic.id} :: ${topic.title}")
                Console.docs(topic.sourceDocs)
                topic.run()
            }
        }

        else -> {
            val topic = TopicRegistry.find(command)
            if (topic == null) {
                Console.banner("Unknown topic")
                println("No topic found for: $command")
                println("Run `./run-topic.sh list` to see available topics.")
                return
            }

            Console.banner("${topic.id} :: ${topic.title}")
            Console.docs(topic.sourceDocs)
            topic.run()
        }
    }
}
