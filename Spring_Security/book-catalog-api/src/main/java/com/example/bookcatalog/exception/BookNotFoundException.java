package com.example.bookcatalog.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException (long id)
    {
        super("Book with id: " + id + " is not found");
    }
    
}
