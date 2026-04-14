package com.taskflow.backend.task.repository;

import com.taskflow.backend.task.model.TaskItem;
import com.taskflow.backend.task.model.TaskStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<TaskItem, UUID> {

    interface StatusCountView {
        String getStatus();
        long getCount();
    }

    interface AssigneeCountView {
        UUID getAssigneeId();
        String getName();
        long getCount();
    }

    List<TaskItem> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    @Query(
            value = """
                    SELECT t
                    FROM TaskItem t
                    WHERE t.projectId = :projectId
                      AND (:status IS NULL OR t.status = :status)
                      AND (:assigneeId IS NULL OR t.assigneeId = :assigneeId)
                    """,
            countQuery = """
                    SELECT COUNT(t)
                    FROM TaskItem t
                    WHERE t.projectId = :projectId
                      AND (:status IS NULL OR t.status = :status)
                      AND (:assigneeId IS NULL OR t.assigneeId = :assigneeId)
                    """
    )
    Page<TaskItem> findFilteredByProjectId(
            @Param("projectId") UUID projectId,
            @Param("status") TaskStatus status,
            @Param("assigneeId") UUID assigneeId,
            Pageable pageable
    );

    @Query("""
            SELECT t.status AS status, COUNT(t) AS count
            FROM TaskItem t
            WHERE t.projectId = :projectId
            GROUP BY t.status
            """)
    List<StatusCountView> countByStatus(@Param("projectId") UUID projectId);

    @Query(value = """
            SELECT t.assignee_id AS assigneeId,
                   COALESCE(u.name, 'Unassigned') AS name,
                   COUNT(*) AS count
            FROM tasks t
            LEFT JOIN users u ON u.id = t.assignee_id
            WHERE t.project_id = :projectId
            GROUP BY t.assignee_id, u.name
            ORDER BY count DESC, name ASC
            """, nativeQuery = true)
    List<AssigneeCountView> countByAssignee(@Param("projectId") UUID projectId);

    @Query("""
            SELECT COUNT(t)
            FROM TaskItem t
            WHERE t.projectId = :projectId
            """)
    long countForProject(@Param("projectId") UUID projectId);

    Optional<TaskItem> findById(UUID id);
}
