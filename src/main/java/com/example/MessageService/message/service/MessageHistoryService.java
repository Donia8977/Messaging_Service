package com.example.MessageService.message.service;

import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.MessageStatus;

import java.util.List;
import java.util.Map;

public interface MessageHistoryService {

    List<Message> getAllMessages();

    void resendMessage(Long messageId);

    long getTotalMessageCount();

    Map<MessageStatus, Long> getMessageCountsByStatus();
}
