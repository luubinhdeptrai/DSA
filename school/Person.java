package school;

abstract class Person
{
    private String name;
    private int age;
    private final String id;

    private static int counter = 1;

    public Person (String name, int age)
    {
        this.name = name;
        this.age = age;
        this.id = "P" + counter++;
    }

    public Person (String name)
    {
        this(name, 0);
    }

    public static int getTotalPersons()
    {
        return (counter-1);
    }
    
    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public int getAge()
    {
        return age;
    }

    public void setName (String set_name)
    {
        this.name = set_name;
    }

    public void setAge (int set_age)
    {
        if (set_age < 0)
        {
            System.out.println ("Age must be greater than or equal 0");
            return;
        }
        this.age = set_age;
    }

    protected String baseInfo()
    {
       return ("[" + this.id + "] " + this.name + " (" + this.age + " years old.)");
    }

    public abstract String getRole();

    public String toString()
    {
        return baseInfo() + " - Role: " + getRole();
    }
}