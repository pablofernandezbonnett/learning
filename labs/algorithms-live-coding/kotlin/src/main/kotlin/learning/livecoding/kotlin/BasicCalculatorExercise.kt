package learning.livecoding.kotlin

import java.util.ArrayDeque

object BasicCalculatorExercise : Exercise {
    override val id = "basic-calculator"
    override val title = "Basic Calculator"
    override val summary = "Track the current result and sign, then push outer state when you enter parentheses."

    fun calculate(expression: String): Int {
        var result = 0
        var number = 0
        var sign = 1
        val stack = ArrayDeque<Int>()

        for (char in expression) {
            when {
                char.isDigit() -> number = number * 10 + (char - '0')
                char == '+' -> {
                    result += sign * number
                    number = 0
                    sign = 1
                }
                char == '-' -> {
                    result += sign * number
                    number = 0
                    sign = -1
                }
                char == '(' -> {
                    stack.addLast(result)
                    stack.addLast(sign)
                    result = 0
                    sign = 1
                }
                char == ')' -> {
                    result += sign * number
                    number = 0
                    result *= stack.removeLast()
                    result += stack.removeLast()
                }
            }
        }

        return result + sign * number
    }

    override fun run() {
        ExerciseSupport.expectEquals("1+1", 2, calculate("1 + 1"))
        ExerciseSupport.expectEquals("2-1+2", 3, calculate(" 2-1 + 2 "))
        ExerciseSupport.expectEquals("nested", 23, calculate("(1+(4+5+2)-3)+(6+8)"))
    }
}
