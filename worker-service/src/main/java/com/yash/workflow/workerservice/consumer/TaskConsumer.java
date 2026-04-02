package com.yash.workflow.workerservice.consumer;

import com.yash.workflow.workerservice.dto.TaskEvent;
import com.yash.workflow.workerservice.entity.Task;
import com.yash.workflow.workerservice.entity.TaskStatus;
import com.yash.workflow.workerservice.repository.TaskRepository;
import com.yash.workflow.workerservice.service.TaskExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskConsumer {

    private final TaskRepository taskRepository;
    private final TaskExecutionService executionService;

    @KafkaListener(topics = "task-topic")
    public void consume(TaskEvent event) {

        log.info("Received TaskEvent: {}", event);

        Optional<Task> existingTaskOpt =
                taskRepository.findByIdempotencyKey(event.getIdempotencyKey());

        // CASE 1: Task already exists
        if (existingTaskOpt.isPresent()) {

            Task existingTask = existingTaskOpt.get();

            // If already completed → skip duplicate
            if (existingTask.getStatus() == TaskStatus.COMPLETED) {
                log.warn("Duplicate completed task skipped: {}", existingTask.getId());
                return;
            }

            // Retry case → execute again
            log.info("Retrying existing task: {}", existingTask.getId());
            executionService.execute(existingTask);
            return;
        }

        // CASE 2: New task
        Task task = new Task();
        task.setId(event.getTaskId());
        task.setWorkflowId(event.getWorkflowId());
        task.setTaskName(event.getTaskName());
        task.setPayload(event.getPayload());
        task.setIdempotencyKey(event.getIdempotencyKey());
        task.setRetryCount(event.getRetryCount());
        task.setMaxRetries(event.getMaxRetries());
        task.setStatus(TaskStatus.CREATED);

        taskRepository.save(task);

        log.info("New task created: {}", task.getId());

        executionService.execute(task);
    }
}