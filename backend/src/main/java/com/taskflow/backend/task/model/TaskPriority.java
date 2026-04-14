package com.taskflow.backend.task.model;

public enum TaskPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    private final String value;

    TaskPriority(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TaskPriority fromValue(String value) {
        for (TaskPriority priority : values()) {
            if (priority.value.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown task priority: " + value);
    }
}
