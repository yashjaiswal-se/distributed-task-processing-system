package com.yash.workflow.workflow_service.service;

import com.yash.workflow.workflow_service.dto.AuthResponse;
import com.yash.workflow.workflow_service.dto.LoginRequest;
import com.yash.workflow.workflow_service.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthResponse register(RegisterRequest request) {
        // TODO: implement
        return null;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // TODO: implement
        return null;
    }
}