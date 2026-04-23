package learning.livecoding.kotlin

import java.util.ArrayDeque

object CalculatorIIExercise : Exercise {
    override val id = "calculator-ii"
    override val title = "Basic Calculator II"
    override val summary = "Parse left to right, commit each number when you hit an operator, and fold multiplication or division immediately."

    fun calculate(expression: String): Int {
        val stack = ArrayDeque<Int>()
        var number = 0
        var operation = '+'

        for (index in expression.indices) {
            val char = expression[index]

            if (char.isDigit()) {
                number = number * 10 + (char - '0')
            }

            if ((!char.isDigit() && char != ' ') || index == expression.lastIndex) {
                when (operation) {
                    '+' -> stack.addLast(number)
                    '-' -> stack.addLast(-number)
                    '*' -> stack.addLast(stack.removeLast() * number)
                    '/' -> stack.addLast(stack.removeLast() / number)
                }

                operation = char
                number = 0
            }
        }

        return stack.sum()
    }

    override fun run() {
        ExerciseSupport.expectEquals("3+2*2", 7, calculate("3+2*2"))
        ExerciseSupport.expectEquals("3/2", 1, calculate(" 3/2 "))
        ExerciseSupport.expectEquals("3+5/2", 5, calculate(" 3+5 / 2 "))
        ExerciseSupport.expectEquals("14-3/2", 13, calculate("14-3/2"))
    }
}
