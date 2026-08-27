package com.example.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.config.DatabaseConfig;

public class AccountTransferService
{

    private static final String DEBIT_SQL = """
            UPDATE accounts
            SET balance = balance - ?
            WHERE id = ? AND balance >= ?
            """;

    private static final String CREDIT_SQL = """
            UPDATE accounts
            SET balance = balance + ?
            WHERE id = ? 
            """;

     private static final String BALANCE_SQL =
            "SELECT balance FROM accounts WHERE id = ?";

    public void transfer (long fromId, long toId, BigDecimal amount) throws SQLException
    {
        BigDecimal exactAmount = validateTransfer (fromId, toId, amount);

        try (Connection connection = DatabaseConfig.getConnection())
        {
            connection.setAutoCommit(false);


            try
            {
                try (PreparedStatement debit = connection.prepareStatement(DEBIT_SQL);
                    PreparedStatement credit = connection.prepareStatement(CREDIT_SQL))
                {
                    debit.setBigDecimal(1, exactAmount);
                    debit.setLong(2, fromId);
                    debit.setBigDecimal(3, exactAmount);
                    if (debit.executeUpdate() != 1)
                    {
                        throw new SQLException(
                                    "Debit failed: source is missing "
                                            + "or balance is insufficient"
                        );
                    }

                    credit.setBigDecimal(1, exactAmount);
                    credit.setLong(2, toId);
                    if (credit.executeUpdate() != 1)
                    {
                        throw new SQLException(
                                    "Credit failed: destination account "
                                            + "is missing"
                        );
                    }

                    connection.commit();
                }
            }
            catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                throw e;
            }
        }

    }


     private static BigDecimal validateTransfer(
            long fromId,
            long toId,
            BigDecimal amount
    ) {
        if (fromId <= 0 || toId <= 0) {
            throw new IllegalArgumentException(
                    "Account IDs must be positive"
            );
        }

        if (fromId == toId) {
            throw new IllegalArgumentException(
                    "Source and destination must differ"
            );
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Amount may have at most two decimal places",
                    e
            );
        }
    }

    private static void rollback(
            Connection connection,
            Throwable originalFailure
    ) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            originalFailure.addSuppressed(rollbackFailure);
        }
    }

        public BigDecimal findBalance(long accountId)
            throws SQLException {
        try (Connection connection =
                     DatabaseConfig.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(BALANCE_SQL)) {

            statement.setLong(1, accountId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {
                return resultSet.next()
                        ? resultSet.getBigDecimal("balance")
                        : null;
            }
        }
    }
}