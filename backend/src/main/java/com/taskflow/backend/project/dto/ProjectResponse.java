package com.taskflow.backend.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        @JsonProperty("owner_id") UUID ownerId,
        @JsonProperty("created_at") Instant createdAt
) {
    public static ProjectResponse of(UUID id, String name, String description, UUID ownerId, Instant createdAt) {
        return new ProjectResponse(id, name, description, ownerId, createdAt);
    }

    public record ProjectDetailResponse(
            UUID id,
            String name,
            String description,
            @JsonProperty("owner_id") UUID ownerId,
            @JsonProperty("created_at") Instant createdAt,
            List<TaskSummary> tasks
    ) {
    }

    public record TaskSummary(
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
}
