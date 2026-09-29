package com.example.bookcatalog.exception;

public class DuplicateIsbnException extends RuntimeException {

    public DuplicateIsbnException (Throwable cause)
    {
        super ("Isbn is duplicated", cause);
    }
    
}
