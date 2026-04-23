package learning.livecoding.java;

public final class RotatedSearchExercise implements Exercise {
    @Override
    public String id() {
        return "rotated-search";
    }

    @Override
    public String title() {
        return "Search in Rotated Sorted Array";
    }

    @Override
    public String summary() {
        return "Binary search still works if you first detect which half is sorted and only keep the half that can contain the target.";
    }

    int search(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (nums[mid] == target) {
                return mid;
            }

            if (nums[left] <= nums[mid]) {
                if (target >= nums[left] && target < nums[mid]) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            } else {
                if (target > nums[mid] && target <= nums[right]) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }

        return -1;
    }

    @Override
    public void run() {
        ExerciseSupport.expectEquals("rotated-hit", 4, search(new int[] {4, 5, 6, 7, 0, 1, 2}, 0));
        ExerciseSupport.expectEquals("rotated-miss", -1, search(new int[] {4, 5, 6, 7, 0, 1, 2}, 3));
        ExerciseSupport.expectEquals("single-hit", 0, search(new int[] {1}, 1));
        ExerciseSupport.expectEquals("single-miss", -1, search(new int[] {1}, 0));
    }
}
