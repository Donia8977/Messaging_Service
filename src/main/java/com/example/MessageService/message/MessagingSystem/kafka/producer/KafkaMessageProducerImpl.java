package com.example.MessageService.message.MessagingSystem.kafka.producer;

import com.example.MessageService.message.MessagingSystem.MessageProducer;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.Priority;
import com.example.MessageService.security.entity.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile("kafka")
public class KafkaMessageProducerImpl implements MessageProducer {

    private final KafkaTemplate<String, Message> kafkaTemplate;

    public KafkaMessageProducerImpl(KafkaTemplate<String, Message> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendMessage(Message message) {
        if (message == null || message.getTenant() == null) {
            log.error("Cannot send a null message or a message without a tenant. Aborting.");
            return;
        }
        String topic = getTopicFor(message.getChannel(), message.getPriority());
        String key = message.getTenant().getId().toString();

        log.info("Sending message with key (tenantId) [{}] to topic [{}]", key, topic);

        try {
            kafkaTemplate.send(topic, key, message);
        } catch (Exception e) {
            log.error("Failed to send message with key [{}] to topic [{}]. Error: {}", key, topic, e.getMessage());
        }
    }


    private String getTopicFor(ChannelType channel, Priority priority) {
        String channelLower = channel.name().toLowerCase();
        String priorityString = (priority == Priority.HIGH) ? "high-priority" : "standard-priority";
        return String.format("%s-%s-topic", channelLower, priorityString);
    }
}