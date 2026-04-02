package com.yash.workflow.workerservice.service;

import com.yash.workflow.workerservice.dto.TaskEvent;
import com.yash.workflow.workerservice.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskRetryPublisher {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;

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

        // Use key for partition consistency
        kafkaTemplate.send("task-topic", task.getId().toString(), event)
                .whenComplete((result, ex) -> {

                    if (ex != null) {
                        log.error("Failed to publish retry event for taskId={}", task.getId(), ex);
                    } else {
                        log.info("Retry event published for taskId={}, partition={}, offset={}",
                                task.getId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}