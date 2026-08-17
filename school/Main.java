package school;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main (String[] args)
    {
        List<Person> persons = new ArrayList<Person>();
        persons.add(new Student("Person 1", 1, "Major 1", 1 ));
        persons.add(new Teacher("Person 2", 2, "Subject 2", 2));
        persons.add(new Principal("Person 3", 3, "Subject 3", 3, 3));
        for (Person p : persons)
        {
            System.out.println(p);
        }

        List<Payable> payables = new ArrayList<Payable>();
        payables.add(new Teacher("Person 4", 4, "Subject 4", 4));
        payables.add(new Principal("Person 5", 5, "Subject 5", 5, 5));
        for (Payable p : payables)
        {
            System.out.println(p.calculateSalary());
        }

        List<Reportable> reportables = new ArrayList<Reportable>();
        reportables.add(new Student("Person 6", 6, "Major 6", 6 ));
        reportables.add(new Teacher("Person 7", 7, "Subject 7", 7));
        reportables.add(new Principal("Person 8", 8, "Subject 8", 8, 8));
        for (Reportable r : reportables)
        {
            System.out.println(r.generateReport());
        }

        System.out.println(Person.getTotalPersons());

    }
}
