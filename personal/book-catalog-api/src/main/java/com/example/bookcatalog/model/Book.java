package com.example.bookcatalog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import jakarta.persistence.Version;


@Entity
@Table (name = "books")
public class Book {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String isbn;

    @Column
    private String title;

    @Column
    private String author;

    @Column
    private BigDecimal price;

    @Column
    private Integer stock;

    @Version
    @Column
    private Long version;

    protected Book()
    {

    }

    public Book (String isbn, String author, String title, BigDecimal price, Integer stock)
    {
        this.isbn = isbn;
        this.author = author;
        this.title = title;
        this.price = price;
        this.stock = stock;
    }

    public void replace (String isbn, String author, String title, BigDecimal price, Integer stock)
    {
        this.isbn = isbn;
        this.author = author;
        this.title = title;
        this.price = price;
        this.stock = stock;
    }

    public void setStock(Integer stock)
    {
        this.stock = stock;
    }

    public long getId()
    {
        return id;
    }

    public String getIsbn()
    {
        return isbn;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getTitle()
    {
        return title;
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
