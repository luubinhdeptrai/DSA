package com.example.poolpractice.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DataSourceFactory {

    private DataSourceFactory()
    {

    }

    public static HikariDataSource create (DatabaseSettings settings)
    {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(settings.url());
        config.setUsername(settings.username());
        config.setPassword(settings.password());

        config.setPoolName("Pool_Postgres");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(20_000);
        config.setIdleTimeout(20_000);
        config.setMaxLifetime(20_000);

        return new HikariDataSource(config);
    }
    
}
