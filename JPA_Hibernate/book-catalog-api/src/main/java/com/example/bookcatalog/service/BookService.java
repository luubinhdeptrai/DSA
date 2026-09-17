package com.example.bookcatalog.service;

// import com.example.bookcatalog.exception.BookNotFoundException;
// import com.example.bookcatalog.exception.DuplicateIsbnException;
// import com.example.bookcatalog.model.Book;
// import com.example.bookcatalog.repository.BookRepository;
// import java.math.BigDecimal;
// import java.util.List;
// import java.util.Optional;
// import org.springframework.stereotype.Service;
// import org.springframework.dao.DuplicateKeyException;

import com.example.bookcatalog.exception.BookNotFoundException;
import com.example.bookcatalog.exception.DuplicateIsbnException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional (readOnly = true)
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
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));

        if (author == null)
        {
            return repo.findAll(pageable).getContent();
        }

        return repo.findByAuthor(author, pageable);
    }

    @Transactional
    public Book create(String isbn, String title, String author, BigDecimal price, Integer stock)
    {
        try
        {
            Book book = repo.save(new Book(isbn, title, author, price, stock));
            repo.flush();
            return book;
        } catch (DataIntegrityViolationException e)
        {
            throw new DuplicateIsbnException(e);
        }



        // try {
        //   return repo.create(isbn, title, author, price, stock);
        // } catch (DuplicateKeyException e)
        // {
        //     throw new DuplicateIsbnException (e);
        // }
    }

    @Transactional
    public Book replace (long id, String isbn, String title, String author, BigDecimal price, Integer stock)
    {
        try 
        {
            Book book = repo.findById(id).orElseThrow(() -> new BookNotFoundException(id));

            book.replace(isbn, title, author, price, stock);

            repo.flush();

            return book;
        } catch (DataIntegrityViolationException e)
        {
            throw new DuplicateIsbnException(e);
        }


        // 
        // try {
        //     return repo.replace(id, isbn, title, author, price, stock).orElseThrow(() -> new BookNotFoundException(id));
        // } catch (DuplicateKeyException e)
        // {
        //     throw new DuplicateIsbnException (e);
        // }
    }

    @Transactional
    public Book setStock (long id, Integer stock)
    {
        Book book = repo.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        book.setStock(stock);
        return book;
    }

    @Transactional
    public void delete (long id)
    {
        Book book = repo.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        repo.delete(book);
    }
}
