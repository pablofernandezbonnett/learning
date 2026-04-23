package learning.livecoding.java;

import java.util.ArrayDeque;
import java.util.Deque;

public final class CalculatorIIExercise implements Exercise {
    @Override
    public String id() {
        return "calculator-ii";
    }

    @Override
    public String title() {
        return "Basic Calculator II";
    }

    @Override
    public String summary() {
        return "Parse left to right, commit each number when you hit an operator, and fold multiplication or division immediately.";
    }

    int calculate(String expression) {
        Deque<Integer> stack = new ArrayDeque<>();
        int number = 0;
        char operation = '+';

        for (int index = 0; index < expression.length(); index++) {
            char current = expression.charAt(index);

            if (Character.isDigit(current)) {
                number = number * 10 + (current - '0');
            }

            if ((!Character.isDigit(current) && current != ' ') || index == expression.length() - 1) {
                switch (operation) {
                    case '+' -> stack.addLast(number);
                    case '-' -> stack.addLast(-number);
                    case '*' -> stack.addLast(stack.removeLast() * number);
                    case '/' -> stack.addLast(stack.removeLast() / number);
                    default -> throw new IllegalStateException("Unexpected operator: " + operation);
                }
                operation = current;
                number = 0;
            }
        }

        return stack.stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public void run() {
        ExerciseSupport.expectEquals("3+2*2", 7, calculate("3+2*2"));
        ExerciseSupport.expectEquals("3/2", 1, calculate(" 3/2 "));
        ExerciseSupport.expectEquals("3+5/2", 5, calculate(" 3+5 / 2 "));
        ExerciseSupport.expectEquals("14-3/2", 13, calculate("14-3/2"));
    }
}
