package com.example.poolpractice.config;

public record DatabaseSettings (String url, String username, String password) {

    public static DatabaseSettings fromEnvironment()
    {
        return new DatabaseSettings (required("DB_URL"), required("DB_USERNAME"), required("DB_PASSWORD"));
    }

    private static String required (String name)
    {
        String result = System.getenv(name);

        if (result == null || result.isBlank())
        {
            throw new IllegalStateException (name + " is not configured in process' env");
        }

        return result;
    }

    @Override
    public String toString()
    {
        return "URL: ${url} , UserName: ${username}";
    }
    
}

