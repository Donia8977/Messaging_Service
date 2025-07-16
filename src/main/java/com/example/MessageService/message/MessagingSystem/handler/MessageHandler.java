package com.example.MessageService.message.MessagingSystem.handler;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.security.entity.ChannelType;


public interface MessageHandler {
    boolean handle(Message message);
    ChannelType getSupportedChannel();
}

