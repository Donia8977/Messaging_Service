package com.example.MessageService.message.MessageBroker.handler;

import com.example.MessageService.Logging.service.MessageLogService;
import com.example.MessageService.message.MessageBroker.provider.EmailProviderImpl;
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
public class EmailMessageHandler extends AbstractMessageHandler {

    private final EmailProviderImpl emailProvider;
    private final MessageRepository messageRepository;
    private final MessageLogService messageLogService;
    private final EmailMessageHandler self;

    public EmailMessageHandler(EmailProviderImpl emailProvider,
                               MessageRepository messageRepository, MessageLogService messageLogService,
                               @Lazy EmailMessageHandler self) {
        super(messageRepository, messageLogService);
        this.emailProvider = emailProvider;
        this.messageRepository = messageRepository;
        this.messageLogService = messageLogService;
        this.self = self;
    }


    @Override
    @Transactional
    public boolean handle(Message message) {
        log.info("Handling EMAIL message ID: {}", message.getId());
        Message managedMessage = messageRepository.findById(message.getId())
                .orElseThrow(() -> new EntityNotFoundException("Message " + message.getId() + " not found."));

        MessageStatus currentStatus = managedMessage.getStatus();

        if (currentStatus == MessageStatus.SENT) {
            log.warn("Message {} has already been sent. Acknowledging to remove from queue.", message.getId());
            return true; // Return true to ACK the message and remove it from the stream.
        }
        if (currentStatus == MessageStatus.FAILED) {
            log.warn("Message {} has already been marked as FAILED. Acknowledging to remove from queue.", message.getId());
            return true; // Return true to ACK the message and prevent redelivery loops.
        }

        try {
            emailProvider.send(managedMessage);
            managedMessage.setStatus(MessageStatus.SENT);
            managedMessage.setSendAt(LocalDateTime.now());
            messageRepository.save(managedMessage);

            // Log the success
            messageLogService.createLog(managedMessage, MessageStatus.SENT, "Email delivered successfully by provider.");
            log.info("Successfully sent email for message ID: {}. Status updated to SENT.", managedMessage.getId());
            return true;

        }  catch (Exception e) {
            log.error("Email sending failed for message ID: {}. Error: {}", managedMessage.getId(), e.getMessage(), e);
            self.updateStatusOnFailure(managedMessage.getId() , e.getMessage());
            return false;
        }
    }


    @Override
    public ChannelType getSupportedChannel() {
        return ChannelType.EMAIL;
    }
}