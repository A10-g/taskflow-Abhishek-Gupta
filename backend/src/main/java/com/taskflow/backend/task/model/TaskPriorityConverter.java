package com.taskflow.backend.task.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TaskPriorityConverter implements AttributeConverter<TaskPriority, String> {

    @Override
    public String convertToDatabaseColumn(TaskPriority attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public TaskPriority convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TaskPriority.fromValue(dbData);
    }
}
