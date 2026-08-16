package com.example.taskapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for PUT /api/tasks/{id}.
 * PUT means "replace the whole resource", so every field is required -
 * unlike a PATCH DTO, where every field would be optional.
 */
public class UpdateTaskRequest {

    @NotBlank(message = "title is required")
    @Size(max = 100, message = "title must be at most 100 characters")
    private String title;

    @Size(max = 500, message = "description must be at most 500 characters")
    private String description;

    @NotNull(message = "completed is required")
    private Boolean completed;

    public UpdateTaskRequest() {
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

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
