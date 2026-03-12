package com.yash.workflow.workflow_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StartWorkflowRequest {

    private String workflowName;

    private List<TaskRequest> tasks;

}