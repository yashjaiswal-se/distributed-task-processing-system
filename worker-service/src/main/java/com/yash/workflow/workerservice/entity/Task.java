package com.yash.workflow.workerservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    private UUID id;

    private UUID workflowId;

    private String taskName;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private int retryCount;
    private int maxRetries;
}