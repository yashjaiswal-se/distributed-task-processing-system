package com.yash.workflow.workflow_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(

        @NotBlank String tenantName,

        @NotBlank String name,

        @Email
        @NotBlank
        String email,

        @NotBlank
        String password

) {}