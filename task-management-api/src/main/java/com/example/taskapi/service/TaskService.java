package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.exception.TaskNotFoundException;
import com.example.taskapi.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Service layer holds business logic and orchestrates the Repository.
 * Controllers never talk to the Repository directly - they always go
 * through here. This class is a Spring Bean (@Service), created once by the
 * IoC container and handed to whoever asks for it via constructor injection
 * (see TaskController).
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    // Constructor injection: Spring sees this is the only constructor and
    // automatically supplies a TaskRepository instance (the dynamic proxy
    // Spring Data generated) when it builds this Bean. Nothing here calls
    // "new TaskRepositoryImpl()" - see GUIDE.md Part 11 for the full story.
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskResponse createTask(CreateTaskRequest request) {
        Task task = new Task(request.getTitle(), request.getDescription());
        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    public List<TaskResponse> getAllTasks() {
        // List<Task> -> core Java Collections + Generics: the repository
        // returns a List of exactly one type, Task, checked at compile time.
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long id) {
        Task task = findTaskOrThrow(id);
        return toResponse(task);
    }

    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        Task task = findTaskOrThrow(id);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCompleted(request.getCompleted());
        // findById() and save() each run in their own short transaction here,
        // so `task` is detached by the time we mutate it. save() on a
        // detached entity with a non-null id makes Hibernate merge it -
        // load the current row, compare, and issue an UPDATE for the
        // changed columns.
        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    public void deleteTask(Long id) {
        Task task = findTaskOrThrow(id);
        taskRepository.delete(task);
    }

    private Task findTaskOrThrow(Long id) {
        // Optional<Task> forces the caller to explicitly handle the
        // "no such row" case instead of risking a silent NullPointerException
        // three layers away from where the row was actually looked up.
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
