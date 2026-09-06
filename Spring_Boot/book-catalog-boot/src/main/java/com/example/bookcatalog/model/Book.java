package com.example.bookcatalog.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Book (String isbn, String title, String author, BigDecimal price, int stock) {

    public Book
    {
        isbn = requireText (isbn, "isbn");
        title = requireText (title, "title");
        author = requireText (author, "author");
        price = Objects.requireNonNull(price, "price");

        if (price.signum() < 0)
        {
            throw new IllegalArgumentException("Price must be greater than or equal 0");
        }

        if (stock < 0)
        {
            throw new IllegalArgumentException("Stock must be greater than or equal 0");
        }
    }

    private static String requireText (String value, String name)
    {
        Objects.requireNonNull(value, name);

        if (value.isBlank())
        {
            throw new IllegalArgumentException (name + " must not be blank");
        }

        return value.trim();
    }
    
}
