package byte_stream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Stream {
    public static void main (String[] args)
    {
        String input = "D:/DSA/stream/source.txt";
        String output = "D:/DSA/stream/dest.txt";

        try (
            FileInputStream fis = new FileInputStream(input);
            FileOutputStream fos = new FileOutputStream(output)
        )
        {
            int data;
            while ( (data = fis.read()) != -1)
            {
                fos.write(data);
            }

            System.out.println("Done IO");
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }
}
