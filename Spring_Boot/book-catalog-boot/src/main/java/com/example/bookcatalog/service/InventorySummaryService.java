package com.example.bookcatalog.service;

import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.model.InventorySummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventorySummaryService {

    public InventorySummary summarize(List<Book> books, int lowStockThreshold) {
        if (lowStockThreshold < 0) {
            throw new IllegalArgumentException(
                    "Low-stock threshold must not be negative");
        }

        int totalCopies = books.stream()
                .mapToInt(Book::stock)
                .sum();

        int lowStockTitles = (int) books.stream()
                .filter(book -> book.stock() <= lowStockThreshold)
                .count();

        BigDecimal inventoryValue = books.stream()
                .map(book -> book.price().multiply(
                        BigDecimal.valueOf(book.stock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new InventorySummary(
                books.size(),
                totalCopies,
                lowStockTitles,
                inventoryValue
        );
    }
}
