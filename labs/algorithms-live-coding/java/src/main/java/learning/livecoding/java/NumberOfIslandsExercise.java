package learning.livecoding.java;

public final class NumberOfIslandsExercise implements Exercise {
    @Override
    public String id() {
        return "number-of-islands";
    }

    @Override
    public String title() {
        return "Number of Islands";
    }

    @Override
    public String summary() {
        return "Grid DFS. Treat each unvisited land cell as the start of one component, then flood-fill it.";
    }

    int countIslands(char[][] grid) {
        int islands = 0;

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == '1') {
                    islands++;
                    sink(grid, row, col);
                }
            }
        }

        return islands;
    }

    private void sink(char[][] grid, int row, int col) {
        if (row < 0 || row >= grid.length || col < 0 || col >= grid[row].length || grid[row][col] != '1') {
            return;
        }

        grid[row][col] = '0';
        sink(grid, row + 1, col);
        sink(grid, row - 1, col);
        sink(grid, row, col + 1);
        sink(grid, row, col - 1);
    }

    @Override
    public void run() {
        char[][] first = {
            {'1', '1', '0', '0', '0'},
            {'1', '1', '0', '0', '0'},
            {'0', '0', '1', '0', '0'},
            {'0', '0', '0', '1', '1'}
        };
        char[][] second = {
            {'1', '1', '1'},
            {'0', '1', '0'},
            {'1', '1', '1'}
        };

        ExerciseSupport.expectEquals("three-islands", 3, countIslands(first));
        ExerciseSupport.expectEquals("single-island", 1, countIslands(second));
    }
}
