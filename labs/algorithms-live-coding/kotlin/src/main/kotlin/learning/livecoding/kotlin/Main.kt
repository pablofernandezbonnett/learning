package learning.livecoding.kotlin

private val exercises: List<Exercise> = listOf(
    AsciiPyramidExercise,
    ContainerQueriesExercise,
    LongestSubstringExercise,
    TopKFrequentExercise,
    NumberOfIslandsExercise,
    LruCacheExercise,
    RateLimiterExercise,
    WalletLedgerExercise,
    CacheAsideExercise,
    EventDedupExercise,
    RetryBackoffExercise,
    PaginationMergeExercise,
    ProducerConsumerExercise,
    RotatedSearchExercise,
    MedianStreamExercise,
    CalculatorIIExercise,
    CoinChangeExercise,
    BasicCalculatorExercise,
    PaymentPriorityExercise,
)

fun main(args: Array<String>) {
    when (val command = args.firstOrNull() ?: "list") {
        "list" -> listExercises()
        "all" -> exercises.forEach(::runExercise)
        else -> {
            val exercise = exercises.firstOrNull { it.id == command }
                ?: error("Unknown exercise: $command")
            runExercise(exercise)
        }
    }
}

private fun listExercises() {
    println("Available Kotlin exercises:")
    exercises.forEach { println("- ${it.id}: ${it.title}") }
}

private fun runExercise(exercise: Exercise) {
    ExerciseSupport.printHeading(exercise)
    exercise.run()
    println()
}
