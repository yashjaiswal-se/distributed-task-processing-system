package com.yash.workflow.workflow_service.controller;

import com.yash.workflow.workflow_service.dto.StartWorkflowRequest;
import com.yash.workflow.workflow_service.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping("/start")
    public ResponseEntity<UUID> startWorkflow(@RequestBody StartWorkflowRequest request) {

        UUID workflowId = workflowService.startWorkflow(request);

        return ResponseEntity.ok(workflowId);
    }
}