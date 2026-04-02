package com.yash.workflow.workerservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskEvent {

    private UUID taskId;

    private UUID workflowId;

    private String taskName;

    private String payload;

    //Idempotency (prevents duplicate execution)
    private String idempotencyKey;

    // Retry tracking
    private int retryCount;

    private int maxRetries;
}