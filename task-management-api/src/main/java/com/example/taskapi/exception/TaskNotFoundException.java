package com.example.taskapi.exception;

/**
 * A custom, unchecked exception (extends RuntimeException, so callers are
 * not forced to declare "throws" or wrap calls in try/catch). It carries no
 * behavior beyond a message - its entire job is to be a distinct type that
 * GlobalExceptionHandler can catch and translate into an HTTP 404.
 */
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("Task not found with id: " + id);
    }
}
