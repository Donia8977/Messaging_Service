package com.example.MessageService.message.service;

import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.entity.Message;
import org.springframework.stereotype.Service;



@Service
public class MessageSchedulerServiceImpl implements MessageSchedulerService {

    @Override
    public void processMessageRequest(MessageSchedulerDto request) {
        Message message = new Message();

    }

    @Override
    public void saveMessage(Message message) {

    }
}
