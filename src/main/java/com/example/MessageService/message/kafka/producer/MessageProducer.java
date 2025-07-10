package com.example.MessageService.message.kafka.producer;
import com.example.MessageService.message.entity.Message;

public interface MessageProducer {
    void sendMessage(Message message);
}
