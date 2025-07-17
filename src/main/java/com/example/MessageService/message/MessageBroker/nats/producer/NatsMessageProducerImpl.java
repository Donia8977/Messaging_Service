package com.example.MessageService.message.MessageBroker.nats.producer;

import com.example.MessageService.message.MessageBroker.MessageProducer;
import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.mapper.MessageMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.JetStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("nats")
@Slf4j
@RequiredArgsConstructor
public class NatsMessageProducerImpl implements MessageProducer {

    private final JetStream jetStream;
    private final ObjectMapper objectMapper;
    private final MessageMapper messageMapper;

    @Override
    public void sendMessage(Message message) {
        if (message == null || message.getTenant() == null) {
            log.error("Cannot send a null message or a message without a tenant. Aborting.");
            return;
        }

        String subject = buildSubject(message);
        try {
            MessageSchedulerDto dto = messageMapper.toDto(message);
            byte[] payload = objectMapper.writeValueAsBytes(dto);

            log.info("Publishing message to NATS subject [{}] for tenantId [{}], channel [{}], targetId [{}]",
                    subject, message.getTenant().getId(), dto.getChannel(), dto.getTargetId());

            jetStream.publish(subject, payload);

        } catch (Exception e) {
            log.error("Failed to publish message with to NATS subject [{}]. Error: {}", subject, e.getMessage(), e);
            // Depending on requirements, you might want to re-throw this
        }
    }

    private String buildSubject(Message message) {
        return String.format("messages.tenant.%d.%s.%s",
                message.getTenant().getId(),
                message.getChannel().name().toLowerCase(),
                message.getPriority().name().toLowerCase());
    }
}