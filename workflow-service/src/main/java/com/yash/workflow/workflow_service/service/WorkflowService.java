package com.yash.workflow.workflow_service.service;

import com.yash.workflow.workflow_service.dto.StartWorkflowRequest;
import com.yash.workflow.workflow_service.dto.TaskRequest;
import com.yash.workflow.workflow_service.entity.Task;
import com.yash.workflow.workflow_service.entity.Workflow;
import com.yash.workflow.workflow_service.entity.enums.TaskStatus;
import com.yash.workflow.workflow_service.repository.TaskRepository;
import com.yash.workflow.workflow_service.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public UUID startWorkflow(StartWorkflowRequest request) {

        //Create workflow
        Workflow workflow = new Workflow();
        workflow.setWorkflowName(request.getWorkflowName());
        workflow.setStatus("CREATED");

        // For now using random tenantId
        workflow.setTenantId(UUID.randomUUID());

        Workflow savedWorkflow = workflowRepository.save(workflow);

        //Create tasks
        List<Task> tasks = new ArrayList<>();

        for (TaskRequest taskRequest : request.getTasks()) {

            Task task = new Task();

            task.setWorkflowId(savedWorkflow.getId());
            task.setTaskName(taskRequest.getTaskName());
            task.setPayload(taskRequest.getPayload());

            task.setStatus(TaskStatus.PENDING);
            task.setRetryCount(0);
            task.setMaxRetries(taskRequest.getMaxRetries());

            tasks.add(task);
        }

        //Save tasks
        taskRepository.saveAll(tasks);

        return savedWorkflow.getId();
    }
}