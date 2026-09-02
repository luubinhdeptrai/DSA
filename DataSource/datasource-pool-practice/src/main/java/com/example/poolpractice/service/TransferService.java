package com.example.poolpractice.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;
import java.util.Objects;


public final class TransferService {

    private static final String SQL_DEBIT = """
            UPDATE accounts
            SET balance = balance - ?
            WHERE id = ? AND balance >= ?
            """;
    
    private static final String SQL_CREDIT = """
            UPDATE accounts
            SET balance = balance + ?
            WHERE id = ?
            """;

    private final DataSource dataSource;

    public TransferService (DataSource dataSource)
    {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    public void transfer (long fromId, long toId, BigDecimal money) throws SQLException
    {
        validateTransfer(fromId, toId, money);

        try (Connection connection = dataSource.getConnection();
            PreparedStatement s1 = connection.prepareStatement(SQL_DEBIT);
            PreparedStatement s2 = connection.prepareStatement(SQL_CREDIT))
        {
            connection.setAutoCommit(false);

            try {
                // s1.setBigDecimal(1, money);
                // s1.setLong(2, fromId);
                // s1.setBigDecimal(3, money);
                // if (s1.executeUpdate() == 0)
                // {
                //     throw new SQLException("Something went wrong at DEBIT process");
                // }
                debit(s1, fromId, money);

                // s2.setBigDecimal(1, money);
                // s2.setLong(2, toId);
                // if (s2.executeUpdate() == 0)
                // {
                //     throw new SQLException ("Something went wrong at CREDIT process");
                // }
                credit(s2, toId, money);

                connection.commit();
            }
            catch (SQLException | RuntimeException e)
            {
                connection.rollback();
                throw e;
            }

        }
    }

    private void debit (PreparedStatement s1, long fromId, BigDecimal money) throws SQLException
    {
            s1.setBigDecimal(1, money);
            s1.setLong(2, fromId);
            s1.setBigDecimal(3, money);
            if (s1.executeUpdate() == 0)
            {
                throw new SQLException("Something went wrong at DEBIT process");
            }
    }

    private void credit (PreparedStatement s2, long toId, BigDecimal money) throws SQLException
    {
        s2.setBigDecimal(1, money);
        s2.setLong(2, toId);
        if (s2.executeUpdate() == 0)
        {
            throw new SQLException ("Something went wrong at CREDIT process");
        }
    }

    private static void validateTransfer(long fromId, long toId, BigDecimal money) throws SQLException
    {
        if (toId < 0 || fromId < 0)
        {
            throw new SQLException ("Id must not less than 0");
        }
    }
    
}
