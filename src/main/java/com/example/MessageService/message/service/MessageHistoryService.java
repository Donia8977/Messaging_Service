package com.example.MessageService.message.service;

import com.example.MessageService.message.entity.Message;

import java.util.List;

public interface MessageHistoryService {

    List<Message> getAllMessages();

    void resendMessage(Long messageId);

    long getTotalMessageCount();
}
