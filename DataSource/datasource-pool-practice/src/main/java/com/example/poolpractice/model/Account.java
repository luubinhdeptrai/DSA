package com.example.poolpractice.model;

import java.math.BigDecimal;

public record Account (long id, String ownerName, BigDecimal balance) {
    
}
