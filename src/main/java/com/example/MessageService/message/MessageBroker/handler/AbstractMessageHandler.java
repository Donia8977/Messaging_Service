package com.example.MessageService.message.MessageBroker.handler;

import com.example.MessageService.Logging.service.MessageLogService;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.MessageStatus;
import com.example.MessageService.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractMessageHandler implements MessageHandler {
    protected final MessageRepository messageRepository;
    protected final MessageLogService messageLogService;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public  void updateStatusOnFailure(Long messageId, String errorMessage) {
        try {
            messageRepository.findById(messageId).ifPresent(msg -> {
                if (msg.getRetryCount() < msg.getMaxRetries()) {
                    msg.setRetryCount(msg.getRetryCount() + 1);
                    msg.setStatus(MessageStatus.RETRYING);
                    messageRepository.save(msg);

                    String retryNote = "Send attempt failed. Reason: " + errorMessage;
                    messageLogService.createLog(msg, MessageStatus.RETRYING, retryNote);

                    log.info("Incremented retry count to {} for message ID: {}", msg.getRetryCount(), messageId);
                } else {
                    msg.setStatus(MessageStatus.FAILED);
                    messageRepository.save(msg);

                    String finalFailureNote = "Max retries reached. Delivery failed permanently. Final error: " + errorMessage;
                    messageLogService.createLog(msg, MessageStatus.FAILED, finalFailureNote);

                    log.warn("Max retries reached for message ID: {}. Marking as FAILED.", messageId);
                }
            });
        } catch (Exception dbEx) {
            log.error("CRITICAL: Could not update failure state for message ID: {}. This may cause infinite retries.", messageId, dbEx);
        }
    }
}