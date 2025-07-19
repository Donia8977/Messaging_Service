package com.example.MessageService.message.service;

import com.example.MessageService.Logging.service.MessageLogService;
import com.example.MessageService.message.MessageBroker.MessageProducer;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.MessageStatus;
import com.example.MessageService.message.repository.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageHistoryServiceImpl implements MessageHistoryService {

    private final MessageRepository messageRepository;
    private final MessageProducer messageProducer;
    private final MessageLogService messageLogService;

    @Override
    @Transactional(readOnly = true)
    public List<Message> getAllMessages() {
        return messageRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    @Transactional
    public void resendMessage(Long messageId) {
        log.info("Attempting to resend message with ID: {}", messageId);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with ID: " + messageId));

        if (message.getStatus() != MessageStatus.FAILED) {
            log.warn("Cannot resend message {}. Its status is {}.", messageId, message.getStatus());
            throw new IllegalStateException("Only failed messages can be resent.");
        }

        message.setStatus(MessageStatus.PENDING);
        message.setRetryCount(0);

        Message savedMessage = messageRepository.save(message);
        messageLogService.createLog(savedMessage, MessageStatus.PENDING, "Message resent manually by administrator.");
        messageProducer.sendMessage(savedMessage);

        log.info("Message {} has been successfully resent to the message queue.", messageId);
    }

    @Override
    public long getTotalMessageCount() {
        return messageRepository.count();
    }

    @Override
    public Map<MessageStatus, Long> getMessageCountsByStatus() {
        Map<MessageStatus, Long> statusCounts = new EnumMap<>(MessageStatus.class);

        for (MessageStatus status : MessageStatus.values()) {
            statusCounts.put(status, 0L);
        }

        List<Map<String, Object>> results = messageRepository.countByStatus();

        for (Map<String, Object> result : results) {
            MessageStatus status = (MessageStatus) result.get("status");
            Long count = (Long) result.get("count");
            statusCounts.put(status, count);
        }

        return statusCounts;

    }
}