package com.example.MessageService.message.MessagingSystem.kafka.consumer.handler;

import com.example.MessageService.message.MessagingSystem.provider.EmailProviderImpl;
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

import java.time.LocalDateTime;

@Component
@Slf4j
public class EmailMessageHandler implements MessageHandler {

    // These dependencies are required immediately, so they can be final.
    private final EmailProviderImpl emailProvider;
    private final MessageRepository messageRepository;

    // THIS IS THE FIX: The self-injected proxy cannot be a 'final' field
    // because its full initialization is delayed by @Lazy.
    private final EmailMessageHandler self;

    // The constructor now correctly uses @Lazy to break the creation cycle.
    public EmailMessageHandler(EmailProviderImpl emailProvider,
                               MessageRepository messageRepository,
                               @Lazy EmailMessageHandler self) { // @Lazy defers initialization of 'self'
        this.emailProvider = emailProvider;
        this.messageRepository = messageRepository;
        this.self = self;
    }


    @Override
    @Transactional
    public void handle(Message message) {
        log.info("Handling EMAIL message ID: {}", message.getId());
        Message managedMessage = messageRepository.findById(message.getId())
                .orElseThrow(() -> new EntityNotFoundException("Message " + message.getId() + " not found."));

        if (managedMessage.getStatus() == MessageStatus.SENT) {
            log.warn("Message {} has already been sent. Skipping.", message.getId());
            return;
        }

        try {
            emailProvider.send(managedMessage);

            managedMessage.setStatus(MessageStatus.SENT);
            managedMessage.setSendAt(LocalDateTime.now());
            log.info("Successfully sent email for message ID: {}. Status updated to SENT.", managedMessage.getId());

        } catch (Exception e) {
            log.error("Email sending failed for message ID: {}. Updating failure status and triggering retry.", managedMessage.getId());
            self.updateStatusOnFailure(managedMessage.getId());
            throw new RuntimeException("Email sending failed, triggering retry for message ID " + managedMessage.getId(), e);
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatusOnFailure(Long messageId) {
        try {
            messageRepository.findById(messageId).ifPresent(msg -> {
                if (msg.getRetryCount() < msg.getMaxRetries()) {
                    msg.setRetryCount(msg.getRetryCount() + 1);
                    log.info("Incremented retry count to {} for message ID: {}", msg.getRetryCount(), messageId);
                } else {
                    msg.setStatus(MessageStatus.FAILED);
                    log.warn("Max retries reached for message ID: {}. Marking as FAILED.", messageId);
                }

                messageRepository.save(msg);
            });
        } catch (Exception dbEx) {
            log.error("CRITICAL: Could not update failure state for message ID: {}. This may cause infinite retries.", messageId, dbEx);
        }
    }

    @Override
    public ChannelType getSupportedChannel() {
        return ChannelType.EMAIL;
    }
}