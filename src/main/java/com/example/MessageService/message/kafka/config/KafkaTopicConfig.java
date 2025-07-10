package com.example.MessageService.message.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    private static final int HIGH_PRIORITY_PARTITIONS = 5;
    private static final int STANDARD_PRIORITY_PARTITIONS = 3;

    //Email Topics
    @Bean
    public NewTopic emailHighPriorityTopic() {
        return TopicBuilder.name("email-high-priority-topic")
                .partitions(HIGH_PRIORITY_PARTITIONS)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emailStandardPriorityTopic() {
        return TopicBuilder.name("email-standard-priority-topic")
                .partitions(STANDARD_PRIORITY_PARTITIONS)
                .replicas(1)
                .build();
    }
}