package school;

public class Teacher extends Person implements Payable, Reportable{
    private String subject;
    private double baseSalary;

    public Teacher (String name, int age, String subject, double baseSalary)
    {
        super(name, age);
        this.subject = subject;
        this.baseSalary = baseSalary;
    }

    public String getSubject()
    {
        return subject;
    }

    public double getBaseSalary()
    {
        return baseSalary;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public void setBaseSalary(double set_baseSalary)
    {
        if (set_baseSalary < 0)
        {
            System.out.println("BaseSalary must be greater than or equal 0");
            return;
        }
        this.baseSalary = set_baseSalary;
    }

    public String getRole()
    {
        return "Teacher";
    }

    public double calculateSalary()
    {
        return this.baseSalary;
    }

    public String generateReport()
    {
        return (baseInfo() + " | Subject: " + this.subject + " | Salary: " + calculateSalary());
    }



}
