# Java OOP Review Exercise

## 1. Exercise Overview

This is a small **School Management System**. You will model people at a school — students, teachers, and a principal — using classes, inheritance, interfaces, and polymorphism.

The goal is not to solve a hard problem. The goal is to **write enough Java by hand** that every OOP keyword you've just learned (`extends`, `implements`, `this`, `super`, `abstract`, `static`, `final`, ...) passes through your fingers at least once.

Estimated time: 30–60 minutes.

## 2. OOP Concepts You Will Review

- Classes and objects
- Attributes/fields
- Methods
- Constructors (including overloading)
- `this`
- Access modifiers (`private`, `protected`, `public`)
- Getters and setters
- Encapsulation
- Inheritance (including multilevel: `Person → Teacher → Principal`)
- Method overriding
- Method overloading
- Polymorphism
- Abstraction
- Abstract classes
- Interfaces
- `static`
- `final`
- `super` (constructor chaining and method calls)
- Basic use of packages

## 3. Scenario

A school wants a simple program to keep track of its people. Every person at the school has a name and an age. Some people are **students**, some are **teachers**, and one of those teachers is also the **principal**, who additionally manages the school and earns a bonus on top of a teacher's salary.

The school also wants two things any "person-like" object might support, independent of what kind of person it is:

- Some people can be **paid** (teachers and the principal).
- Some people can **generate a report** about themselves (students and teachers).

## 4. Class / Interface Design

```text
                    Person  (abstract class)
                       ↑
        ┌──────────────┴──────────────┐
     Student                       Teacher
                                       ↑
                                   Principal


        Payable  (interface)              Reportable  (interface)
              ↑                                  ↑
     Teacher, Principal                Student, Teacher, Principal
```

| Type | Represents |
|---|---|
| `Person` | Abstract base class. Anything every person has: an id, a name, an age. Cannot be instantiated directly — you can never have a bare "Person," only a specific kind of one. |
| `Student` | A person enrolled at the school. Has a major and a GPA. |
| `Teacher` | A person who works at the school. Has a subject and a base salary. Can be paid, can generate a report. |
| `Principal` | A special kind of `Teacher` who also manages the school and earns a bonus. |
| `Payable` | Interface for "this object can compute a salary." |
| `Reportable` | Interface for "this object can describe itself as a report." |

All classes live in a package named `school`.

## 5. Requirements

1. All classes/interfaces belong to package `school`.
2. `Person` is `abstract` and cannot be instantiated on its own.
3. Every `Person` has a unique, auto-generated, immutable id (hint: `final`, and a `static` counter).
4. All fields are `private`, accessed only through getters/setters (except where a `protected` helper method is explicitly asked for).
5. `Student` and `Teacher` both extend `Person`.
6. `Principal` extends `Teacher` (not `Person` directly).
7. `Payable` is implemented by `Teacher` and effectively by `Principal` too (through inheritance + override).
8. `Reportable` is implemented by `Student` and `Teacher` (and inherited/extended by `Principal`).
9. `Main.java` must demonstrate **polymorphism at least three separate ways**: through a `List<Person>`, a `List<Payable>`, and a `List<Reportable>`.
10. The program must compile and run, producing output close to the example in Section 7.

## 6. Tasks

### Task 1 — Package and the `Person` class skeleton

Create the `school` package and the `Person` class inside it. Give it `private` fields for `name` (`String`) and `age` (`int`), plus a `private final String id`.

Write **two constructors**:
- `Person(String name, int age)`
- `Person(String name)` — should reuse the first constructor with a default age of `0` via `this(...)`.

**Concepts practiced:** classes, fields, access modifiers, constructors, constructor overloading, `this`, packages.

### Task 2 — The static id counter

Add a `private static int` counter to `Person`. Each time a `Person` is constructed, increment the counter and use it to build the id (e.g. `"P" + counter`), then assign it to the `final` field.

Add a `public static int getTotalPersons()` method that returns the current counter value.

**Concepts practiced:** `static` fields and methods, `final` fields, the difference between an instance field and a class-level field.

**Expected behavior:** after creating 3 `Person`-family objects anywhere in the program, `Person.getTotalPersons()` should return `3`, even though you never called it directly on any single object.

### Task 3 — Getters, setters, and a protected helper

Add public getters for `id`, `name`, `age`. Add a setter for `name` and a setter for `age` that **rejects negative ages** (simply ignore the update if `age < 0`).

Add one `protected` method, `baseInfo()`, returning a formatted string like `[P1] Alice (20 years old)`. This method should **not** be `public` — subclasses need it, outside code doesn't.

**Concepts practiced:** encapsulation, getters/setters, `protected` access, defensive setters.

### Task 4 — Abstraction: the abstract role

Add one `abstract` method to `Person`:

```java
public abstract String getRole();
```

Then override `toString()` in `Person` itself so it returns something like `baseInfo() + " - Role: " + getRole()`.

Think carefully about this one: `Person` calls `getRole()`, but `Person` has no implementation of it. Whose implementation actually runs when a `Student` object's `toString()` is called? Write down your prediction before you move on — you'll test it in Task 8.

**Concepts practiced:** abstraction, abstract methods, overriding `toString()`, and a preview of dynamic dispatch.

### Task 5 — Create `Student`

`Student extends Person`. Add `private String major` and `private double gpa`, a constructor that takes `(name, age, major, gpa)` and calls `super(name, age)`, plus getters/setters. Override `getRole()` to return `"Student"`.

**Concepts practiced:** inheritance, `super(...)` constructor chaining, overriding.

### Task 6 — Create `Teacher`

`Teacher extends Person`. Add `private String subject` and `private double baseSalary`, a constructor `(name, age, subject, baseSalary)` calling `super(name, age)`, plus getters/setters. Override `getRole()` to return `"Teacher"`.

Leave `Teacher` implementing nothing yet — that's the next task.

**Concepts practiced:** inheritance, `super(...)`, overriding.

### Task 7 — The two interfaces

Create:

```java
public interface Payable {
    double calculateSalary();
}
```

```java
public interface Reportable {
    String generateReport();
}
```

Make `Student implements Reportable` (its report should include major and GPA).

Make `Teacher implements Payable, Reportable` — `calculateSalary()` should just return `baseSalary`; `generateReport()` should include subject and the result of `calculateSalary()`.

**Concepts practiced:** interfaces, multiple interface implementation, abstraction (a `Payable` reference doesn't need to know *what* it is, only that it can be paid).

### Task 8 — Create `Principal`

`Principal extends Teacher`. Add `private double bonus`.

Write **two constructors**: one that takes `(name, age, subject, baseSalary, bonus)`, and a second, overloaded one that takes only `(name, age, subject, baseSalary)` and delegates to the first with a sensible default bonus (`this(...)`). The first constructor should call `super(name, age, subject, baseSalary)`.

Override `getRole()` to return `"Principal"`.

Override `calculateSalary()` so it returns the teacher's base salary **plus** the bonus — without repeating the base-salary logic. (Hint: you have access to the parent class's version of this method through a specific keyword.)

Optionally override `generateReport()` too, building on top of the parent version the same way.

**Concepts practiced:** multilevel inheritance, constructor overloading, `super.method()` calls (not just `super(...)` constructors), overriding across more than one level.

### Task 9 — `Main.java`: put it all together

In `Main`, create a mix of `Student`, `Teacher`, and `Principal` objects, then:

1. Put several of them into a `List<Person>` and loop over it printing each one — this exercises `toString()` through a `Person`-typed reference.
2. Put the `Payable`-capable ones into a `List<Payable>` and loop over it printing `calculateSalary()`.
3. Put the `Reportable`-capable ones into a `List<Reportable>` and loop over it printing `generateReport()`.
4. Print `Person.getTotalPersons()` at the end.

**Concepts practiced:** polymorphism (three different ways — through a superclass and through two interfaces), `ArrayList`/`List`, `static` method access.

Was your Task 4 prediction correct? A `Student` stored in a `List<Person>` should still print `Role: Student`, not something generic — that's polymorphism: the reference type is `Person`, but the method that actually runs is chosen based on the object's real type at runtime.

## 7. Expected Output

Your exact object values may differ, but running `Main` with the sample data below should look like this:

```text
=== All People (Polymorphism via Person) ===
[P1] Alice (20 years old) - Role: Student
[P2] Mr. Brown (40 years old) - Role: Teacher
[P3] Dr. Smith (50 years old) - Role: Principal

=== Payroll (Polymorphism via Payable) ===
Payment: 2800.0
Payment: 4500.0

=== Reports (Polymorphism via Reportable) ===
[P6] Bob (22 years old) | Major: Physics | GPA: 3.5
[P7] Mr. Brown (40 years old) | Subject: Mathematics | Salary: 3000.0

Total people created: 7
```

(The ids `P1`...`P7` depend on the order you construct objects in — 7 total objects are created across all three lists in the sample `Main` in Section 10.)

## 8. Hints

- `Person` should never appear after `new` in your code. If it does, something is designed wrong.
- The static counter belongs to the **class**, not to any one object — think about where in the constructor you increment it, and why it has to be `static` for `getTotalPersons()` to make sense without an object.
- `Principal`'s constructor chain is three levels deep: `Principal(...)` → `super(...)` in `Teacher` → `super(...)` in `Person`. Trace it on paper before you write it.
- `calculateSalary()` in `Principal` should call `super.calculateSalary()`, not re-read `baseSalary` itself — that's the difference between reusing a parent's behavior and duplicating it.
- A class can `implement` more than one interface, separated by commas — you don't need a separate class per interface.
- If you store a `Student` in a variable typed `Reportable`, you can only call `generateReport()` on it through that variable — not `getMajor()`. That restriction is the whole point of coding to an interface.
- `baseInfo()` being `protected` (not `public`) means `Main` can't call it directly on a `Person` — only subclasses (and code in the same package) can. If your `Main` needs that string, it should go through `toString()` or `generateReport()` instead.

## 9. Self-Review Checklist

- [ ] Did I use constructors correctly?
- [ ] Did I use `this` correctly (both `this.field = ...` and `this(...)` chaining)?
- [ ] Did I apply encapsulation (private fields, controlled access)?
- [ ] Do I understand why `Student` and `Teacher` extend `Person`, and why `Principal` extends `Teacher` specifically (not `Person`)?
- [ ] Did I override a method (and use `@Override`)?
- [ ] Did I use polymorphism (a supertype/interface reference pointing at a subtype object)?
- [ ] Did I use an abstract class and an interface correctly, and can I explain the difference in my own words?
- [ ] Do I understand the difference between abstraction and encapsulation?
- [ ] Do I understand why `getTotalPersons()` is `static` but `getName()` is not?
- [ ] Did I call `super.calculateSalary()` instead of duplicating `Teacher`'s logic inside `Principal`?

---

## 10. Solution — Do Not Read Until You Finish

Six files, all in package `school`. Put them in a folder named `school/` and compile/run from the folder above it (`javac school/*.java && java school.Main`), or adjust for however your IDE organizes source roots.

### `Payable.java`

```java
package school;

public interface Payable {
    double calculateSalary();
}
```

### `Reportable.java`

```java
package school;

public interface Reportable {
    String generateReport();
}
```

### `Person.java`

```java
package school;

public abstract class Person {

    private static int counter = 0;

    private final String id;
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
        counter++;
        this.id = "P" + counter;
    }

    public Person(String name) {
        this(name, 0);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age >= 0) {
            this.age = age;
        }
    }

    public static int getTotalPersons() {
        return counter;
    }

    public abstract String getRole();

    protected String baseInfo() {
        return "[" + id + "] " + name + " (" + age + " years old)";
    }

    @Override
    public String toString() {
        return baseInfo() + " - Role: " + getRole();
    }
}
```

### `Student.java`

```java
package school;

public class Student extends Person implements Reportable {

    private String major;
    private double gpa;

    public Student(String name, int age, String major, double gpa) {
        super(name, age);
        this.major = major;
        this.gpa = gpa;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        if (gpa >= 0.0 && gpa <= 4.0) {
            this.gpa = gpa;
        }
    }

    @Override
    public String getRole() {
        return "Student";
    }

    @Override
    public String generateReport() {
        return baseInfo() + " | Major: " + major + " | GPA: " + gpa;
    }
}
```

### `Teacher.java`

```java
package school;

public class Teacher extends Person implements Payable, Reportable {

    private String subject;
    private double baseSalary;

    public Teacher(String name, int age, String subject, double baseSalary) {
        super(name, age);
        this.subject = subject;
        this.baseSalary = baseSalary;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        if (baseSalary >= 0) {
            this.baseSalary = baseSalary;
        }
    }

    @Override
    public String getRole() {
        return "Teacher";
    }

    @Override
    public double calculateSalary() {
        return baseSalary;
    }

    @Override
    public String generateReport() {
        return baseInfo() + " | Subject: " + subject + " | Salary: " + calculateSalary();
    }
}
```

### `Principal.java`

```java
package school;

public class Principal extends Teacher {

    private static final double DEFAULT_BONUS = 500.0;

    private double bonus;

    public Principal(String name, int age, String subject, double baseSalary, double bonus) {
        super(name, age, subject, baseSalary);
        this.bonus = bonus;
    }

    public Principal(String name, int age, String subject, double baseSalary) {
        this(name, age, subject, baseSalary, DEFAULT_BONUS);
    }

    public double getBonus() {
        return bonus;
    }

    public void setBonus(double bonus) {
        if (bonus >= 0) {
            this.bonus = bonus;
        }
    }

    @Override
    public String getRole() {
        return "Principal";
    }

    @Override
    public double calculateSalary() {
        return super.calculateSalary() + bonus;
    }

    @Override
    public String generateReport() {
        return super.generateReport() + " | Bonus: " + bonus + " | Total Pay: " + calculateSalary();
    }
}
```

### `Main.java`

```java
package school;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        List<Person> people = new ArrayList<>();
        people.add(new Student("Alice", 20, "Computer Science", 3.8));
        people.add(new Teacher("Mr. Brown", 40, "Mathematics", 3000.0));
        people.add(new Principal("Dr. Smith", 50, "Administration", 4000.0, 800.0));

        System.out.println("=== All People (Polymorphism via Person) ===");
        for (Person p : people) {
            System.out.println(p);
        }

        System.out.println();
        System.out.println("=== Payroll (Polymorphism via Payable) ===");
        List<Payable> payables = new ArrayList<>();
        payables.add(new Teacher("Ms. Davis", 35, "Science", 2800.0));
        payables.add(new Principal("Dr. Smith", 50, "Administration", 4000.0));
        for (Payable p : payables) {
            System.out.println("Payment: " + p.calculateSalary());
        }

        System.out.println();
        System.out.println("=== Reports (Polymorphism via Reportable) ===");
        List<Reportable> reportables = new ArrayList<>();
        reportables.add(new Student("Bob", 22, "Physics", 3.5));
        reportables.add(new Teacher("Mr. Brown", 40, "Mathematics", 3000.0));
        for (Reportable r : reportables) {
            System.out.println(r.generateReport());
        }

        System.out.println();
        System.out.println("Total people created: " + Person.getTotalPersons());
    }
}
```

## 11. Explanation of the Solution

Trace what happens when `Main` runs, top to bottom:

- Each `new Student(...)`, `new Teacher(...)`, or `new Principal(...)` call runs a chain of constructors down to `Person`'s, which is where `id` gets assigned and `counter` gets incremented — every single object created anywhere in `Main` shares that one counter, which is exactly what `static` means: one value per class, not per object.
- `people` is declared as `List<Person>`, but holds a `Student`, a `Teacher`, and a `Principal`. When `System.out.println(p)` runs, Java calls `p.toString()` — and because `toString()` (defined in `Person`) calls `getRole()`, and `getRole()` is `abstract` in `Person`, the JVM looks at the **actual object's class** at runtime to decide which `getRole()` to run. That's why the `Student` prints `Role: Student` even though the variable's declared type is `Person`. This runtime method selection is polymorphism/dynamic dispatch — the single most important idea in this whole exercise.
- The same thing happens again with `List<Payable>` and `List<Reportable>`: the code that loops over them has no idea whether it's holding a `Teacher` or a `Principal`, and doesn't need to — it just trusts that whatever is in the list can do `calculateSalary()` or `generateReport()`.
- `Principal.calculateSalary()` calls `super.calculateSalary()` rather than reading `baseSalary` itself. `super` here means "run `Teacher`'s version of this method, not mine" — letting `Principal` reuse `Teacher`'s logic instead of copying it.

## 12. OOP Concepts Used

```text
Encapsulation
→ private fields in every class, accessed only via getters/setters and defensive setters (setAge, setGpa, setBaseSalary reject invalid values)

Inheritance
→ Student extends Person, Teacher extends Person, Principal extends Teacher (multilevel)

super (constructor)
→ Student/Teacher call super(name, age); Principal calls super(name, age, subject, baseSalary)

super (method call)
→ Principal.calculateSalary() and generateReport() both call the super version and build on top of it

this (field assignment)
→ this.name = name, this.age = age, etc. in every constructor

this (constructor chaining)
→ Person(String name) calls this(name, 0); Principal's 4-arg constructor calls this(..., DEFAULT_BONUS)

Method overloading
→ two Person constructors, two Principal constructors (same name, different parameter lists)

Method overriding
→ getRole(), calculateSalary(), generateReport(), toString() are all overridden in subclasses

Polymorphism
→ List<Person>, List<Payable>, and List<Reportable> in Main all hold mixed subtypes and call the correct overridden method at runtime

Abstraction
→ abstract class Person with an abstract getRole() method — subclasses are forced to define what "role" means for them

Interfaces
→ Payable and Reportable define capabilities independent of the class hierarchy; Teacher implements both, Student implements one

static
→ Person.counter and Person.getTotalPersons() belong to the class, shared across every instance

final
→ Person.id (assigned once, never changed) and Principal.DEFAULT_BONUS (a constant)

Access modifiers
→ private fields everywhere, protected baseInfo() (visible to subclasses only), public constructors/getters/setters/interface methods

Packages
→ every class lives in package school
```
