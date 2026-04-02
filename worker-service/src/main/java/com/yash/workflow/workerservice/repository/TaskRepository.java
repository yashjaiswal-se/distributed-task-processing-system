package com.yash.workflow.workerservice.repository;

import com.yash.workflow.workerservice.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    Optional<Task> findByIdempotencyKey(String idempotencyKey);
}