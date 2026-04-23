package learning.livecoding.kotlin

object AsciiPyramidExercise : Exercise {
    override val id = "ascii-pyramid"
    override val title = "ASCII Pyramid"
    override val summary = "Nested loops plus spacing arithmetic. Good warm-up for centering logic and off-by-one control."

    fun buildPyramid(levels: Int): String {
        if (levels <= 0) return ""

        return (1..levels).joinToString("\n") { row ->
            " ".repeat(levels - row) + "*".repeat(2 * row - 1)
        }
    }

    override fun run() {
        val expected3 = listOf(
            "  *",
            " ***",
            "*****",
        ).joinToString("\n")
        val expected5 = listOf(
            "    *",
            "   ***",
            "  *****",
            " *******",
            "*********",
        ).joinToString("\n")

        ExerciseSupport.expectEquals("levels 3", expected3, buildPyramid(3))
        ExerciseSupport.expectEquals("levels 5", expected5, buildPyramid(5))
        ExerciseSupport.expectEquals("levels 0", "", buildPyramid(0))

        println("Pyramid for N = 10")
        println(buildPyramid(10))
    }
}
