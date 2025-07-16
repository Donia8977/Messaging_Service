package com.example.MessageService.message.MessagingSystem.nats.consumer;

import com.example.MessageService.message.MessagingSystem.handler.MessageHandler;
import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.mapper.MessageMapper;
import com.example.MessageService.security.entity.ChannelType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.JetStream;
import io.nats.client.PushSubscribeOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent; // <-- IMPORT THIS
import org.springframework.context.ApplicationListener; // <-- IMPORT THIS
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Profile("nats")
@Slf4j
// --- START OF FIX 1 ---
// Implement ApplicationListener instead of using @PostConstruct
public class NatsMessageConsumer implements ApplicationListener<ApplicationReadyEvent> {
// --- END OF FIX 1 ---

    private final JetStream jetStream;
    private final Connection connection;
    private final Map<ChannelType, MessageHandler> handlerRegistry;
    private final ObjectMapper objectMapper;
    private final MessageMapper messageMapper;

    public NatsMessageConsumer(JetStream jetStream, Connection connection, List<MessageHandler> handlers, ObjectMapper objectMapper, MessageMapper messageMapper) {
        this.jetStream = jetStream;
        this.connection = connection;
        this.handlerRegistry = handlers.stream()
                .collect(Collectors.toMap(MessageHandler::getSupportedChannel, Function.identity()));
        this.objectMapper = objectMapper;
        this.messageMapper = messageMapper;
    }

    // --- START OF FIX 2 ---
    // This method will now be called AFTER the CommandLineRunner in NatsConfig has finished.
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Application is ready. Initializing NATS JetStream subscriptions...");
        subscribeToWorkload("email", "messages.tenant.*.email.*");
        // In the future, you could add:
        // subscribeToWorkload("sms", "messages.tenant.*.sms.*");
    }
    // --- END OF FIX 2 ---

    private void subscribeToWorkload(String workloadName, String subjectFilter) {
        try {
            PushSubscribeOptions pushOptions = PushSubscribeOptions.builder()
                    .durable(workloadName + "-processor")
                    .build();

            Dispatcher dispatcher = connection.createDispatcher();

            jetStream.subscribe(subjectFilter, dispatcher, this::onNatsMessage, false, pushOptions);

            log.info("Successfully subscribed to NATS workload '{}' with subject filter '{}'", workloadName, subjectFilter);

        } catch (Exception e) {
            log.error("CRITICAL: Failed to subscribe to NATS workload '{}'", workloadName, e);
        }
    }

    public void onNatsMessage(io.nats.client.Message natsMessage) {
        try {
            MessageSchedulerDto dto = objectMapper.readValue(natsMessage.getData(), MessageSchedulerDto.class);
            Message message = messageMapper.toEntity(dto);

            MessageHandler handler = handlerRegistry.get(message.getChannel());

            if (handler != null) {
                boolean success = handler.handle(message);
                if (success) {
                    natsMessage.ack();
                    log.info("Successfully processed and ACKed message for subject: {}", natsMessage.getSubject());
                } else {
                    natsMessage.nak();
                    log.warn("Handler failed for message on subject {}. NAKing for redelivery.", natsMessage.getSubject());
                }
            } else {
                log.warn("No handler found for channel {}. Terminating message.", message.getChannel());
                natsMessage.term();
            }
        } catch (Exception processingException) {
            log.error("Uncaught exception while processing message on subject {}. NAKing for redelivery.", natsMessage.getSubject(), processingException);
            natsMessage.nak();
        }
    }
}