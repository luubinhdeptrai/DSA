package com.example.bookcatalog;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.BigDecimal;

import org.springframework.boot.SpringApplication;

import com.example.bookcatalog.config.CatalogProperties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import com.example.bookcatalog.model.Book;

import com.example.bookcatalog.service.BookImportService;


@SpringBootApplication
@ConfigurationPropertiesScan
public class BookCatalogApplication {
    

    public static void main (String[] args)
    {
        try (ConfigurableApplicationContext context = SpringApplication.run(BookCatalogApplication.class, args))
        {
           BookImportService sv = context.getBean(BookImportService.class);

           System.out.println("The number of Book in .csv: " + sv.importConfiguredBooks());
        }
    }
}



