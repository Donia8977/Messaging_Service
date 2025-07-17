package com.example.MessageService.message.MessageBroker.handler;

import com.example.MessageService.Logging.service.MessageLogService;
import com.example.MessageService.message.MessageBroker.provider.SmsProviderImpl;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.MessageStatus;
import com.example.MessageService.message.repository.MessageRepository;
import com.example.MessageService.security.entity.ChannelType;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class SmsMessageHandler implements MessageHandler {

    private final SmsProviderImpl smsProvider; // Inject the SMS-specific provider
    private final MessageRepository messageRepository;
    private final MessageLogService messageLogService;
    private final SmsMessageHandler self; // Self-injection for transactions



    public SmsMessageHandler(SmsProviderImpl smsProvider,
                             MessageRepository messageRepository, MessageLogService messageLogService,
                             @Lazy SmsMessageHandler self) {
        this.smsProvider = smsProvider;
        this.messageRepository = messageRepository;
        this.messageLogService = messageLogService;
        this.self = self;
    }


    @Override
    @Transactional
    public boolean handle(Message message) {
        log.info("Handling SMS message ID: {}", message.getId());
        Message managedMessage = messageRepository.findById(message.getId())
                .orElseThrow(() -> new EntityNotFoundException("Message " + message.getId() + " not found."));

        MessageStatus currentStatus = managedMessage.getStatus();
        if (currentStatus == MessageStatus.SENT || currentStatus == MessageStatus.FAILED) {
            log.warn("Message {} is already in a terminal state ({}). Acknowledging.", message.getId(), currentStatus);
            return true;
        }

        try {
            smsProvider.send(managedMessage);
            managedMessage.setStatus(MessageStatus.SENT);
            messageRepository.save(managedMessage);
            messageLogService.createLog(managedMessage, MessageStatus.SENT, "SMS message delivered successfully by provider.");
            log.info("Successfully sent SMS for message ID: {}.", managedMessage.getId());
            return true;

        } catch (Exception e) {
            log.error("SMS sending failed for message ID: {}. Error: {}", managedMessage.getId(), e.getMessage());
            self.updateStatusOnFailure(managedMessage, e.getMessage());
            return false;
        }
    }

    // This failure logic is duplicated from the other handlers, which is the nature of this separate architecture.
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
        return ChannelType.SMS;
    }
}