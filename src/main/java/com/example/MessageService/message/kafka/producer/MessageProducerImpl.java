package com.example.MessageService.message.kafka.producer;

import com.example.MessageService.message.entity.Message;
import org.springframework.stereotype.Service;

@Service
public class MessageProducerImpl implements MessageProducer {

    @Override
    public void sendMessage(Message message) {
        // Logic to send the message to Kafka
        System.out.println("Message sent: " + message);
    }
}
