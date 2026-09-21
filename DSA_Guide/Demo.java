import java.util.*;


public class Demo {

    public static void main (String[] args)
    {
        // int[] arr = {3,1,6};

        // System.out.println(arr[2]);
        // arr[2] = 10;
        // System.out.println(arr[2]);

        List<Integer> list = new ArrayList<>();
        list.add(2);
        list.add(5);
        System.out.println(list.get(1));
        list.set(1, 3);
        System.out.println(list.get(1));
        list.remove(0);
        System.out.println(list.get(0));
    }
    
}
