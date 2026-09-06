package com.example.bookcatalog.service;

import com.example.bookcatalog.config.CatalogProperties;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Service
public class BookImportService {

    private final CatalogProperties properties;
    private final ResourceLoader resourceLoader;
    private final BookRepository repository;

    public BookImportService(
            CatalogProperties properties,
            ResourceLoader resourceLoader,
            BookRepository repository
    ) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.repository = repository;
    }

    public int importConfiguredBooks() {
        if (!properties.importEnabled()) {
            return 0;
        }

        Resource resource = resourceLoader.getResource(properties.importLocation());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine();

            int affectedRows = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    affectedRows += repository.upsert(parse(line));
                }
            }
            return affectedRows;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read catalog resource: " + properties.importLocation(),
                    exception
            );
        }
    }

    private Book parse(String line) {
        String[] cells = line.split(",", -1);
        if (cells.length != 5) {
            throw new IllegalArgumentException("Expected five CSV columns: " + line);
        }

        return new Book(
                cells[0].trim(),
                cells[1].trim(),
                cells[2].trim(),
                new BigDecimal(cells[3].trim()),
                Integer.parseInt(cells[4].trim())
        );
    }
}
