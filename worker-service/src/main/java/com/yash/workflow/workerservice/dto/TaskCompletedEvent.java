package com.yash.workflow.workerservice.dto;

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
    private String status; // COMPLETED / FAILED
}