package com.example.bookcatalog.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.DecimalMax;



import java.math.BigDecimal;


public record BookRequest(
        @NotBlank(message = "isbn must not be blank")
        @Size(max = 20, message = "isbn size must be less than or equal to 20 chars")
        String isbn,

        @NotBlank(message = "title must not be blank")
        @Size(max=200, message = "title must be less than or equal to 200 chars")
        String title,

        @NotBlank(message = "author must not be blank")
        @Size(max = 120, message = "author must <= 120 chars")
        String author,
        
        @NotNull(message = "price must not NULL")
        @PositiveOrZero(message = "price must be >= 0")
        @DecimalMax(value = "9999999999.99", message = "price must be <= 9999999999.99")
        // TODO: exact decimal-place rule in Task 4
        BigDecimal price,

        @NotNull(message = "stock must not NULL")
        @PositiveOrZero(message = "stock must be >= 0")
        Integer stock) {

    public BookRequest {
        isbn = normalize(isbn);
        title = normalize(title);
        author = normalize(author);
    }

    private static String normalize(String value) {
        if (value != null)
        {
            value = value.trim();
        }
        return value;
    }
}