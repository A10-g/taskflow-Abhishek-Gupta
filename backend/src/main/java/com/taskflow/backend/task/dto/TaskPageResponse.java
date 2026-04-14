package com.taskflow.backend.task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TaskPageResponse(
        List<TaskResponse> tasks,
        int page,
        int limit,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages
) {
}
