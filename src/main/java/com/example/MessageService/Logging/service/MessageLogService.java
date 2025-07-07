package com.example.MessageService.Logging.service;

import com.example.MessageService.Logging.dto.MessageLogResponse;
import com.example.MessageService.Logging.entity.MessageLog;
import com.example.MessageService.Logging.mapper.MessageLogMapper;
import com.example.MessageService.Logging.repository.MessageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageLogService {
    private final MessageLogRepository messageLogRepository;
    private final MessageLogMapper messageLogMapper;

    @Transactional(readOnly = true) // Good practice to mark read-only methods
    public List<MessageLogResponse> findLogsByMessageId(Long messageId) {
        List<MessageLog> logEntities = messageLogRepository.findByMessageId(messageId);
        return messageLogMapper.toDtoList(logEntities);
    }
}