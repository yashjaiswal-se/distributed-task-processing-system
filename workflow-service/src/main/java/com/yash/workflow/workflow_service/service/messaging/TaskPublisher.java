package com.yash.workflow.workflow_service.service.messaging;

import com.yash.workflow.workflow_service.dto.events.TaskEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "task-execution-topic";

    /**
     * Publishes TaskEvent to Kafka asynchronously
     */
    public void publishTask(TaskEvent event) {

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        TOPIC,
                        event.getTaskId().toString(), // partition key
                        event
                );

        future.whenComplete((result, ex) -> {

            if (ex != null) {
                log.error("Kafka publish FAILED: taskId={}, idempotencyKey={}",
                        event.getTaskId(),
                        event.getIdempotencyKey(),
                        ex);
            } else {
                log.info("Kafka publish SUCCESS: taskId={}, partition={}, offset={}, retryCount={}",
                        event.getTaskId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.getRetryCount());
            }
        });
    }
}