package com.example.bookcatalog.dto;

import com.example.bookcatalog.model.Book;
import java.math.BigDecimal;

public record BookResponse (long id, String isbn, String title, String author, BigDecimal price, int stock) {

    public static BookResponse from(Book book)
    {
        return new BookResponse(book.id(), book.isbn(), book.title(), book.author(), book.price(), book.stock());
    }
}
