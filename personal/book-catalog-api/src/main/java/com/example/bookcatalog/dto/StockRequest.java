package com.example.bookcatalog.dto;
import jakarta.validation.constraints.PositiveOrZero;


public record StockRequest (@PositiveOrZero Integer stock) {
    
}
