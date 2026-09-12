package com.example.bookcatalog.dto;

import java.math.BigDecimal;


public record BookRequest (String isbn, String title, String author, BigDecimal price, Integer stock)
{

}