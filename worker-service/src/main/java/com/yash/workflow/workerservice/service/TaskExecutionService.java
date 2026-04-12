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

            log.info("event=TASK_EXECUTION_STARTED workflowId={} taskId={} taskName={} retry={}",
                    task.getWorkflowId(),
                    task.getId(),
                    task.getTaskName(),
                    task.getRetryCount());

            switch (task.getTaskName()) {

                case "EMAIL":
                    sendEmail(task);
                    break;

                case "PAYMENT":
                    processPayment(task);
                    break;

                case "FAIL_TEST":
                    throw new RuntimeException("Intentional failure for testing");

                default:
                    throw new RuntimeException("Unknown task: " + task.getTaskName());
            }

            // Success path
            task.setStatus(TaskStatus.COMPLETED);
            taskRepository.save(task);

            log.info("event=TASK_COMPLETED workflowId={} taskId={}",
                    task.getWorkflowId(),
                    task.getId());

            completedPublisher.publish(
                    new TaskCompletedEvent(
                            task.getId(),
                            task.getWorkflowId(),
                            "COMPLETED"
                    )
            );

        } catch (Exception e) {

            log.error("event=TASK_EXECUTION_ERROR workflowId={} taskId={} retry={} error={}",
                    task.getWorkflowId(),
                    task.getId(),
                    task.getRetryCount(),
                    e.getMessage());

            task.setRetryCount(task.getRetryCount() + 1);

            if (task.getRetryCount() >= task.getMaxRetries()) {

                task.setStatus(TaskStatus.FAILED);
                taskRepository.save(task);

                log.error("event=TASK_FINAL_FAILURE workflowId={} taskId={} totalAttempts={}",
                        task.getWorkflowId(),
                        task.getId(),
                        task.getRetryCount());

                completedPublisher.publish(
                        new TaskCompletedEvent(
                                task.getId(),
                                task.getWorkflowId(),
                                "FAILED"
                        )
                );

            } else {

                // Retry path
                task.setStatus(TaskStatus.CREATED);

                log.warn("event=TASK_RETRY workflowId={} taskId={} attempt={} maxRetries={}",
                        task.getWorkflowId(),
                        task.getId(),
                        task.getRetryCount(),
                        task.getMaxRetries());

                taskRepository.save(task);

                retryPublisher.retry(task);
                return;
            }
        }
    }

    private void sendEmail(Task task) {
        log.info("event=EMAIL_TASK workflowId={} taskId={} payload={}",
                task.getWorkflowId(),
                task.getId(),
                task.getPayload());
    }

    private void processPayment(Task task) {
        log.info("event=PAYMENT_TASK workflowId={} taskId={} payload={}",
                task.getWorkflowId(),
                task.getId(),
                task.getPayload());
    }
}