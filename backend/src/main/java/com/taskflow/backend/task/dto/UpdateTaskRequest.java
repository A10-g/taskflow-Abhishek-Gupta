package com.taskflow.backend.task.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTaskRequest(
        @Size(min = 1, max = 255, message = "must be between 1 and 255 characters")
        String title,

        @Size(max = 2000, message = "must be at most 2000 characters")
        String description,

        @Pattern(regexp = "todo|in_progress|done", message = "must be one of: todo, in_progress, done")
        String status,

        @Pattern(regexp = "low|medium|high", message = "must be one of: low, medium, high")
        String priority,

        UUID assigneeId,
        LocalDate dueDate
) {
}
