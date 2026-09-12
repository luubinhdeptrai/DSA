package com.example.bookcatalog.repository;

import com.example.bookcatalog.model.Book;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;


@Repository
public class BookRepository {
    
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Book> ROW_MAPPER = (rs, rowNum) -> new Book (rs.getLong("id"), rs.getString("isbn"), rs.getString("title"), rs.getString("author"), rs.getBigDecimal("price"), rs.getInt("stock"));

    public BookRepository (JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Book> findById (long id)
    {
        return jdbcTemplate.query("SELECT * FROM books WHERE id = ?", 
                                    ROW_MAPPER,
                                    id).stream().findFirst();
    }

    public List<Book> findAll(String author, long offset, int size)
    {
        if (author == null)
        {
            return jdbcTemplate.query("SELECT * FROM books ORDER BY id ASC LIMIT ? OFFSET ?",
                                        ROW_MAPPER,
                                        size,
                                        offset);
        }

        return jdbcTemplate.query("SELECT * FROM books WHERE author = ? ORDER BY id ASC LIMIT ? OFFSET ?",
                                        ROW_MAPPER,
                                        author,
                                        size,
                                        offset);
    }

    public Book create (String isbn, String title, String author, BigDecimal price, Integer stock)
    {
        return jdbcTemplate.query("INSERT INTO books(isbn, title, author, price, stock) VALUES(?,?,?,?,?)  RETURNING id, isbn, title, author, price, stock",
                                    ROW_MAPPER,
                                    isbn,
                                    title,
                                    author,
                                    price,
                                    stock).get(0);
    }
}
