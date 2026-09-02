package com.example.poolpractice;

import com.example.poolpractice.config.DatabaseSettings;
import com.zaxxer.hikari.HikariDataSource;
import com.example.poolpractice.config.DataSourceFactory;

import com.example.poolpractice.repository.AccountRepository;
import java.math.BigDecimal;
import com.example.poolpractice.model.Account;

import com.example.poolpractice.diagnostics.PoolDiagnostics;


import java.util.ArrayList;

public class Main {

    public static void main (String[] args)
    {
        ArrayList<Account> list = new ArrayList<>();
        try 
        {
            DatabaseSettings settings = DatabaseSettings.fromEnvironment();
            try (HikariDataSource pool = DataSourceFactory.create(settings))
            {
                PoolDiagnostics.print(pool, "startup");

                AccountRepository repo = new AccountRepository(pool);
                repo.insert("Binh Luu 1", new BigDecimal("1.00"));
                repo.insert("Binh Luu 2", new BigDecimal("2.00"));
                repo.insert("Binh Luu 3", new BigDecimal("3.00"));
                repo.insert("Binh Luu 4", new BigDecimal("4.00"));
                repo.insert("Binh Luu 5", new BigDecimal("5.00"));





            }
        }
        catch (Exception e)
        {
            System.out.println (e.getMessage());
        }
    }
    
}
