package com.example.bookcatalog.service;


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
import java.lang.IllegalStateException;
import com.example.bookcatalog.exception.TransactionLabCheckedException ;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository)
    {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> findAll (String author, int page, int size)
    {
        Pageable pageable = PageRequest.of
                                        (
                                            page,
                                            size,
                                            Sort.by(Sort.Direction.ASC, "id")
                                        );
        if (author != null)
        {
            return bookRepository.findByAuthor(author, pageable);
        }
        return bookRepository.findAll(pageable).getContent();
    }

    @Transactional
    public Book create(String isbn, String author, String title, BigDecimal price, Integer stock)
    {
        Book book = new Book(isbn, author, title, price, stock);

        try 
        {
            return bookRepository.saveAndFlush(book);
        }
        catch (DataIntegrityViolationException e)
        {
            throw new DuplicateIsbnException(e);
        }
    }

    @Transactional(readOnly = true)
    public Book findById (long id)
    {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional
    public Book replace (long id, String isbn, String author, String title, BigDecimal price, Integer stock)
    {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        book.replace(isbn, author, title, price, stock);
        try
        {
            bookRepository.flush();
            return book;
        }
        catch (DataIntegrityViolationException e)
        {
            throw new DuplicateIsbnException(e);
        }
    }

    @Transactional
    public Book setStock (long id, Integer stock)
    {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));

        book.setStock(stock);

        return book;
    }

    @Transactional
    public void deleteById(long id)
    {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));

        bookRepository.delete(book);
    }
    
}
