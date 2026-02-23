package com.yash.workflow.workflow_service.service;

import com.yash.workflow.workflow_service.dto.LoginRequest;
import com.yash.workflow.workflow_service.dto.RegisterRequest;
import com.yash.workflow.workflow_service.dto.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);

}
