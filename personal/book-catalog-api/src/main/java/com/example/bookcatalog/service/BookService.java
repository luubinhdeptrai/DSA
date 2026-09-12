package com.example.bookcatalog.service;

import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;


@Service
public class BookService {
    
    private final BookRepository repo;

    public BookService (BookRepository repo)
    {
        this.repo = repo;
    }

    public Optional<Book> findById (long id)
    {
        return repo.findById(id);
    }

    public List<Book> findAll(String author, int page, int size)
    {
        return repo.findAll(author, page*size, size);
    }

    public Book create(String isbn, String title, String author, BigDecimal price, Integer stock)
    {
        return repo.create(isbn, title, author, price, stock);
    }
}
