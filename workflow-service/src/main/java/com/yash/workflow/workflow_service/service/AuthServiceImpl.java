package com.yash.workflow.workflow_service.service;

import com.yash.workflow.workflow_service.dto.AuthResponse;
import com.yash.workflow.workflow_service.dto.LoginRequest;
import com.yash.workflow.workflow_service.dto.RegisterRequest;
import com.yash.workflow.workflow_service.entity.Role;
import com.yash.workflow.workflow_service.entity.Tenant;
import com.yash.workflow.workflow_service.entity.User;
import com.yash.workflow.workflow_service.exception.BusinessException;
import com.yash.workflow.workflow_service.mapper.UserMapper;
import com.yash.workflow.workflow_service.repository.TenantRepository;
import com.yash.workflow.workflow_service.repository.UserRepository;
import com.yash.workflow.workflow_service.security.CustomUserPrincipal;
import com.yash.workflow.workflow_service.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.email())){
            throw new BusinessException("Email already registered");
        }

        Tenant tenant=Tenant.builder()
                .name(request.tenantName())
                .build();
        tenantRepository.save(tenant);

        User user =userMapper.toEntity(request,tenant);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);


        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId().toString(),
                tenant.getId().toString()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        CustomUserPrincipal principal =
                (CustomUserPrincipal) authentication.getPrincipal();

        User user = principal.getUser();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);


        return new AuthResponse(
                accessToken,
                refreshToken,
                principal.getUserId().toString(),
                principal.getTenantId().toString()
        );
    }
}