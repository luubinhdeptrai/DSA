package com.example.securitydemo.admin;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuditEventRepository
        extends JpaRepository<AdminAuditEvent, Long> {
}
