package com.taskflow.backend.project;

import com.taskflow.backend.auth.security.CurrentUser;
import com.taskflow.backend.project.dto.CreateProjectRequest;
import com.taskflow.backend.project.dto.ProjectPageResponse;
import com.taskflow.backend.project.dto.ProjectResponse;
import com.taskflow.backend.project.dto.ProjectStatsResponse;
import com.taskflow.backend.project.dto.UpdateProjectRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<ProjectPageResponse> listProjects(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return ResponseEntity.ok(projectService.listProjects(currentUser, page, limit));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request, currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse.ProjectDetailResponse> getProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return ResponseEntity.ok(projectService.getProject(id, currentUser));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ProjectStatsResponse> getProjectStats(
            @PathVariable UUID id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return ResponseEntity.ok(projectService.getProjectStats(id, currentUser));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return ResponseEntity.ok(projectService.updateProject(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
