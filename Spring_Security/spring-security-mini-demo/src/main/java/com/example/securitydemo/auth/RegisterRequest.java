package com.example.securitydemo.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(
                regexp = "[A-Za-z0-9._-]+",
                message = "must contain only letters, digits, dot, underscore, or hyphen"
        )
        String username,

        @NotBlank
        @Size(min = 12, max = 72)
        String password
) {
}
