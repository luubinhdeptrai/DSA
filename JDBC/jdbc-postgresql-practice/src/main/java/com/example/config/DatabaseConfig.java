package com.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {

    private static final Properties PROPERTIES = loadProperties();
    private static final String CONFIG_FILE = "database_properties";

    private DatabaseConfig()
    {

    }

    public static Connection getConnection() throws SQLException 
    {
        return DriverManager.getConnection(
                requiredValue("DB_URL", "db.url"),
                requiredValue("DB_USERNAME", "db.username"),
                requiredValue("DB_PASSWORD", "db.passwird"));
    }

    private static Properties loadProperties()
    {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE) )
        {
            if (input != null)
            {
                properties.load(input);
            }
            return properties;
        }
        catch (IOException e)
        {
            throw new IllegalStateException ("Could not read: " + CONFIG_FILE + " " + e.getMessage());
        }
    }


    private static String requiredValue(String environmentName, String propertyName)
    {
        String result = System.getenv(environmentName);

        if (result == null || result.isBlank())
        {
            result = PROPERTIES.getProperty(propertyName);
        }

        if (result == null || result.isBlank())
        {
            throw new IllegalStateException ("Missing configuration: " + propertyName
                            + " (or environment variable "
                            + environmentName + ")");
        }

        return result;
    }
    
}
