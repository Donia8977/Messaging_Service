package com.example.MessageService.message.MessagingSystem.kafka.consumer;

import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.MessagingSystem.kafka.consumer.handler.MessageHandler;
import com.example.MessageService.security.entity.ChannelType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MessageConsumer {

    private final Map<ChannelType, MessageHandler> handlerRegistry;

    public MessageConsumer(List<MessageHandler> handlers) {
        this.handlerRegistry = handlers.stream()
                .collect(Collectors.toMap(MessageHandler::getSupportedChannel, Function.identity()));
    }

    @PostConstruct
    public void logRegisteredHandlers() {
        log.info("Registered Message Handlers: {}", handlerRegistry.keySet());
    }

    @KafkaListener(
            topics = {
                    "email-high-priority-topic", "email-standard-priority-topic",
                    "sms-high-priority-topic", "sms-standard-priority-topic"
            },
            groupId = "unified-message-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )

    public void listen(@Payload Message message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("The consumer received message on topic '{}' for channel {}", topic, message.getChannel());
        MessageHandler handler = handlerRegistry.get(message.getChannel());
        if (handler != null) {
            handler.handle(message);
        } else {
            log.warn("No message handler found for channel: {}. Cannot process message ID: {}",
                    message.getChannel(), message.getId());
        }
    }
}