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

    private static final RowMapper<Book> ROW_MAPPER = (rs, rowNum) -> new Book (rs.getLong("id"), rs.getString("isbn"), rs.getString("title"), rs.getString("author"), rs.getBigDecimal("price"), rs.getInt("stock"));

    public BookRepository (JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Book> findById (long id)
    {
        return jdbcTemplate.query("SELECT id, isbn, title, author, price, stock FROM books WHERE id = ?",
                                    ROW_MAPPER,
                                    id).stream().findFirst();
    }

    public List<Book> findAll (String author, int size, long offset)
    {
        if (author == null)
        {
            return jdbcTemplate.query("SELECT id, isbn, title, author, price, stock FROM books ORDER BY id ASC LIMIT ? OFFSET ?",
                                        ROW_MAPPER,
                                        size,
                                        offset);
        }
        return jdbcTemplate.query("SELECT id, isbn, title, author, price, stock FROM books WHERE author = ? ORDER BY id ASC LIMIT ? OFFSET ?",
                                        ROW_MAPPER,
                                        author,
                                        size,
                                        offset);
    }

    public Book insert (String isbn, String title, String author, BigDecimal price, int stock)
    {
        return jdbcTemplate.query("INSERT INTO books (isbn, title, author, price, stock) VALUES (?,?,?,?,?) RETURNING id, isbn, title, author, price, stock", ROW_MAPPER, isbn, title, author, price, stock).get(0);
    }

    public Optional<Book> replace (long id, String isbn, String title, String author, BigDecimal price, int stock)
    {
        return jdbcTemplate.query("UPDATE books SET isbn = ?, title = ?, author = ?, price = ?, stock = ? WHERE id = ? RETURNING id, isbn, title, author, price, stock",
                                    ROW_MAPPER,
                                    isbn,
                                    title,
                                    author,
                                    price,
                                    stock,
                                    id).stream().findFirst();
    }

    public Optional<Book> setStock(long id, Integer stock)
    {
        return jdbcTemplate.query("UPDATE books SET stock = ? WHERE id = ? RETURNING id, isbn, title, author, price, stock", 
                                    ROW_MAPPER,
                                    stock,
                                    id).stream().findFirst();
    }

    public boolean delete (long id)
    {
        return jdbcTemplate.update("DELETE FROM books WHERE id = ?", id) == 1;
    }
}
