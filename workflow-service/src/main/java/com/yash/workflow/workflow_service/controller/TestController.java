package com.yash.workflow.workflow_service.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test/secure")
    public String secure(Authentication authentication) {
        return "Authenticated userId: " + authentication.getName();
    }
}