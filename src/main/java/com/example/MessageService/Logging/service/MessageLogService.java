package com.example.MessageService.Logging.service;

import com.example.MessageService.Logging.dto.MessageLogResponse;

import java.util.List;

public interface MessageLogService {
    public List<MessageLogResponse> findLogsByMessageId(Long messageId);
}
