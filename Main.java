import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    public static void main (String[] args)
    {
        Scanner input = new Scanner(System.in);

        ArrayList<Integer> nums = new ArrayList<Integer>();

        int n = input.nextInt();

        for (int i=0; i<n; i++)
        {
            int num = input.nextInt();
            nums.add(num);
        }

        if (n==0)
        {
            System.out.println("No element");
        }
        else if (n==1)
        {
            System.out.println(nums.get(0));
        }
        else
        {
            int max = nums.get(0);
            for (int i=1; i<n; i++)
            {
                if (max < nums.get(i))
                {
                    max = nums.get(i);
                }
            }
            System.out.println(max);
        }

    

    }
}
