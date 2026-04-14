package com.taskflow.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "is required")
        @Size(max = 255, message = "must be at most 255 characters")
        String name,

        @NotBlank(message = "is required")
        @Email(message = "must be a valid email")
        @Size(max = 255, message = "must be at most 255 characters")
        String email,

        @NotBlank(message = "is required")
        @Size(min = 8, max = 72, message = "must be between 8 and 72 characters")
        String password
) {
}
