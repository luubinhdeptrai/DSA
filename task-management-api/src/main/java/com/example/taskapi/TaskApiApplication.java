package com.example.taskapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the whole application. Running main() here starts an
 * embedded Tomcat server and boots the Spring IoC container - see
 * GUIDE.md Part 11 for what that container actually does.
 */
@SpringBootApplication
public class TaskApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskApiApplication.class, args);
    }
}
