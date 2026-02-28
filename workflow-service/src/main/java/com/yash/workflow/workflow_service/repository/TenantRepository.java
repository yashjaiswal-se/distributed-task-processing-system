package com.yash.workflow.workflow_service.repository;

import com.yash.workflow.workflow_service.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}
