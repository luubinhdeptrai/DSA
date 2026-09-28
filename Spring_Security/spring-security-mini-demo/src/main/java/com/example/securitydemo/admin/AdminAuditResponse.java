package com.example.securitydemo.admin;

import java.time.Instant;

public record AdminAuditResponse(
        Long id,
        String action,
        Instant createdAt
) {
}
