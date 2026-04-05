package com.yash.workflow.workflow_service.consumer;

import com.yash.workflow.workflow_service.dto.events.TaskCompletedEvent;
import com.yash.workflow.workflow_service.entity.Workflow;
import com.yash.workflow.workflow_service.entity.enums.WorkflowStatus;
import com.yash.workflow.workflow_service.repository.WorkflowRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCompletedConsumer {

    private final WorkflowRepository workflowRepository;

    @KafkaListener(
            topics = "task-completed-topic",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(TaskCompletedEvent event) {

        log.info("Received TaskCompletedEvent: {}", event);

        Optional<Workflow> workflowOpt =
                workflowRepository.findById(event.getWorkflowId());

        if (workflowOpt.isEmpty()) {
            log.error("Workflow NOT FOUND: {}", event.getWorkflowId());
            return;
        }

        Workflow workflow = workflowOpt.get();

        // 🔹 Update counters
        if ("COMPLETED".equalsIgnoreCase(event.getStatus())) {
            workflow.setCompletedTasks(workflow.getCompletedTasks() + 1);
        } else if ("FAILED".equalsIgnoreCase(event.getStatus())) {
            workflow.setFailedTasks(workflow.getFailedTasks() + 1);
        }

        // 🔹 Decide workflow status
        if (workflow.getFailedTasks() > 0) {
            workflow.setStatus(WorkflowStatus.FAILED);
        } else if (workflow.getCompletedTasks() == workflow.getTotalTasks()) {
            workflow.setStatus(WorkflowStatus.COMPLETED);
        } else {
            workflow.setStatus(WorkflowStatus.IN_PROGRESS);
        }

        workflowRepository.save(workflow);

        log.info("Workflow updated: workflowId={}, status={}, completed={}, failed={}",
                workflow.getId(),
                workflow.getStatus(),
                workflow.getCompletedTasks(),
                workflow.getFailedTasks());
    }
}