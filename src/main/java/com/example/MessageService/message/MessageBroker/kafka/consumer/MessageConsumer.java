package com.example.MessageService.message.MessageBroker.kafka.consumer;

import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.mapper.MessageMapper;
import com.example.MessageService.message.MessageBroker.handler.MessageHandler;
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
            topics = {"email-high-priority-topic", "sms-high-priority-topic", "whatsapp-high-priority-topic"},
            groupId = "high-priority-consumer-group",
            containerFactory = "highPriorityKafkaListenerContainerFactory"
    )
    public void listenHighPriority(@Payload MessageSchedulerDto dto, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("HIGH-PRIORITY consumer received message on topic '{}'", topic);
        processMessage(dto);
    }


    @KafkaListener(
            topics = {"email-standard-priority-topic", "sms-standard-priority-topic", "whatsapp-standard-priority-topic"},
            groupId = "standard-priority-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenStandardPriority(@Payload MessageSchedulerDto dto, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("STANDARD-PRIORITY consumer received message on topic '{}'", topic);
        processMessage(dto);
    }

    private void processMessage(MessageSchedulerDto dto) {
        Message message = messageMapper.toEntity(dto);
        MessageHandler handler = handlerRegistry.get(message.getChannel());

        if (handler != null) {
            boolean success = handler.handle(message);
            if (!success) {
                throw new RuntimeException("Handler failed to process message for target ID " + dto.getTargetId() + ". Triggering Kafka retry mechanism.");
            }
        } else {
            log.warn("No message handler found for channel: {}. Skipping message.", message.getChannel());
        }
    }

}
