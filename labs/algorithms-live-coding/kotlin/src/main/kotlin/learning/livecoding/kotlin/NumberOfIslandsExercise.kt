package learning.livecoding.kotlin

object NumberOfIslandsExercise : Exercise {
    override val id = "number-of-islands"
    override val title = "Number of Islands"
    override val summary = "Grid DFS. Treat each unvisited land cell as the start of one component, then flood-fill it."

    fun countIslands(grid: Array<CharArray>): Int {
        var islands = 0

        for (row in grid.indices) {
            for (col in grid[row].indices) {
                if (grid[row][col] == '1') {
                    islands++
                    sink(grid, row, col)
                }
            }
        }

        return islands
    }

    private fun sink(grid: Array<CharArray>, row: Int, col: Int) {
        if (row !in grid.indices || col !in grid[row].indices || grid[row][col] != '1') {
            return
        }

        grid[row][col] = '0'
        sink(grid, row + 1, col)
        sink(grid, row - 1, col)
        sink(grid, row, col + 1)
        sink(grid, row, col - 1)
    }

    override fun run() {
        val first = arrayOf(
            charArrayOf('1', '1', '0', '0', '0'),
            charArrayOf('1', '1', '0', '0', '0'),
            charArrayOf('0', '0', '1', '0', '0'),
            charArrayOf('0', '0', '0', '1', '1'),
        )
        val second = arrayOf(
            charArrayOf('1', '1', '1'),
            charArrayOf('0', '1', '0'),
            charArrayOf('1', '1', '1'),
        )

        ExerciseSupport.expectEquals("three-islands", 3, countIslands(first))
        ExerciseSupport.expectEquals("single-island", 1, countIslands(second))
    }
}
