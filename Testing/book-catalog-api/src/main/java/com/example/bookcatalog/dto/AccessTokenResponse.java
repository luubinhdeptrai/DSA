package com.example.bookcatalog.dto;

public record AccessTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {
}
