package com.example.bookcatalog.model;

import java.math.BigDecimal;

public record InventorySummary (int distinctTitles, int totalCopies, int lowStockTitles, BigDecimal inventoryValue) {
    
}
