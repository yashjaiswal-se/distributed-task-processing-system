package com.yash.workflow.workflow_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import com.yash.workflow.workflow_service.entity.enums.WorkflowStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflows")
@Getter
@Setter
public class Workflow {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String workflowName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStatus status;

    @Column(nullable = false)
    private int totalTasks;

    @Column(nullable = false)
    private int completedTasks;

    @Column(nullable = false)
    private int failedTasks;

    @CreationTimestamp
    private Instant createdAt;
}