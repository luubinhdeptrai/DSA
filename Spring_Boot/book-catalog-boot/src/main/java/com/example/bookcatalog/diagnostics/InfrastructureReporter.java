package com.example.bookcatalog.diagnostics;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class InfrastructureReporter {

    private final DataSource dataSource;

    public InfrastructureReporter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void printSnapshot() {
        System.out.println(
                "DataSource implementation: " + dataSource.getClass().getName());

        try (Connection connection = dataSource.getConnection()) {
            System.out.println(
                    "Logical connection class: " + connection.getClass().getName());
            System.out.println(
                    "Database product: " + connection.getMetaData().getDatabaseProductName());
            System.out.println(
                    "JDBC driver: " + connection.getMetaData().getDriverName());

            if (dataSource instanceof HikariDataSource hikari) {
                System.out.printf(
                        "Pool configuration: name=%s, maximum=%d, minimumIdle=%d%n",
                        hikari.getPoolName(),
                        hikari.getMaximumPoolSize(),
                        hikari.getMinimumIdle()
                );
                printPoolMetrics("While borrowed", hikari);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not inspect database infrastructure", exception);
        }

        if (dataSource instanceof HikariDataSource hikari) {
            printPoolMetrics("After return", hikari);
        }
    }

    private static void printPoolMetrics(
            String phase,
            HikariDataSource hikari
    ) {
        HikariPoolMXBean metrics = hikari.getHikariPoolMXBean();
        if (metrics == null) {
            System.out.println(phase + ": pool metrics unavailable");
            return;
        }

        System.out.printf(
                "%s: active=%d, idle=%d, total=%d, waiting=%d%n",
                phase,
                metrics.getActiveConnections(),
                metrics.getIdleConnections(),
                metrics.getTotalConnections(),
                metrics.getThreadsAwaitingConnection()
        );
    }
}
