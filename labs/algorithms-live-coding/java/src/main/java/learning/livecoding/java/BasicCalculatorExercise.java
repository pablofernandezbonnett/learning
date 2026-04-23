package learning.livecoding.java;

import java.util.ArrayDeque;
import java.util.Deque;

public final class BasicCalculatorExercise implements Exercise {
    @Override
    public String id() {
        return "basic-calculator";
    }

    @Override
    public String title() {
        return "Basic Calculator";
    }

    @Override
    public String summary() {
        return "Track the current result and sign, then push outer state when you enter parentheses.";
    }

    int calculate(String expression) {
        int result = 0;
        int number = 0;
        int sign = 1;
        Deque<Integer> stack = new ArrayDeque<>();

        for (int index = 0; index < expression.length(); index++) {
            char current = expression.charAt(index);

            if (Character.isDigit(current)) {
                number = number * 10 + (current - '0');
            } else if (current == '+') {
                result += sign * number;
                number = 0;
                sign = 1;
            } else if (current == '-') {
                result += sign * number;
                number = 0;
                sign = -1;
            } else if (current == '(') {
                stack.addLast(result);
                stack.addLast(sign);
                result = 0;
                sign = 1;
            } else if (current == ')') {
                result += sign * number;
                number = 0;
                result *= stack.removeLast();
                result += stack.removeLast();
            }
        }

        return result + sign * number;
    }

    @Override
    public void run() {
        ExerciseSupport.expectEquals("1+1", 2, calculate("1 + 1"));
        ExerciseSupport.expectEquals("2-1+2", 3, calculate(" 2-1 + 2 "));
        ExerciseSupport.expectEquals("nested", 23, calculate("(1+(4+5+2)-3)+(6+8)"));
    }
}
