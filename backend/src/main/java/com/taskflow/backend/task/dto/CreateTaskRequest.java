package com.taskflow.backend.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank(message = "is required")
        @Size(max = 255, message = "must be at most 255 characters")
        String title,

        @Size(max = 2000, message = "must be at most 2000 characters")
        String description,

        @Pattern(regexp = "low|medium|high", message = "must be one of: low, medium, high")
        String priority,

        UUID assigneeId,
        LocalDate dueDate
) {
}
