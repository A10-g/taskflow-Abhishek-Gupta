package com.taskflow.backend.task;

import com.taskflow.backend.auth.security.CurrentUser;
import com.taskflow.backend.common.exception.ForbiddenException;
import com.taskflow.backend.common.exception.NotFoundException;
import com.taskflow.backend.common.pagination.PageParams;
import com.taskflow.backend.project.model.Project;
import com.taskflow.backend.project.repository.ProjectRepository;
import com.taskflow.backend.task.dto.CreateTaskRequest;
import com.taskflow.backend.task.dto.TaskPageResponse;
import com.taskflow.backend.task.dto.TaskResponse;
import com.taskflow.backend.task.dto.UpdateTaskRequest;
import com.taskflow.backend.task.model.TaskItem;
import com.taskflow.backend.task.model.TaskPriority;
import com.taskflow.backend.task.model.TaskStatus;
import com.taskflow.backend.task.repository.TaskRepository;
import com.taskflow.backend.user.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public TaskPageResponse listTasks(
            UUID projectId,
            String status,
            UUID assigneeId,
            Integer page,
            Integer limit,
            CurrentUser currentUser
    ) {
        ensureProjectAccess(projectId, currentUser);
        validateAssigneeIfPresent(assigneeId);
        PageParams pageParams = PageParams.of(page, limit);

        Page<TaskItem> taskPage = taskRepository.findFilteredByProjectId(
                projectId,
                normalizeStatus(status),
                assigneeId,
                PageRequest.of(pageParams.page() - 1, pageParams.limit(), Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<TaskResponse> tasks = taskPage.getContent().stream()
                .map(this::toTaskResponse)
                .toList();

        return new TaskPageResponse(
                tasks,
                pageParams.page(),
                pageParams.limit(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages()
        );
    }

    @Transactional
    public TaskResponse createTask(UUID projectId, CreateTaskRequest request, CurrentUser currentUser) {
        ensureProjectAccess(projectId, currentUser);
        validateAssigneeIfPresent(request.assigneeId());

        TaskItem task = new TaskItem();
        task.setTitle(request.title().trim());
        task.setDescription(normalizeDescription(request.description()));
        task.setStatus(TaskStatus.TODO);
        task.setPriority(normalizePriority(request.priority()));
        task.setProjectId(projectId);
        task.setAssigneeId(request.assigneeId());
        task.setCreatedBy(currentUser.id());
        task.setDueDate(request.dueDate());

        return toTaskResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request, CurrentUser currentUser) {
        TaskItem task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("task not found"));

        ensureProjectAccess(task.getProjectId(), currentUser);

        if (request.title() != null) {
            String trimmedTitle = request.title().trim();
            if (trimmedTitle.isEmpty()) {
                throw new IllegalArgumentException("task title cannot be blank");
            }
            task.setTitle(trimmedTitle);
        }
        if (request.description() != null) {
            task.setDescription(normalizeDescription(request.description()));
        }
        if (request.status() != null) {
            task.setStatus(normalizeStatus(request.status()));
        }
        if (request.priority() != null) {
            task.setPriority(normalizePriority(request.priority()));
        }
        if (request.assigneeId() != null) {
            validateAssigneeIfPresent(request.assigneeId());
            task.setAssigneeId(request.assigneeId());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }

        return toTaskResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(UUID taskId, CurrentUser currentUser) {
        TaskItem task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("task not found"));

        Project project = projectRepository.findById(task.getProjectId())
                .orElseThrow(() -> new NotFoundException("project not found"));

        boolean isOwner = Objects.equals(project.getOwnerId(), currentUser.id());
        boolean isCreator = Objects.equals(task.getCreatedBy(), currentUser.id());
        if (!isOwner && !isCreator) {
            throw new ForbiddenException("task delete requires owner or creator access");
        }

        taskRepository.delete(task);
    }

    private void ensureProjectAccess(UUID projectId, CurrentUser currentUser) {
        if (!projectRepository.existsById(projectId)) {
            throw new NotFoundException("project not found");
        }
        if (!projectRepository.hasAccess(projectId, currentUser.id())) {
            throw new ForbiddenException("project access denied");
        }
    }

    private void validateAssigneeIfPresent(UUID assigneeId) {
        if (assigneeId != null && !userRepository.existsById(assigneeId)) {
            throw new NotFoundException("assignee not found");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private TaskStatus normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        return TaskStatus.fromValue(status.trim().toLowerCase());
    }

    private TaskPriority normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return TaskPriority.MEDIUM;
        }
        return TaskPriority.fromValue(priority.trim().toLowerCase());
    }

    private TaskResponse toTaskResponse(TaskItem task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().getValue(),
                task.getPriority().getValue(),
                task.getProjectId(),
                task.getAssigneeId(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
