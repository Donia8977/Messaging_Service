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
public class SmsMessageHandler extends AbstractMessageHandler {

    private final SmsProviderImpl smsProvider;
    private final MessageRepository messageRepository;
    private final MessageLogService messageLogService;
    private final SmsMessageHandler self;



    public SmsMessageHandler(SmsProviderImpl smsProvider,
                             MessageRepository messageRepository,
                             MessageLogService messageLogService,
                             @Lazy SmsMessageHandler self) {
        super(messageRepository, messageLogService);
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
            self.updateStatusOnFailure(managedMessage.getId(), e.getMessage());
            return false;
        }
    }


    @Override
    public ChannelType getSupportedChannel() {
        return ChannelType.SMS;
    }
}