package com.taskflow.backend.task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        String status,
        String priority,
        @JsonProperty("project_id") UUID projectId,
        @JsonProperty("assignee_id") UUID assigneeId,
        @JsonProperty("due_date") LocalDate dueDate,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {
}
