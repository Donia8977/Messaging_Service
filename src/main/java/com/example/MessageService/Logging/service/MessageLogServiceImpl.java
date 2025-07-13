package com.example.MessageService.Logging.service;

import com.example.MessageService.Logging.dto.MessageLogResponse;
import com.example.MessageService.Logging.entity.MessageLog;
import com.example.MessageService.Logging.mapper.MessageLogMapper;
import com.example.MessageService.Logging.repository.MessageLogRepository;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.MessageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageLogServiceImpl implements MessageLogService {
    private final MessageLogRepository messageLogRepository;
    private final MessageLogMapper messageLogMapper;

    @Transactional(readOnly = true) // Good practice to mark read-only methods
    public List<MessageLogResponse> findLogsByMessageId(Long messageId) {
        List<MessageLog> logEntities = messageLogRepository.findByMessageId(messageId);
        return messageLogMapper.toDtoList(logEntities);
    }

    @Override
    @Transactional //(propagation = Propagation.REQUIRES_NEW)
    public void createLog(Message message, MessageStatus status, String notes) {
        MessageLog logEntry = new MessageLog();
        logEntry.setMessage(message);
        logEntry.setStatus(status);
        logEntry.setAttemptNumber(message.getRetryCount() + 1);
        logEntry.setTimestamp(LocalDateTime.now());

        if (status == MessageStatus.FAILED || status == MessageStatus.RETRYING) {
            logEntry.setErrorMessage(notes);
        } else {
            logEntry.setChannelResponse(notes);
        }

        messageLogRepository.save(logEntry);
    }
}