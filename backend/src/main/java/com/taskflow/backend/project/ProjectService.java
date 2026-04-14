package com.taskflow.backend.project;

import com.taskflow.backend.auth.security.CurrentUser;
import com.taskflow.backend.common.exception.ForbiddenException;
import com.taskflow.backend.common.exception.NotFoundException;
import com.taskflow.backend.common.pagination.PageParams;
import com.taskflow.backend.project.dto.CreateProjectRequest;
import com.taskflow.backend.project.dto.ProjectPageResponse;
import com.taskflow.backend.project.dto.ProjectResponse;
import com.taskflow.backend.project.dto.ProjectStatsResponse;
import com.taskflow.backend.project.dto.UpdateProjectRequest;
import com.taskflow.backend.project.model.Project;
import com.taskflow.backend.project.repository.ProjectRepository;
import com.taskflow.backend.task.model.TaskItem;
import com.taskflow.backend.task.repository.TaskRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ProjectService(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public ProjectPageResponse listProjects(CurrentUser currentUser, Integer page, Integer limit) {
        PageParams pageParams = PageParams.of(page, limit);
        Page<Project> projectPage = projectRepository.findAccessibleProjects(
                currentUser.id(),
                PageRequest.of(pageParams.page() - 1, pageParams.limit(), Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<ProjectResponse> projects = projectPage.getContent().stream()
                .map(this::toProjectResponse)
                .toList();

        return new ProjectPageResponse(
                projects,
                pageParams.page(),
                pageParams.limit(),
                projectPage.getTotalElements(),
                projectPage.getTotalPages()
        );
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, CurrentUser currentUser) {
        Project project = new Project();
        project.setName(request.name().trim());
        project.setDescription(normalizeDescription(request.description()));
        project.setOwnerId(currentUser.id());

        return toProjectResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public ProjectResponse.ProjectDetailResponse getProject(UUID projectId, CurrentUser currentUser) {
        Project project = getAccessibleProject(projectId, currentUser);

        List<ProjectResponse.TaskSummary> tasks = taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toTaskSummary)
                .toList();

        return new ProjectResponse.ProjectDetailResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwnerId(),
                project.getCreatedAt(),
                tasks
        );
    }

    @Transactional(readOnly = true)
    public ProjectStatsResponse getProjectStats(UUID projectId, CurrentUser currentUser) {
        Project project = getAccessibleProject(projectId, currentUser);

        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put("todo", 0L);
        byStatus.put("in_progress", 0L);
        byStatus.put("done", 0L);
        taskRepository.countByStatus(project.getId()).forEach(row -> byStatus.put(row.getStatus(), row.getCount()));

        List<ProjectStatsResponse.AssigneeStats> byAssignee = taskRepository.countByAssignee(project.getId()).stream()
                .map(row -> new ProjectStatsResponse.AssigneeStats(row.getAssigneeId(), row.getName(), row.getCount()))
                .toList();

        return new ProjectStatsResponse(
                project.getId(),
                byStatus,
                byAssignee,
                taskRepository.countForProject(project.getId())
        );
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request, CurrentUser currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("project not found"));

        ensureOwner(project, currentUser);

        if (request.name() != null) {
            String trimmedName = request.name().trim();
            if (trimmedName.isEmpty()) {
                throw new IllegalArgumentException("project name cannot be blank");
            }
            project.setName(trimmedName);
        }
        if (request.description() != null) {
            project.setDescription(normalizeDescription(request.description()));
        }

        return toProjectResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(UUID projectId, CurrentUser currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("project not found"));
        ensureOwner(project, currentUser);
        projectRepository.delete(project);
    }

    private Project getAccessibleProject(UUID projectId, CurrentUser currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("project not found"));

        if (!projectRepository.hasAccess(projectId, currentUser.id())) {
            throw new ForbiddenException("project access denied");
        }
        return project;
    }

    private void ensureOwner(Project project, CurrentUser currentUser) {
        if (!Objects.equals(project.getOwnerId(), currentUser.id())) {
            throw new ForbiddenException("project owner access required");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProjectResponse toProjectResponse(Project project) {
        return ProjectResponse.of(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwnerId(),
                project.getCreatedAt()
        );
    }

    private ProjectResponse.TaskSummary toTaskSummary(TaskItem task) {
        return new ProjectResponse.TaskSummary(
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
