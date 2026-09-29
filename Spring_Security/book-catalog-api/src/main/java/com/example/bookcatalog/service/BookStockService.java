package com.example.bookcatalog.service;

import com.example.bookcatalog.exception.BookNotFoundException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookStockService {

    private final BookRepository bookRepository;

    public BookStockService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void decrease(long bookId, int amount) {
        requirePositiveAmount(amount);
        Book book = requiredBook(bookId);

        if (book.getStock() < amount) {
            throw new IllegalStateException(
                    "Insufficient stock for transaction exercise");
        }

        book.setStock(book.getStock() - amount);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void increase(long bookId, int amount) {
        requirePositiveAmount(amount);
        Book book = requiredBook(bookId);
        book.setStock(Math.addExact(book.getStock(), amount));
    }

    private Book requiredBook(long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
    }

    private static void requirePositiveAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
