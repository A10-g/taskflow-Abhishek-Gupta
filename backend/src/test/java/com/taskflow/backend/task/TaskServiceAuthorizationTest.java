package com.taskflow.backend.task;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.taskflow.backend.auth.security.CurrentUser;
import com.taskflow.backend.common.exception.ForbiddenException;
import com.taskflow.backend.project.model.Project;
import com.taskflow.backend.project.repository.ProjectRepository;
import com.taskflow.backend.task.model.TaskItem;
import com.taskflow.backend.task.repository.TaskRepository;
import com.taskflow.backend.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceAuthorizationTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, projectRepository, userRepository);
    }

    @Test
    void deleteTaskThrowsForbiddenWhenUserIsNeitherProjectOwnerNorTaskCreator() {
        UUID taskId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID projectId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID creatorId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID currentUserId = UUID.fromString("55555555-5555-5555-5555-555555555555");

        TaskItem task = new TaskItem();
        task.setId(taskId);
        task.setProjectId(projectId);
        task.setCreatedBy(creatorId);

        Project project = new Project();
        project.setId(projectId);
        project.setOwnerId(ownerId);

        CurrentUser currentUser = new CurrentUser(currentUserId, "Other User", "other@example.com", "hash");

        given(taskRepository.findById(taskId)).willReturn(Optional.of(task));
        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

        assertThrows(ForbiddenException.class, () -> taskService.deleteTask(taskId, currentUser));
        verify(taskRepository, never()).delete(task);
    }
}
