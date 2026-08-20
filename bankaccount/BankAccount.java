package bankaccount;

public class BankAccount {
   
    private double balance;

    public BankAccount(double balance)
    {
        this.balance = balance;
    }

    public double getBalance()
    {
        return balance;
    }

    public void deposit (double amount)
    {
        validateAmount(amount);

        balance += amount;
    }

    public void withdraw (double amount) throws InsufficientBalanceException
    {
        validateAmount(amount);
        if (amount > this.balance)
        {
            throw new InsufficientBalanceException("Not enough BALANCE");
        }

        balance -= amount;
    }

    private void validateAmount (double amount)
    {
        if (amount <= 0)
        {
            throw new IllegalArgumentException("Amount must not be less than or equal 0");
        }
    }
}
