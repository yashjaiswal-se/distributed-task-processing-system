package com.yash.workflow.workflow_service.consumer;

import com.yash.workflow.workflow_service.dto.events.TaskCompletedEvent;
import com.yash.workflow.workflow_service.entity.Workflow;
import com.yash.workflow.workflow_service.entity.enums.WorkflowStatus;
import com.yash.workflow.workflow_service.repository.WorkflowRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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

        final String workflowId = event.getWorkflowId().toString();
        final String taskId = event.getTaskId().toString();
        final String status = event.getStatus();

        // Event received
        log.info("event=TASK_COMPLETION_RECEIVED workflowId={} taskId={} status={}",
                workflowId, taskId, status);

        Workflow workflow = workflowRepository.findById(event.getWorkflowId())
                .orElse(null);

        if (workflow == null) {
            log.error("event=WORKFLOW_NOT_FOUND workflowId={}", workflowId);
            return;
        }

        // Duplicate protection (post-completion guard)
        if (isWorkflowAlreadyFinal(workflow)) {
            log.warn("event=DUPLICATE_EVENT_SKIPPED workflowId={} taskId={}",
                    workflowId, taskId);
            return;
        }

        // Update counters
        updateCounters(workflow, status);

        // Update workflow status
        updateWorkflowStatus(workflow);

        workflowRepository.save(workflow);

        // Final state log
        log.info("event=WORKFLOW_UPDATED workflowId={} status={} completed={} failed={}",
                workflow.getId(),
                workflow.getStatus(),
                workflow.getCompletedTasks(),
                workflow.getFailedTasks());
    }

    private boolean isWorkflowAlreadyFinal(Workflow workflow) {
        return workflow.getCompletedTasks() + workflow.getFailedTasks() >= workflow.getTotalTasks();
    }

    private void updateCounters(Workflow workflow, String status) {
        if ("COMPLETED".equalsIgnoreCase(status)) {
            workflow.setCompletedTasks(workflow.getCompletedTasks() + 1);
        } else if ("FAILED".equalsIgnoreCase(status)) {
            workflow.setFailedTasks(workflow.getFailedTasks() + 1);
        }
    }

    private void updateWorkflowStatus(Workflow workflow) {
        if (workflow.getFailedTasks() > 0) {
            workflow.setStatus(WorkflowStatus.FAILED);
        } else if (workflow.getCompletedTasks() == workflow.getTotalTasks()) {
            workflow.setStatus(WorkflowStatus.COMPLETED);
        } else {
            workflow.setStatus(WorkflowStatus.IN_PROGRESS);
        }
    }
}