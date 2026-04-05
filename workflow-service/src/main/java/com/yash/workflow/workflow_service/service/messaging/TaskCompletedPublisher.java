package com.yash.workflow.workflow_service.service.messaging;

import com.yash.workflow.workflow_service.dto.events.TaskCompletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskCompletedPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "task-completed-topic";

    public void publish(TaskCompletedEvent event) {

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        TOPIC,
                        event.getWorkflowId().toString(), // partition key
                        event
                );

        future.whenComplete((result, ex) -> {

            if (ex != null) {
                log.error("TaskCompletedEvent FAILED: taskId={}, workflowId={}",
                        event.getTaskId(),
                        event.getWorkflowId(),
                        ex);
            } else {
                log.info("TaskCompletedEvent SENT: taskId={}, workflowId={}, partition={}, offset={}",
                        event.getTaskId(),
                        event.getWorkflowId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}