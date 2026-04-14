package com.taskflow.backend.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProjectStatsResponse(
        @JsonProperty("project_id") UUID projectId,
        @JsonProperty("by_status") Map<String, Long> byStatus,
        @JsonProperty("by_assignee") List<AssigneeStats> byAssignee,
        long total
) {
    public record AssigneeStats(
            @JsonProperty("assignee_id") UUID assigneeId,
            String name,
            long count
    ) {
    }
}
