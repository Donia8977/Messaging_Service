package com.example.MessageService.message.MessagingSystem.kafka.consumer.handler;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.security.entity.ChannelType;


public interface MessageHandler {
    void handle(Message message);
    ChannelType getSupportedChannel();
}

