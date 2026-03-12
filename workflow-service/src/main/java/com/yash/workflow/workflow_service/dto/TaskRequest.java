package com.yash.workflow.workflow_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {

    private String taskName;

    private String payload;

    private int maxRetries;
}
