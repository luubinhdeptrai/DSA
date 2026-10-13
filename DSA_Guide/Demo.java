import java.util.*;


public class Demo {

    public static void main (String[] args)
    {
            int[] nums = {3};
            int[] leftArray = Arrays.copyOfRange(nums, 0, 1);
            int result = Arrays.binarySearch(leftArray, 3);
            System.out.println(result<0 ? -1 : result);
    }
    
}