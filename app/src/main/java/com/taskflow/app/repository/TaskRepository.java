package com.taskflow.app.repository;

import com.taskflow.app.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(Task.TaskStatus status);

    List<Task> findByPriority(Task.Priority priority);

    List<Task> findByStatusAndPriority(Task.TaskStatus status, Task.Priority priority);

    List<Task> findByTitleContainingIgnoreCase(String keyword);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    long countByStatus(Task.TaskStatus status);
}
