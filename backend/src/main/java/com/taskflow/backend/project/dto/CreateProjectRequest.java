package com.taskflow.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "is required")
        @Size(max = 255, message = "must be at most 255 characters")
        String name,

        @Size(max = 2000, message = "must be at most 2000 characters")
        String description
) {
}
