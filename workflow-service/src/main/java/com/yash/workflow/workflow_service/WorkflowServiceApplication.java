package com.yash.workflow.workflow_service;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class WorkflowServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkflowServiceApplication.class, args);
	}

}

