import java.util.LinkedList;
import java.util.Collections;

public class Main
{
    public static void main (String[] args)
    {
        LinkedList<String> list = new LinkedList<String>();

        list.add("a");
        list.add("d");
        list.add("b");

        Collections.sort(list, Collections.reverseOrder());

        System.out.println(list);
    }
}