package com.yash.workflow.workflow_service.repository;

import com.yash.workflow.workflow_service.entity.Task;
import com.yash.workflow.workflow_service.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByWorkflowId(UUID workflowId);

    List<Task> findByStatus(TaskStatus status);

}