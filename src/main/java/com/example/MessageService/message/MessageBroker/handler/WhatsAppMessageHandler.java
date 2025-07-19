package com.example.MessageService.message.MessageBroker.handler;

import com.example.MessageService.Logging.service.MessageLogService;
import com.example.MessageService.message.MessageBroker.provider.WhatsAppProvider;
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
public class WhatsAppMessageHandler extends AbstractMessageHandler {

    private final WhatsAppProvider whatsAppProvider;
    private final MessageRepository messageRepository;
    private final MessageLogService messageLogService;
    private final WhatsAppMessageHandler self;

    public WhatsAppMessageHandler(WhatsAppProvider whatsAppProvider,
                                  MessageRepository messageRepository,
                                  MessageLogService messageLogService,
                                  @Lazy WhatsAppMessageHandler self) {
        super(messageRepository, messageLogService);
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
            managedMessage.setSendAt(LocalDateTime.now());
            messageRepository.save(managedMessage);
            messageLogService.createLog(managedMessage, MessageStatus.SENT, "WhatsApp message delivered successfully by provider.");
            log.info("Successfully sent WhatsApp message for message ID: {}.", managedMessage.getId());
            return true;

        } catch (Exception e) {
            log.error("WhatsApp sending failed for message ID: {}. Error: {}", managedMessage.getId(), e.getMessage());
            self.updateStatusOnFailure(managedMessage.getId(), e.getMessage());
            return false;
        }
    }


    @Override
    public ChannelType getSupportedChannel() {
        return ChannelType.WHATSAPP;
    }
}