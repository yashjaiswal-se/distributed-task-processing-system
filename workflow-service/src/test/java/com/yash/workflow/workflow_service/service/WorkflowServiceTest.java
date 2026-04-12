package com.yash.workflow.workflow_service.service;

import com.yash.workflow.workflow_service.dto.StartWorkflowRequest;
import com.yash.workflow.workflow_service.dto.TaskRequest;
import com.yash.workflow.workflow_service.entity.Task;
import com.yash.workflow.workflow_service.entity.enums.TaskStatus;
import com.yash.workflow.workflow_service.repository.TaskRepository;
import com.yash.workflow.workflow_service.repository.WorkflowRepository;
import com.yash.workflow.workflow_service.service.messaging.TaskPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskPublisher taskPublisher;

    @InjectMocks
    private WorkflowService workflowService;

    @Test
    void shouldStartWorkflowSuccessfully() {
        // Arrange
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTaskName("task1");
        taskRequest.setPayload("data");
        taskRequest.setMaxRetries(3);

        StartWorkflowRequest request = new StartWorkflowRequest();
        request.setWorkflowName("test-workflow");
        request.setTasks(List.of(taskRequest));

        Task savedTask = new Task();
        savedTask.setWorkflowId(UUID.randomUUID());

        when(workflowRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(taskRepository.saveAll(any())).thenReturn(List.of(savedTask));

        // Act
        UUID workflowId = workflowService.startWorkflow(request);

        // Assert
        assertThat(workflowId).isNotNull();

        verify(workflowRepository, times(1)).save(any());
        verify(taskRepository, atLeastOnce()).saveAll(any());
        verify(taskPublisher, times(1)).publishTask(any());
    }
    @Test
void shouldDispatchTasksAndUpdateStatus() {
    // Arrange
    TaskRequest taskRequest = new TaskRequest();
    taskRequest.setTaskName("task1");
    taskRequest.setPayload("data");
    taskRequest.setMaxRetries(3);

    StartWorkflowRequest request = new StartWorkflowRequest();
    request.setWorkflowName("test-workflow");
    request.setTasks(List.of(taskRequest));

    Task savedTask = new Task();
    savedTask.setWorkflowId(UUID.randomUUID());
    savedTask.setStatus(TaskStatus.CREATED);

    when(workflowRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(taskRepository.saveAll(any()))
            .thenReturn(List.of(savedTask)); // first save

    // Act
    workflowService.startWorkflow(request);

    // Assert

    // 1. Kafka publish called
    verify(taskPublisher, times(1)).publishTask(any());

    // 2. Task saved again after dispatch (status update)
    verify(taskRepository, atLeast(2)).saveAll(any());
}
}