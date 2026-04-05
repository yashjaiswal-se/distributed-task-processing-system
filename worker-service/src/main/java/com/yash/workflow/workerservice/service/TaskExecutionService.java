package com.yash.workflow.workerservice.service;

import com.yash.workflow.workerservice.entity.Task;
import com.yash.workflow.workerservice.entity.TaskStatus;
import com.yash.workflow.workerservice.repository.TaskRepository;
import com.yash.workflow.workerservice.dto.TaskCompletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskExecutionService {

    private final TaskRepository taskRepository;
    private final TaskRetryPublisher retryPublisher;
    private final TaskCompletedPublisher completedPublisher;

    public void execute(Task task) {

        try {
            // Move to IN_PROGRESS
            task.setStatus(TaskStatus.IN_PROGRESS);
            taskRepository.save(task);

            log.info("Executing Task: {}", task.getTaskName());

            switch (task.getTaskName()) {

                case "EMAIL":
                    sendEmail(task);
                    break;

                case "PAYMENT":
                    processPayment(task);
                    break;

                default:
                    throw new RuntimeException("Unknown task: " + task.getTaskName());
            }

            task.setStatus(TaskStatus.COMPLETED);
            taskRepository.save(task);

            log.info("Task completed: {}", task.getId());

            completedPublisher.publish(
                    new TaskCompletedEvent(
                            task.getId(),
                            task.getWorkflowId(),
                            "COMPLETED"
                    )
            );

        } catch (Exception e) {

            log.error("Task failed: {}", e.getMessage());

            task.setRetryCount(task.getRetryCount() + 1);

            if (task.getRetryCount() >= task.getMaxRetries()) {

                task.setStatus(TaskStatus.FAILED);
                taskRepository.save(task);

                log.error("Max retries reached. Task FAILED: {}", task.getId());

                completedPublisher.publish(
                        new TaskCompletedEvent(
                                task.getId(),
                                task.getWorkflowId(),
                                "FAILED"
                        )
                );

            } else {

                // ✅ RETRY PATH (NO EVENT)
                task.setStatus(TaskStatus.CREATED);

                log.warn("Retrying task. Attempt: {}", task.getRetryCount());

                taskRepository.save(task);

                retryPublisher.retry(task);
                return;
            }
        }
    }

    private void sendEmail(Task task) {
        log.info("Sending email with payload: {}", task.getPayload());
    }

    private void processPayment(Task task) {
        log.info("Processing payment with payload: {}", task.getPayload());
    }
}