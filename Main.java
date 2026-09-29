import java.util.*;

public class Main {
    public static void main (String[] args)
    {
      int[] input = {0,1,2,3};
      System.out.println(search(input, 10));

    }

    private static int search(int[] nums, int target) {
        int indexOfMinimumValue = findMin(nums);
        int left = 0;
        int right = 0;

        if (nums[indexOfMinimumValue] == target)
        {
            return indexOfMinimumValue;
        }
        else
        {
            if (target >= nums[0])
            {
                left = 0;
                right = indexOfMinimumValue - 1;
            }
            else 
            {
                left = indexOfMinimumValue + 1;
                right = nums.length - 1;
            }
        }

        while (left <= right)
        {
            int mid = (left + right)/2;

            if (nums[mid]==target)
            {
                return mid;
            }
            else if (nums[mid] > target)
            {
                right = mid - 1;
            }
            else
            {
                left = mid + 1;
            }
        }
        return -1;

    }

    private static int findMin(int[] nums)
    {
        int left = 0;
        int right = nums.length - 1;

        while (left <= right)
        {
            int mid = (left+right)/2;

            if (nums[mid]<nums[right])
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }
        return right;
    }

}


// C2: ĐỔi gốc nhìn từ left đên mid thử