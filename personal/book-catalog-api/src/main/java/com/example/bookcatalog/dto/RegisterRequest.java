package com.example.bookcatalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Pattern(regexp = "[A-Za-z][A-Za-z0-9._-]{2,79}",
                 message = "username must be 3-80 allowed characters")
        String username,

        @NotBlank
        @Size(min = 12, max = 72)
        String password) {
}
