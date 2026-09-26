import java.util.*;

public class Main {
    public static void main (String[] args)
    {
      int[] input = {0,3,2,4,6,1,1};
      System.out.println(checkTarget(input, 1));

    }

    private static boolean checkTarget(int[] nums, int target)
    {
        Arrays.sort(nums);
        int left = 0;
        int right = nums.length - 1;
        int mid;

        while (left <= right)
        {
            mid = (left + right)/2;
            if (nums[mid] == target)
            {
                return true;
            }

            if (nums[mid] < target)
            {
                left = mid + 1;
            }

            else
            {
                right = mid - 1;
            }
        }
        return false;
    }
}
