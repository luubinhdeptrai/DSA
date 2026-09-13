package com.example.bookcatalog.service;

import com.example.bookcatalog.exception.BookNotFoundException;
import com.example.bookcatalog.exception.DuplicateIsbnException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;


@Service
public class BookService {
    
    private final BookRepository repo;

    public BookService (BookRepository repo)
    {
        this.repo = repo;
    }

    public Book findById (long id)
    {
        return repo.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    public List<Book> findAll(String author, int page, int size)
    {
        return repo.findAll(author, page*size, size);
    }

    public Book create(String isbn, String title, String author, BigDecimal price, Integer stock)
    {
        try {
          return repo.create(isbn, title, author, price, stock);
        } catch (DuplicateKeyException e)
        {
            throw new DuplicateIsbnException (e);
        }
    }

    public Book replace (long id, String isbn, String title, String author, BigDecimal price, Integer stock)
    {
        try {
            return repo.replace(id, isbn, title, author, price, stock).orElseThrow(() -> new BookNotFoundException(id));
        } catch (DuplicateKeyException e)
        {
            throw new DuplicateIsbnException (e);
        }
    }

    public Book setStock (long id, Integer stock)
    {
        return repo.setStock(id, stock).orElseThrow(() -> new BookNotFoundException(id));
    }

    public void delete (long id)
    {
        if (!repo.delete(id))
        {
            throw new BookNotFoundException(id);
        }
    }
}
