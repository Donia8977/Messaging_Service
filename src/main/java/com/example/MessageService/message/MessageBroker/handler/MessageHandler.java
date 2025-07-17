package com.example.MessageService.message.MessageBroker.handler;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.security.entity.ChannelType;


public interface MessageHandler {
    boolean handle(Message message);
    ChannelType getSupportedChannel();
}

