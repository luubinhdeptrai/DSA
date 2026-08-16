package com.example.taskapi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * An Entity is a plain Java class that Hibernate maps onto a database table.
 * One Task object in memory <-> one row in the "tasks" table.
 * This class deliberately uses plain fields + manual getters/setters instead
 * of Lombok, so every generated method is visible.
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA requires a no-arg constructor: Hibernate creates instances via
    // reflection and then fills in the fields itself, it never calls "new
    // Task(...)" with arguments.
    protected Task() {
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.completed = false;
    }

    // Hibernate lifecycle callbacks: methods annotated with @PrePersist /
    // @PreUpdate are invoked automatically right before Hibernate issues the
    // corresponding INSERT / UPDATE statement.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
