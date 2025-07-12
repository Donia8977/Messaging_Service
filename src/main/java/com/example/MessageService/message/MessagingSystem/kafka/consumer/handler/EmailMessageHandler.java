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

    private final EmailProviderImpl emailProvider;
    private final MessageRepository messageRepository;


    private final EmailMessageHandler self;

    public EmailMessageHandler(EmailProviderImpl emailProvider,
                               MessageRepository messageRepository,
                               @Lazy EmailMessageHandler self) {
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

        }  catch (Exception e) {
            log.error("Email sending failed for message ID: {}. Error: {}", managedMessage.getId(), e.getMessage(), e);
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