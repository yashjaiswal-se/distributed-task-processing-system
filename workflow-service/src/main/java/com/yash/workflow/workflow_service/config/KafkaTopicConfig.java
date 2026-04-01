package com.yash.workflow.workflow_service.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic taskExecutionTopic() {

        return TopicBuilder
                .name("task-execution-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
