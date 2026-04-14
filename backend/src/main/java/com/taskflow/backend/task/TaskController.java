package com.taskflow.backend.task;

import com.taskflow.backend.auth.security.CurrentUser;
import com.taskflow.backend.task.dto.CreateTaskRequest;
import com.taskflow.backend.task.dto.TaskPageResponse;
import com.taskflow.backend.task.dto.TaskResponse;
import com.taskflow.backend.task.dto.UpdateTaskRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/projects/{id}/tasks")
    public ResponseEntity<TaskPageResponse> listTasks(
            @PathVariable UUID id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID assignee,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return ResponseEntity.ok(taskService.listTasks(id, status, assignee, page, limit, currentUser));
    }

    @PostMapping("/projects/{id}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(id, request, currentUser));
    }

    @PatchMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return ResponseEntity.ok(taskService.updateTask(id, request, currentUser));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
