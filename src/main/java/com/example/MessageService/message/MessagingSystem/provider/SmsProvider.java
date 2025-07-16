package com.example.MessageService.message.MessagingSystem.provider;

import com.example.MessageService.message.entity.Message;
import com.example.MessageService.security.entity.ChannelType;

public interface SmsProvider {
    void send(Message  message);
    ChannelType getSupportedChannel();
}
