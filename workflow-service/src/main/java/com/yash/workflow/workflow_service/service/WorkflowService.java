package com.yash.workflow.workflow_service.service;

import com.yash.workflow.workflow_service.dto.StartWorkflowRequest;
import com.yash.workflow.workflow_service.dto.TaskRequest;
import com.yash.workflow.workflow_service.dto.events.TaskEvent;
import com.yash.workflow.workflow_service.entity.Task;
import com.yash.workflow.workflow_service.entity.Workflow;
import com.yash.workflow.workflow_service.entity.enums.TaskStatus;
import com.yash.workflow.workflow_service.repository.TaskRepository;
import com.yash.workflow.workflow_service.repository.WorkflowRepository;
import com.yash.workflow.workflow_service.service.messaging.TaskPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final TaskRepository taskRepository;
    private final TaskPublisher taskPublisher;

    /**
     * Entry point:
     * 1. Persist workflow + tasks (transactional)
     * 2. Publish events (outside transaction)
     * 3. Update state → DISPATCHED
     */
    public UUID startWorkflow(StartWorkflowRequest request) {

        // Step 1: DB operations (transactional)
        List<Task> savedTasks = createAndPersistWorkflow(request);

        // Step 2: Kafka publish (outside transaction)
        dispatchTasks(savedTasks);

        return savedTasks.get(0).getWorkflowId();
    }

    /**
     * Handles DB persistence inside transaction
     */
    @Transactional
    protected List<Task> createAndPersistWorkflow(StartWorkflowRequest request) {

        Workflow workflow = createWorkflow(request);
        List<Task> tasks = createTasks(request, workflow.getId());

        return taskRepository.saveAll(tasks);
    }

    /**
     * Create and persist workflow
     */
    private Workflow createWorkflow(StartWorkflowRequest request) {

        Workflow workflow = new Workflow();
        workflow.setWorkflowName(request.getWorkflowName());
        workflow.setStatus("CREATED");

        // Temporary tenant strategy
        workflow.setTenantId(UUID.randomUUID());

        return workflowRepository.save(workflow);
    }

    /**
     * Build task list with initial state
     */
    private List<Task> createTasks(StartWorkflowRequest request, UUID workflowId) {

        List<Task> tasks = new ArrayList<>();

        for (TaskRequest taskRequest : request.getTasks()) {

            Task task = new Task();
            task.setId(UUID.randomUUID());
            task.setWorkflowId(workflowId);
            task.setTaskName(taskRequest.getTaskName());
            task.setPayload(taskRequest.getPayload());

            // Initial state
            task.setStatus(TaskStatus.CREATED);

            // Retry config
            task.setRetryCount(0);
            task.setMaxRetries(taskRequest.getMaxRetries());

            // Idempotency
            task.setIdempotencyKey(UUID.randomUUID().toString());

            tasks.add(task);
        }

        return tasks;
    }

    /**
     * Publish tasks to Kafka and update state → DISPATCHED
     */
    private void dispatchTasks(List<Task> tasks) {

        for (Task task : tasks) {

            TaskEvent event = buildTaskEvent(task);

            // Publish event
            taskPublisher.publishTask(event);

            // State transition
            task.setStatus(TaskStatus.DISPATCHED);

            log.info("Task dispatched: taskId={}, workflowId={}",
                    task.getId(), task.getWorkflowId());
        }

        // Persist updated states
        taskRepository.saveAll(tasks);
    }

    /**
     * Convert Task → TaskEvent
     */
    private TaskEvent buildTaskEvent(Task task) {

        return new TaskEvent(
                task.getId(),
                task.getWorkflowId(),
                task.getTaskName(),
                task.getPayload(),
                task.getIdempotencyKey(),
                task.getRetryCount(),
                task.getMaxRetries()
        );
    }
}