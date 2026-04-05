package com.yash.workflow.workflow_service.dto.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCompletedEvent {

    private UUID taskId;

    private UUID workflowId;

    // FINAL state only
    private String status; // COMPLETED or FAILED
}