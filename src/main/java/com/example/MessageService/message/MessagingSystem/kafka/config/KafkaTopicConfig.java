package com.example.MessageService.message.MessagingSystem.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("kafka")
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


    @Bean
    public NewTopic smsHighPriorityTopic() {
        return TopicBuilder.name("sms-high-priority-topic")
                .partitions(HIGH_PRIORITY_PARTITIONS)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic smsStandardPriorityTopic() {
        return TopicBuilder.name("sms-standard-priority-topic")
                .partitions(STANDARD_PRIORITY_PARTITIONS)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic whatsappHighPriorityTopic() {
        return TopicBuilder.name("whatsapp-high-priority-topic")
                .partitions(HIGH_PRIORITY_PARTITIONS)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic whatsappStandardPriorityTopic() {
        return TopicBuilder.name("whatsapp-standard-priority-topic")
                .partitions(STANDARD_PRIORITY_PARTITIONS)
                .replicas(1)
                .build();
    }


}