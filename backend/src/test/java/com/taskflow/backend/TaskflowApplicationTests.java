package com.taskflow.backend;

import com.taskflow.backend.project.repository.ProjectRepository;
import com.taskflow.backend.task.repository.TaskRepository;
import com.taskflow.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
        "spring.liquibase.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class TaskflowApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private TaskRepository taskRepository;

    @Test
    void contextLoads() {
    }
}
