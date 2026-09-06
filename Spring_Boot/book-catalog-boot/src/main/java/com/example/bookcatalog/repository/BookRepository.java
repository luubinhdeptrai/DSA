package com.example.bookcatalog.repository;

import com.example.bookcatalog.model.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BookRepository {
    
    private static final String UPSERT_SQL = """
            INSERT INTO books (isbn, title, author, price, stock)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (isbn) DO UPDATE SET
                title = EXCLUDED.title,
                author = EXCLUDED.author,
                price = EXCLUDED.price,
                stock = EXCLUDED.stock,
                last_imported_at = CURRENT_TIMESTAMP
            """;

    private static final String FIND_ALL_SQL = """
            SELECT *
            FROM books
            ORDER BY isbn   
            """;

    private final JdbcTemplate jdbcTemplate;

    public BookRepository (JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int upsert (Book book)
    {
        return jdbcTemplate.update (UPSERT_SQL, book.isbn(), book.title(), book.author(), book.price(), book.stock());
    }

    public List<Book> findAll()
    {
        List<Book> list = new ArrayList<Book>();

        jdbcTemplate.query(
            FIND_ALL_SQL,
            (rs, rowNum) -> list.add(new Book (rs.getString("isbn"), rs.getString("title"), rs.getString("author"), rs.getBigDecimal("price"), rs.getInt("stock")))
        );

        return list;
    }
}   
