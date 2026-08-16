package com.example.taskapi.controller;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The Controller is the entry point for HTTP requests. It knows about HTTP
 * (methods, status codes, paths) but nothing about business rules or the
 * database - it just translates a request into a Service call and a Service
 * result into a response.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    // Constructor injection, same pattern as TaskService -> TaskRepository.
    // Spring's IoC container builds a TaskService Bean first (since this
    // constructor needs one), then builds this TaskController Bean and
    // passes that TaskService in.
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // POST creates a new resource. 201 Created signals "a new resource now
    // exists", distinct from 200 OK which just means "request succeeded".
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse created = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET never changes server state, so it's always safe to call repeatedly.
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    // PUT replaces the entire resource - the client must send every field.
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    // 204 No Content: the request succeeded and there is deliberately no
    // response body to return (the resource is gone).
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
