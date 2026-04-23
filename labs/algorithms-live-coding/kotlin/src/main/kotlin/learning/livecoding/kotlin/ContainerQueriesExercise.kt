package learning.livecoding.kotlin

object ContainerQueriesExercise : Exercise {
    override val id = "container-queries"
    override val title = "Container Queries with ADD / EXISTS / REMOVE"
    override val summary =
        "Use a hash set for presence-only queries, but switch to a frequency map once REMOVE must preserve duplicates."

    fun solution(queries: List<List<String>>): List<String> {
        val counts = mutableMapOf<String, Int>()
        val results = MutableList(queries.size) { "" }

        for ((index, query) in queries.withIndex()) {
            val operation = query[0]
            val value = query[1]

            when (operation) {
                "ADD" -> {
                    counts[value] = (counts[value] ?: 0) + 1
                    results[index] = ""
                }
                "EXISTS" -> {
                    results[index] = if ((counts[value] ?: 0) > 0) "true" else "false"
                }
                "REMOVE" -> {
                    val current = counts[value] ?: 0
                    results[index] = if (current == 0) {
                        "false"
                    } else {
                        if (current == 1) counts.remove(value) else counts[value] = current - 1
                        "true"
                    }
                }
            }
        }

        return results
    }

    override fun run() {
        val queries = listOf(
            listOf("ADD", "1"),
            listOf("ADD", "2"),
            listOf("ADD", "2"),
            listOf("ADD", "3"),
            listOf("EXISTS", "1"),
            listOf("EXISTS", "2"),
            listOf("EXISTS", "3"),
            listOf("REMOVE", "2"),
            listOf("REMOVE", "1"),
            listOf("EXISTS", "2"),
            listOf("EXISTS", "1"),
        )

        ExerciseSupport.expectEquals(
            "sample",
            listOf("", "", "", "", "true", "true", "true", "true", "true", "true", "false"),
            solution(queries),
        )
    }
}
