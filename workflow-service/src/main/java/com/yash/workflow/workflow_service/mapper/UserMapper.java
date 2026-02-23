package com.yash.workflow.workflow_service.mapper;

import com.yash.workflow.workflow_service.dto.RegisterRequest;
import com.yash.workflow.workflow_service.entity.Tenant;
import com.yash.workflow.workflow_service.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request, Tenant tenant) {
        return User.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password())
                .tenant(tenant)
                .build();
    }
}
