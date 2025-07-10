package com.example.MessageService.message.MessagingSystem;
import com.example.MessageService.message.entity.Message;

public interface MessageProducer {
    void sendMessage(Message message);
}
