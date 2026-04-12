package com.yash.workflow.workflow_service.controller;

import com.yash.workflow.workflow_service.dto.StartWorkflowRequest;
import com.yash.workflow.workflow_service.entity.Workflow;
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
    @GetMapping("/{id}")
    public ResponseEntity<Workflow> getWorkflow(@PathVariable UUID id) {
        return ResponseEntity.ok(workflowService.getWorkflow(id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<String> getStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(workflowService.getStatus(id));
    }
}