package com.example;

import com.example.config.DatabaseConfig;
import com.example.dao.StudentDAO;
import com.example.model.Student;
import com.example.service.AccountTransferService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import java.util.List;
import java.util.ArrayList;

public class Main {

    private final Scanner scanner = new Scanner(System.in);
    private final StudentDAO studentDAO = new StudentDAO();
    private final AccountTransferService transferService =
            new AccountTransferService();

    public static void main(String[] args) {
        if (!testConnection()) {
            return;
        }

        new Main().runMenu();
    }

    private static boolean testConnection() {
        try (Connection connection =
                     DatabaseConfig.getConnection()) {
            System.out.println("Connected successfully");
            return true;
        } catch (SQLException e) {
            printSqlException(e);
            return false;
        } catch (RuntimeException e) {
            System.err.println(
                    "Configuration error: " + e.getMessage()
            );
            return false;
        }
    }

    private void runMenu() {
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> addStudent();
                    case "2" -> findStudent();
                    case "3" -> listStudents();
                    case "4" -> updateStudent();
                    case "5" -> deleteStudent();
                    case "6" -> batchInsert();
                    case "7" -> transferMoney();
                    case "0" -> {
                        return;
                    }
                    default ->
                            System.out.println("Unknown option.");
                }
            } catch (SQLException e) {
                printSqlException(e);
            } catch (IllegalArgumentException e) {
                System.err.println(
                        "Invalid input: " + e.getMessage()
                );
            }
        }
    }

    private void addStudent() throws SQLException {
        Student student = readStudentWithoutId();
        long id = studentDAO.create(student);
        System.out.println("Created student with ID " + id);
    }

    private void findStudent() throws SQLException {
        Student student =
                studentDAO.findById(readLong("ID: "));

        System.out.println(
                student == null
                        ? "Student not found."
                        : student
        );
    }

    private void listStudents() throws SQLException {
        List<Student> students = studentDAO.findAll();

        if (students.isEmpty()) {
            System.out.println("No students.");
            return;
        }

        students.forEach(System.out::println);
    }

    private void updateStudent() throws SQLException {
        long id = readLong("ID to update: ");
        Student student = readStudentWithoutId();
        student.setId(id);

        System.out.println(
                studentDAO.update(student)
                        ? "Updated."
                        : "Student not found."
        );
    }

    private void deleteStudent() throws SQLException {
        boolean deleted =
                studentDAO.delete(readLong("ID to delete: "));

        System.out.println(
                deleted ? "Deleted." : "Student not found."
        );
    }

    private void batchInsert() throws SQLException {
        long suffix = System.currentTimeMillis();

        List<Student> students = List.of(
                new Student(
                        "Batch A",
                        "batch.a." + suffix + "@example.com",
                        20
                ),
                new Student(
                        "Batch B",
                        "batch.b." + suffix + "@example.com",
                        21
                ),
                new Student(
                        "Batch C",
                        "batch.c." + suffix + "@example.com",
                        22
                ),
                new Student(
                        "Batch D",
                        "batch.d." + suffix + "@example.com",
                        23
                )
        );

        int[] counts = studentDAO.batchInsert(students);
        System.out.println(
                "Batch completed for "
                        + counts.length + " statements."
        );
    }

    private void transferMoney() throws SQLException {
        long from = readLong("From account ID: ");
        long to = readLong("To account ID: ");
        BigDecimal amount =
                new BigDecimal(readRequired("Amount: "));

        System.out.println(
                "Before: from="
                        + transferService.findBalance(from)
                        + ", to="
                        + transferService.findBalance(to)
        );

        transferService.transfer(from, to, amount);

        System.out.println("Committed.");
        System.out.println(
                "After: from="
                        + transferService.findBalance(from)
                        + ", to="
                        + transferService.findBalance(to)
        );
    }

    private Student readStudentWithoutId() {
        String name = readRequired("Name: ");
        String email = readRequired("Email: ");
        int age = Integer.parseInt(readRequired("Age: "));

        return new Student(name, email, age);
    }

    private long readLong(String prompt) {
        return Long.parseLong(readRequired(prompt));
    }

    private String readRequired(String prompt) {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Value cannot be blank"
            );
        }

        return value;
    }

    private static void printSqlException(SQLException e) {
        for (SQLException current = e;
             current != null;
             current = current.getNextException()) {
            System.err.printf(
                    "Database error: %s | SQLState=%s | code=%d%n",
                    current.getMessage(),
                    current.getSQLState(),
                    current.getErrorCode()
            );
        }
    }

    private static void printMenu() {
        System.out.println("""

                === Student Management ===
                1. Add student
                2. Find student by ID
                3. List students
                4. Update student
                5. Delete student
                6. Batch insert sample students
                7. Transfer money (transaction exercise)
                0. Exit
                """);

        System.out.print("Choose: ");
    }
}
