package com.yash.workflow.workerservice.service;

import com.yash.workflow.workerservice.entity.Task;
import com.yash.workflow.workerservice.entity.TaskStatus;
import com.yash.workflow.workerservice.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskExecutionServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskRetryPublisher retryPublisher;

    @Mock
    private TaskCompletedPublisher completedPublisher;

    @InjectMocks
    private TaskExecutionService taskExecutionService;

    private Task buildTask(String taskName, int retryCount, int maxRetries) {
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setWorkflowId(UUID.randomUUID());
        task.setTaskName(taskName);
        task.setPayload("data");
        task.setRetryCount(retryCount);
        task.setMaxRetries(maxRetries);
        task.setStatus(TaskStatus.CREATED);
        return task;
    }

    @Test
    void shouldCompleteTaskSuccessfully() {
        Task task = buildTask("EMAIL", 0, 3);

        taskExecutionService.execute(task);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);

        verify(taskRepository, atLeast(2)).save(task);
        verify(completedPublisher, times(1)).publish(any());
        verify(retryPublisher, never()).retry(any());
    }

    @Test
    void shouldRetryTask_whenFailureAndRetriesRemaining() {
        Task task = buildTask("FAIL_TEST", 0, 3);

        taskExecutionService.execute(task);

        assertThat(task.getRetryCount()).isEqualTo(1);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.CREATED);

        verify(taskRepository, atLeast(2)).save(task);
        verify(retryPublisher, times(1)).retry(task);
        verify(completedPublisher, never()).publish(any());
    }

    @Test
    void shouldMarkTaskFailed_whenRetriesExhausted() {
        Task task = buildTask("FAIL_TEST", 2, 3);

        taskExecutionService.execute(task);

        assertThat(task.getRetryCount()).isEqualTo(3);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);

        verify(taskRepository, atLeast(2)).save(task);
        verify(completedPublisher, times(1)).publish(any());
        verify(retryPublisher, never()).retry(any());
    }
}