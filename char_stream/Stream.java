package char_stream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;

public class Stream {

    public static void main (String[] args)
    {
        String input = "source.txt";
        String output = "dest.txt";

        try (
            BufferedReader br = new BufferedReader (new FileReader(input));
            BufferedWriter bw = new BufferedWriter (new FileWriter(output)))
            {
                String line;
                int index = 1;

                while ( (line = br.readLine()) != null )
                {
                    bw.write(index + "." + line);
                    bw.newLine();
                    index++;
                }

                System.out.println("Done IO");

            }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }
}
