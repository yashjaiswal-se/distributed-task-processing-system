package com.yash.workflow.workflow_service.consumer;

import com.yash.workflow.workflow_service.consumer.TaskCompletedConsumer;
import com.yash.workflow.workflow_service.dto.events.TaskCompletedEvent;
import com.yash.workflow.workflow_service.entity.Workflow;
import com.yash.workflow.workflow_service.entity.enums.WorkflowStatus;
import com.yash.workflow.workflow_service.repository.WorkflowRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskCompletedConsumerTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @InjectMocks
    private TaskCompletedConsumer consumer;

    @Test
    void shouldMarkWorkflowCompleted_whenAllTasksDone() {

        UUID workflowId = UUID.randomUUID();

        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setTotalTasks(2);
        workflow.setCompletedTasks(1);
        workflow.setFailedTasks(0);

        TaskCompletedEvent event = new TaskCompletedEvent(
                UUID.randomUUID(), workflowId, "COMPLETED"
        );

        when(workflowRepository.findById(workflowId))
                .thenReturn(Optional.of(workflow));

        consumer.consume(event);

        assertThat(workflow.getStatus())
                .isEqualTo(WorkflowStatus.COMPLETED);
    }
}