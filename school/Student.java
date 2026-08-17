package school;

public class Student extends Person implements Reportable {
    private String major;
    private double GPA;

    public Student(String name, int age, String major, double GPA)
    {
        super(name, age);
        this.major = major;
        this.GPA = GPA;
    }

    public String getMajor()
    {
        return major;
    }

    public double getGPA()
    {
        return GPA;
    }

    public void setMajor (String major)
    {
        this.major = major;
    }

    public void setGPA (double set_GPA)
    {
        if (set_GPA < 0)
        {
            System.out.println ("GPA must be greater than or equal 0");
            return;
        }
        this.GPA = set_GPA;
    }

    public String getRole()
    {
        return "Student";
    }

    public String generateReport()
    {
        return (baseInfo() + " | Major: " + this.major + " | GPA: " + this.GPA);
    }


}
