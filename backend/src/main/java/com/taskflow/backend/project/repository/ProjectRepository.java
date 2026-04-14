package com.taskflow.backend.project.repository;

import com.taskflow.backend.project.model.Project;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query(
            value = """
                    SELECT DISTINCT p
                    FROM Project p
                    LEFT JOIN TaskItem t ON t.projectId = p.id AND t.assigneeId = :userId
                    WHERE p.ownerId = :userId OR t.id IS NOT NULL
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT p.id)
                    FROM Project p
                    LEFT JOIN TaskItem t ON t.projectId = p.id AND t.assigneeId = :userId
                    WHERE p.ownerId = :userId OR t.id IS NOT NULL
                    """
    )
    Page<Project> findAccessibleProjects(@Param("userId") UUID userId, Pageable pageable);

    Optional<Project> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM projects p
                LEFT JOIN tasks t ON t.project_id = p.id AND t.assignee_id = :userId
                WHERE p.id = :projectId
                  AND (p.owner_id = :userId OR t.id IS NOT NULL)
            )
            """, nativeQuery = true)
    boolean hasAccess(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}
