package com.example.bookcatalog.runner;

import com.example.bookcatalog.config.CatalogProperties;
import com.example.bookcatalog.diagnostics.InfrastructureReporter;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.model.InventorySummary;
import com.example.bookcatalog.repository.BookRepository;
import com.example.bookcatalog.service.BookImportService;
import com.example.bookcatalog.service.InventorySummaryService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogApplicationRunner implements ApplicationRunner {

    private final CatalogProperties properties;
    private final BookImportService importService;
    private final BookRepository repository;
    private final InventorySummaryService summaryService;
    private final InfrastructureReporter infrastructureReporter;
    private final Environment environment;

    public CatalogApplicationRunner(
            CatalogProperties properties,
            BookImportService importService,
            BookRepository repository,
            InventorySummaryService summaryService,
            InfrastructureReporter infrastructureReporter,
            Environment environment
    ) {
        this.properties = properties;
        this.importService = importService;
        this.repository = repository;
        this.summaryService = summaryService;
        this.infrastructureReporter = infrastructureReporter;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        // infrastructureReporter.printSnapshot();

        // int affectedRows = importService.importConfiguredBooks();
        // List<Book> books = repository.findAll();
        // InventorySummary summary = summaryService.summarize(
        //         books, properties.lowStockThreshold());

        // String[] activeProfiles = environment.getActiveProfiles();
        // String profileText = activeProfiles.length == 0
        //         ? "(default)"
        //         : String.join(", ", activeProfiles);

        // System.out.println("=== " + properties.reportTitle() + " ===");
        // System.out.println("Active profiles: " + profileText);
        // System.out.println("Imported/updated rows: " + affectedRows);

        // books.forEach(book -> System.out.printf(
        //         "- %s | %s | %s | %s | %d%n",
        //         book.isbn(),
        //         book.title(),
        //         book.author(),
        //         book.price(),
        //         book.stock()
        // ));

        // System.out.println("Titles: " + summary.distinctTitles());
        // System.out.println("Copies: " + summary.totalCopies());
        // System.out.printf(
        //         "Low-stock titles (<= %d): %d%n",
        //         properties.lowStockThreshold(),
        //         summary.lowStockTitles()
        // );
        // System.out.println("Inventory value: " + summary.inventoryValue());
    }
}
