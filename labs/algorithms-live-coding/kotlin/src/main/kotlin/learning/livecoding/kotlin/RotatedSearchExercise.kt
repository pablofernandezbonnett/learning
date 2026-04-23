package learning.livecoding.kotlin

object RotatedSearchExercise : Exercise {
    override val id = "rotated-search"
    override val title = "Search in Rotated Sorted Array"
    override val summary = "Binary search still works if you first detect which half is sorted and only keep the half that can contain the target."

    fun search(nums: IntArray, target: Int): Int {
        var left = 0
        var right = nums.lastIndex

        while (left <= right) {
            val mid = left + (right - left) / 2
            if (nums[mid] == target) return mid

            if (nums[left] <= nums[mid]) {
                // Left half is sorted.
                if (target >= nums[left] && target < nums[mid]) {
                    right = mid - 1
                } else {
                    left = mid + 1
                }
            } else {
                // Right half is sorted.
                if (target > nums[mid] && target <= nums[right]) {
                    left = mid + 1
                } else {
                    right = mid - 1
                }
            }
        }

        return -1
    }

    override fun run() {
        ExerciseSupport.expectEquals("rotated-hit", 4, search(intArrayOf(4, 5, 6, 7, 0, 1, 2), 0))
        ExerciseSupport.expectEquals("rotated-miss", -1, search(intArrayOf(4, 5, 6, 7, 0, 1, 2), 3))
        ExerciseSupport.expectEquals("single-hit", 0, search(intArrayOf(1), 1))
        ExerciseSupport.expectEquals("single-miss", -1, search(intArrayOf(1), 0))
    }
}
