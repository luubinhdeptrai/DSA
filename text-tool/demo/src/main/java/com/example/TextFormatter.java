package com.example;

import java.util.Locale;

/**
 * Hello world!
 *
 */
public class TextFormatter 
{

    public TextFormatter()
    {

    }

    public static String shout  (String input) throws IllegalArgumentException
    {
        if (input == null)
        {
            throw new IllegalArgumentException("Input must not be NULL");
        }

        String s = input.trim();
        if (s.length() == 0)
        {
            System.out.println("Input must not be empty");
            return "";
        }

        return s.toUpperCase(Locale.ROOT);
        
    }

    public static void main( String[] args )
    {
        String input = args.length == 0 ? " hello maven " : String.join(" ", args);

        System.out.println(shout(input));

    }
}
