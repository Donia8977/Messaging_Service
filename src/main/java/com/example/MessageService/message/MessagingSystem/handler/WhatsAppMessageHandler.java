package com.example.MessageService.message.MessagingSystem.handler;

import com.example.MessageService.Logging.service.MessageLogService;
import com.example.MessageService.message.MessagingSystem.provider.WhatsAppProvider;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.MessageStatus;
import com.example.MessageService.message.repository.MessageRepository;
import com.example.MessageService.security.entity.ChannelType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class WhatsAppMessageHandler implements MessageHandler {

    private final WhatsAppProvider whatsAppProvider;
    private final MessageRepository messageRepository;
    private final MessageLogService messageLogService;
    private final WhatsAppMessageHandler self; // Self-injection for transactions

    public WhatsAppMessageHandler(WhatsAppProvider whatsAppProvider,
                                  MessageRepository messageRepository, MessageLogService messageLogService,
                                  @Lazy WhatsAppMessageHandler self) {
        this.whatsAppProvider = whatsAppProvider;
        this.messageRepository = messageRepository;
        this.messageLogService = messageLogService;
        this.self = self;
    }

    @Override
    @Transactional
    public boolean handle(Message message) {
        log.info("Handling WHATSAPP message ID: {}", message.getId());
        Message managedMessage = messageRepository.findById(message.getId())
                .orElseThrow(() -> new EntityNotFoundException("Message " + message.getId() + " not found."));

        MessageStatus currentStatus = managedMessage.getStatus();
        if (currentStatus == MessageStatus.SENT || currentStatus == MessageStatus.FAILED) {
            log.warn("Message {} is already in a terminal state ({}). Acknowledging to remove from queue.", message.getId(), currentStatus);
            return true;
        }

        try {
            whatsAppProvider.send(managedMessage);
            managedMessage.setStatus(MessageStatus.SENT);
            messageRepository.save(managedMessage);
            messageLogService.createLog(managedMessage, MessageStatus.SENT, "WhatsApp message delivered successfully by provider.");
            log.info("Successfully sent WhatsApp message for message ID: {}.", managedMessage.getId());
            return true;

        } catch (Exception e) {
            log.error("WhatsApp sending failed for message ID: {}. Error: {}", managedMessage.getId(), e.getMessage());
            self.updateStatusOnFailure(managedMessage, e.getMessage());
            return false;
        }
    }

    // This logic is identical to the EmailMessageHandler. In a large system,
    // you might move this to a shared abstract base class to avoid duplication.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatusOnFailure(Message message, String errorMessage) {
        try {
            if (message.getRetryCount() < message.getMaxRetries()) {
                message.setRetryCount(message.getRetryCount() + 1);
                message.setStatus(MessageStatus.RETRYING);
                String retryNote = "Send attempt failed. Reason: " + errorMessage;
                messageLogService.createLog(message, MessageStatus.RETRYING, retryNote);
                log.info("Incremented retry count to {} for message ID: {}", message.getRetryCount(), message.getId());
            } else {
                message.setStatus(MessageStatus.FAILED);
                String finalFailureNote = "Max retries reached. Delivery failed permanently. Final error: " + errorMessage;
                messageLogService.createLog(message, MessageStatus.FAILED, finalFailureNote);
                log.warn("Max retries reached for message ID: {}. Marking as FAILED.", message.getId());
            }
            messageRepository.save(message);
        } catch (Exception dbEx) {
            log.error("CRITICAL: Could not update failure state for message ID: {}. This may cause infinite retries.", message.getId(), dbEx);
        }
    }

    @Override
    public ChannelType getSupportedChannel() {
        return ChannelType.WHATSAPP;
    }
}