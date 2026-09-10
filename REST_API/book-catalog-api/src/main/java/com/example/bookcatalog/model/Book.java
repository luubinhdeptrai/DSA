package com.example.bookcatalog.model;

import java.math.BigDecimal;

public record Book (long id, String isbn, String title, String author, BigDecimal price, int stock) {
    
}
