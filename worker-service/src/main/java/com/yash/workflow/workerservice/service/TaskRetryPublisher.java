package com.yash.workflow.workerservice.service;

import com.yash.workflow.workerservice.dto.TaskEvent;
import com.yash.workflow.workerservice.entity.Task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskRetryPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "task-execution-topic";

    public void retry(Task task) {

        TaskEvent event = new TaskEvent(
                task.getId(),
                task.getWorkflowId(),
                task.getTaskName(),
                task.getPayload(),
                task.getIdempotencyKey(),
                task.getRetryCount(),
                task.getMaxRetries()
        );

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        TOPIC,
                        task.getId().toString(),
                        event
                );

        future.whenComplete((result, ex) -> {

            if (ex != null) {
                log.error("Retry publish FAILED: taskId={}, workflowId={}",
                        task.getId(),
                        task.getWorkflowId(),
                        ex);
            } else {
                log.info("Retry published: taskId={}, workflowId={}, partition={}, offset={}, retryCount={}",
                        task.getId(),
                        task.getWorkflowId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        task.getRetryCount());
            }
        });
    }
}