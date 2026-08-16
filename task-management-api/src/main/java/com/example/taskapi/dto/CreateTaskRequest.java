package com.example.taskapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for POST /api/tasks.
 * Only contains the fields a client is allowed to send when creating a
 * task. Notice there is no "id", "completed", or timestamp field here -
 * the client does not get to set those; the server decides them.
 */
public class CreateTaskRequest {

    @NotBlank(message = "title is required")
    @Size(max = 100, message = "title must be at most 100 characters")
    private String title;

    @Size(max = 500, message = "description must be at most 500 characters")
    private String description;

    public CreateTaskRequest() {
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
}
