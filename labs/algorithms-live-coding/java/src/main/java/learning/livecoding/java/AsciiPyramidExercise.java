package learning.livecoding.java;

public final class AsciiPyramidExercise implements Exercise {
    @Override
    public String id() {
        return "ascii-pyramid";
    }

    @Override
    public String title() {
        return "ASCII Pyramid";
    }

    @Override
    public String summary() {
        return "Nested loops plus spacing arithmetic. Good warm-up for centering logic and off-by-one control.";
    }

    String buildPyramid(int levels) {
        if (levels <= 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int row = 1; row <= levels; row++) {
            if (row > 1) {
                result.append('\n');
            }

            result.append(" ".repeat(levels - row));
            result.append("*".repeat(2 * row - 1));
        }

        return result.toString();
    }

    @Override
    public void run() {
        String expected3 = String.join("\n",
            "  *",
            " ***",
            "*****"
        );
        String expected5 = String.join("\n",
            "    *",
            "   ***",
            "  *****",
            " *******",
            "*********"
        );

        ExerciseSupport.expectEquals("levels 3", expected3, buildPyramid(3));
        ExerciseSupport.expectEquals("levels 5", expected5, buildPyramid(5));
        ExerciseSupport.expectEquals("levels 0", "", buildPyramid(0));

        System.out.println("Pyramid for N = 10");
        System.out.println(buildPyramid(10));
    }
}
