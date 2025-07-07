package com.example.MessageService.message.service;

import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;


public interface MessageSchedulerService {
    void processMessageRequest(MessageSchedulerDto request);
}
