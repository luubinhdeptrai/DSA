package com.example.bookcatalog.service;

import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    
    private final BookRepository bookRepository;

    public BookService (BookRepository bookRepository)
    {
        this.bookRepository = bookRepository;
    }

    public Optional<Book> findById (long id)
    {
        return bookRepository.findById(id);
    }

    public List<Book> findAll (String author, int page, int size)
    {
        return bookRepository.findAll(author, size, page*size );
    }

    public Book create (String isbn, String title, String author, BigDecimal price, int stock)
    {
        return bookRepository.insert(isbn, title, author, price, stock);
    }

    public Optional<Book> replace (long id, String isbn, String title, String author, BigDecimal price, int stock)
    {
        return bookRepository.replace(id, isbn, title, author, price, stock);
    }

    public Optional<Book> setStock (long id, Integer stock)
    {
        return bookRepository.setStock(id, stock);
    }

    public boolean delete(long id)
    {
        return bookRepository.delete(id);
    }
}

