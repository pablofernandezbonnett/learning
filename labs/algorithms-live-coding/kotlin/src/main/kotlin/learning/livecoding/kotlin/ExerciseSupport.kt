package learning.livecoding.kotlin

object ExerciseSupport {
    fun printHeading(exercise: Exercise) {
        println("== ${exercise.id}: ${exercise.title} ==")
        println(exercise.summary)
    }

    fun expectEquals(label: String, expected: Any?, actual: Any?) {
        require(expected == actual) { "$label expected <$expected> but was <$actual>" }
        println("PASS $label -> $actual")
    }

    fun expectDouble(label: String, expected: Double, actual: Double, epsilon: Double = 0.000001) {
        require(kotlin.math.abs(expected - actual) < epsilon) {
            "$label expected <$expected> but was <$actual>"
        }
        println("PASS $label -> $actual")
    }
}
