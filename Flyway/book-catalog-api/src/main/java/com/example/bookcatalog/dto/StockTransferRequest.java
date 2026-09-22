package com.example.bookcatalog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockTransferRequest(
        @NotNull(message = "fromBookId must not be null")
        @Positive(message = "fromBookId must be positive")
        Long fromBookId,

        @NotNull(message = "toBookId must not be null")
        @Positive(message = "toBookId must be positive")
        Long toBookId,

        @NotNull(message = "amount must not be null")
        @Positive(message = "amount must be positive")
        Integer amount,

        @NotNull(message = "scenario must not be null")
        Scenario scenario) {

    public enum Scenario {
        SUCCESS_REQUIRED,
        RUNTIME_ROLLBACK,
        FLUSH_THEN_RUNTIME_ROLLBACK,
        CHECKED_DEFAULT_COMMIT,
        CHECKED_ROLLBACK,
        CAUGHT_REQUIRED_INNER_FAILURE,
        REQUIRES_NEW_AUDIT
    }
}