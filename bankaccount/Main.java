package bankaccount;


public class Main {
    public static void main (String[] args)
    {
        BankAccount ac = new BankAccount(100);

        String[] depositInputs = {"50", "-10", "abc"};
        String[] withdrawInputs = {"30", "500"};

        try
        {
            for (String s : depositInputs)
            {
                double amount = TransactionParser.parseAmount(s);
                ac.deposit(amount);
            }
        }        
        catch (IllegalArgumentException e)
        {
            System.out.println(e.getMessage());
        }

        try 
        {
            for (String s : withdrawInputs)
            {
                double amount = TransactionParser.parseAmount(s);
                ac.withdraw(amount);
            }
        }
        catch (IllegalArgumentException e)
        {
            System.out.println(e.getMessage());
        }
        catch (InsufficientBalanceException e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            System.out.println("Withdrawal attempt finished.");         
        }

        System.out.println(ac.getBalance());
    }
    
}
