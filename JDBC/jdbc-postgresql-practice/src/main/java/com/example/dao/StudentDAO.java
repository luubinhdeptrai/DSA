package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.example.config.DatabaseConfig;
import com.example.model.Student;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO
{
     private static final String INSERT_SQL = """
            INSERT INTO students (name, email, age)
            VALUES (?, ?, ?)
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT id, name, email, age
            FROM students
            WHERE id = ?
            """;

    private static final String FIND_ALL_SQL = """
            SELECT id, name, email, age
            FROM students
            ORDER BY id
            """;

    private static final String UPDATE_SQL = """
            UPDATE students
            SET name = ?, email = ?, age = ?
            WHERE id = ?
            """;

    private static final String DELETE_SQL =
            "DELETE FROM students WHERE id = ?";

    public long create(Student student) throws SQLException
    {
        try (Connection connection = DatabaseConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS))
        {
            bindStudent (statement, student);

            int affected = statement.executeUpdate();

            if (affected != 1)
            {
                throw new SQLException(
                        "Expected one inserted row, got "
                                + affected
                );
            }

            try (ResultSet keys = statement.getGeneratedKeys())
            {
                if (!keys.next())
                {
                     throw new SQLException(
                            "Insert succeeded, but no generated ID "
                                    + "was returned"
                    );
                }

                long id = keys.getLong(1);
                student.setId(id);
                return id;
            }
        }
    }

    public Student findById (long id) throws SQLException
    {
        try (Connection connection = DatabaseConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL))
        {
            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery())
            {
                return (rs.next() ? mapRow(rs) : null);
            }
        } 
    }

    public List<Student> findAll() throws SQLException
    {
        try (Connection connection = DatabaseConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
            ResultSet rs = statement.executeQuery())
        {
            List<Student> list = new ArrayList<>();

            while (rs.next())
            {
                list.add(mapRow(rs));
            }
            return list;
        }
    }

    public boolean update (Student student) throws SQLException
    {
        try (Connection connection = DatabaseConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(UPDATE_SQL))
        {
            bindStudent(statement, student);
            statement.setLong(4, student.getId());

            return statement.executeUpdate() == 1;

        }
    }

    public boolean delete (long id) throws SQLException
    {
        try (Connection connection = DatabaseConfig.getConnection();
            PreparedStatement  statement = connection.prepareStatement(DELETE_SQL))
        {
            statement.setLong(1, id);

            return statement.executeUpdate() == 1;
        }
    }

    public int[] batchInsert (ArrayList<Student> students) throws SQLException
    {
        if (students.isEmpty())
        {
            return new int[0];
        }

        try (Connection connection = DatabaseConfig.getConnection())
        {
            connection.setAutoCommit(false);

            try 
            {
                int[] updateCounts;

                try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL))
                {
                    for (Student st :  students)
                    {
                        bindStudent (statement, st);
                        statement.addBatch();
                    }

                    updateCounts =  statement.executeBatch();
                }

                connection.commit();
                return updateCounts;
            }
            catch (SQLException | RuntimeException e)
            {
                rollback (connection, e);
                throw e;
            }
        }
    }

    private static void rollback (Connection connection, Throwable originalFailure)
    {
        try {
            connection.rollback();
        } 
        catch (SQLException rollbackFailure) {
            originalFailure.addSuppressed(rollbackFailure);
        }
    }
    

    private static Student mapRow (ResultSet rs) throws SQLException
    {
        return new Student (rs.getLong("id"), rs.getString("name"), rs.getString("email"), rs.getInt("age"));
    }


    private static void bindStudent (PreparedStatement statement, Student student) throws SQLException
    {
        statement.setString(1, student.getName());
        statement.setString(2, student.getEmail());
        statement.setInt(3, student.getAge());
    }
}