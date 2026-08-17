package school;

public class Principal extends Teacher implements Payable, Reportable {
    private double bonus;

    public Principal (String name, int age, String subject, double baseSalary, double bonus)
    {
        super (name, age, subject, baseSalary);
        this.bonus = bonus;
    }

    public Principal (String name, int age, String subject, double baseSalary)
    {
        this(name, age, subject, baseSalary, 0);
    }

    public String getRole()
    {
        return "Principal";
    }

    public double calculateSalary()
    {
        return getBaseSalary() + bonus;
    }

    
}
