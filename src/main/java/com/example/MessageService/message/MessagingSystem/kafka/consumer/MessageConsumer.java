package com.example.MessageService.message.MessagingSystem.kafka.consumer;

import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.mapper.MessageMapper;
import com.example.MessageService.message.MessagingSystem.handler.MessageHandler;
import com.example.MessageService.security.entity.ChannelType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
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
@Profile("kafka")
public class MessageConsumer {

    private final Map<ChannelType, MessageHandler> handlerRegistry;
    private final MessageMapper messageMapper;

    public MessageConsumer(List<MessageHandler> handlers, MessageMapper messageMapper) {
        this.handlerRegistry = handlers.stream()
                .collect(Collectors.toMap(MessageHandler::getSupportedChannel, Function.identity()));
        this.messageMapper = messageMapper;
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

    public void listen(@Payload MessageSchedulerDto dto, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Message message = messageMapper.toEntity(dto);
        log.info("Kafka consumer received message on topic '{}' for channel {} and target ID {}", topic, message.getChannel(), dto.getTargetId());
        MessageHandler handler = handlerRegistry.get(message.getChannel());
        if (handler != null) {
            boolean success = handler.handle(message);
            if (!success) {
                throw new RuntimeException("Handler failed to process message for target ID " + dto.getTargetId() + ". Triggering Kafka retry mechanism.");
            }
        } else {
            log.warn("No message handler found for channel: {}. Skipping message for target ID: {}", message.getChannel(), dto.getTargetId());
        }
    }
}
