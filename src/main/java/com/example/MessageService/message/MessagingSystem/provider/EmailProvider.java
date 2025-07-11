package com.example.MessageService.message.MessagingSystem.provider;

import com.example.MessageService.message.entity.Message;

public interface EmailProvider {

 void send(Message message);

}
