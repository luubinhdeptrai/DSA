package bankaccount;


public class TransactionParser {
    
    public static double parseAmount (String input)
    {
        try
        {
            double output = Double.parseDouble(input);
            System.out.println("Parse money SUCCESSFULLY!");
            return output;
        }
        catch (NumberFormatException e)
        {
            System.out.println("Parse money UNSUCCESSFULLY. Exception is: " + e.getMessage());
            return 0;
        }
    }
}
