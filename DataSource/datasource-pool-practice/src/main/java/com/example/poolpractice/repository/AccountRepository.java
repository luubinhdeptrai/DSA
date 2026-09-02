package com.example.poolpractice.repository;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import com.example.poolpractice.model.Account;
import java.util.ArrayList;


public final class AccountRepository {

    private static final String INSERT_SQL = """
            INSERT INTO accounts (owner_name, balance) VALUES (?,?)
            """;

    private static final String SELECT_SQL = """
            SELECT *
            FROM accounts
            ORDER BY id 
            """;

    private final DataSource dataSource;

    public AccountRepository(DataSource dataSource)
    {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    public long insert(String ownerName, BigDecimal openingBalance) throws SQLException
    {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, ownerName);
            statement.setBigDecimal(2, openingBalance);

            int affectedRow = statement.executeUpdate();

            if (affectedRow == 0)
            {
                throw new SQLException ("Creating account failing.");
            }

            try (ResultSet rs = statement.getGeneratedKeys())
            {
                if (rs.next())
                {
                    return rs.getLong(1);
                }
                else
                {
                    throw new SQLException ("No generated key returned");
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return 0;
        }


    }

    public ArrayList<Account> listAll () throws SQLException
    {
        ArrayList<Account> list = new ArrayList<>();
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(SELECT_SQL);
            ResultSet rs = statement.executeQuery())
        {
            while (rs.next())
            {
                list.add(new Account(rs.getLong("id"), rs.getString("owner_name"), rs.getBigDecimal("balance")));
            }
        }
        return list;
    }


    
}
