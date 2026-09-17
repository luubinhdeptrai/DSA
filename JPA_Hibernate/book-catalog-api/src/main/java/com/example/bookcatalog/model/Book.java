package com.example.bookcatalog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;


@Entity
@Table(name = "books")
public class Book {
    
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column //The attribute have the same name as the corresponding column. So we can ignore declare "name" in Column
    private String isbn;

    @Column
    private String title;

    @Column
    private String author;

    @Column
    private BigDecimal price;

    @Column
    private Integer stock;

    protected Book()
    {

    }

    public Book (String isbn, String title, String author, BigDecimal price, Integer stock)
    {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
    }

    public void replace (String isbn, String title, String author, BigDecimal price, Integer stock)
    {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
    }

    public void setStock (Integer stock)
    {
        this.stock = stock;
    }

    public Long getId()
    {
        return id;
    }

    public String getIsbn()
    {
        return isbn;
    }

    public String getTitle()
    {
        return title;
    }

    public String getAuthor()
    {
        return author;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public Integer getStock()
    {
        return stock;
    }

}

