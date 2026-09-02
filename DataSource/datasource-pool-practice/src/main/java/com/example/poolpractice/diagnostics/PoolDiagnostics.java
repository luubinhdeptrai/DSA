package com.example.poolpractice.diagnostics;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import javax.sql.DataSource;

public final class PoolDiagnostics {
    private PoolDiagnostics()
    {

    }

    public static void print (HikariDataSource pool, String label)
    {
        HikariPoolMXBean metrics = pool.getHikariPoolMXBean();
        System.out.println(label + ":");
        System.out.println(metrics.getTotalConnections());
        System.out.println(metrics.getActiveConnections());
        System.out.println(metrics.getIdleConnections());
        System.out.println(metrics.getThreadsAwaitingConnection());

    }
}
