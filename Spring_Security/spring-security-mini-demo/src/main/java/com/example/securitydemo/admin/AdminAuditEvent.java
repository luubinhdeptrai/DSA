package com.example.securitydemo.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "admin_audit_events")
public class AdminAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AdminAuditEvent() {
    }

    public AdminAuditEvent(String username, String action, Instant createdAt) {
        this.username = username;
        this.action = action;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
