package com.example.bookcatalog.dto;

import java.util.List;

public record CurrentUserResponse(String username, List<String> authorities) {
}
