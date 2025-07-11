package com.example.MessageService.message.MessagingSystem.kafka.consumer.handler;

import com.example.MessageService.message.MessagingSystem.provider.EmailProviderImpl;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.MessageStatus;
import com.example.MessageService.message.repository.MessageRepository;
import com.example.MessageService.security.entity.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private EmailMessageHandler self;

    public EmailMessageHandler(EmailProviderImpl emailProvider, MessageRepository messageRepository) {
        this.emailProvider = emailProvider;
        this.messageRepository = messageRepository;
    }

    @Autowired
    public void setSelf(EmailMessageHandler self) {
        this.self = self;
    }


    @Override
    @Transactional
    public void handle(Message message) {
        log.info("Handling EMAIL message ID: {}", message.getId());
        Message currentMessage = messageRepository.findById(message.getId()).orElse(message);
        if (currentMessage.getStatus() == MessageStatus.SENT) {
            log.warn("Message with ID {} has already been processed and sent. Skipping to prevent duplicates.", message.getId());
            return;
        }
        try {
            emailProvider.send(message);
            currentMessage.setStatus(MessageStatus.SENT);
            currentMessage.setSendAt(LocalDateTime.now());
            log.info("Successfully sent email for message ID: {}. Status updated to SENT.", message.getId());
        }
        catch (Exception e) {
            self.updateStatusOnFailure(currentMessage.getId());
            log.error("Failed to send email for message ID: {}. Error: {}", message.getId(), e.getMessage());
            throw new RuntimeException("Email sending failed for message ID " + message.getId(), e);
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatusOnFailure(Long messageId) {
        try {
            messageRepository.findById(messageId).ifPresent(msg -> {
                if (msg.getRetryCount() < msg.getMaxRetries()) {
                    msg.setRetryCount(msg.getRetryCount() + 1);
                } else {
                    msg.setStatus(MessageStatus.FAILED);
                }
                messageRepository.save(msg);
                log.info("Persisted failure state for message ID: {}", messageId);
            });
        } catch (Exception dbEx) {
            log.error("CRITICAL: Could not update failure state for ID: {}", messageId, dbEx);
        }
    }

    @Override
    public ChannelType getSupportedChannel() {
        return ChannelType.EMAIL;
    }
}