package com.yash.workflow.workflow_service.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String userId,
        String tenantId
) {}
