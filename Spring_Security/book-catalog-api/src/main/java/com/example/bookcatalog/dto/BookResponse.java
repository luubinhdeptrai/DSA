package com.example.bookcatalog.dto;

import java.math.BigDecimal;

import com.example.bookcatalog.model.Book;

public record BookResponse (long id, String isbn, String title, String author, BigDecimal price, Integer stock) {

    public static BookResponse from(Book book)
    {
        return new BookResponse(book.getId(), book.getIsbn(), book.getTitle(), book.getAuthor(), book.getPrice(), book.getStock());
    }
    
}
