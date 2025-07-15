package com.example.MessageService.Logging.service;

import com.example.MessageService.Logging.dto.MessageLogResponse;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.MessageStatus;

import java.util.List;

public interface MessageLogService {
    public List<MessageLogResponse> findLogsByMessageId(Long messageId);
    void createLog(Message message, MessageStatus status, String notes);
}
