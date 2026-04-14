package com.taskflow.backend.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ProjectPageResponse(
        List<ProjectResponse> projects,
        int page,
        int limit,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages
) {
}
